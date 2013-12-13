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
import com.ghisguth.gfx.*;
import com.ghisguth.shared.ResourceHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.Random;

public class Lines extends RendererBase {
    private static String TAG = "Lines";
    private final int lineCount = 1500;
    private Program phenixLineProgram;
    private Program postProgram;
    private Texture baseTexture;
    private Texture noiseTexture;
    private Texture colorTexture;
    private VertexBuffer quadVertices;
    private VertexBuffer lineVertices;
    private float[] MVP_matrix = new float[16];
    private float[] P_matrix = new float[16];
    private float[] M_matrix = new float[16];
    private float[] V_matrix = new float[16];
    private float[] Q_matrix = new float[16];
    private RenderTexture renderTexture;
    private FrameBuffer frameBuffer;
    private int frameBufferWidth = 256;
    private int frameBufferHeight = 256;
    private int surfaceWidth = 256;
    private int surfaceHeight = 256;
    private boolean useSmallerTextures_ = false;
    private boolean useNonPowerOfTwoTextures_ = false;
    private boolean useNonSquareTextures_ = false;
    private boolean postEffectsEnabled = true;

    private float backgroundColorRed = 0.0f;
    private float backgroundColorGreen = 0.0f;
    private float backgroundColorBlue = 0.0f;
    private float linesColorRed = 1.0f;
    private float linesColorGreen = 1.0f;
    private float linesColorBlue = 1.0f;

    private float blur = 0.86f;
    private float blurFactor = 1.0f;
    private float brightness = 0.15f + 0.5f;
    private float brightnessFactor = 1.0f;


    private float lineWidth = 1.0f;
    private float lineWidthFactor = 1.0f;

    private float speedFactor = 1.0f;
    private float rotationSpeedFactor = 1.0f;

    public Lines(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        quadVertices = GeometryHelper.createScreenQuad();

        // Create lines
        Random rnd = new Random();
        float[] lineVerticesArray = new float[lineCount * 3];
        for (int i = 0; i < lineCount; ++i) {
            lineVerticesArray[i * 3 + 0] = rnd.nextFloat() * 2 - 1;
            lineVerticesArray[i * 3 + 1] = rnd.nextFloat() * 2 - 1;
            lineVerticesArray[i * 3 + 2] = rnd.nextFloat();
        }

        lineVertices = new VertexBuffer(lineVerticesArray, new short[0], false);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Matrix.orthoM(Q_matrix, 0, 0, 1, 0, 1, -1, 1);

        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        if (phenixLineProgram != null) {
            phenixLineProgram.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();

        setupFrameBuffer(unused);

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 1.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
    }

    private void setupFrameBuffer(GL10 unused) {
        TextureManager textureManager = TextureManager.getSingletonObject();

        renderTexture = textureManager.createRenderTexture(frameBufferWidth, frameBufferHeight);

        if (!renderTexture.load()) {
            Log.e(TAG, "Could not create render texture");
            throw new RuntimeException("Could not create render texture");
        }

        frameBuffer = textureManager.createFrameBuffer(renderTexture);

        if (!frameBuffer.load()) {
            Log.e(TAG, "Could not create frame buffer");
            throw new RuntimeException("Could not create frame buffer");
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

        renderTexture.update(frameBufferWidth, frameBufferHeight);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(backgroundColorRed, backgroundColorGreen, backgroundColorBlue, 1.0f);

        if (postEffectsEnabled) {
            frameBuffer.bind();
            GLES20.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            renderPostEffect();
            renderLines();

            frameBuffer.unbind();

            GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            renderPostEffect();
        } else { 
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            renderLines();
        }
    }

    private void renderPostEffect() {
        if (postProgram.use()) {
            renderTexture.bind(GLES20.GL_TEXTURE0, postProgram, "sTexture");
            quadVertices.bind(postProgram, "aPosition", "aTextureCoord");

            GLES20.glUniform1f(postProgram.getUniformLocation("uBlur"), blur * blurFactor);

            GLES20.glUniformMatrix4fv(postProgram.getUniformLocation("uMVPMatrix"), 1, false, Q_matrix, 0);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

            quadVertices.draw(GLES20.GL_TRIANGLE_STRIP);

            GLES20.glDisable(GLES20.GL_BLEND);

            quadVertices.unbind(postProgram, "aPosition", "aTextureCoord");

            renderTexture.unbind(GLES20.GL_TEXTURE0);
        }
    }

    private void renderLines() {
        if (phenixLineProgram != null) {
            if (!phenixLineProgram.use()) {
                return;
            }

            float angle = 360.0f * getTimeDeltaByScale((long) (1 * 50000L / speedFactor / rotationSpeedFactor));
            Matrix.setRotateM(M_matrix, 0, angle, 0, 0, 1.0f);

            Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
            Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

            float delta = getTimeDeltaByScale((long) (1 * 25000L / speedFactor));

            lineVertices.bind(phenixLineProgram, "aPosition", null);

            GLES20.glUniformMatrix4fv(phenixLineProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);

            GLES20.glUniform1f(phenixLineProgram.getUniformLocation("uDelta"), delta);
            GLES20.glUniform1f(phenixLineProgram.getUniformLocation("uBrightness"), brightness * brightnessFactor);
            GLES20.glUniform3f(phenixLineProgram.getUniformLocation("uColor"), linesColorRed, linesColorGreen, linesColorBlue);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
            GLES20.glLineWidth(lineWidth * lineWidthFactor);

            lineVertices.draw(GLES20.GL_LINES);

            GLES20.glDisable(GLES20.GL_BLEND);

            lineVertices.unbind(phenixLineProgram, "aPosition", null);
        }
    }

    private float getTimeDeltaByScale(long scale) {
        if (scale < 1)
            return 0.0f;
        long time = SystemClock.uptimeMillis() % scale;
        return (float) ((int) time) / (float) scale;
    }

    private void loadResources() {
        loadShaders();
        loadTextures();
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

    private void loadShaders() {
        if (phenixLineProgram != null && postProgram != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();
            Shader vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.phenix_line_vertex)));
            Shader fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.phenix_line_fragment)));
            phenixLineProgram = shaderManager.createShaderProgram(vertex, fragment);

            vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.post_blur_vertex)));
            fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.post_blur_fragment)));
            postProgram = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }
}
