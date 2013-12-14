package com.ghisguth.blurredlines;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

import java.io.InputStream;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cxa.lineswallpaper.R;


public class BlurredLinesRenderer implements GLWallpaperService.Renderer {

    private static String TAG = "Sunlight.BlurredLines";
    private int frameBufferWidth = 256;
    private int frameBufferHeight = 256;
    private int surfaceWidth = 256;
    private int surfaceHeight = 256;
    private Context context;
    private Program phenixLineProgram;
    private Program postProgram;
    private VertexBuffer quadVertices;
    private VertexBuffer lineVertices;
    private RenderTexture[] renderTextures = new RenderTexture[2];
    private FrameBuffer[] frameBuffers = new FrameBuffer[2];
    private boolean useSmallerTextures_ = false;
    private boolean useNonPowerOfTwoTextures_ = false;
    private boolean useNonSquareTextures_ = false;
    private boolean postEffectsEnabled = true;
    private float[] MVP_matrix = new float[16];
    private float[] P_matrix = new float[16];
    private float[] M_matrix = new float[16];
    private float[] V_matrix = new float[16];
    private float[] Q_matrix = new float[16];
    private SharedPreferences preferences;
    private SettingsUpdater settingsUpdater;

    private boolean useOneFramebuffer = false;

    private boolean resetFramebuffers = false;

    private int activeTargettexture = 0;

    private int lineCount = 1500;

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

    private float lineWidth = 1.0f;
    private float lineWidthFactor = 1.0f;

    private float speedFactor = 1.0f;
    private float rotationSpeedFactor = 1.0f;

    public BlurredLinesRenderer(Context context) {
        this.context = context;

        quadVertices = GeometryHelper.createScreenQuad();
        lineVertices = CreateLineVertices(lineCount);
    }

    private VertexBuffer CreateLineVertices(int count) {
        // Create lines
        Random rnd = new Random();
        float[] lineVerticesArray = new float[count * 3];
        for (int i = 0; i < count; ++i) {
            lineVerticesArray[i * 3 + 0] = rnd.nextFloat() * 2 - 1;
            lineVerticesArray[i * 3 + 1] = rnd.nextFloat() * 2 - 1;
            lineVerticesArray[i * 3 + 2] = rnd.nextFloat();
        }

        return new VertexBuffer(lineVerticesArray, new short[0], false);
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        ShaderManager.getSingletonObject().cleanUp();

        GLES20.glViewport(0, 0, width, height);
        final float scale = 0.1f;
        float vertical = scale;
        float horizontal = vertical * width / height;

        if (width < height) {
            horizontal = scale;
            vertical = horizontal * height / width;
        }

        Matrix.frustumM(P_matrix, 0, -horizontal, horizontal, -vertical, vertical, 0.1f, 100.0f);

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

        for (int i = 0; i < renderTextures.length; ++i) {
            if (renderTextures[i] != null) {
                renderTextures[i].update(frameBufferWidth, frameBufferHeight);
            }
        }

        resetFramebuffers = true;
        activeTargettexture = 0;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        ShaderManager.getSingletonObject().unloadAllShaders();

        if (phenixLineProgram != null) {
            phenixLineProgram.load();
        }

        if(postProgram != null) {
            postProgram.load();
        }

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
            renderTextures[i] = textureManager.createRenderTexture(frameBufferWidth, frameBufferHeight);

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

    private void loadResources() {
        loadShaders();
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

    protected InputStream openResource(int id) {
        return context.getResources().openRawResource(id);
    }

    private void renderBlurTexture(int textureIndex) {
        if (postProgram.use()) {
            renderTextures[textureIndex].bind(GLES20.GL_TEXTURE0, postProgram, "sTexture");
            quadVertices.bind(postProgram, "aPosition", "aTextureCoord");

            GLES20.glUniform1f(postProgram.getUniformLocation("uBlur"), blur * blurFactor);

            GLES20.glUniformMatrix4fv(postProgram.getUniformLocation("uMVPMatrix"), 1, false, Q_matrix, 0);

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

            float angle = 360.0f * getTimeDeltaByScale((long) (1 * 50000L / speedFactor / rotationSpeedFactor));
            Matrix.setRotateM(M_matrix, 0, angle, 0, 0, 1.0f);

            Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
            Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

            float delta = getTimeDeltaByScale((long) (1 * 25000L / speedFactor));

            lineVertices.bind(phenixLineProgram, "aPosition", null);

            GLES20.glUniformMatrix4fv(phenixLineProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);

            float finalBrightness = brightness * brightnessFactor;

            GLES20.glUniform1f(phenixLineProgram.getUniformLocation("uDelta"), delta);
            GLES20.glUniform1f(phenixLineProgram.getUniformLocation("uBrightness"), finalBrightness);
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

    public void setSharedPreferences(SharedPreferences preferences) {
        settingsUpdater = new SettingsUpdater(this);
        this.preferences = preferences;
        this.preferences.registerOnSharedPreferenceChangeListener(settingsUpdater);
        settingsUpdater.onSharedPreferenceChanged(this.preferences, null);
    }

    public void setColors(int backgroundInt, int linesInt) {
        float scale = 1.0f / 255.0f;
        float scaleBackground = scale * 0.05f;
        backgroundColorRed = scaleBackground * Color.red(backgroundInt);
        backgroundColorGreen = scaleBackground * Color.green(backgroundInt);
        backgroundColorBlue = scaleBackground * Color.blue(backgroundInt);
        linesColorRed = scale * Color.red(linesInt);
        linesColorGreen = scale * Color.green(linesInt);
        linesColorBlue = scale * Color.blue(linesInt);
    }

    public void setBlur(int value) {
        blurFactor = getScaledFactor(value, 0.125f);
    }

    public void setBrightness(int value) {
        brightnessFactor = getScaledFactor(value, 1.0f);
    }

    public void setLineWidth(int value) {
        lineWidthFactor = getScaledFactor(value, 1.0f);

        if (lineWidthFactor < 0.01f) {
            lineWidthFactor = 0.01f;
        }
    }

    public void setRotationSpeed(int value) {
        rotationSpeedFactor = getScaledFactor(value, 1.0f);
    }

    public void setSpeed(int value) {
        speedFactor = getScaledFactor(value, 1.0f);
    }

    public void setLinesCount(int value) {
        if (value < 0) {
            value = 0;
        }

        int newLinesCount = 750 + value * 250;

        if (this.lineCount != newLinesCount) {
            this.lineCount = newLinesCount;

            this.lineVertices = CreateLineVertices(lineCount);
        }
    }

    public void setCompatibilitySettings(boolean useSmallerTextures,
                                         boolean useNonPowerOfTwoTextures,
                                         boolean useNonSquareTextures,
                                         boolean useOneFramebuffer) {
        this.useSmallerTextures_ = useSmallerTextures;
        this.useNonPowerOfTwoTextures_ = useNonPowerOfTwoTextures;
        this.useNonSquareTextures_ = useNonSquareTextures;
        this.useOneFramebuffer = useOneFramebuffer;
    }

    private float getScaledFactor(int value, float multiplier) {
        float scale = 1.0f / 127.0f;
        float scaledValue = scale * (value - 127) * multiplier;
        float result = (float) Math.exp(scaledValue);
        return result;
    }

    private class SettingsUpdater implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        private BlurredLinesRenderer renderer;

        public SettingsUpdater(BlurredLinesRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            try {
                boolean useSmallerTextures = sharedPreferences.getBoolean("use_smaller_textures", false);
                boolean useNonPowerOfTwoTextures = sharedPreferences.getBoolean("use_non_power_of_two_textures", false);
                boolean useNonSquareTextures = sharedPreferences.getBoolean("use_non_square_textures", false);
                boolean useOneFramebuffer = sharedPreferences.getBoolean("use_one_framebuffer", false);

                renderer.setCompatibilitySettings(useSmallerTextures, useNonPowerOfTwoTextures, useNonSquareTextures, useOneFramebuffer);

                renderer.setColors(sharedPreferences.getInt("backgroundColor", 0), sharedPreferences.getInt("linesColor", -1));
                renderer.setBlur(sharedPreferences.getInt("blur", 127));
                renderer.setBrightness(sharedPreferences.getInt("brightness", 127));
                renderer.setLineWidth(sharedPreferences.getInt("linewidth", 127));
                renderer.setRotationSpeed(sharedPreferences.getInt("rotationspeed", 127));
                renderer.setSpeed(sharedPreferences.getInt("speed", 127));
                renderer.setLinesCount(Integer.parseInt(sharedPreferences.getString("lineCount", "3")));

            } catch (final Exception e) {
                Log.e(TAG, "PREF init error: " + e);
            }
        }
    }
}
