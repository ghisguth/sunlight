/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.demo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.Toast;

import java.io.InputStream;

abstract class RendererBase extends GLSurfaceView implements GLSurfaceView.Renderer {

    public RendererBase(Context context) {
        super(context);
    }

    protected InputStream openResource(int id) {
        return getContext().getResources().openRawResource(id);
    }

    protected void showError(final String errorMsg) {
        post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

}
