package dev.prismriver.legacyskyboxes.sky.component

class Biomes(private val keys: Set<String>, val exclusion: Boolean) {
    val isEmpty: Boolean
        get() = keys.isEmpty()

    fun matches(rawBiomeName: String?): Boolean {
        if (keys.isEmpty()) return true
        if (rawBiomeName == null) return false
        val present = normalize(rawBiomeName) in keys
        return if (exclusion) !present else present
    }

    companion object {
        val ANY = Biomes(emptySet(), false)

        fun parse(raw: String): Biomes {
            var input = raw.trim()
            val exclusion = input.startsWith("!")
            if (exclusion) {
                input = input.substring(1)
            }

            val keys = input.split(Regex("\\s+"))
                .map { normalize(it) }
                .filter { it.isNotEmpty() }
                .toSet()
            return if (keys.isEmpty()) ANY else Biomes(keys, exclusion)
        }

        private fun normalize(name: String): String {
            val withoutNamespace = name.substringAfterLast(':')
            val builder = StringBuilder(withoutNamespace.length)
            for (c in withoutNamespace.lowercase()) {
                if (c in 'a'..'z' || c in '0'..'9') {
                    builder.append(c)
                }
            }
            return builder.toString()
        }
    }
}
