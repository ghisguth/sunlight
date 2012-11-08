/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ghisugth.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import com.ghisguth.gfx.Shader;
import com.ghisguth.gfx.ShaderManager;
import com.ghisguth.gfx.ShaderProgram;
import com.ghisguth.shared.ResourceHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Test extends RendererBase {
    private static String TAG = "SunTest";

    private ShaderProgram program;

    public Test(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    private void loadResources() {
        if (program != null) {
            return;
        }

        try {
            Shader vertexTmp = new Shader(GLES20.GL_FRAGMENT_SHADER, ResourceHelper.loadRawString(openResource(R.raw.post_blur_fragment)));

            vertexTmp.load();

            Shader vertex = new Shader(GLES20.GL_VERTEX_SHADER, ResourceHelper.loadRawString(openResource(R.raw.phenix_line_vertex)));
            Shader fragment = new Shader(GLES20.GL_FRAGMENT_SHADER, ResourceHelper.loadRawString(openResource(R.raw.phenix_line_fragment)));
            program = new ShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(0.0f, 0.15f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (program != null) {
            if (!program.load()) {
                return;
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        ShaderManager.getSingletonObject().cleanUp();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        loadResources();

        if (program != null) {
            program.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();
    }
}
