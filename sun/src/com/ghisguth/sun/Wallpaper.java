package com.ghisguth.sun;

import android.content.Context;
import android.content.SharedPreferences;
import com.ghisguth.wallpaper.GLES20WallpaperService;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

public class Wallpaper extends GLES20WallpaperService {
    private static String TAG = "Sunlight";
    private static final boolean DEBUG = false;
    public static final String SHARED_PREF_NAME = "SunSettings";

    class WallpaperEngine extends GLWallpaperService.GLEngine {
        public WallpaperEngine(SharedPreferences preferences) {
            super();

            setEGLContextFactory(new ContextFactory());
            setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0));

            SunRenderer renderer = new SunRenderer(null);
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new WallpaperEngine(this.getSharedPreferences(SHARED_PREF_NAME,
                Context.MODE_PRIVATE));
    }
}
