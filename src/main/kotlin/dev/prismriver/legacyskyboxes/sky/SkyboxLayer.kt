package dev.prismriver.legacyskyboxes.sky

import dev.prismriver.legacyskyboxes.sky.component.Biomes
import dev.prismriver.legacyskyboxes.sky.component.Blend
import dev.prismriver.legacyskyboxes.sky.component.DayLoop
import dev.prismriver.legacyskyboxes.sky.component.Fade
import dev.prismriver.legacyskyboxes.sky.component.HeightRange
import dev.prismriver.legacyskyboxes.sky.component.Weather
import dev.prismriver.legacyskyboxes.util.SkyTime
import net.minecraft.entity.Entity
import net.minecraft.resource.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.floor
import kotlin.math.roundToInt

class SkyboxLayer(
    val texture: Identifier,
    val fade: Fade,
    val blend: Blend,
    val weather: Weather,
    val biomes: Biomes,
    val heights: List<HeightRange>,
    val loop: DayLoop,
    val axis: FloatArray,
    val rotate: Boolean,
    val speed: Float,
    val transition: Int,
) {
    var conditionAlpha: Float = UNSET
        private set

    private val hasPositionalConditions: Boolean
        get() = !biomes.isEmpty || heights.isNotEmpty()

    fun resetCondition() {
        conditionAlpha = UNSET
    }

    fun updateCondition(world: World, camera: Entity?, elapsedTicks: Int) {
        conditionAlpha = computeConditionBrightness(world, camera, elapsedTicks)
    }

    private fun computeConditionBrightness(world: World, camera: Entity?, elapsedTicks: Int): Float {
        if (!hasPositionalConditions) {
            return 1.0f
        }
        val inside = conditionSatisfied(world, camera)
        val previous = conditionAlpha
        if (previous == UNSET) {
            return if (inside) 1.0f else 0.0f
        }
        if (transition <= 0) {
            return if (inside) 1.0f else 0.0f
        }
        val step = elapsedTicks.toFloat() / transition
        val result = if (inside) previous + step else previous - step
        return result.coerceIn(0.0f, 1.0f)
    }

    private fun conditionSatisfied(world: World, camera: Entity?): Boolean {
        if (camera == null) return false
        if (!biomes.isEmpty) {
            val biome = world.getBiome(BlockPos(camera))
            if (!biomes.matches(biome?.name)) {
                return false
            }
        }
        if (heights.isEmpty()) return true
        val feetY = floor(camera.y).toFloat()
        return HeightRange.anyContains(heights, feetY)
    }

    fun isScheduled(worldDayTime: Long, clampedTimeOfDay: Int): Boolean {
        if (!fade.alwaysOn && SkyTime.isWithin(clampedTimeOfDay, fade.endFadeOut, fade.startFadeIn)) {
            return false
        }
        return loop.isDayActive(worldDayTime, fade.startFadeIn)
    }

    fun computeAlpha(clampedTimeOfDay: Int, rainStrength: Float, thunderStrength: Float): Float {
        val condition = when {
            !hasPositionalConditions -> 1.0f
            conditionAlpha == UNSET -> 0.0f
            else -> conditionAlpha
        }
        val weatherAlpha = weather.getAlpha(rainStrength, thunderStrength)
        val fadeAlpha = fade.alphaAt(clampedTimeOfDay)
        return (condition * weatherAlpha * fadeAlpha).coerceIn(0.0f, 1.0f)
    }

    fun rotationDegrees(world: World, skyAngle: Float): Float {
        var dayStartOffset = 0.0f
        if (speed != speed.roundToInt().toFloat()) {
            val worldDay = (world.timeOfDay + 18000L) / SkyTime.DAY_LENGTH
            val currentAngle = worldDay.toFloat() * (speed % 1.0f)
            dayStartOffset = currentAngle % 1.0f
        }
        return 360.0f * (dayStartOffset + skyAngle * speed)
    }

    companion object {
        const val MIN_VISIBLE_ALPHA = 1.0e-4f
        private const val UNSET = -1.0f
    }
}
