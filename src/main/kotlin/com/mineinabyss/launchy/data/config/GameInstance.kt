package com.mineinabyss.launchy.data.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.charleskorn.kaml.encodeToStream
import com.mineinabyss.launchy.data.Dirs
import com.mineinabyss.launchy.data.Formats
import com.mineinabyss.launchy.logic.AppDispatchers
import com.mineinabyss.launchy.logic.Downloader
import com.mineinabyss.launchy.logic.UpdateResult
import com.mineinabyss.launchy.logic.showDialogOnError
import com.mineinabyss.launchy.state.InProgressTask
import com.mineinabyss.launchy.state.LaunchyState
import com.mineinabyss.launchy.state.modpack.ModpackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.*

class GameInstance(
    val configDir: Path,
) {
    val instanceFile = configDir / "instance.yml"
    val config: GameInstanceConfig = GameInstanceConfig.read(instanceFile).getOrThrow()

    val overridesDir = configDir / "overrides"

    val minecraftDir = config.overrideMinecraftDir?.let { Path(it) } ?: Dirs.modpackDir(configDir.name)

    val modsDir = (minecraftDir / "mods").createDirectories()
    val userMods = (minecraftDir / "modsFromUser").createDirectories()

    val downloadsDir: Path = minecraftDir / "launchyDownloads"
    val userConfigFile = (configDir / "config.yml")

    val updateCheckerScope = CoroutineScope(Dispatchers.IO)

    var updatesAvailable by mutableStateOf(false)
    var enabled: Boolean by mutableStateOf(true)

    suspend fun createModpackState(state: LaunchyState): ModpackState? {
        val userConfig = ModpackUserConfig.load(userConfigFile).getOrNull() ?: ModpackUserConfig()

        val modpack = state.runTask("loadingModpack ${config.name}", InProgressTask("Loading modpack ${config.name}")) {
            config.source.loadInstance(this)
                .showDialogOnError("Failed to read instance")
                .getOrElse {
                    it.printStackTrace()
                    return null
                }
        }

        val cloudUrl = config.cloudInstanceURL
        if (cloudUrl != null) AppDispatchers.IO.launch {
            val updates = Downloader.checkUpdates(cloudUrl)
            if (updates.result != UpdateResult.UpToDate) {
                updatesAvailable = true
            }
        }
        return ModpackState(this, modpack, userConfig)
    }

    init {
        require(configDir.isDirectory()) { "Game instance at $configDir must be a directory" }
        userMods
    }

    companion object {
        fun create(state: LaunchyState, config: GameInstanceConfig) {
            val instanceDir = Dirs.modpackConfigDir(config.name)
            instanceDir.createDirectories()

            Formats.yaml.encodeToStream(config, (instanceDir / "instance.yml").outputStream())
            state.gameInstances += GameInstance(instanceDir)
        }

        fun readAll(rootDir: Path): List<GameInstance> {
            return rootDir
                .listDirectoryEntries()
                .filter { it.isDirectory() }
                .mapNotNull {
                    runCatching { GameInstance(it) }
                        .onFailure { it.printStackTrace() }
                        .getOrNull()
                }
        }
    }
}
