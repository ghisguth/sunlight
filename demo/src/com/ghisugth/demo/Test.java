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
import com.ghisguth.gfx.*;
import com.ghisguth.shared.ResourceHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Test extends RendererBase {
    private static String TAG = "Sunlight";

    private Program sunProgram;
    private Program coronaProgram;
    private Program postRayProgram;

    private Texture baseTexture;
    private Texture noiseTexture;
    private Texture colorTexture;

    private final int horizontalResolution = 64;
    private final int verticalResolution = 32;

    private VertexBuffer sphereVertices;
    private VertexBuffer quadVertices;

    private float[] MVP_matrix = new float[16];
    private float[] P_matrix = new float[16];
    private float[] M_matrix = new float[16];
    private float[] V_matrix = new float[16];
    private float[] Q_matrix = new float[16];

    private RenderTexture[] renderTextures;
    private FrameBuffer[] frameBuffers;
    private int targetTextureIndex = 0;

    private int frameBufferWidth = 256;
    private int frameBufferHeight = 256;
    private int surfaceWidth = 256;
    private int surfaceHeight = 256;

    private boolean useSmallerTextures_ = false;
    private boolean useNonPowerOfTwoTextures_ = false;
    private boolean useNonSquareTextures_ = false;
    private boolean useOneFrameBuffer = false;

    private boolean resetFrameBuffers = false;

    private boolean postEffectsEnabled = true;

    public Test(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        sphereVertices = GeometryHelper.createSphere(horizontalResolution, verticalResolution);
        quadVertices = GeometryHelper.createScreenQuad();
    }

    private void loadShaders() {
        if (sunProgram != null && coronaProgram != null && postRayProgram != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();
            Shader vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_vertex)));
            Shader fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sun_lookup_fragment)));
            sunProgram = shaderManager.createShaderProgram(vertex, fragment);

            vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_corona_vertex)));
            fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sun_corona_lookup_fragment)));
            coronaProgram = shaderManager.createShaderProgram(vertex, fragment);

            vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_ray_vertex)));
            fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sun_ray_fragment)));
            postRayProgram = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }

    private void loadTextures() {
        try {
            TextureManager textureManager = TextureManager.getSingletonObject();
            if (baseTexture == null) {
                baseTexture = textureManager.createTexture(getResources(), R.raw.sun_surface_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
            if (noiseTexture == null) {
                noiseTexture = textureManager.createTexture(getResources(), R.raw.noise_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
            if (colorTexture == null) {
                colorTexture = textureManager.createTexture(getResources(), R.raw.star_color_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load textures from resources " + ex.toString());
        }
    }


    private void loadResources() {
        loadShaders();
        loadTextures();
    }

    private void renderSun() {
        if (sunProgram != null && baseTexture != null) {
            if (!sunProgram.use() || !baseTexture.load() || !noiseTexture.load() || !colorTexture.load()) {
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


            baseTexture.bind(GLES20.GL_TEXTURE0, sunProgram, "sBaseTexture");
            noiseTexture.bind(GLES20.GL_TEXTURE1, sunProgram, "sNoiseTexture");
            colorTexture.bind(GLES20.GL_TEXTURE2, sunProgram, "sColorTexture");

            sphereVertices.bind(sunProgram, "aPosition", "aTextureCoord");

            GLES20.glUniformMatrix4fv(sunProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);


            float animationTime = getTimeDeltaByScale(790000L);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uTime"), animationTime);

            float animationTime2 = getTimeDeltaByScale(669000L);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uTime2"), animationTime2);

            float animationTime3 = getTimeDeltaByScale(637000L);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uTime3"), animationTime3);

            //float surfaceColorOffset = 0.02734375f;
            //float surfaceColorAdd = 0.3f;
            //float surfaceColorMul = 2.0f;

            float surfaceColorOffset = 0.83734375f;
            float surfaceColorAdd = 0.0f;
            float surfaceColorMul = 1.0f;

            surfaceColorOffset = getTimeDeltaByScale(19000L);

            GLES20.glUniform1f(sunProgram.getUniformLocation("uColorOffset"), surfaceColorOffset);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uColorAdd"), surfaceColorAdd);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uColorMul"), surfaceColorMul);

            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glCullFace(GLES20.GL_BACK);

            sphereVertices.draw(GLES20.GL_TRIANGLE_STRIP);

            sphereVertices.unbind(sunProgram, "aPosition", "aTextureCoord");

            if (coronaProgram != null && coronaProgram.use()) {

                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

                Matrix.setRotateM(M_matrix, 0, 90, 1, 0, 0);
                Matrix.rotateM(M_matrix, 0, 360 * angle, 0, 0, 1);
                float scale = 1.0f;
                Matrix.scaleM(M_matrix, 0, scale, scale, scale);
                Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
                Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

                baseTexture.bind(GLES20.GL_TEXTURE0, coronaProgram, "sBaseTexture");
                noiseTexture.bind(GLES20.GL_TEXTURE1, coronaProgram, "sNoiseTexture");
                colorTexture.bind(GLES20.GL_TEXTURE2, coronaProgram, "sColorTexture");

                sphereVertices.bind(coronaProgram, "aPosition", "aTextureCoord");
                GLES20.glUniformMatrix4fv(coronaProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime"), animationTime);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime2"), animationTime2);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime3"), animationTime3);
                float animationTime4 = getTimeDeltaByScale(3370000L);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime4"), animationTime4);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uLevel"), 0.5f);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uColorOffset"), surfaceColorOffset);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uColorAdd"), surfaceColorAdd);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uColorMul"), surfaceColorMul);

                sphereVertices.draw(GLES20.GL_TRIANGLE_STRIP);

                sphereVertices.unbind(sunProgram, "aPosition", "aTextureCoord");

                GLES20.glDisable(GLES20.GL_BLEND);
            }

            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if(postEffectsEnabled) {
            if (resetFrameBuffers) {
                resetFrameBuffers = false;

                if (!useOneFrameBuffer) {
                    frameBuffers[1 - targetTextureIndex].bind();
                    GLES20.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                }
            }

            frameBuffers[targetTextureIndex].bind();
            GLES20.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            /*if (!useOneFrameBuffer) {
                renderPostEffect(1 - targetTextureIndex);
            } else {
                renderPostEffect(targetTextureIndex);
            } */
            renderSun();

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            renderPostEffect(targetTextureIndex);

            if (!useOneFrameBuffer) {
                targetTextureIndex = 1 - targetTextureIndex;
            }
        }
        else
        {
            renderSun();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        ShaderManager.getSingletonObject().cleanUp();

        GLES20.glViewport(0, 0, width, height);
        float scale = 0.1f;
        float ratio = scale * width / height;
        Matrix.frustumM(P_matrix, 0, -ratio, ratio, -scale, scale, 0.1f, 100.0f);

        surfaceWidth = width;
        surfaceHeight = height;

        if (!useNonPowerOfTwoTextures_) {
            // lets make framebuffer have power of 2 dimension
            // and it should be less then display size
            frameBufferWidth = 1 << (int) (Math.log(width) / Math.log(2));
            if (frameBufferWidth == surfaceWidth)
                frameBufferWidth >>= 1;
            frameBufferHeight = 1 << (int) (Math.log(height) / Math.log(2));
            if (frameBufferHeight == surfaceHeight)
                frameBufferHeight >>= 1;
        } else {
            frameBufferWidth = surfaceWidth;
            frameBufferHeight = surfaceHeight;
        }

        if (!useNonSquareTextures_) {
            // http://code.google.com/p/android/issues/detail?id=14835
            // The size of the FBO should have square size.
            if (frameBufferHeight > frameBufferWidth) {
                frameBufferWidth = frameBufferHeight;
            } else if (frameBufferWidth > frameBufferHeight) {
                frameBufferHeight = frameBufferWidth;
            }
        }

        if (useSmallerTextures_) {
            frameBufferWidth >>= 1;
            frameBufferHeight >>= 1;
        }

        Log.i("BL***", "frameBufferWidth=" + frameBufferWidth
                + " frameBufferHeight=" + frameBufferHeight);

        renderTextures[0].update(frameBufferWidth, frameBufferHeight);

        if (!useOneFrameBuffer) {
            renderTextures[1].update(frameBufferWidth, frameBufferHeight);
        }

        targetTextureIndex = 0;
        resetFrameBuffers = true;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Matrix.orthoM(Q_matrix, 0, 0, 1, 0, 1, -1, 1);

        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        if (sunProgram != null) {
            sunProgram.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();

        setupFrameBuffer(unused);

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 2.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
    }

    private void renderPostEffect(int textureIndex) {
        if (postRayProgram.use()) {
            renderTextures[textureIndex].bind(GLES20.GL_TEXTURE0, postRayProgram, "sTexture");
            quadVertices.bind(postRayProgram, "aPosition", "aTextureCoord");

            GLES20.glUniform1f(postRayProgram.getUniformLocation("blur"), 0.6f);

            GLES20.glUniformMatrix4fv(postRayProgram.getUniformLocation("uMVPMatrix"), 1, false, Q_matrix, 0);

            quadVertices.draw(GLES20.GL_TRIANGLE_STRIP);
        }
    }

    private void setupFrameBuffer(GL10 unused) {
        TextureManager textureManager = TextureManager.getSingletonObject();

        renderTextures = new RenderTexture[2];

        renderTextures[0] = textureManager.createRenderTexture(frameBufferWidth, frameBufferHeight);
        renderTextures[1] = textureManager.createRenderTexture(frameBufferWidth, frameBufferHeight);

        if(!renderTextures[0].load())
        {
            Log.e(TAG, "Could not create render texture");
            throw new RuntimeException("Could not create render texture");
        }

        if (!useOneFrameBuffer) {
            if(!renderTextures[1].load())
            {
                Log.e(TAG, "Could not create second render texture");
                throw new RuntimeException("Could not create second render texture");
            }
        }

        frameBuffers = new FrameBuffer[2];

        frameBuffers[0] = textureManager.createFrameBuffer(renderTextures[0]);

        if (!frameBuffers[0].load()) {
            Log.e(TAG, "Could not create frame buffer");
            throw new RuntimeException("Could not create frame buffer");
        }

        if (!useOneFrameBuffer) {
            frameBuffers[1] = textureManager.createFrameBuffer(renderTextures[1]);

            if (!frameBuffers[1].load()) {
                Log.e(TAG, "Could not create second frame buffer");
                throw new RuntimeException(
                        "Could not create second frame buffer");
            }
        }
    }

    private float getTimeDeltaByScale(long scale) {
        if (scale < 1)
            return 0.0f;
        long time = SystemClock.uptimeMillis() % scale;
        return (float) ((int) time) / (float) scale;
    }
}
