@file:Suppress("ConstantConditionIf")

package com.zoiper.zdk.android.demokt.video.out

import android.media.Image
import android.os.Build
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.util.Log

import java.util.Arrays

/**
 * I420Helper
 *
 *
 * A set of various utility methods and algorithms to manipulate YUV420 (i420) byte arrays
 *
 * @since 27/02/2019
 */
class I420Helper(sensorOrientation: Int,
                 deviceOrientation: Int, // Post-capture editing stuff
                 private val captureDimensions: ImageDimensions
) {

    val postRotateDimensions: I420Helper.ImageDimensions

    private val timesToRotate: Int

    init {
        this.timesToRotate = timesToRotateFrame(sensorOrientation, deviceOrientation)
        this.postRotateDimensions = rotateDimentions(captureDimensions, timesToRotate)
    }

    fun straightenFrame(i420: ByteArray): ByteArray {
        val started = SystemClock.elapsedRealtime()

        // The amount of Y values we expect to have in the input array
        val yCount = captureDimensions.width * captureDimensions.height

        // The amount of U and amount of V values we expect in the input array
        val uAndVCount = yCount / 4

        // Split up the common i420-formatted byte array into the different planes
        var yBuffer = Arrays.copyOfRange(i420, 0, yCount)
        var uBuffer = Arrays.copyOfRange(i420, yCount, yCount + uAndVCount)
        var vBuffer = Arrays.copyOfRange(i420, yCount + uAndVCount, yCount + uAndVCount * 2)

        // Convert all the plane buffers to matrices since they are much easier to work with and
        // you can find almost any kind of matrix manipulation algorithm with a simple google search
        var yMatrix = MatrixUtil.arrayToMatrix(yBuffer, captureDimensions.width, captureDimensions.height)
        var uMatrix = MatrixUtil.arrayToMatrix(uBuffer, captureDimensions.width / 2, captureDimensions.height / 2)
        var vMatrix = MatrixUtil.arrayToMatrix(vBuffer, captureDimensions.width / 2, captureDimensions.height / 2)

        // Rotate the matrices a number of times to get them upright
        when (timesToRotate) {
            1 -> {
                yMatrix = MatrixUtil.rotateMatrixBy90DegreeClockwise(yMatrix)
                uMatrix = MatrixUtil.rotateMatrixBy90DegreeClockwise(uMatrix)
                vMatrix = MatrixUtil.rotateMatrixBy90DegreeClockwise(vMatrix)
            }
            3 -> {
                // Since 3 clockwise rotations is the same as 1 counter clockwise
                yMatrix = MatrixUtil.rotateMatrixBy90DegreeCounterClockwise(yMatrix)
                uMatrix = MatrixUtil.rotateMatrixBy90DegreeCounterClockwise(uMatrix)
                vMatrix = MatrixUtil.rotateMatrixBy90DegreeCounterClockwise(vMatrix)
            }
            2 -> // If we need to rotate 2 times, we genuinely don't care in which direction we rotate
                for (i in 0..1) {
                    yMatrix = MatrixUtil.rotateMatrixBy90DegreeClockwise(yMatrix)
                    uMatrix = MatrixUtil.rotateMatrixBy90DegreeClockwise(uMatrix)
                    vMatrix = MatrixUtil.rotateMatrixBy90DegreeClockwise(vMatrix)
                }
        }

        // Mirror the upright image in order to match preview
        // NOTE: I would accept the argument that this step might be redundant because the only
        // thing it fixes is the fact that you go to the bottom of the screen (as you would expect)
        // when rotating the device, rather than going upside down on the top

        // Convert the matrices back to an array after we've done all necessary manipulations

        // Set the result by reference, we do this in order to not have to declare another
        // variable here, since this code is ran tens of times per second so any
        // optimization is welcome

        // Mirror the upright image in order to match preview
        // NOTE: I would accept the argument that this step might be redundant because the only
        // thing it fixes is the fact that you go to the bottom of the screen (as you would expect)
        // when rotating the device, rather than going upside down on the top
        yMatrix = MatrixUtil.mirror(postRotateDimensions.width, postRotateDimensions.height, yMatrix)
        uMatrix = MatrixUtil.mirror(postRotateDimensions.width / 2, postRotateDimensions.height / 2, uMatrix)
        vMatrix = MatrixUtil.mirror(postRotateDimensions.width / 2, postRotateDimensions.height / 2, vMatrix)

        // Convert the matrices back to an array after we've done all necessary manipulations
        yBuffer = MatrixUtil.matrixToArray(yMatrix, postRotateDimensions.width, postRotateDimensions.height)
        uBuffer = MatrixUtil.matrixToArray(uMatrix, postRotateDimensions.width / 2, postRotateDimensions.height / 2)
        vBuffer = MatrixUtil.matrixToArray(vMatrix, postRotateDimensions.width / 2, postRotateDimensions.height / 2)

        // Set the result by reference, we do this in order to not have to declare another
        // variable here, since this code is ran tens of times per second so any
        // optimization is welcome
        if (DETAILED_LOG) Log.d(TAG, "straightenFrame: " + (SystemClock.elapsedRealtime() - started))

        return ArrayUtil.merge(yBuffer, uBuffer, vBuffer)
    }

    @Suppress("unused")
    data class ImageDimensions(val width: Int, val height: Int) {
        fun i420YUVArraySize(): Int {
            return this.width * this.height + this.width * this.height / 2
        }
    }

    companion object {
        private const val TAG = "I420Helper"
        private const val DETAILED_LOG = false

        private fun timesToRotateFrame(sensorOrientation: Int, deviceOrientation: Int): Int {
            return (sensorOrientation + deviceOrientation) / 90 % 4
        }

        private fun rotateDimentions(dimentions: ImageDimensions, timesToRotate: Int): ImageDimensions {
            return ImageDimensions(
                if (timesToRotate % 2 == 1) dimentions.height else dimentions.width,
                if (timesToRotate % 2 == 1) dimentions.width else dimentions.height
            )
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun imageToI420ByteArray(image: Image): ByteArray {
            val started = SystemClock.elapsedRealtime()

            val data = ByteArray((image.width.toDouble() * image.height.toDouble() * 1.5).toInt())

            val imageWidth = image.width
            val imageHeight = image.height
            val planes = image.planes
            var offset = 0
            for (plane in planes.indices) {
                val buffer = planes[plane].buffer
                val rowStride = planes[plane].rowStride
                // Experimentally, U and V planes have |pixelStride| = 2, which
                // essentially means they are packed. That's silly, because we are
                // forced to unpack here.
                val pixelStride = planes[plane].pixelStride
                val planeWidth = if (plane == 0) imageWidth else imageWidth / 2
                val planeHeight = if (plane == 0) imageHeight else imageHeight / 2
                if (pixelStride == 1 && rowStride == planeWidth) {
                    // Copy whole plane from buffer into |data| at once.
                    buffer.get(data, offset, planeWidth * planeHeight)
                    offset += planeWidth * planeHeight
                } else {
                    // Copy pixels one by one respecting pixelStride and rowStride.
                    val rowData = ByteArray(rowStride)
                    for (row in 0 until planeHeight - 1) {
                        buffer.get(rowData, 0, rowStride)
                        for (col in 0 until planeWidth) {
                            data[offset++] = rowData[col * pixelStride]
                        }
                    }
                    // Last row is special in some devices and may not contain the full
                    // |rowStride| bytes of data. See  http://crbug.com/458701  and
                    // http://developer.android.com/reference/android/media/Image.Plane.html#getBuffer()
                    buffer.get(rowData, 0, Math.min(rowStride, buffer.remaining()))
                    for (col in 0 until planeWidth) {
                        data[offset++] = rowData[col * pixelStride]
                    }
                }
            }

            if (DETAILED_LOG) Log.d(TAG, "imageToI420ByteArray: " + (SystemClock.elapsedRealtime() - started))
            return data
        }
    }
}
