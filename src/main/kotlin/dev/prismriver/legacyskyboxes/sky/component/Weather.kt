package dev.prismriver.legacyskyboxes.sky.component

class Weather(val conditions: Set<Condition>) {
    fun getAlpha(rainStrength: Float, thunderStrength: Float): Float {
        val clearAmount = 1.0f - rainStrength
        val plainRainAmount = rainStrength - thunderStrength

        var alpha = 0.0f
        if (Condition.CLEAR in conditions) alpha += clearAmount
        if (Condition.RAIN in conditions) alpha += plainRainAmount
        if (Condition.THUNDER in conditions) alpha += thunderStrength
        return alpha.coerceIn(0.0f, 1.0f)
    }

    enum class Condition {
        CLEAR, RAIN, THUNDER;

        companion object {
            fun fromName(name: String): Condition? {
                for (condition in entries) {
                    if (condition.name.equals(name.trim(), ignoreCase = true)) {
                        return condition
                    }
                }
                return null
            }
        }
    }

    companion object {
        val CLEAR = Weather(setOf(Condition.CLEAR))

        fun parse(raw: String): Weather {
            val conditions = raw.trim().split(Regex("\\s+"))
                .mapNotNull { token -> if (token.isEmpty()) null else Condition.fromName(token) }
                .toSet()
            return if (conditions.isEmpty()) CLEAR else Weather(conditions)
        }
    }
}
