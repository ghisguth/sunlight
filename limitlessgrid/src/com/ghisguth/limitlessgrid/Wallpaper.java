package com.ghisguth.limitlessgrid;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.ghisguth.wallpaper.GLES20WallpaperService;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

public class Wallpaper extends GLES20WallpaperService {
    public static final String SHARED_PREF_NAME = "GridSettings";
    private static final boolean DEBUG = false;
    private static String TAG = "Sunlight.Grid";
    private long lastTap = 0;

    @Override
    public Engine onCreateEngine() {
        Context dpContext = com.ghisguth.shared.ContextHelper.getDeviceProtectedContext(this);
        return new WallpaperEngine(
                this, dpContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE));
    }

    class WallpaperEngine extends GLWallpaperService.GLEngine
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean doubleTapEnabled = true;

        public WallpaperEngine(Context context, SharedPreferences preferences) {
            super();

            setEGLContextFactory(new ContextFactory());
            setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0));

            GridRenderer renderer = new GridRenderer(context);
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

            Intent myIntent = new Intent();

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTap) > 500) {
                lastTap = currentTime;
            } else { // this is a valid doubletap
                String appPackageName = getApplicationContext().getPackageName();
                try {
                    myIntent.setClassName(
                            appPackageName, "com.ghisguth.limitlessgrid.WallpaperSettings");
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
                int lineColor = prefs.getInt("linesColor", 0xFF00FF00); // Green default
                int backgroundColor = prefs.getInt("backgroundColor", 0xFF000000); // Black default

                return new android.app.WallpaperColors(
                        android.graphics.Color.valueOf(lineColor),
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
