package com.ghisguth.sun;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class SunRenderer implements GLWallpaperService.Renderer {

    private static String TAG = "SunRenderer";

    private int surface_width_ = 256;
    private int surface_height_ = 256;

    private float[] MVP_matrix_ = new float[16];
    private float[] P_matrix_ = new float[16];
    private float[] M_matrix_ = new float[16];
    private float[] V_matrix_ = new float[16];
    private float[] Q_matrix_ = new float[16];


    public SunRenderer(Context context) {
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glClearColor(0.0f, 0.15f, 0.0f, 1.0f);
        GLES20.glViewport(0, 0, surface_width_, surface_height_);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float scale = 0.1f;
        float ratio = scale * width / height;
        Matrix.frustumM(P_matrix_, 0, -ratio, ratio, -scale, scale, 0.1f,
                100.0f);

        surface_width_ = width;
        surface_height_ = height;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        Matrix.setLookAtM(V_matrix_, 0, 0, 0, 1.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
        Matrix.orthoM(Q_matrix_, 0, 0, 1, 0, 1, -1, 1);
    }
}
