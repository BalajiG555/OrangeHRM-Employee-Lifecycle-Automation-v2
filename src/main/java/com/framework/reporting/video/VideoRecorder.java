package com.framework.reporting.video;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Records a per-scenario video of the browser using the Chrome DevTools Protocol screencast.
 *
 * <p>Why CDP instead of screen recording: it works headless, needs no X display, captures only this
 * scenario's tab (so parallel scenarios never bleed into each other's videos) and works on Selenium Grid.
 * Raw {@link Command}/{@link Event} objects are used so the code does not depend on a CDP version module.</p>
 *
 * <p>Non-Chromium browsers simply run without video; recording problems never fail a test.</p>
 */
public final class VideoRecorder {

    private static final Logger LOG = LoggerFactory.getLogger(VideoRecorder.class);
    private static final int JPEG_QUALITY = 60;
    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;
    private static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private static final Event<Map<String, Object>> SCREENCAST_FRAME =
            new Event<>("Page.screencastFrame", input -> input.read(Json.MAP_TYPE));

    private final DevTools devTools;
    private final int maxFrames;
    private final List<VideoFrame> frames = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService ackExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "video-frame-ack");
        thread.setDaemon(true);
        return thread;
    });
    private volatile boolean recording;

    private VideoRecorder(DevTools devTools, int maxFrames) {
        this.devTools = devTools;
        this.maxFrames = maxFrames;
    }

    /**
     * Starts recording if the driver supports DevTools; otherwise returns empty.
     */
    public static Optional<VideoRecorder> start(WebDriver driver, int maxFrames) {
        if (!(driver instanceof HasDevTools hasDevTools)) {
            LOG.info("Video recording skipped: {} does not support DevTools.", driver.getClass().getSimpleName());
            return Optional.empty();
        }
        try {
            DevTools devTools = hasDevTools.getDevTools();
            devTools.createSessionIfThereIsNotOne();
            VideoRecorder recorder = new VideoRecorder(devTools, maxFrames);
            recorder.begin();
            return Optional.of(recorder);
        } catch (RuntimeException e) {
            LOG.warn("Video recording could not start; continuing without video: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Stops recording and returns the captured frames (in capture order).
     */
    public List<VideoFrame> stop() {
        recording = false;
        try {
            devTools.send(new Command<Void>("Page.stopScreencast", Map.of()));
        } catch (RuntimeException e) {
            LOG.debug("stopScreencast failed (browser may already be gone): {}", e.getMessage());
        } finally {
            devTools.clearListeners();
            ackExecutor.shutdownNow();
        }
        synchronized (frames) {
            return List.copyOf(frames);
        }
    }

    private void begin() {
        devTools.addListener(SCREENCAST_FRAME, this::onFrame);
        devTools.send(new Command<Void>("Page.enable", Map.of()));
        devTools.send(new Command<Void>("Page.startScreencast", Map.of(
                "format", "jpeg",
                "quality", JPEG_QUALITY,
                "maxWidth", MAX_WIDTH,
                "maxHeight", MAX_HEIGHT,
                "everyNthFrame", 1)));
        recording = true;
        LOG.debug("Video recording started");
    }

    private void onFrame(Map<String, Object> event) {
        Object sessionId = event.get("sessionId");
        try {
            if (recording && frames.size() < maxFrames && event.get("data") instanceof String data) {
                frames.add(new VideoFrame(Base64.getDecoder().decode(data), timestampOf(event)));
            }
        } finally {
            if (recording && sessionId != null) {
                acknowledge(sessionId);
            }
        }
    }

    /**
     * Chrome stops sending frames until each one is acknowledged. The ack is sent from a dedicated thread so
     * the DevTools event thread is never blocked waiting for a response.
     */
    private void acknowledge(Object sessionId) {
        try {
            ackExecutor.execute(() -> {
                try {
                    devTools.send(new Command<Void>("Page.screencastFrameAck", Map.of("sessionId", sessionId)));
                } catch (RuntimeException e) {
                    LOG.debug("Frame ack failed: {}", e.getMessage());
                }
            });
        } catch (RejectedExecutionException e) {
            LOG.debug("Recording already stopped; frame ack skipped.");
        }
    }

    private static double timestampOf(Map<String, Object> event) {
        if (event.get("metadata") instanceof Map<?, ?> metadata && metadata.get("timestamp") instanceof Number ts) {
            return ts.doubleValue();
        }
        return System.nanoTime() / NANOS_PER_SECOND;
    }
}
