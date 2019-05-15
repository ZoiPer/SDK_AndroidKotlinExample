package com.zoiper.zdk.android.demokt.video.out

import java.util.Arrays

/**
 * MatrixUtil
 *
 * A set of utility methods for byte matrix manipulation
 *
 * @since 7/03/2019
 */
object MatrixUtil {
    fun arrayToMatrix(array: ByteArray, width: Int, height: Int): Array<ByteArray> {
        val matrix = Array(height) { ByteArray(width) }
        for (i in 0 until height) {
            matrix[i] = Arrays.copyOfRange(array, i * width, (i + 1) * width)
        }
        return matrix
    }

    fun matrixToArray(matrix: Array<ByteArray>, width: Int, height: Int): ByteArray {
        val result = ByteArray(matrix.size * width)
        for (row in 0 until height) {
            if (matrix[row].size >= 0)
                System.arraycopy(matrix[row], 0, result, row * width, matrix[row].size)
        }
        return result
    }

    fun rotateMatrixBy90DegreeCounterClockwise(matrix: Array<ByteArray>): Array<ByteArray> {
        val totalRowsOfRotatedMatrix = matrix[0].size //Total columns of Original Matrix
        val totalColsOfRotatedMatrix = matrix.size //Total rows of Original Matrix

        val rotatedMatrix = Array(totalRowsOfRotatedMatrix) { ByteArray(totalColsOfRotatedMatrix) }

        for (i in matrix.indices) {
            for (j in 0 until matrix[0].size) {
                rotatedMatrix[totalRowsOfRotatedMatrix - 1 - j][i] = matrix[i][j]
            }
        }
        return rotatedMatrix
    }

    fun rotateMatrixBy90DegreeClockwise(matrix: Array<ByteArray>): Array<ByteArray> {
        val totalRowsOfRotatedMatrix = matrix[0].size //Total columns of Original Matrix
        val totalColsOfRotatedMatrix = matrix.size //Total rows of Original Matrix

        val rotatedMatrix = Array(totalRowsOfRotatedMatrix) { ByteArray(totalColsOfRotatedMatrix) }

        for (i in matrix.indices) {
            for (j in 0 until matrix[0].size) {
                rotatedMatrix[j][totalColsOfRotatedMatrix - 1 - i] = matrix[i][j]
            }
        }
        return rotatedMatrix
    }

    fun mirror(width: Int, height: Int, input: Array<ByteArray>): Array<ByteArray> {
        val out = Array(height) { ByteArray(width) }
        for (i in 0 until height) {
            for (j in 0 until width) {
                out[i][width - j - 1] = input[i][j]
            }
        }
        return out
    }
}
