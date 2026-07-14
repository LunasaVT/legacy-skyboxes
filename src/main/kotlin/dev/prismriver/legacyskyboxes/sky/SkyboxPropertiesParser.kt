package dev.prismriver.legacyskyboxes.sky

import dev.prismriver.legacyskyboxes.sky.component.Biomes
import dev.prismriver.legacyskyboxes.sky.component.Blend
import dev.prismriver.legacyskyboxes.sky.component.DayLoop
import dev.prismriver.legacyskyboxes.sky.component.Fade
import dev.prismriver.legacyskyboxes.sky.component.HeightRange
import dev.prismriver.legacyskyboxes.sky.component.Weather
import dev.prismriver.legacyskyboxes.util.SkyTime
import net.minecraft.resource.Identifier
import java.util.Properties

object SkyboxPropertiesParser {
    fun parse(props: Properties, propertiesId: Identifier): SkyboxLayer {
        val texture = resolveSource(props.getProperty("source"), propertiesId)

        return SkyboxLayer(
            texture = texture,
            fade = parseFade(props),
            blend = Blend.fromName(props.getProperty("blend")),
            weather = props.getProperty("weather")?.let { Weather.parse(it) } ?: Weather.CLEAR,
            biomes = props.getProperty("biomes")?.let { Biomes.parse(it) } ?: Biomes.ANY,
            heights = props.getProperty("heights")?.let { HeightRange.parseList(it, allowNegative = true) } ?: emptyList(),
            loop = parseLoop(props),
            axis = parseAxis(props.getProperty("axis")),
            rotate = props.getProperty("rotate")?.trim()?.toBoolean() ?: true,
            speed = props.getProperty("speed")?.trim()?.toFloatOrNull() ?: 1.0f,
            transition = parseTransition(props.getProperty("transition")),
        )
    }

    fun parseCelestial(props: Properties, propertiesId: Identifier): CelestialOverride {
        val texture = resolveSource(props.getProperty("source"), propertiesId)
        val blend = Blend.fromName(props.getProperty("blend"))
        return CelestialOverride(texture, blend)
    }

    private fun resolveSource(rawSource: String?, propertiesId: Identifier): Identifier {
        val source = rawSource?.trim()
        if (source.isNullOrEmpty()) {
            return Identifier(propertiesId.namespace, propertiesId.path.replace(".properties", ".png"))
        }

        if (source.startsWith("./")) {
            val path = propertiesId.path
            val fileName = path.substringAfterLast('/')
            val siblingPath = path.substring(0, path.length - fileName.length) + source.substring(2)
            return Identifier(propertiesId.namespace, siblingPath)
        }

        if (source.startsWith("assets/")) {
            val parts = source.split("/", limit = 3)
            if (parts.size == 3) {
                return Identifier(parts[1], parts[2])
            }
        }

        return Identifier(source)
    }

    private fun parseFade(props: Properties): Fade {
        val startFadeIn = props.getProperty("startFadeIn")
        val endFadeIn = props.getProperty("endFadeIn")
        val endFadeOut = props.getProperty("endFadeOut")
        if (startFadeIn == null || endFadeIn == null || endFadeOut == null) {
            return Fade.ALWAYS_ON
        }

        val sIn = SkyTime.clockToTicks(startFadeIn)
        val eIn = SkyTime.clockToTicks(endFadeIn)
        val eOut = SkyTime.clockToTicks(endFadeOut)
        if (sIn < 0 || eIn < 0 || eOut < 0) {
            return Fade.ALWAYS_ON
        }

        val sOut: Int
        val explicitStartFadeOut = props.getProperty("startFadeOut")
        if (explicitStartFadeOut != null) {
            val parsed = SkyTime.clockToTicks(explicitStartFadeOut)
            sOut = if (parsed < 0) eOut else parsed
        } else {
            var derived = eOut - (eIn - sIn)
            if (derived in sIn..eIn) {
                derived = eOut
            }
            sOut = derived
        }

        return Fade(sIn, eIn, sOut, eOut, false)
    }

    private fun parseLoop(props: Properties): DayLoop {
        val days = props.getProperty("days") ?: return DayLoop.EVERY_DAY
        val ranges = HeightRange.parseList(days, allowNegative = false)
        val period = props.getProperty("daysLoop")?.trim()?.toIntOrNull()?.coerceAtLeast(1) ?: 8
        return DayLoop(period, ranges)
    }

    private fun parseTransition(raw: String?): Int {
        val value = raw?.trim()?.toIntOrNull() ?: 1
        return value * 20
    }

    private fun parseAxis(raw: String?): FloatArray {
        val default = floatArrayOf(1.0f, 0.0f, 0.0f)
        if (raw == null) return default

        val parts = raw.trim().replace(Regex(" +"), " ").split(" ")
        if (parts.size != 3) return default

        val x = parts[0].toFloatOrNull() ?: return default
        val y = parts[1].toFloatOrNull() ?: return default
        val z = parts[2].toFloatOrNull() ?: return default
        if (x * x + y * y + z * z <= 1.0e-6f) return default

        return floatArrayOf(z, y, -x)
    }
}
