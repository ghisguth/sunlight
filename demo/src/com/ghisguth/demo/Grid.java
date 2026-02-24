/**
 * This file is a part of sunlight project Copyright (c) $today.year sunlight authors (see file
 * `COPYRIGHT` for the license)
 */
package com.ghisguth.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import com.ghisguth.gfx.FrameBuffer;
import com.ghisguth.gfx.GeometryHelper;
import com.ghisguth.gfx.Program;
import com.ghisguth.gfx.RenderTexture;
import com.ghisguth.gfx.Shader;
import com.ghisguth.gfx.ShaderManager;
import com.ghisguth.gfx.TextureManager;
import com.ghisguth.gfx.VertexBuffer;
import com.ghisguth.shared.ResourceHelper;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Grid extends RendererBase {
    private static final int GRID_DIM = 13;
    private static final int MOVING_LINE_COUNT = (GRID_DIM + 1) * (GRID_DIM + 1) * 2 * 2;
    private static final int STALE_LINE_COUNT = (GRID_DIM + 1) * (GRID_DIM + 1) * 2;
    private static final int LINE_COUNT = MOVING_LINE_COUNT + STALE_LINE_COUNT;
    private static String TAG = "Lines";
    private Program phenixLineProgram;
    private Program postProgram;
    private VertexBuffer quadVertices;
    private VertexBuffer lineVertices;
    private float[] MVP_matrix = new float[16];
    private float[] P_matrix = new float[16];
    private float[] M_matrix = new float[16];
    private float[] V_matrix = new float[16];
    private float[] Q_matrix = new float[16];
    private RenderTexture[] renderTextures = new RenderTexture[2];
    private FrameBuffer[] frameBuffers = new FrameBuffer[2];
    private int frameBufferWidth = 256;
    private int frameBufferHeight = 256;
    private int surfaceWidth = 256;
    private int surfaceHeight = 256;
    private boolean useSmallerTextures_ = false;
    private boolean useNonPowerOfTwoTextures_ = false;
    private boolean useNonSquareTextures_ = false;

    private boolean useOneFramebuffer = false;

    private boolean resetFramebuffers = false;

    private int activeTargettexture = 0;

    private float backgroundColorRed = 0.0f;
    private float backgroundColorGreen = 0.0f;
    private float backgroundColorBlue = 0.0f;
    private float linesColorRed = 1.0f;
    private float linesColorGreen = 1.0f;
    private float linesColorBlue = 1.0f;

    private float blur = 0.86f;
    private float blurFactor = 1.0f;
    private float brightness = 0.15f;
    private float brightnessFactor = 1.0f;

    private float lineWidth = 1.5f;
    private float lineWidthFactor = 1.0f;

    private float speedFactor = 1.0f;
    private float rotationSpeedFactor = 1.0f;

    public Grid(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        quadVertices = GeometryHelper.createScreenQuad();

        float[] line_data = new float[LINE_COUNT * 3];

        int line = 0;

        final float k = 1.0f;

        for (int x = 0; x <= GRID_DIM; ++x) {
            for (int y = 0; y <= GRID_DIM; ++y) {
                line_data[6 * line + 0] = (((float) x / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 1] = -1.0f * k;
                line_data[6 * line + 2] = (((float) y / GRID_DIM));
                line_data[6 * line + 3] = (((float) x / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 4] = 1.0f * k;
                line_data[6 * line + 5] = (((float) y / GRID_DIM));

                ++line;

                line_data[6 * line + 0] = -1.0f * k;
                line_data[6 * line + 1] = (((float) x / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 2] = (((float) y / GRID_DIM));
                line_data[6 * line + 3] = 1.0f * k;
                line_data[6 * line + 4] = (((float) x / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 5] = (((float) y / GRID_DIM));

                ++line;
            }
        }

        for (int x = 0; x <= GRID_DIM; ++x) {
            for (int y = 0; y <= GRID_DIM; ++y) {
                line_data[6 * line + 0] = (((float) x / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 1] = (((float) y / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 2] = 0.0f;
                line_data[6 * line + 3] = (((float) x / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 4] = (((float) y / GRID_DIM) * 2 - 1) * k;
                line_data[6 * line + 5] = 1.0f;

                ++line;
            }
        }

        lineVertices = new VertexBuffer(line_data, new short[0], false);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        if (phenixLineProgram != null) {
            phenixLineProgram.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();

        renderTextures = new RenderTexture[2];
        frameBuffers = new FrameBuffer[2];

        setupFrameBuffer(unused);

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 1.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
        Matrix.orthoM(Q_matrix, 0, 0, 1, 0, 1, -1, 1);
    }

    private void setupFrameBuffer(GL10 unused) {
        TextureManager textureManager = TextureManager.getSingletonObject();

        int numberOfRequiredTextures = useOneFramebuffer ? 1 : 2;

        for (int i = 0; i < numberOfRequiredTextures; ++i) {
            renderTextures[i] =
                    textureManager.createRenderTexture(frameBufferWidth, frameBufferHeight);

            if (!renderTextures[i].load()) {
                Log.e(TAG, "Could not create render texture");
                throw new RuntimeException("Could not create render texture");
            }

            frameBuffers[i] = textureManager.createFrameBuffer(renderTextures[i]);

            if (!frameBuffers[i].load()) {
                Log.e(TAG, "Could not create frame buffer");
                throw new RuntimeException("Could not create frame buffer");
            }
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
            if (frameBufferWidth == surfaceWidth) frameBufferWidth >>= 1;
            frameBufferHeight = 1 << (int) (Math.log(height) / Math.log(2));
            if (frameBufferHeight == surfaceHeight) frameBufferHeight >>= 1;
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

        Log.i(
                "BL***",
                "frameBufferWidth=" + frameBufferWidth + " frameBufferHeight=" + frameBufferHeight);

        for (int i = 0; i < renderTextures.length; ++i) {
            if (renderTextures[i] != null) {
                renderTextures[i].update(frameBufferWidth, frameBufferHeight);
            }
        }

        resetFramebuffers = true;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(backgroundColorRed, backgroundColorGreen, backgroundColorBlue, 1.0f);

        if (resetFramebuffers) {
            resetFramebuffers = false;
            if (!useOneFramebuffer) {
                frameBuffers[1 - activeTargettexture].bind();
                GLES20.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            }
        }

        frameBuffers[activeTargettexture].bind();
        GLES20.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        renderBlurTexture(useOneFramebuffer ? (activeTargettexture) : (1 - activeTargettexture));
        renderLines();

        frameBuffers[activeTargettexture].unbind();

        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        renderBlurTexture(activeTargettexture);

        if (!useOneFramebuffer) {
            activeTargettexture = 1 - activeTargettexture;
        }
    }

    private void renderBlurTexture(int textureIndex) {
        if (postProgram.use()) {
            renderTextures[textureIndex].bind(GLES20.GL_TEXTURE0, postProgram, "sTexture");
            quadVertices.bind(postProgram, "aPosition", "aTextureCoord");

            GLES20.glUniform1f(postProgram.getUniformLocation("uBlur"), blur * blurFactor);

            GLES20.glUniformMatrix4fv(
                    postProgram.getUniformLocation("uMVPMatrix"), 1, false, Q_matrix, 0);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

            quadVertices.draw(GLES20.GL_TRIANGLE_STRIP);

            GLES20.glDisable(GLES20.GL_BLEND);

            quadVertices.unbind(postProgram, "aPosition", "aTextureCoord");

            renderTextures[textureIndex].unbind(GLES20.GL_TEXTURE0);
        }
    }

    private void renderLines() {
        if (phenixLineProgram != null) {
            if (!phenixLineProgram.use()) {
                return;
            }

            float angle =
                    360.0f
                            * getTimeDeltaByScale(
                                    (long) (1 * 50000L / speedFactor / rotationSpeedFactor));
            Matrix.setRotateM(M_matrix, 0, angle, 0, 0, 1.0f);

            Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
            Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

            float delta = getTimeDeltaByScale((long) (1 * 25000L / speedFactor));

            lineVertices.bind(phenixLineProgram, "aPosition", null);

            GLES20.glUniformMatrix4fv(
                    phenixLineProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);

            GLES20.glUniform1f(phenixLineProgram.getUniformLocation("uDelta"), delta);
            GLES20.glUniform1f(
                    phenixLineProgram.getUniformLocation("uBrightness"),
                    brightness * brightnessFactor);
            GLES20.glUniform3f(
                    phenixLineProgram.getUniformLocation("uColor"),
                    linesColorRed,
                    linesColorGreen,
                    linesColorBlue);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
            GLES20.glLineWidth(lineWidth * lineWidthFactor);

            lineVertices.draw(GLES20.GL_LINES, 0, MOVING_LINE_COUNT);

            GLES20.glUniform1f(phenixLineProgram.getUniformLocation("uDelta"), 0.0f);

            lineVertices.draw(GLES20.GL_LINES, MOVING_LINE_COUNT, STALE_LINE_COUNT);

            GLES20.glDisable(GLES20.GL_BLEND);

            lineVertices.unbind(phenixLineProgram, "aPosition", null);
        }
    }

    private float getTimeDeltaByScale(long scale) {
        if (scale < 1) return 0.0f;
        long time = SystemClock.uptimeMillis() % scale;
        return (float) ((int) time) / (float) scale;
    }

    private void loadResources() {
        loadShaders();
    }

    private void loadShaders() {
        if (phenixLineProgram != null && postProgram != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();
            Shader vertex =
                    shaderManager.createVertexShader(
                            ResourceHelper.loadRawString(
                                    openResource(com.ghisguth.gfx.R.raw.phenix_line_vertex)));
            Shader fragment =
                    shaderManager.createFragmentShader(
                            ResourceHelper.loadRawString(
                                    openResource(com.ghisguth.gfx.R.raw.phenix_line_fragment)));
            phenixLineProgram = shaderManager.createShaderProgram(vertex, fragment);

            vertex =
                    shaderManager.createVertexShader(
                            ResourceHelper.loadRawString(
                                    openResource(com.ghisguth.gfx.R.raw.post_blur_vertex)));
            fragment =
                    shaderManager.createFragmentShader(
                            ResourceHelper.loadRawString(
                                    openResource(com.ghisguth.gfx.R.raw.post_blur_fragment)));
            postProgram = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }
}
