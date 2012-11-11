/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisugth.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import com.ghisguth.gfx.ErrorHelper;
import com.ghisguth.gfx.Program;
import com.ghisguth.gfx.Shader;
import com.ghisguth.gfx.ShaderManager;
import com.ghisguth.shared.ResourceHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Test extends RendererBase {
    private static String TAG = "Sunlight";

    private Program program;

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int SHORT_SIZE_BYTES = 2;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;

    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private final float[] triangle_vertices_data = {
            // X, Y, Z, U, V
            1.0f, 0.0f, -1.0f, 1.0f, 0.0f,
            0.0f, 0.0f, -0.5f, 0.0f, 0.0f,
            1.0f, 1.0f, -1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, -1.0f, 0.0f, 1.0f
    };

    private final int horizontalResolution = 64;
    private final int verticalResolution = 32;
    private final int verticesCount = horizontalResolution * verticalResolution;
    private final int indicesCount = horizontalResolution * 2 * (verticalResolution-1);
    //private final int indicesCount = horizontalResolution * (verticalResolution-1) * 3 * 2;


    private FloatBuffer triangle_vertices;
    private ShortBuffer triangle_indices;

    private float[] MVP_matrix = new float[16];
    private float[] P_matrix = new float[16];
    private float[] M_matrix = new float[16];
    private float[] V_matrix = new float[16];

    public Test(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        float[] vertices = new float[verticesCount * 5];
        short[] indices = new short[indicesCount];
        int index = 0;
        float radius = 1.0f;

        for(int j = 0; j < verticalResolution; ++j) {
            double v = (double)j / (verticalResolution - 1);
            double theta = v * Math.PI;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            for(int i = 0; i < horizontalResolution; ++i) {
                double u = (double)i / (horizontalResolution - 1);

                double phi = 2.0f * u * Math.PI;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);

                vertices[index+0] = (float) (radius * sinTheta * cosPhi);
                vertices[index+1] = (float) (radius * sinTheta * sinPhi);
                vertices[index+2] = (float) (radius * cosTheta);
                vertices[index+3] = (float) (u);
                vertices[index+4] = (float) (v);
                index += 5;
            }
        }

        index = 0;

        for(int j = 0; j < verticalResolution - 1; ++j) {
            boolean dir = (j & 1) != 0;
            if(dir || true) {
                for(int i = 0; i < horizontalResolution; ++i) {
                    indices[index+0] = (short)(j * horizontalResolution + i);
                    indices[index+1] = (short)((j+1) * horizontalResolution + i);
                    index += 2;
                }
            }
            else
            {
                for(int i = horizontalResolution - 1; i >= 0; --i) {
                    indices[index+0] = (short)(j * horizontalResolution + i);
                    indices[index+1] = (short)((j+1) * horizontalResolution + i);
                    index += 2;
                }
            }
        }

        triangle_vertices = ByteBuffer
                .allocateDirect(
                        vertices.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        triangle_vertices.put(vertices).position(0);

        triangle_indices = ByteBuffer
                .allocateDirect(
                        indices.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        triangle_indices.put(indices).position(0);
    }

    private void loadResources() {
        if (program != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();
            Shader vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_vertex)));
            Shader fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sun_fragment)));
            program = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (program != null) {
            if (!program.use()) {
                return;
            }

            float angle = getTimeDeltaByScale(15000L);
            //Matrix.setIdentityM(M_matrix, 0);
            Matrix.setRotateM(M_matrix, 0, 360 * angle, 1, 1, 1);
            //Matrix.translateM(M_matrix, 0, 0, angle*10-5, 0);

            //Matrix.translateM(M_matrix, 0, 0, 0, 1.0f);



            Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
            Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

            //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,target_texture_[textureIndex]);
            //GLES20.glUniform1i(texture_loc_, 0);
            //GLES20.glUniform1f(blur_handle_, blur_ * blurFactor_);

            triangle_vertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(program.getAttributeLocation("aPosition"), 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangle_vertices);
            ErrorHelper.checkGlError(TAG, "glVertexAttribPointer aPosition");
            GLES20.glEnableVertexAttribArray(program.getAttributeLocation("aPosition"));
            ErrorHelper.checkGlError(TAG, "glEnableVertexAttribArray aPosition");

            triangle_vertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(program.getAttributeLocation("aTextureCoord"), 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangle_vertices);
            ErrorHelper.checkGlError(TAG, "glVertexAttribPointer aTextureCoord");
            GLES20.glEnableVertexAttribArray(program.getAttributeLocation("aTextureCoord"));
            ErrorHelper.checkGlError(TAG, "glEnableVertexAttribArray aTextureCoord");

            GLES20.glUniformMatrix4fv(program.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);

            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glCullFace(GLES20.GL_BACK);

            //GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, verticesCount);
            //GLES20.glDrawArrays(GLES20.GL_POINTS, 0, verticesCount);
            //ErrorHelper.checkGlError(TAG, "glDrawArrays");

            float dt = getTimeDeltaByScale(22020L);
            int count = indicesCount;//(int)Math.max(4, indicesCount * dt);

            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, count, GLES20.GL_UNSIGNED_SHORT, triangle_indices);
            //ErrorHelper.checkGlError(TAG, "glDrawElements");

            GLES20.glDisableVertexAttribArray(program.getAttributeLocation("aPosition"));
            ErrorHelper.checkGlError(TAG, "glEnableVertexAttribArray aPosition");
            GLES20.glDisableVertexAttribArray(program.getAttributeLocation("aTextureCoord"));
            ErrorHelper.checkGlError(TAG, "glEnableVertexAttribArray aTextureCoord");

            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        ShaderManager.getSingletonObject().cleanUp();

        GLES20.glViewport(0, 0, width, height);
        float scale = 0.1f;
        float ratio = scale * width / height;
        Matrix.frustumM(P_matrix, 0, -ratio, ratio, -scale, scale, 0.1f,
                100.0f);
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

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 2.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
    }

    private float getTimeDeltaByScale(long scale) {
        if (scale < 1)
            return 0.0f;
        long time = SystemClock.uptimeMillis() % scale;
        return (float) ((int) time) / (float) scale;
    }
}
