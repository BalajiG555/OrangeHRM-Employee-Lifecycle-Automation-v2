package com.framework.reporting.video;

/**
 * A single JPEG frame captured from the browser.
 *
 * @param jpeg             encoded image bytes
 * @param timestampSeconds capture time in seconds (monotonic within one recording)
 */
public record VideoFrame(byte[] jpeg, double timestampSeconds) {
}
