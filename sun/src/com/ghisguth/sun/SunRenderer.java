package com.ghisguth.sun;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import com.ghisguth.gfx.*;
import com.ghisguth.shared.ResourceHelper;
import com.ghisguth.wallpaper.glwallpaperservice.GLWallpaperService;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.InputStream;


public class SunRenderer implements GLWallpaperService.Renderer {

    private static String TAG = "Sunlight";
    private final int sunHorizontalResolution = 64;
    private final int sunVerticalResolution = 32;
    private int frameBufferWidth = 256;
    private int frameBufferHeight = 256;
    private int surfaceWidth = 256;
    private int surfaceHeight = 256;
    private Context context;
    private Program sunProgram;
    private Program coronaProgram;
    private Program postRayProgram;
    private Texture baseTexture;
    private Texture noiseTexture;
    private Texture colorTexture;
    private VertexBuffer sphereVertices;
    private VertexBuffer quadVertices;
    private RenderTexture renderTexture;
    private FrameBuffer frameBuffer;
    private boolean resetFrameBuffers = false;
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
    private float preferenceSize = 1.0f;
    private float size = 1.1f;
    private float preferenceCoronaSize = 1.0f;
    private float coronaSize = 0.05f;
    private float coronaOpacity = 0.8f;
    private float preferenceCoronaOpacity = 1.0f;
    private float coronaTurbulence = 16.0f;
    private float preferenceCoronaTurbulence = 1.0f;
    private float coronaHeight = 0.06f;
    private float preferenceCoronaHeight = 1.0f;
    private float coronaSpeed = 1.0f;
    private float preferenceCoronaSpeed = 1.0f;
    private float colorAdd = 0.0f;
    private float preferenceColorAdd = 0.0f;
    private float colorMul = 2.0f;
    private float preferenceColorMul = 1.0f;
    private float rotationSpeed = 0.5f;
    private float preferenceRotationSpeed = 1.0f;
    private float animationSpeed = 0.8f;
    private float preferenceAnimationSpeed = 1.0f;
    private float rayDensity = 0.1f;
    private float preferenceRayDensity = 1.0f;
    private float rayDecay = 0.95f;
    private float preferenceRayDecay = 1.0f;
    private float rayWeight = 0.11f;
    private float preferenceRayWeight = 1.0f;
    private float rayExposure = 0.58f;
    private float preferenceRayExposure = 1.0f;
    private int rayQuality = 0;
    private float preferenceTemperature = 1.0f;
    private boolean preferenceAnimateTemperature = false;

    public SunRenderer(Context context) {
        this.context = context;
        sphereVertices = GeometryHelper.createSphere(sunHorizontalResolution, sunVerticalResolution);
        quadVertices = GeometryHelper.createScreenQuad();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (postEffectsEnabled) {
            frameBuffer.bind();
            GLES20.glViewport(0, 0, frameBufferWidth, frameBufferHeight);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            renderSun();

            frameBuffer.unbind();
            GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            renderPostEffect();
        } else {
            renderSun();
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

        renderTexture.update(frameBufferWidth, frameBufferHeight);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        ShaderManager.getSingletonObject().unloadAll();
        ShaderManager.getSingletonObject().cleanUp();

        TextureManager.getSingletonObject().unloadAll();
        TextureManager.getSingletonObject().cleanUp();

        loadResources();

        if (sunProgram != null) {
            sunProgram.load();
        }

        if (coronaProgram != null) {
            coronaProgram.load();
        }

        if (postRayProgram != null) {
            postRayProgram.load();
        }

        if (baseTexture != null) {
            baseTexture.load();
        }

        if (colorTexture != null) {
            colorTexture.load();
        }

        if (noiseTexture != null) {
            noiseTexture.load();
        }

        ShaderManager.getSingletonObject().unloadAllShaders();

        setupFrameBuffer(unused);

        Matrix.setLookAtM(V_matrix, 0, 0, 0, 2.0f, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
        Matrix.orthoM(Q_matrix, 0, 0, 1, 0, 1, -1, 1);
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

    private void loadResources() {
        loadShaders();
        loadPostEffectShaders();
        loadTextures();
    }

    private void loadTextures() {
        try {
            TextureManager textureManager = TextureManager.getSingletonObject();
            if (baseTexture == null) {
                baseTexture = textureManager.createTexture(context.getResources(), R.raw.sun_surface_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
            if (noiseTexture == null) {
                noiseTexture = textureManager.createTexture(context.getResources(), R.raw.noise_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT, GLES20.GL_REPEAT);
            }
            if (colorTexture == null) {
                colorTexture = textureManager.createTexture(context.getResources(), R.raw.star_color_etc1, true, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load textures from resources " + ex.toString());
        }
    }

    private void loadShaders() {
        if (sunProgram != null && coronaProgram != null) {
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
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load shaders from resources " + ex.toString());
        }
    }

    protected InputStream openResource(int id) {
        return context.getResources().openRawResource(id);
    }

    private void loadPostEffectShaders() {
        if (postRayProgram != null) {
            return;
        }

        try {
            ShaderManager shaderManager = ShaderManager.getSingletonObject();

            String postRayFragmentDefines = "";
            if (rayQuality == 0) {
                postRayFragmentDefines = "#define LOW_QUALITY\n";
            } else if (rayQuality == 2) {
                postRayFragmentDefines = "#define HIGH_QUALITY\n";
            }
            String postRayFragment = ResourceHelper.loadRawString(openResource(R.raw.sun_ray_fragment));

            Shader vertex = shaderManager.createVertexShader(ResourceHelper.loadRawString(openResource(R.raw.sun_ray_vertex)));
            Shader fragment = shaderManager.createFragmentShader(postRayFragmentDefines + postRayFragment);

            Log.e(TAG, "SHADERS: " + postRayFragmentDefines);

            postRayProgram = shaderManager.createShaderProgram(vertex, fragment);
        } catch (Exception ex) {
            Log.e(TAG, "Unable to load effect shaders from resources " + ex.toString());
        }
    }

    private void renderPostEffect() {
        if (postRayProgram != null && postRayProgram.use()) {
            renderTexture.bind(GLES20.GL_TEXTURE0, postRayProgram, "sTexture");
            quadVertices.bind(postRayProgram, "aPosition", "aTextureCoord");

            GLES20.glUniform1f(postRayProgram.getUniformLocation("uDecay"), rayDecay * preferenceRayDecay);
            GLES20.glUniform1f(postRayProgram.getUniformLocation("uWeight"), rayWeight * preferenceRayWeight);
            GLES20.glUniform1f(postRayProgram.getUniformLocation("uDensity"), rayDensity * preferenceRayDensity);
            GLES20.glUniform1f(postRayProgram.getUniformLocation("uExposure"), rayExposure * preferenceRayExposure);

            GLES20.glUniformMatrix4fv(postRayProgram.getUniformLocation("uMVPMatrix"), 1, false, Q_matrix, 0);

            quadVertices.draw(GLES20.GL_TRIANGLE_STRIP);
        }
    }

    private void renderSun() {
        if (sunProgram != null && baseTexture != null && noiseTexture != null && colorTexture != null) {
            if (!sunProgram.use() || !baseTexture.load() || !noiseTexture.load() || !colorTexture.load()) {
                return;
            }

            float finalRotationSpeed = rotationSpeed * preferenceRotationSpeed;
            float finalAnimationSpeed = animationSpeed * preferenceAnimationSpeed;

            float angle = getTimeDeltaByScale((long) (600000.0 / finalRotationSpeed));
            float scale = size * preferenceSize;
            Matrix.setRotateM(M_matrix, 0, 90, 1, 0, 0);
            Matrix.rotateM(M_matrix, 0, 360 * angle, 0, 0, 1);
            Matrix.scaleM(M_matrix, 0, scale, scale, scale);

            Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
            Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

            baseTexture.bind(GLES20.GL_TEXTURE0, sunProgram, "sBaseTexture");
            noiseTexture.bind(GLES20.GL_TEXTURE1, sunProgram, "sNoiseTexture");
            colorTexture.bind(GLES20.GL_TEXTURE2, sunProgram, "sColorTexture");

            sphereVertices.bind(sunProgram, "aPosition", "aTextureCoord");

            GLES20.glUniformMatrix4fv(sunProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);

            float animationTime = getTimeDeltaByScale((long) (790000.0 / finalAnimationSpeed));
            float animationTime2 = getTimeDeltaByScale((long) (669000.0 / finalAnimationSpeed));
            float animationTime3 = getTimeDeltaByScale((long) (637000.0 / finalAnimationSpeed));

            GLES20.glUniform1f(sunProgram.getUniformLocation("uTime"), animationTime);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uTime2"), animationTime2);
            GLES20.glUniform1f(sunProgram.getUniformLocation("uTime3"), animationTime3);

            float surfaceColorOffset = preferenceTemperature;
            float surfaceColorAdd = colorAdd + preferenceColorAdd;
            float surfaceColorMul = colorMul * preferenceColorMul;

            if(preferenceAnimateTemperature) {
                surfaceColorOffset = (float) (Math.cos(getTimeDeltaByScale(190000L) * Math.PI * 2.0) * 0.5 + 0.5);
            }

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
                float coronaScale = scale + coronaSize * preferenceCoronaSize;
                Matrix.scaleM(M_matrix, 0, coronaScale, coronaScale, coronaScale);
                Matrix.multiplyMM(MVP_matrix, 0, V_matrix, 0, M_matrix, 0);
                Matrix.multiplyMM(MVP_matrix, 0, P_matrix, 0, MVP_matrix, 0);

                baseTexture.bind(GLES20.GL_TEXTURE0, coronaProgram, "sBaseTexture");
                noiseTexture.bind(GLES20.GL_TEXTURE1, coronaProgram, "sNoiseTexture");
                colorTexture.bind(GLES20.GL_TEXTURE2, coronaProgram, "sColorTexture");

                sphereVertices.bind(coronaProgram, "aPosition", "aTextureCoord");
                GLES20.glUniformMatrix4fv(coronaProgram.getUniformLocation("uMVPMatrix"), 1, false, MVP_matrix, 0);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime"), animationTime2);
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime2"), animationTime3);

                float speed = coronaSpeed * preferenceCoronaSpeed;
                float animationTime4 = getTimeDeltaByScale((long) (6370000.0 / speed));
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTime3"), animationTime4);

                float alpha = coronaOpacity * preferenceCoronaOpacity;
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uAlpha"), alpha);

                float height = coronaHeight * preferenceCoronaHeight;
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uHeight"), height);

                float turbulence = coronaTurbulence * preferenceCoronaTurbulence;
                GLES20.glUniform1f(coronaProgram.getUniformLocation("uTurbulence"), turbulence);

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

    private class SettingsUpdater implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        private SunRenderer renderer;

        public SettingsUpdater(SunRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            try {
                boolean useSmallerTextures = sharedPreferences.getBoolean("use_smaller_textures", false);
                boolean useNonPowerOfTwoTextures = sharedPreferences.getBoolean("use_non_power_of_two_textures", false);
                boolean useNonSquareTextures = sharedPreferences.getBoolean("use_non_square_textures", false);

                renderer.setCompatibilitySettings(useSmallerTextures,
                        useNonPowerOfTwoTextures, useNonSquareTextures);

                renderer.setSize(sharedPreferences.getInt("size", 200));

                renderer.setCoronaSize(sharedPreferences.getInt("coronaSize", 127));

                renderer.setCoronaOpacity(sharedPreferences.getInt("coronaOpacity", 127));

                renderer.setCoronaTurbulence(sharedPreferences.getInt("coronaTurbulence", 127));
                renderer.setCoronaHeight(sharedPreferences.getInt("coronaHeight", 127));
                renderer.setCoronaSpeed(sharedPreferences.getInt("coronaSpeed", 127));

                renderer.setColorAdd(sharedPreferences.getInt("baseBrightness", 16));
                renderer.setColorMul(sharedPreferences.getInt("maxBrightness", 127));

                renderer.setRotationSpeed(sharedPreferences.getInt("rotationSpeed", 127));
                renderer.setAnimationSpeed(sharedPreferences.getInt("animationSpeed", 127));

                renderer.setPostEffectsEnabled(sharedPreferences.getBoolean("postEnabled", true));
                renderer.setRayDensity(sharedPreferences.getInt("rayDensity", 127));
                renderer.setRayDecay(sharedPreferences.getInt("rayDecay", 127));
                renderer.setRayWeight(sharedPreferences.getInt("rayWeight", 127));
                renderer.setRayExposure(sharedPreferences.getInt("rayExposure", 127));
                renderer.setRayQuality(Integer.parseInt(sharedPreferences.getString("rayQuality", "0")));

                renderer.setTemperature(sharedPreferences.getInt("temperature", 7));
                renderer.setTemperatureAnimation(sharedPreferences.getBoolean("animateTemperature", false));

            } catch (final Exception e) {
                Log.e(TAG, "PREF init error: " + e);
            }
        }
    }

    private void setTemperatureAnimation(boolean animateTemperature) {
        preferenceAnimateTemperature = animateTemperature;
    }

    private void setTemperature(int temperature) {
        preferenceTemperature = getLinearFactor(temperature, 1.0f) * 0.5f + 0.5f;
    }

    private void setRayExposure(int value) {
        preferenceRayExposure = getScaledFactor(value, 0.3f);
    }

    private void setRayQuality(int value) {
        rayQuality = value;

        postRayProgram = null;
        loadPostEffectShaders();

        if (rayQuality == 0) {
            rayDecay = 0.95f;
            rayWeight = 0.11f;
            rayExposure = 0.58f;
        } else if (rayQuality == 2) {
            // High quality
            rayDecay = 0.9875f;
            rayWeight = 0.0275f;
            rayExposure = 0.58f;
        } else {
            // Medium quality
            rayDecay = 0.975f;
            rayWeight = 0.055f;
            rayExposure = 0.58f;
        }
    }

    private void setRayWeight(int value) {
        preferenceRayWeight = getScaledFactor(value, 0.3f);
    }

    private void setRayDecay(int value) {
        preferenceRayDecay = getScaledFactor(value, 0.1f);
    }

    private void setRayDensity(int value) {
        preferenceRayDensity = getScaledFactor(value, 1.0f);

    }

    private void setPostEffectsEnabled(boolean value) {
        postEffectsEnabled = value;
    }


    public void setCompatibilitySettings(boolean useSmallerTextures,
                                         boolean useNonPowerOfTwoTextures,
                                         boolean useNonSquareTextures) {
        useSmallerTextures_ = useSmallerTextures;
        useNonPowerOfTwoTextures_ = useNonPowerOfTwoTextures;
        useNonSquareTextures_ = useNonSquareTextures;
    }

    public void setSize(int value) {
        preferenceSize = getScaledFactor(value, 0.3f);
    }

    public void setColorAdd(int value) {
        preferenceColorAdd = getLinearFactor(value, 0.5f) + 0.5f;
    }

    public void setColorMul(int value) {
        preferenceColorMul = getScaledFactor(value, 1.0f);
    }

    public void setRotationSpeed(int value) {
        preferenceRotationSpeed = getScaledFactor(value, 1.0f);
    }

    public void setAnimationSpeed(int value) {
        preferenceAnimationSpeed = getScaledFactor(value, 1.0f);
    }

    public void setCoronaSize(int value) {
        preferenceCoronaSize = getLinearFactor(value, 1.0f);
    }

    public void setCoronaOpacity(int value) {
        preferenceCoronaOpacity = getScaledFactor(value, 1.0f);
    }

    public void setCoronaTurbulence(int value) {
        preferenceCoronaTurbulence = getScaledFactor(value, 1.0f);
    }

    public void setCoronaHeight(int value) {
        preferenceCoronaHeight = getScaledFactor(value, 1.0f);
    }

    public void setCoronaSpeed(int value) {
        preferenceCoronaSpeed = getScaledFactor(value, 1.0f);
    }

    private float getScaledFactor(int value, float multiplier) {
        float scale = 1.0f / 127.0f;
        float scaledValue = scale * (value - 127) * multiplier;
        float result = (float) Math.exp(scaledValue);
        return result;
    }

    private float getLinearFactor(int value, float multiplier) {
        float scale = 1.0f / 127.0f;
        return scale * (value - 127) * multiplier;
    }
}
