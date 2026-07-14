package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.LegacySkyboxes
import dev.prismriver.legacyskyboxes.mixin.MixinBuiltInResourcePack_CtmAccessor
import dev.prismriver.legacyskyboxes.mixin.MixinCustomResourcePack_CtmAccessor
import net.minecraft.client.resource.pack.BuiltInResourcePack
import net.minecraft.client.resource.pack.DirectoryResourcePack
import net.minecraft.client.resource.pack.ResourcePack
import net.minecraft.client.resource.pack.ZippedResourcePack
import net.minecraft.resource.Identifier
import java.io.File
import java.util.zip.ZipFile

object CtmDiscovery {
    private val PREFIXES = listOf("optifine/ctm/", "mcpatcher/ctm/")
    private var activePacks: List<ResourcePack> = emptyList()
    private val warnedPackTypes = HashSet<Class<*>>()

    fun setActivePacks(packs: List<ResourcePack>) {
        activePacks = ArrayList(packs)
    }

    fun discoverPropertiesIds(): List<Identifier> {
        val seen = LinkedHashSet<Identifier>()
        for (pack in activePacks) {
            try {
                when (pack) {
                    is DirectoryResourcePack -> walkDirectory(pack, seen)
                    is ZippedResourcePack -> walkZip(pack, seen)
                    is BuiltInResourcePack -> walkBuiltIn(pack, seen)
                    else -> warnUnsupported(pack)
                }
            } catch (e: Exception) {
                LegacySkyboxes.LOGGER.warn("Failed to scan resource pack '{}' for CTM properties: {}", pack.name, e.message)
            }
        }
        return seen.toList()
    }

    private fun warnUnsupported(pack: ResourcePack) {
        if (warnedPackTypes.add(pack.javaClass)) {
            LegacySkyboxes.LOGGER.warn(
                "Resource pack '{}' ({}) can't be scanned for CTM properties (unsupported pack type)",
                pack.name, pack.javaClass.simpleName,
            )
        }
    }

    private fun walkDirectory(pack: DirectoryResourcePack, out: MutableSet<Identifier>) {
        val root = (pack as MixinCustomResourcePack_CtmAccessor).getCtmFile()
        val assetsDir = File(root, "assets")
        val namespaceDirs = assetsDir.listFiles { f -> f.isDirectory } ?: return
        for (namespaceDir in namespaceDirs) {
            for (prefix in PREFIXES) {
                val ctmDir = File(namespaceDir, prefix)
                if (!ctmDir.isDirectory) continue
                ctmDir.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".properties") }
                    .forEach { file ->
                        val relative = file.relativeTo(namespaceDir).invariantSeparatorsPath
                        out += Identifier(namespaceDir.name, relative)
                    }
            }
        }
    }

    private fun walkZip(pack: ZippedResourcePack, out: MutableSet<Identifier>) {
        val zipFile = (pack as MixinCustomResourcePack_CtmAccessor).getCtmFile()
        ZipFile(zipFile).use { zip ->
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory) continue
                val name = entry.name
                val match = ZIP_PATTERN.matchEntire(name) ?: continue
                out += Identifier(match.groupValues[1], match.groupValues[2] + match.groupValues[3])
            }
        }
    }

    private fun walkBuiltIn(pack: BuiltInResourcePack, out: MutableSet<Identifier>) {
        val assets = (pack as MixinBuiltInResourcePack_CtmAccessor).getCtmAssets()
        for ((namespace, dir) in assets) {
            for (prefix in PREFIXES) {
                val ctmDir = File(dir, prefix)
                if (!ctmDir.isDirectory) continue
                ctmDir.walkTopDown()
                    .filter { it.isFile && it.name.endsWith(".properties") }
                    .forEach { file ->
                        val relative = file.relativeTo(dir).invariantSeparatorsPath
                        out += Identifier(namespace, relative)
                    }
            }
        }
    }

    private val ZIP_PATTERN = Regex("^assets/([a-z0-9_.-]+)/(optifine/ctm/|mcpatcher/ctm/)(.+\\.properties)$")
}
