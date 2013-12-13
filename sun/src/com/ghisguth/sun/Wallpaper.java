package com.ghisguth.sun;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.ghisguth.wallpaper.GLES20WallpaperService;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

public class Wallpaper extends GLES20WallpaperService {
    public static final String SHARED_PREF_NAME = "SunSettings";
    private static final boolean DEBUG = false;
    private static String TAG = "Sunlight";
    private long lastTap = 0;

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine(this, this.getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE));
    }

    class WallpaperEngine extends GLWallpaperService.GLEngine {
        public WallpaperEngine(Context context, SharedPreferences preferences) {
            super();

            setEGLContextFactory(new ContextFactory());
            setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0));

            SunRenderer renderer = new SunRenderer(context);
            renderer.setSharedPreferences(preferences);
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }


        public Bundle onCommand(java.lang.String action, int x, int y, int z,
                                android.os.Bundle extras, boolean resultRequested) {
            Intent myIntent = new Intent();

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTap) > 500) {
                lastTap = currentTime;
            } else { //this is a valid doubletap
                String appPackageName = getApplicationContext().getPackageName();
                try {
                    myIntent.setClassName(appPackageName, "com.ghisguth.sun.WallpaperSettings");
                    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
