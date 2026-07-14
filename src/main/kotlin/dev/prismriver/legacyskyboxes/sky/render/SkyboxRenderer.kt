package dev.prismriver.legacyskyboxes.sky.render

import dev.prismriver.legacyskyboxes.sky.SkyboxLayer
import dev.prismriver.legacyskyboxes.sky.Skybox
import dev.prismriver.legacyskyboxes.util.SkyTime
import net.minecraft.client.Minecraft
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.client.render.vertex.DefaultVertexFormat
import net.minecraft.client.render.vertex.Tesselator
import net.minecraft.world.World
import org.lwjgl.opengl.GL11

object SkyboxRenderer {
    private val scratch = FloatArray(4)

    fun render(mc: Minecraft, world: World, skyboxes: List<Skybox>, partialTick: Float) {
        if (skyboxes.isEmpty()) {
            return
        }

        val skyAngle = world.getTimeOfDay(partialTick)
        val worldDayTime = world.timeOfDay
        val clampedTimeOfDay = (((worldDayTime % SkyTime.DAY_LENGTH) + SkyTime.DAY_LENGTH) % SkyTime.DAY_LENGTH).toInt()

        val rain = world.getRain(partialTick).coerceIn(0.0f, 1.0f)
        var thunder = world.getThunder(partialTick).coerceIn(0.0f, 1.0f)
        if (rain > 0.0f) {
            thunder = (thunder / rain).coerceIn(0.0f, 1.0f)
        }

        prepareState()
        GlStateManager.pushMatrix()
        GlStateManager.rotatef(-90.0f, 0.0f, 1.0f, 0.0f)

        for (skybox in skyboxes) {
            for (layer in skybox.layers) {
                if (!layer.isScheduled(worldDayTime, clampedTimeOfDay)) {
                    continue
                }
                val alpha = layer.computeAlpha(clampedTimeOfDay, rain, thunder)
                if (alpha < SkyboxLayer.MIN_VISIBLE_ALPHA) {
                    continue
                }
                drawLayer(mc, world, layer, alpha, skyAngle)
            }
        }

        GlStateManager.popMatrix()
        restoreState()
    }

    private fun drawLayer(mc: Minecraft, world: World, layer: SkyboxLayer, alpha: Float, skyAngle: Float) {
        GlStateManager.pushMatrix()

        if (layer.rotate) {
            val degrees = layer.rotationDegrees(world, skyAngle)
            GlStateManager.rotatef(degrees, layer.axis[0], layer.axis[1], layer.axis[2])
        }

        GlStateManager.blendFunc(layer.blend.srcFactor, layer.blend.dstFactor)
        layer.blend.writeColor(alpha, scratch)
        GlStateManager.color4f(scratch[0], scratch[1], scratch[2], scratch[3])

        mc.textureManager.bind(layer.texture)

        val tesselator = Tesselator.getInstance()
        val buffer = tesselator.buffer
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX)

        val v = SkyboxGeometry.vertices
        var i = 0
        while (i < v.size) {
            buffer.vertex(v[i].toDouble(), v[i + 1].toDouble(), v[i + 2].toDouble())
                .texture(v[i + 3].toDouble(), v[i + 4].toDouble())
            buffer.nextVertex()
            i += SkyboxGeometry.STRIDE
        }
        tesselator.end()

        GlStateManager.popMatrix()
    }

    private fun prepareState() {
        GlStateManager.enableTexture()
        GlStateManager.enableBlend()
        GlStateManager.disableAlphaTest()
        GlStateManager.disableCull()
        GlStateManager.disableFog()
        GlStateManager.depthMask(false)
    }

    private fun restoreState() {
        GlStateManager.depthMask(true)
        GlStateManager.enableAlphaTest()
        GlStateManager.disableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableCull()
        GlStateManager.enableFog()
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
    }
}
