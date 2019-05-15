@file:Suppress("ConstantConditionIf")

package com.zoiper.zdk.android.demokt.video.`in`

import android.util.Log

/**
 * Helper class to handle vertex shader parameter calculations, so that a
 * texture can be drawn in a target area preserving the aspect ratio.
 */
internal class VideoShaderHelper(
    sourceWidth: Float,
    sourceHeight: Float,
    targetWidth: Float,
    targetHeight: Float,
    targetCenterX: Float,
    targetCenterY: Float,
    maxSizePercentage: Float,
    mode: FitMode
) {

    private val fragmentStartX: Float
    private val fragmentEndX: Float
    private val fragmentStartY: Float
    private val fragmentEndY: Float
    private val vertexStartX: Float
    private val vertexEndX: Float
    private val vertexStartY: Float
    private val vertexEndY: Float

    /**
     * Gets a shader that is not rotated or mirrored
     */
    val vertexShaderNormal: FloatArray
        get() = floatArrayOf(
            vertexStartX,
            vertexEndY,
            vertexEndX,
            vertexEndY,
            vertexStartX,
            vertexStartY,
            vertexEndX,
            vertexStartY
        )

    val fragmentShaderNormal: FloatArray
        get() = floatArrayOf(
            fragmentStartX,
            fragmentStartY,
            fragmentEndX,
            fragmentStartY,
            fragmentStartX,
            fragmentEndY,
            fragmentEndX,
            fragmentEndY
        )

    internal enum class FitMode {
        FIT_PAD, FIT_STRETCH, FIT_CROP
    }

    init {
        var maxSizePercentageLocal = maxSizePercentage

        if(DEBUG_LOGGING) Log.d(TAG, "calculateDisplayPosition")
        if(DEBUG_LOGGING) Log.d(TAG, "sourceWidth: $sourceWidth, sourceHeight: $sourceHeight")
        if(DEBUG_LOGGING) Log.d(TAG, "targetWidth: $targetWidth, targetHeight: $targetHeight")
        if(DEBUG_LOGGING) Log.d(TAG, "targetPositionX: $targetCenterX, targetPositionY: $targetCenterY")
        if(DEBUG_LOGGING) Log.d(TAG, "maxSizePercentage: $maxSizePercentageLocal")
        if(DEBUG_LOGGING) Log.d(TAG, "mode: $mode")

        maxSizePercentageLocal /= 100.0f

        var vertexWidth = 0f
        var vertexHeight = 0f
        var fragmentWidth = 0f
        var fragmentHeight = 0f

        when (mode) {
            VideoShaderHelper.FitMode.FIT_PAD -> {
                vertexHeight = Math.min(
                    1.0f,
                    sourceHeight / targetHeight * targetWidth / sourceWidth
                ) * maxSizePercentageLocal
                vertexWidth = Math.min(
                    1.0f,
                    sourceWidth / targetWidth * targetHeight / sourceHeight
                ) * maxSizePercentageLocal
                fragmentWidth = 1f
                fragmentHeight = 1f
            }

            VideoShaderHelper.FitMode.FIT_CROP -> {
                vertexHeight = 1 * maxSizePercentageLocal
                vertexWidth = 1 * maxSizePercentageLocal
                fragmentWidth = Math.min(
                    1.0f,
                    targetHeight / (sourceHeight * (targetWidth / sourceWidth))
                )
                fragmentHeight = Math.min(
                    1.0f,
                    targetWidth / (sourceWidth * (targetHeight / sourceHeight))
                )
            }

            VideoShaderHelper.FitMode.FIT_STRETCH -> {
            }
        }

        /* vertex coordinates are in range [-1;1] */
        vertexStartX = targetCenterX - vertexWidth
        vertexEndX = targetCenterX + vertexWidth
        vertexStartY = targetCenterY - vertexHeight
        vertexEndY = targetCenterY + vertexHeight

        /* fragment coordinates are in range [0;1] */
        fragmentStartX = 0.5f - fragmentWidth * 0.5f
        fragmentEndX = 0.5f + fragmentWidth * 0.5f
        fragmentStartY = 0.5f - fragmentHeight * 0.5f
        fragmentEndY = 0.5f + fragmentHeight * 0.5f

        if(DEBUG_LOGGING) Log.d(TAG, "vertexStartX: $vertexStartX, vertexStartY: $vertexStartY")
        if(DEBUG_LOGGING) Log.d(TAG, "vertexStartX: $vertexEndX, vertexEndY: $vertexEndY")
        if(DEBUG_LOGGING) Log.d(TAG, "fragmentStartX: $fragmentStartX, fragmentStartY: $fragmentStartY")
        if(DEBUG_LOGGING) Log.d(TAG, "fragmentEndX: $fragmentEndX, oglEndY: $fragmentEndY")

    }

    companion object {
        private const val TAG = "VideoShaderHelper"
        private const val DEBUG_LOGGING = false
    }
}
