package com.zoiper.zdk.android.demokt.video.`in`

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * ZDKVideoSurfaceView
 *
 * @since 19/02/2019
 */
class ZDKVideoSurfaceView : GLSurfaceView, LifecycleObserver {

    private var renderer: LibraryVideoRenderer? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        renderer = LibraryVideoRenderer()

        setEGLContextClientVersion(EGL_CONTEXT_CLIENT_VERSION)
        preserveEGLContextOnPause = true

        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onActivityPause() {
        this.onPause()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onActivityResume() {
        this.onResume()
    }

    /**
     * This method is here just to make using a method reference easier
     *
     * @param bytes The actual info in YUV I420 format
     * @param ignored This would be the length of the array, we don't really need it.
     * @param width of the image
     * @param height of the image
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    fun renderI420YUV(bytes: ByteArray, ignored: Int, width: Int, height: Int) {
        renderI420YUV(bytes, width, height)
    }

    /**
     * Render a frame
     *
     * @param bytes The actual info in YUV I420 format
     * @param width of the image
     * @param height of the image
     */
    fun renderI420YUV(bytes: ByteArray, width: Int, height: Int) {
        renderer?.renderI420YUV(bytes, width, height)
        requestRender()
    }

    companion object {
        private const val EGL_CONTEXT_CLIENT_VERSION = 2
    }
}
