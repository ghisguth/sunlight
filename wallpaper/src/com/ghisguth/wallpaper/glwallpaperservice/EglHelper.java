/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ghisguth.wallpaper.glwallpaperservice;

import android.view.SurfaceHolder;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

// Original code provided by Robert Green
// http://www.rbgrn.net/content/354-glsurfaceview-adapted-3d-live-wallpapers
class EglHelper {

    EGLConfig mEglConfig;
    private EGL10 mEgl;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglContext;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLWrapper mGLWrapper;

    public EglHelper(
            EGLConfigChooser chooser,
            EGLContextFactory contextFactory,
            EGLWindowSurfaceFactory surfaceFactory,
            GLWrapper wrapper) {
        this.mEGLConfigChooser = chooser;
        this.mEGLContextFactory = contextFactory;
        this.mEGLWindowSurfaceFactory = surfaceFactory;
        this.mGLWrapper = wrapper;
    }

    /*
     * React to the creation of a new surface by creating and returning an OpenGL interface that renders to that
     * surface.
     */
    public GL createSurface(SurfaceHolder holder) {
        /*
         * The window size has changed, so we need to create a new surface.
         */
        if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {

            /*
             * Unbind and destroy the old EGL surface, if there is one.
             */
            mEgl.eglMakeCurrent(
                    mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
        }

        /*
         * Create an EGL surface we can render into.
         */
        mEglSurface =
                mEGLWindowSurfaceFactory.createWindowSurface(mEgl, mEglDisplay, mEglConfig, holder);

        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            throw new RuntimeException("createWindowSurface failed");
        }

        /*
         * Before we can issue GL commands, we need to make sure the context is current and bound to a surface.
         */
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed.");
        }

        GL gl = mEglContext.getGL();
        if (mGLWrapper != null) {
            gl = mGLWrapper.wrap(gl);
        }

        /*
         * if ((mDebugFlags & (DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS))!= 0) { int configFlags = 0; Writer log =
         * null; if ((mDebugFlags & DEBUG_CHECK_GL_ERROR) != 0) { configFlags |= GLDebugHelper.CONFIG_CHECK_GL_ERROR; }
         * if ((mDebugFlags & DEBUG_LOG_GL_CALLS) != 0) { log = new LogWriter(); } gl = GLDebugHelper.wrap(gl,
         * configFlags, log); }
         */
        return gl;
    }

    public void destroySurface() {
        if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
            mEgl.eglMakeCurrent(
                    mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
            mEglSurface = null;
        }
    }

    public void finish() {
        if (mEglContext != null) {
            mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
            mEglContext = null;
        }
        if (mEglDisplay != null) {
            mEgl.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
    }

    /**
     * Initialize EGL for a given configuration spec.
     *
     * @param configSpec
     */
    public void start() {
        // Log.d("EglHelper" + instanceId, "start()");
        if (mEgl == null) {
            // Log.d("EglHelper" + instanceId, "getting new EGL");
            /*
             * Get an EGL instance
             */
            mEgl = (EGL10) EGLContext.getEGL();
        } else {
            // Log.d("EglHelper" + instanceId, "reusing EGL");
        }

        if (mEglDisplay == null) {
            // Log.d("EglHelper" + instanceId, "getting new display");
            /*
             * Get to the default display.
             */
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        } else {
            // Log.d("EglHelper" + instanceId, "reusing display");
        }

        if (mEglConfig == null) {
            // Log.d("EglHelper" + instanceId, "getting new config");
            /*
             * We can now initialize EGL for that display
             */
            int[] version = new int[2];
            mEgl.eglInitialize(mEglDisplay, version);
            mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);
        } else {
            // Log.d("EglHelper" + instanceId, "reusing config");
        }

        if (mEglContext == null) {
            // Log.d("EglHelper" + instanceId, "creating new context");
            /*
             * Create an OpenGL ES context. This must be done only once, an OpenGL context is a somewhat heavy object.
             */
            mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
            if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                throw new RuntimeException("createContext failed");
            }
        } else {
            // Log.d("EglHelper" + instanceId, "reusing context");
        }

        mEglSurface = null;
    }

    /**
     * Display the current render surface.
     *
     * @return false if the context has been lost.
     */
    public boolean swap() {
        mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);

        /*
         * Always check for EGL_CONTEXT_LOST, which means the context and all associated data were lost (For instance
         * because the device went to sleep). We need to sleep until we get a new surface.
         */
        return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
    }
}
