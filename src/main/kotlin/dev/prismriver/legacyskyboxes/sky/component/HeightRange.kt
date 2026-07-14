package dev.prismriver.legacyskyboxes.sky.component

class HeightRange(val min: Float, val max: Float) {
    operator fun contains(value: Float): Boolean = value in min..max

    companion object {
        fun anyContains(ranges: List<HeightRange>, value: Float): Boolean {
            if (ranges.isEmpty()) return true
            return ranges.any { value in it }
        }

        fun parseList(raw: String, allowNegative: Boolean): List<HeightRange> {
            val result = ArrayList<HeightRange>()
            for (token in raw.trim().split(Regex("\\s*,\\s*|\\s+"))) {
                val range = parseToken(token, allowNegative) ?: continue
                result.add(range)
            }
            return result
        }

        private fun parseToken(token: String, allowNegative: Boolean): HeightRange? {
            val trimmed = token.trim()
            if (trimmed.isEmpty()) return null

            val split = trimmed.indexOf('-', 1)
            if (split > 0) {
                val min = trimmed.substring(0, split).trim().toIntOrNull()
                val max = trimmed.substring(split + 1).trim().toIntOrNull()
                if (min != null && max != null && accept(min, allowNegative) && accept(max, allowNegative)) {
                    return if (min <= max) HeightRange(min.toFloat(), max.toFloat())
                    else HeightRange(max.toFloat(), min.toFloat())
                }
                return null
            }

            val cleaned = if (allowNegative && trimmed.startsWith("(") && trimmed.endsWith(")")) {
                trimmed.substring(1, trimmed.length - 1)
            } else {
                trimmed
            }
            val value = cleaned.toIntOrNull() ?: return null
            if (!accept(value, allowNegative)) return null
            return HeightRange(value.toFloat(), value.toFloat())
        }

        private fun accept(value: Int, allowNegative: Boolean): Boolean = allowNegative || value >= 0
    }
}
