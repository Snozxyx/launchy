package com.mineinabyss.launchy.data.modpacks.formats

import com.mineinabyss.launchy.data.modpacks.InstanceModLoaders
import com.mineinabyss.launchy.data.modpacks.Mod
import com.mineinabyss.launchy.data.modpacks.ModConfig
import com.mineinabyss.launchy.data.modpacks.Mods
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.div

@Serializable
data class ModrinthPackFormat(
    val dependencies: InstanceModLoaders,
    val files: List<PackFile>,
    val formatVersion: Int,
    val name: String,
    val versionId: String,
) : PackFormat {
    @Serializable
    data class PackFile(
        val downloads: List<String>,
        val fileSize: Long,
        val path: ModDownloadPath,
        val hashes: Hashes,
    ) {
        fun toMod(packDir: Path) = Mod(
            packDir,
            ModConfig(
                name = path.validated.toString().removePrefix("mods/").removeSuffix(".jar"),
                desc = "",
                url = downloads.single(),
                downloadPath = path,
            ),
            modId = downloads.single().removePrefix("https://cdn.modrinth.com/data/").substringBefore("/versions"),
            desiredHashes = hashes,
        )
    }

    @Serializable
    data class Hashes(
        val sha1: String,
        val sha512: String,
    )

    override fun getModLoaders(): InstanceModLoaders {
        return dependencies
    }

    override fun toGenericMods(downloadsDir: Path) =
        Mods.withSingleGroup(files.map { it.toMod(downloadsDir) })

    override fun getOverridesPaths(configDir: Path): List<Path> = listOf(configDir / "mrpack" / "overrides")
}

