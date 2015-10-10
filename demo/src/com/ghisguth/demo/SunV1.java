/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.ghisguth.gfx.Program;
import com.ghisguth.gfx.Shader;
import com.ghisguth.gfx.ShaderManager;
import com.ghisguth.gfx.Texture;
import com.ghisguth.gfx.TextureManager;
import com.ghisguth.gfx.VertexBuffer;
import com.ghisguth.shared.ResourceHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SunV1 extends RendererBase {
    private static String TAG = "Sunlight";
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
    private final int indicesCount = horizontalResolution * 2 * (verticalResolution - 1);
    private Program program;
    private Program coronaProgram;
    private Texture baseTexture;
    private Texture noiseTexture;
    private VertexBuffer sphereVertices;
    private float[] MVP_matrix = new float[16];
    private float[] P_matrix = new float[16];
    private float[] M_matrix = new float[16];
    private float[] V_matrix = new float[16];

    public SunV1(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        float[] vertices = new float[verticesCount * 5];
        short[] indices = new short[indicesCount];
        int index = 0;
        float radius = 1.0f;

        for (int j = 0; j < verticalResolution; ++j) {
            double v = (double) j / (verticalResolution - 1);
            double theta = v * Math.PI;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            for (int i = 0; i < horizontalResolution; ++i) {
                double u = (double) i / (horizontalResolution - 1);

                double phi = 2.0f * u * Math.PI;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);

                vertices[index + 0] = (float) (radius * sinTheta * cosPhi);
                vertices[index + 1] = (float) (radius * sinTheta * sinPhi);
                vertices[index + 2] = (float) (radius * cosTheta);
                vertices[index + 3] = (float) (u);
                vertices[index + 4] = (float) (v);
                index += 5;
            }
        }

        index = 0;

        for (int j = 0; j < verticalResolution - 1; ++j) {
            boolean dir = (j & 1) != 0;
            if (dir || true) {
                for (int i = 0; i < horizontalResolution; ++i) {
                    indices[index + 0] = (short) (j * horizontalResolution + i);
                    indices[index + 1] = (short) ((j + 1) * horizontalResolution + i);
                    index += 2;
                }
            } else {
                for (int i = horizontalResolution - 1; i >= 0; --i) {
                    indices[index + 0] = (short) (j * horizontalResolution + i);
                    indices[index + 1] = (short) ((j + 1) * horizontalResolution + i);
                    index += 2;
                }
            }
        }

        sphereVertices = new VertexBuffer(vertices, indices, true);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated");
        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        if (program != null) {
            program.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 2.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
    }

    private void loadResources() {
        loadShaders();
        loadTextures();
    }

    private void loadShaders() {
        if (program != null && coronaProgram != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();
            Shader vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_vertex)));
            Shader fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sun_fragment)));
            program = shaderManager.createShaderProgram(vertex, fragment);

            vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_corona_vertex)));
            fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sun_corona_fragment)));
            coronaProgram = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }

    private void loadTextures() {
        try {
            TextureManager textureManager = TextureManager.getSingletonObject();
            if (baseTexture == null) {
                baseTexture = textureManager.createTexture(getResources(), R.raw.base_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
            if (noiseTexture == null) {
                noiseTexture = textureManager.createTexture(getResources(), R.raw.noise_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load textures from resources " + ex.toString());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.e(TAG, "onSurfaceChanged");
        ShaderManager.getSingletonObject().cleanUp();

        GLES20.glViewport(0, 0, width, height);
        float scale = 0.1f;
        float ratio = scale * width / height;
        Matrix.frustumM(P_matrix, 0, -ratio, ratio, -scale, scale, 0.1f,
                100.0f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (program != null && baseTexture != null) {
            if (!program.use() || !baseTexture.load() || !noiseTexture.load()) {
                return;
            }

            float angle = getTimeDeltaByScale(600000L);
            //Matrix.setIdentityM(M_matrix, 0);
            Matrix.setRotateM(M_matrix, 0, 90, 1, 0, 0);
            Matrix.rotateM(M_matrix, 0, 360 * angle, 0, 0, 1);
            //Matrix.translateM(M_matrix, 0, 0, angle*10-5, 0);

            //Matrix.translateM(M_matrix, 0, 0, 0, 1.0f);

            Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
            Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);


            baseTexture.bind(GLES20.GL_TEXTURE0, program, "sBaseTexture");
            noiseTexture.bind(GLES20.GL_TEXTURE1, program, "sNoiseTexture");

            sphereVertices.bind(program, "aPosition", "aTextureCoord");

            GLES20.glUniformMatrix4fv(program.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);


            float animationTime = getTimeDeltaByScale(790000L);
            GLES20.glUniform1f(program.getUniformLocation("uTime"), animationTime);

            float animationTime2 = getTimeDeltaByScale(669000L);
            GLES20.glUniform1f(program.getUniformLocation("uTime2"), animationTime2);

            float animationTime3 = getTimeDeltaByScale(637000L);
            GLES20.glUniform1f(program.getUniformLocation("uTime3"), animationTime3);

            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glCullFace(GLES20.GL_BACK);

            sphereVertices.draw(GLES20.GL_TRIANGLE_STRIP);

            sphereVertices.unbind(program, "aPosition", "aTextureCoord");

            if (coronaProgram != null && coronaProgram.use()) {

                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

                Matrix.setRotateM(M_matrix, 0, 90, 1, 0, 0);
                Matrix.rotateM(M_matrix, 0, 360 * getTimeDeltaByScale(400000L), 0, 0, 1);
                float scale = 1.0f;
                Matrix.scaleM(M_matrix, 0, scale, scale, scale);
                Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
                Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

                baseTexture.bind(GLES20.GL_TEXTURE0, coronaProgram, "sBaseTexture");
                noiseTexture.bind(GLES20.GL_TEXTURE1, coronaProgram, "sNoiseTexture");

                sphereVertices.bind(coronaProgram, "aPosition", "aTextureCoord");
                GLES20.glUniformMatrix4fv(coronaProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime"), animationTime);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime2"), animationTime2);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime3"), animationTime3);
                float animationTime4 = getTimeDeltaByScale(4370000L);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime4"), animationTime4);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uLevel"), 0.5f);

                sphereVertices.draw(GLES20.GL_TRIANGLE_STRIP);

                sphereVertices.unbind(program, "aPosition", "aTextureCoord");
            }

            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
    }

    private float getTimeDeltaByScale(long scale) {
        if (scale < 1)
            return 0.0f;
        long time = SystemClock.uptimeMillis() % scale;
        return (float) ((int) time) / (float) scale;
    }
}
