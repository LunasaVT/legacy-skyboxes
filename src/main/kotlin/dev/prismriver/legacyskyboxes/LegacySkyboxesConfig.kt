package dev.prismriver.legacyskyboxes

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

// TODO: switch over to oneconfig
class LegacySkyboxesConfig {
    var enabled: Boolean = true
    var showOverworldForUnknownDimension: Boolean = true
    var debug: Boolean = false

    fun save() {
        try {
            val path = configPath()
            Files.createDirectories(path.parent)
            Files.newBufferedWriter(path).use { writer ->
                GSON.toJson(this, writer)
            }
        } catch (e: IOException) {
            LegacySkyboxes.LOGGER.warn("Could not save config: {}", e.message)
        }
    }

    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()

        private fun configPath(): Path =
            FabricLoader.getInstance().configDir.resolve("legacyskyboxes.json")

        fun load(): LegacySkyboxesConfig {
            val path = configPath()
            if (Files.exists(path)) {
                try {
                    Files.newBufferedReader(path).use { reader ->
                        GSON.fromJson(reader, LegacySkyboxesConfig::class.java)?.let {
                            return it
                        }
                    }
                } catch (e: Exception) {
                    LegacySkyboxes.LOGGER.warn("Could not read config, regenerating defaults: {}", e.message)
                }
            }
            val defaults = LegacySkyboxesConfig()
            defaults.save()
            return defaults
        }
    }
}
