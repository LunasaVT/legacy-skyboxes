package dev.prismriver.legacyskyboxes.sky.component

import dev.prismriver.legacyskyboxes.util.SkyTime

class Fade(
    startFadeIn: Int,
    endFadeIn: Int,
    startFadeOut: Int,
    endFadeOut: Int,
    val alwaysOn: Boolean,
) {
    val startFadeIn: Int = if (alwaysOn) startFadeIn else SkyTime.wrapDayTime(startFadeIn)
    val endFadeIn: Int = if (alwaysOn) endFadeIn else SkyTime.wrapDayTime(endFadeIn)
    val startFadeOut: Int = if (alwaysOn) startFadeOut else SkyTime.wrapDayTime(startFadeOut)
    val endFadeOut: Int = if (alwaysOn) endFadeOut else SkyTime.wrapDayTime(endFadeOut)

    fun alphaAt(dayTimeTick: Int): Float {
        if (alwaysOn || SkyTime.isWithin(dayTimeTick, endFadeIn, startFadeOut)) {
            return 1.0f
        }

        if (SkyTime.isWithin(dayTimeTick, startFadeIn, endFadeIn)) {
            val window = SkyTime.forwardDistance(startFadeIn, endFadeIn)
            if (window == 0) return 1.0f
            return SkyTime.forwardDistance(startFadeIn, dayTimeTick).toFloat() / window
        }

        if (SkyTime.isWithin(dayTimeTick, startFadeOut, endFadeOut)) {
            val window = SkyTime.forwardDistance(startFadeOut, endFadeOut)
            if (window == 0) return 0.0f
            return 1.0f - SkyTime.forwardDistance(startFadeOut, dayTimeTick).toFloat() / window
        }

        return 0.0f
    }

    companion object {
        val ALWAYS_ON = Fade(0, 0, 0, 0, true)
    }
}
