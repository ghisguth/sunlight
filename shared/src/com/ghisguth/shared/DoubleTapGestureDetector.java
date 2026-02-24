package com.ghisguth.shared;

public class DoubleTapGestureDetector {
    private final long maxTapIntervalMs;
    private long lastTapTimeMs = 0;

    public DoubleTapGestureDetector(long maxTapIntervalMs) {
        this.maxTapIntervalMs = maxTapIntervalMs;
    }

    public boolean registerTap(long currentTimeMs) {
        if ((currentTimeMs - lastTapTimeMs) > maxTapIntervalMs) {
            // This is the first tap of a potential double-tap gesture (or it's been too long)
            lastTapTimeMs = currentTimeMs;
            return false;
        } else {
            // This is a valid double tap! Reset the timer to prevent triple-tap matching.
            lastTapTimeMs = 0;
            return true;
        }
    }
}
