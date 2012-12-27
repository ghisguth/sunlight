package com.ghisguth.sun;

import android.content.Context;
import android.content.SharedPreferences;
import com.ghisguth.wallpaper.GLES20WallpaperService;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

public class Wallpaper extends GLES20WallpaperService {
    public static final String SHARED_PREF_NAME = "SunSettings";
    private static final boolean DEBUG = false;
    private static String TAG = "Sunlight";

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
    }
}
