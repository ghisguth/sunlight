/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class Texture {
    private static String TAG = "Sunlight";

    private int texture;

    private Resources resources;
    private int resource;
    private boolean compressed;
    private int minFilter;
    private int maxFilter;
    private int wrapS;
    private int wrapT;

    private int activeTexture;

    public Texture(Resources resources, int resource, boolean compressed, int minFilter, int maxFilter, int wrapS, int wrapT) {
        this.resources = resources;
        this.resource = resource;
        this.compressed = compressed;
        this.minFilter = minFilter;
        this.maxFilter = maxFilter;
        this.wrapS = wrapS;
        this.wrapT = wrapT;
        TextureManager.getSingletonObject().registerTexture(this);
    }

    protected void finalize() throws Throwable {
        unload();
        super.finalize();
    }

    public boolean load() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        texture = textures[0];

        if (texture == 0) {
            return false;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, minFilter);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, maxFilter);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);

        InputStream is = resources.openRawResource(resource);

        try {
            if (compressed) {
                ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0,
                        GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, is);
                ErrorHelper.checkGlError(TAG, "ETC1Util.loadTexture");
            } else {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                bitmap.recycle();
                ErrorHelper.checkGlError(TAG, "texImage2D");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not load texture: " + e);
            unload();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
                Log.w(TAG, "Unable to close input stream: " + e);
            }
        }

        return true;
    }

    public void unload() {
        if (texture != 0) {
            int[] textures = new int[1];
            textures[0] = texture;
            GLES20.glDeleteTextures(1, textures, 0);
            texture = 0;
            ErrorHelper.checkGlError(TAG, "glDeleteTextures");
        }
    }

    public void bind(int activeTexture)
    {
        this.activeTexture = 0;
        GLES20.glActiveTexture(activeTexture);
        ErrorHelper.checkGlError(TAG, "glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        ErrorHelper.checkGlError(TAG, "glBindTexture texture");
    }

    public void bind(int activeTexture, Program program, String name)
    {
        bind(activeTexture);
        GLES20.glUniform1i(program.getUniformLocation(name), activeTexture - GLES20.GL_TEXTURE0);
        ErrorHelper.checkGlError(TAG, "glUniform1i");
    }

    public int getTexture() {
        return texture;
    }
}
