package com.framework.reporting.video;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Turns captured frames into a playable file.
 */
public interface VideoEncoder {

    /**
     * Encodes frames into {@code <outputStem>.<extension()>}; returns empty if encoding was not possible.
     */
    Optional<Path> encode(List<VideoFrame> frames, Path outputStem);

    /**
     * MIME type of the produced file (used for the Allure attachment).
     */
    String mimeType();

    /**
     * File extension without the dot.
     */
    String extension();

    /**
     * Picks the best available encoder: MP4 via ffmpeg when installed, else an animated GIF (pure Java).
     */
    static VideoEncoder best() {
        return FfmpegVideoEncoder.isAvailable() ? new FfmpegVideoEncoder() : new GifVideoEncoder();
    }
}
