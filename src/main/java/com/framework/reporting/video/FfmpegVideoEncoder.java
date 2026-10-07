package com.framework.reporting.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Encodes frames into a browser-playable H.264 MP4 using the {@code ffmpeg} binary (pre-installed on
 * GitHub-hosted Ubuntu runners). Frame timing is preserved with the concat demuxer, so the video plays
 * back in real time rather than as a slideshow.
 */
public final class FfmpegVideoEncoder implements VideoEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(FfmpegVideoEncoder.class);
    private static final long ENCODE_TIMEOUT_SECONDS = 120;
    private static final long PROBE_TIMEOUT_SECONDS = 10;
    private static final double MIN_FRAME_SECONDS = 0.04;
    private static final double MAX_FRAME_SECONDS = 5.0;
    private static final double LAST_FRAME_SECONDS = 1.5;
    private static final String VIDEO_FILTER = "scale=1280:720:force_original_aspect_ratio=decrease,"
            + "pad=1280:720:(ow-iw)/2:(oh-ih)/2,format=yuv420p";
    private static volatile Boolean available;

    /**
     * Returns {@code true} when {@code ffmpeg} can be executed (result cached).
     */
    public static boolean isAvailable() {
        Boolean cached = available;
        if (cached == null) {
            cached = probe();
            available = cached;
        }
        return cached;
    }

    @Override
    public Optional<Path> encode(List<VideoFrame> frames, Path outputStem) {
        if (frames.isEmpty()) {
            return Optional.empty();
        }
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("video-frames");
            Path concatList = writeFrames(frames, workDir);
            Path output = outputStem.resolveSibling(outputStem.getFileName() + "." + extension());
            List<String> command = List.of("ffmpeg", "-y", "-loglevel", "error",
                    "-f", "concat", "-safe", "0", "-i", concatList.toString(),
                    "-vf", VIDEO_FILTER, "-r", "10", "-c:v", "libx264", "-preset", "veryfast",
                    "-movflags", "+faststart", output.toString());
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(workDir.resolve("ffmpeg.log").toFile())
                    .start();
            if (!process.waitFor(ENCODE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                LOG.warn("ffmpeg timed out encoding {}", output);
                return Optional.empty();
            }
            if (process.exitValue() != 0) {
                LOG.warn("ffmpeg failed (exit {}): {}", process.exitValue(),
                        Files.readString(workDir.resolve("ffmpeg.log"), StandardCharsets.UTF_8));
                return Optional.empty();
            }
            return Optional.of(output);
        } catch (IOException e) {
            LOG.warn("Video encoding failed: {}", e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            deleteQuietly(workDir);
        }
    }

    @Override
    public String mimeType() {
        return "video/mp4";
    }

    @Override
    public String extension() {
        return "mp4";
    }

    private static Path writeFrames(List<VideoFrame> frames, Path workDir) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < frames.size(); i++) {
            Path file = workDir.resolve(String.format(Locale.ROOT, "frame_%05d.jpg", i));
            Files.write(file, frames.get(i).jpeg());
            double duration = i + 1 < frames.size()
                    ? frames.get(i + 1).timestampSeconds() - frames.get(i).timestampSeconds()
                    : LAST_FRAME_SECONDS;
            duration = Math.max(MIN_FRAME_SECONDS, Math.min(MAX_FRAME_SECONDS, duration));
            lines.add("file '" + file.toAbsolutePath() + "'");
            lines.add(String.format(Locale.ROOT, "duration %.3f", duration));
        }
        // The concat demuxer ignores the duration of the final entry unless the file is repeated.
        lines.add("file '" + workDir.resolve(String.format(Locale.ROOT, "frame_%05d.jpg", frames.size() - 1))
                .toAbsolutePath() + "'");
        Path list = workDir.resolve("frames.txt");
        Files.write(list, lines, StandardCharsets.UTF_8);
        return list;
    }

    private static boolean probe() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            return process.waitFor(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void deleteQuietly(Path directory) {
        if (directory == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            LOG.debug("Could not delete temp directory {}: {}", directory, e.getMessage());
        }
    }
}
