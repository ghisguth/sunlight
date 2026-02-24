package com.ghisguth.shared;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DoubleTapGestureDetectorTest {

    private DoubleTapGestureDetector detector;

    @Before
    public void setup() {
        // Initialize detector with a 500ms double-tap timeframe
        detector = new DoubleTapGestureDetector(500);
    }

    @Test
    public void testFirstTap_ReturnsFalse() {
        boolean result = detector.registerTap(1000L);
        assertFalse("First tap should never trigger a double tap action", result);
    }

    @Test
    public void testDoubleTapWithinThreshold_ReturnsTrue() {
        assertFalse(detector.registerTap(1000L));

        // Second tap 300ms later (within 500ms)
        boolean result = detector.registerTap(1300L);
        assertTrue("Second tap within 500ms should trigger a double tap action", result);
    }

    @Test
    public void testDoubleTapOutsideThreshold_ReturnsFalse() {
        assertFalse(detector.registerTap(1000L));

        // Second tap 600ms later (outside 500ms)
        boolean result = detector.registerTap(1600L);
        assertFalse("Second tap after 500ms should NOT trigger a double tap action", result);
    }

    @Test
    public void testTripleTap_PreventsAccidentalTrigger() {
        assertFalse(detector.registerTap(1000L));
        assertTrue(detector.registerTap(1300L)); // Valid double tap

        // Third tap exactly 200ms after the second.
        // This simulates a user rapidly tapping 3 times.
        // We expect `false` because the double-tap should "consume" the first two taps.
        boolean result = detector.registerTap(1500L);
        assertFalse(
                "A third tap immediately following a valid double tap should be treated as a new first tap",
                result);
    }
}
