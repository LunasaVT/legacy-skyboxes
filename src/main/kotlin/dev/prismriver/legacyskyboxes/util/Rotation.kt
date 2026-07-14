package dev.prismriver.legacyskyboxes.util

import kotlin.math.cos
import kotlin.math.sin

class Rotation private constructor(private val m: FloatArray) {
    fun rotateX(degrees: Float): Rotation = times(fromX(degrees))
    fun rotateY(degrees: Float): Rotation = times(fromY(degrees))
    fun rotateZ(degrees: Float): Rotation = times(fromZ(degrees))

    fun apply(x: Float, y: Float, z: Float): FloatArray {
        return floatArrayOf(
            m[0] * x + m[1] * y + m[2] * z,
            m[3] * x + m[4] * y + m[5] * z,
            m[6] * x + m[7] * y + m[8] * z,
        )
    }

    private fun times(o: Rotation): Rotation {
        val a = this.m
        val b = o.m
        val r = FloatArray(9)
        for (row in 0..2) {
            for (col in 0..2) {
                var sum = 0.0f
                for (k in 0..2) {
                    sum += a[row * 3 + k] * b[k * 3 + col]
                }
                r[row * 3 + col] = sum
            }
        }
        return Rotation(r)
    }

    companion object {
        fun identity(): Rotation = Rotation(
            floatArrayOf(
                1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f,
            )
        )

        private fun fromX(degrees: Float): Rotation {
            val r = Math.toRadians(degrees.toDouble())
            val c = cos(r).toFloat()
            val s = sin(r).toFloat()
            return Rotation(
                floatArrayOf(
                    1.0f, 0.0f, 0.0f,
                    0.0f, c, -s,
                    0.0f, s, c,
                )
            )
        }

        private fun fromY(degrees: Float): Rotation {
            val r = Math.toRadians(degrees.toDouble())
            val c = cos(r).toFloat()
            val s = sin(r).toFloat()
            return Rotation(
                floatArrayOf(
                    c, 0.0f, s,
                    0.0f, 1.0f, 0.0f,
                    -s, 0.0f, c,
                )
            )
        }

        private fun fromZ(degrees: Float): Rotation {
            val r = Math.toRadians(degrees.toDouble())
            val c = cos(r).toFloat()
            val s = sin(r).toFloat()
            return Rotation(
                floatArrayOf(
                    c, -s, 0.0f,
                    s, c, 0.0f,
                    0.0f, 0.0f, 1.0f,
                )
            )
        }
    }
}
