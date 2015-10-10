/**
 * This file is a part of sunlight project
 * Copyright (c) $today.year sunlight authors (see file `COPYRIGHT` for the license)
 */

package com.ghisguth.demo;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.ghisguth.gfx.FrameBuffer;
import com.ghisguth.gfx.GeometryHelper;
import com.ghisguth.gfx.Program;
import com.ghisguth.gfx.RenderTexture;
import com.ghisguth.gfx.Shader;
import com.ghisguth.gfx.ShaderManager;
import com.ghisguth.gfx.Texture;
import com.ghisguth.gfx.TextureManager;
import com.ghisguth.gfx.VertexBuffer;
import com.ghisguth.shared.ResourceHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Test extends RendererBase {
    private static String TAG = "Sunlight";
    private Program spriteProgram;
    private Texture baseTexture;
    private VertexBuffer quadVertices;
    private float[] P_matrix = new float[16];
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

    public Test(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        quadVertices = GeometryHelper.createSprite();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Matrix.orthoM(Q_matrix, 0, 0, 1, 0, 1, -1, 1);

        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        if (spriteProgram != null) {
            spriteProgram.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();

        setupFrameBuffer(unused);

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 2.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
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
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        renderScene();
    }

    private void renderScene() {
        if (spriteProgram != null && baseTexture != null) {
            if (!spriteProgram.use() || !baseTexture.load()) {
                return;
            }

            baseTexture.bind(GLES20.GL_TEXTURE0, spriteProgram, "sTexture");
            quadVertices.bind(spriteProgram, "aPosition", "aTextureCoord");

            GLES20.glUniformMatrix4fv(spriteProgram.getUniformLocation("uMVPMatrix"), 1, false, Q_matrix, 0);

            quadVertices.draw(GLES20.GL_TRIANGLE_STRIP);
        }
    }

    private void loadResources() {
        loadShaders();
        loadTextures();
    }

    private void loadTextures() {
        try {
            TextureManager textureManager = TextureManager.getSingletonObject();
            if (baseTexture == null) {
                baseTexture = textureManager.createTexture(getResources(), R.raw.noise, false, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load textures from resources " + ex.toString());
        }
    }

    private void loadShaders() {
        if (spriteProgram != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();
            Shader vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sprite_vertex)));
            Shader fragment = shaderManager.createFragmentShader(ResourceHelper.loadRawString(openResource(R.raw.sprite_fragment)));
            spriteProgram = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }
}
