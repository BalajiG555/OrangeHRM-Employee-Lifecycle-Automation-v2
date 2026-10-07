package com.framework.unit;

import com.framework.reporting.video.FfmpegVideoEncoder;
import com.framework.reporting.video.GifVideoEncoder;
import com.framework.reporting.video.VideoFrame;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Both video encoders must turn screencast-style JPEG frames into a non-empty playable file.
 */
public class VideoEncoderTest {

    private static final int FRAME_COUNT = 12;
    private static final double FRAME_INTERVAL_SECONDS = 0.2;

    @Test
    public void gifEncoderProducesAnimation() throws IOException {
        Path output = encodeWith(new GifVideoEncoder());
        Assert.assertTrue(Files.size(output) > 0);
        Assert.assertEquals(new String(Files.readAllBytes(output), 0, 3, java.nio.charset.StandardCharsets.US_ASCII),
                "GIF");
    }

    @Test
    public void ffmpegEncoderProducesMp4() throws IOException {
        if (!FfmpegVideoEncoder.isAvailable()) {
            throw new SkipException("ffmpeg not installed on this machine");
        }
        Path output = encodeWith(new FfmpegVideoEncoder());
        Assert.assertTrue(Files.size(output) > 0);
        Assert.assertTrue(output.toString().endsWith(".mp4"));
    }

    private static Path encodeWith(com.framework.reporting.video.VideoEncoder encoder) throws IOException {
        Path stem = Files.createTempDirectory("video-test").resolve("sample");
        return encoder.encode(frames(), stem).orElseThrow(() -> new AssertionError("Encoder returned no file"));
    }

    private static List<VideoFrame> frames() throws IOException {
        List<VideoFrame> frames = new ArrayList<>();
        for (int i = 0; i < FRAME_COUNT; i++) {
            BufferedImage image = new BufferedImage(641, 361, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(new Color(20 * i % 255, 80, 160));
            g.fillRect(0, 0, 641, 361);
            g.setColor(Color.WHITE);
            g.fillRect(40 * i, 150, 60, 60);
            g.dispose();
            ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", jpeg);
            frames.add(new VideoFrame(jpeg.toByteArray(), i * FRAME_INTERVAL_SECONDS));
        }
        return frames;
    }
}
