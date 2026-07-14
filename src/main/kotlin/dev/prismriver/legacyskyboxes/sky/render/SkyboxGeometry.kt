package dev.prismriver.legacyskyboxes.sky.render

import dev.prismriver.legacyskyboxes.util.Rotation

object SkyboxGeometry {
    const val SIZE = 100.0f
    const val VERTEX_COUNT = 24
    const val STRIDE = 5

    private const val T3 = 1.0f / 3.0f
    private const val T23 = 2.0f / 3.0f

    val vertices: FloatArray = build()

    private fun build(): FloatArray {
        val faces = arrayOf(
            // bottom
            Face(Rotation.identity().rotateY(90.0f), 0.0f, 0.0f, T3, 0.5f),
            // top
            Face(Rotation.identity().rotateX(180.0f).rotateY(-90.0f), T3, 0.0f, T23, 0.5f),
            // east
            Face(Rotation.identity().rotateX(90.0f).rotateZ(90.0f), T23, 0.0f, 1.0f, 0.5f),
            // south
            Face(Rotation.identity().rotateX(90.0f).rotateZ(180.0f), 0.0f, 0.5f, T3, 1.0f),
            // west
            Face(Rotation.identity().rotateX(90.0f).rotateZ(-90.0f), T3, 0.5f, T23, 1.0f),
            // north
            Face(Rotation.identity().rotateX(90.0f), T23, 0.5f, 1.0f, 1.0f),
        )

        val out = FloatArray(VERTEX_COUNT * STRIDE)
        val s = SIZE
        var offset = 0
        for (face in faces) {
            offset = emitVertex(out, offset, face, -s, -s, -s, face.minU, face.minV)
            offset = emitVertex(out, offset, face, -s, -s, s, face.minU, face.maxV)
            offset = emitVertex(out, offset, face, s, -s, s, face.maxU, face.maxV)
            offset = emitVertex(out, offset, face, s, -s, -s, face.maxU, face.minV)
        }
        return out
    }

    private fun emitVertex(out: FloatArray, offset: Int, face: Face, x: Float, y: Float, z: Float, u: Float, v: Float): Int {
        val p = face.rotation.apply(x, y, z)
        out[offset] = p[0]
        out[offset + 1] = p[1]
        out[offset + 2] = p[2]
        out[offset + 3] = u
        out[offset + 4] = v
        return offset + STRIDE
    }

    private class Face(
        val rotation: Rotation,
        val minU: Float,
        val minV: Float,
        val maxU: Float,
        val maxV: Float,
    )
}
