/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.gfx;

import android.opengl.GLES20;

public class Texture {
    private int texture;

    public boolean load() {
        int texture;
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        texture = textures[0];

        if (texture == 0) {
            return false;
        }

        return true;
    }

    public void unload() {
        if (texture != 0) {
            int[] textures = new int[1];
            textures[0] = texture;
            GLES20.glDeleteTextures(1, textures, 0);
            texture = 0;
        }
    }

    public int getTexture() {
        return texture;
    }
}
