package dev.prismriver.legacyskyboxes.sky.component

import dev.prismriver.legacyskyboxes.util.SkyTime

class DayLoop(val days: Int, val ranges: List<HeightRange>) {
    val isActiveEveryDay: Boolean
        get() = ranges.isEmpty()

    fun isDayActive(worldDayTime: Long, fadeStartIn: Int): Boolean {
        if (isActiveEveryDay) return true

        var adjusted = worldDayTime - fadeStartIn
        while (adjusted < 0L) {
            adjusted += SkyTime.DAY_LENGTH.toLong() * days
        }

        val daysPassed = (adjusted / SkyTime.DAY_LENGTH).toInt()
        val currentDay = daysPassed % days
        return HeightRange.anyContains(ranges, currentDay.toFloat())
    }

    companion object {
        val EVERY_DAY = DayLoop(8, emptyList())
    }
}
