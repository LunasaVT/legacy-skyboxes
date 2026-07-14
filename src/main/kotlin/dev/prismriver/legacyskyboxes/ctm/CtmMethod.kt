package dev.prismriver.legacyskyboxes.ctm

enum class CtmMethod(val isOverlay: Boolean) {
    CTM(false),
    CTM_COMPACT(false),
    HORIZONTAL(false),
    VERTICAL(false),
    HORIZONTAL_VERTICAL(false),
    VERTICAL_HORIZONTAL(false),
    TOP(false),
    RANDOM(false),
    REPEAT(false),
    FIXED(false),
    OVERLAY(true),
    OVERLAY_CTM(true),
    OVERLAY_RANDOM(true),
    OVERLAY_REPEAT(true),
    OVERLAY_FIXED(true);

    companion object {
        fun fromName(raw: String?): CtmMethod? = when (raw?.trim()?.lowercase()) {
            "ctm" -> CTM
            "ctm_compact" -> CTM_COMPACT
            "horizontal" -> HORIZONTAL
            "vertical" -> VERTICAL
            "horizontal+vertical" -> HORIZONTAL_VERTICAL
            "vertical+horizontal" -> VERTICAL_HORIZONTAL
            "top" -> TOP
            "random" -> RANDOM
            "repeat" -> REPEAT
            "fixed" -> FIXED
            "overlay" -> OVERLAY
            "overlay_ctm" -> OVERLAY_CTM
            "overlay_random" -> OVERLAY_RANDOM
            "overlay_repeat" -> OVERLAY_REPEAT
            "overlay_fixed" -> OVERLAY_FIXED
            else -> null
        }
    }
}

enum class ConnectMode { BLOCK, TILE, STATE }

enum class Symmetry { NONE, OPPOSITE, ALL }

enum class OverlayLayer { CUTOUT, TRANSLUCENT }
