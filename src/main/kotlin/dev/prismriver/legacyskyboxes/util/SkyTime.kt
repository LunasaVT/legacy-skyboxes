package dev.prismriver.legacyskyboxes.util

object SkyTime {
    const val DAY_LENGTH = 24000

    fun wrapDayTime(tick: Int): Int {
        val result = tick % DAY_LENGTH
        return if (result < 0) result + DAY_LENGTH else result
    }

    fun isWithin(current: Int, start: Int, end: Int): Boolean {
        if (current !in 0..<DAY_LENGTH) {
            return false
        }
        return if (start <= end) {
            current in start..end
        } else {
            current !in (end + 1)..<start
        }
    }

    fun forwardDistance(start: Int, end: Int): Int {
        return ((end - start) % DAY_LENGTH + DAY_LENGTH) % DAY_LENGTH
    }

    fun clockToTicks(clock: String): Int {
        val parts = clock.trim().split(":")
        if (parts.size == 2) {
            val hours = parts[0].trim().toIntOrNull() ?: -1
            val minutes = parts[1].trim().toIntOrNull() ?: -1
            if (hours in 0..23 && minutes in 0..59) {
                var shifted = hours - 6
                if (shifted < 0) {
                    shifted += 24
                }
                return shifted * 1000 + (minutes / 60.0f * 1000.0f).toInt()
            }
        }
        return -1
    }
}
