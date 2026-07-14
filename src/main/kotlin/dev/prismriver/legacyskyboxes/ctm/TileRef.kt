package dev.prismriver.legacyskyboxes.ctm

import net.minecraft.resource.Identifier

sealed class TileRef {
    data class Named(val id: Identifier) : TileRef()
    object Skip : TileRef()
    object Default : TileRef()

    companion object {
        fun parseList(raw: String, propertiesId: Identifier): List<TileRef> {
            val result = ArrayList<TileRef>()
            for (token in raw.trim().split(Regex("\\s+"))) {
                if (token.isEmpty()) continue
                result += parseToken(token, propertiesId)
            }
            return result
        }

        private fun parseToken(token: String, propertiesId: Identifier): List<TileRef> {
            when (token) {
                "<skip>" -> return listOf(Skip)
                "<default>" -> return listOf(Default)
            }

            val rangeMatch = RANGE_REGEX.matchEntire(token)
            if (rangeMatch != null) {
                val start = rangeMatch.groupValues[1].toInt()
                val end = rangeMatch.groupValues[2].toInt()
                val range = if (start <= end) start..end else end downTo start
                return range.map { Named(resolve(it.toString(), propertiesId)) }
            }

            return listOf(Named(resolve(token, propertiesId)))
        }

        private fun resolve(name: String, propertiesId: Identifier): Identifier {
            if (name.contains(':')) {
                return Identifier(name.removeSuffix(".png"))
            }
            if (name.contains('/')) {
                return Identifier(propertiesId.namespace, name.removeSuffix(".png"))
            }

            val fileName = propertiesId.path.substringAfterLast('/')
            val dir = propertiesId.path.substring(0, propertiesId.path.length - fileName.length)
            return Identifier(propertiesId.namespace, dir + name.removeSuffix(".png"))
        }

        private val RANGE_REGEX = Regex("^(\\d+)-(\\d+)$")
    }
}
