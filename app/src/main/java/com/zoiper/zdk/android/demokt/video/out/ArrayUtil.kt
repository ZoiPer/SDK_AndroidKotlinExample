package com.zoiper.zdk.android.demokt.video.out

/**
 * ArrayUtil
 *
 * @since 7/03/2019
 */
object ArrayUtil {
    fun merge(vararg arrays: ByteArray): ByteArray {
        var finalLength = 0
        for (array in arrays) {
            finalLength += array.size
        }

        var dest: ByteArray? = null
        var destPos = 0

        for (array in arrays) {
            if (dest == null) {
                dest = array.copyOf(finalLength)
                destPos = array.size
            } else {
                System.arraycopy(array, 0, dest, destPos, array.size)
                destPos += array.size
            }
        }
        return dest ?: ByteArray(0)
    }
}
