package com.ghisguth.sun;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.ghisguth.wallpaper.GLES20WallpaperService;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

public class Wallpaper extends GLES20WallpaperService {
    public static final String SHARED_PREF_NAME = "SunSettings";
    private static final boolean DEBUG = false;
    private static String TAG = "Sunlight";

    @Override
    public Engine onCreateEngine() {
        Context dpContext = com.ghisguth.shared.ContextHelper.getDeviceProtectedContext(this);
        return new WallpaperEngine(
                this, dpContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE));
    }

    class WallpaperEngine extends GLWallpaperService.GLEngine
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean doubleTapEnabled = true;
        private final com.ghisguth.shared.DoubleTapGestureDetector gestureDetector =
                new com.ghisguth.shared.DoubleTapGestureDetector(500);

        public WallpaperEngine(Context context, SharedPreferences preferences) {
            super();

            setEGLContextFactory(new ContextFactory());
            setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0));

            SunRenderer renderer = new SunRenderer(context);
            renderer.setSharedPreferences(preferences);
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);

            preferences.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(preferences, null);
        }

        public Bundle onCommand(
                java.lang.String action,
                int x,
                int y,
                int z,
                android.os.Bundle extras,
                boolean resultRequested) {
            if (!this.doubleTapEnabled
                    || !android.app.WallpaperManager.COMMAND_TAP.equals(action)) {
                return super.onCommand(action, x, y, z, extras, resultRequested);
            }

            if (gestureDetector.registerTap(System.currentTimeMillis())) {
                Intent myIntent = new Intent();
                String appPackageName = getApplicationContext().getPackageName();
                try {
                    myIntent.setClassName(appPackageName, "com.ghisguth.sun.WallpaperSettings");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return super.onCommand(action, x, y, z, extras, resultRequested);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            try {
                this.doubleTapEnabled = sharedPreferences.getBoolean("double_tab_settings", true);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                    notifyColorsChanged();
                }
            } catch (final Exception e) {
                Log.e(TAG, "PREF init error: " + e);
            }
        }

        @Override
        public android.app.WallpaperColors onComputeColors() {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                Context dpContext =
                        com.ghisguth.shared.ContextHelper.getDeviceProtectedContext(Wallpaper.this);
                SharedPreferences prefs =
                        dpContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

                // The sun background is always black space
                // The sun color is determined by the temperature slider (0-255)
                int temperature = prefs.getInt("temperature", 7);

                // Map the 0-255 temperature to a Hue (0 = Red, 240 = Blue)
                float hue = (temperature / 255.0f) * 240.0f;
                float[] hsv = {hue, 1.0f, 1.0f};
                int sunColor = android.graphics.Color.HSVToColor(hsv);

                // The sun background is always black space
                int backgroundColor = 0xFF000000;

                return new android.app.WallpaperColors(
                        android.graphics.Color.valueOf(sunColor),
                        android.graphics.Color.valueOf(backgroundColor),
                        null);
            }
            return super.onComputeColors();
        }

        @Override
        public void onZoomChanged(float zoom) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                // Pass zoom directly to OpenGL render logic if supported, or just ignore if static
            }
            super.onZoomChanged(zoom);
        }
    }
}
