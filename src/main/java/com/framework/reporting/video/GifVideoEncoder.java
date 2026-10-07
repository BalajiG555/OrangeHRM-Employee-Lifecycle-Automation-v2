package com.framework.reporting.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/**
 * Pure-Java fallback encoder producing an animated GIF (no native dependencies), used on machines
 * without ffmpeg. Frames are downscaled to keep attachments small.
 */
public final class GifVideoEncoder implements VideoEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(GifVideoEncoder.class);
    private static final int MAX_WIDTH = 960;
    private static final int MIN_DELAY_CS = 4;
    private static final int MAX_DELAY_CS = 500;
    private static final int LAST_FRAME_DELAY_CS = 150;
    private static final int CENTISECONDS_PER_SECOND = 100;
    private static final String METADATA_FORMAT = "javax_imageio_gif_image_1.0";

    @Override
    public Optional<Path> encode(List<VideoFrame> frames, Path outputStem) {
        if (frames.isEmpty()) {
            return Optional.empty();
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("gif");
        if (!writers.hasNext()) {
            return Optional.empty();
        }
        ImageWriter writer = writers.next();
        Path output = outputStem.resolveSibling(outputStem.getFileName() + "." + extension());
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(Files.newOutputStream(output))) {
            writer.setOutput(stream);
            writer.prepareWriteSequence(null);
            for (int i = 0; i < frames.size(); i++) {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(frames.get(i).jpeg()));
                if (image == null) {
                    continue;
                }
                BufferedImage scaled = downscale(image);
                IIOMetadata metadata = frameMetadata(writer, scaled, delayCentiseconds(frames, i), i == 0);
                writer.writeToSequence(new IIOImage(scaled, null, metadata), null);
            }
            writer.endWriteSequence();
            return Optional.of(output);
        } catch (IOException e) {
            LOG.warn("GIF encoding failed: {}", e.getMessage());
            return Optional.empty();
        } finally {
            writer.dispose();
        }
    }

    @Override
    public String mimeType() {
        return "image/gif";
    }

    @Override
    public String extension() {
        return "gif";
    }

    private static int delayCentiseconds(List<VideoFrame> frames, int index) {
        if (index + 1 >= frames.size()) {
            return LAST_FRAME_DELAY_CS;
        }
        double seconds = frames.get(index + 1).timestampSeconds() - frames.get(index).timestampSeconds();
        int delay = (int) Math.round(seconds * CENTISECONDS_PER_SECOND);
        return Math.max(MIN_DELAY_CS, Math.min(MAX_DELAY_CS, delay));
    }

    private static BufferedImage downscale(BufferedImage source) {
        int width = Math.min(MAX_WIDTH, source.getWidth());
        int height = (int) Math.round(source.getHeight() * (width / (double) source.getWidth()));
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private static IIOMetadata frameMetadata(ImageWriter writer, BufferedImage image, int delayCs, boolean first)
            throws IOException {
        IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), null);
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(METADATA_FORMAT);

        IIOMetadataNode control = child(root, "GraphicControlExtension");
        control.setAttribute("disposalMethod", "none");
        control.setAttribute("userInputFlag", "FALSE");
        control.setAttribute("transparentColorFlag", "FALSE");
        control.setAttribute("delayTime", Integer.toString(delayCs));
        control.setAttribute("transparentColorIndex", "0");

        if (first) {
            IIOMetadataNode extensions = child(root, "ApplicationExtensions");
            IIOMetadataNode loop = new IIOMetadataNode("ApplicationExtension");
            loop.setAttribute("applicationID", "NETSCAPE");
            loop.setAttribute("authenticationCode", "2.0");
            loop.setUserObject(new byte[] {0x1, 0x0, 0x0});
            extensions.appendChild(loop);
        }
        metadata.setFromTree(METADATA_FORMAT, root);
        return metadata;
    }

    private static IIOMetadataNode child(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(name)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(name);
        root.appendChild(node);
        return node;
    }
}
