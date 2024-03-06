package com.mineinabyss.launchy.ui.screens.modpack.main.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayDisabled
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mineinabyss.launchy.LocalLaunchyState
import com.mineinabyss.launchy.data.config.GameInstance
import com.mineinabyss.launchy.logic.Launcher
import com.mineinabyss.launchy.logic.ModDownloader.ensureCurrentDepsInstalled
import com.mineinabyss.launchy.logic.ModDownloader.install
import com.mineinabyss.launchy.state.modpack.ModpackState
import com.mineinabyss.launchy.ui.elements.PrimaryButtonColors
import com.mineinabyss.launchy.ui.elements.SecondaryButtonColors
import com.mineinabyss.launchy.ui.screens.Dialog
import com.mineinabyss.launchy.ui.screens.dialog
import com.mineinabyss.launchy.ui.screens.snackbarHostState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PlayButton(
    hideText: Boolean = false,
    instance: GameInstance,
    getModpackState: suspend () -> ModpackState?,
) {
    val state = LocalLaunchyState
    val process = state.processFor(instance)
    val coroutineScope = rememberCoroutineScope()
    val buttonIcon by remember(state.profile.currentSession, process) {
        mutableStateOf(
            when {
                state.profile.currentProfile == null -> Icons.Rounded.PlayDisabled
                process == null -> Icons.Rounded.PlayArrow
                else -> Icons.Rounded.Stop
            }
        )
    }
    val buttonText by remember(process) {
        mutableStateOf(if (process == null) "Play" else "Stop")
    }
    val buttonColors by mutableStateOf(
        if (process == null) PrimaryButtonColors
        else SecondaryButtonColors
    )

    Box {
        var foundPackState: ModpackState? by remember { mutableStateOf(null) }
        val updateBeforeLaunch = foundPackState?.queued?.downloads?.isNotEmpty() ?: false
                || foundPackState?.queued?.deletions?.isNotEmpty() ?: false
        val onClick: () -> Unit = {
            coroutineScope.launch(Dispatchers.IO) {
                val packState = foundPackState ?: getModpackState() ?: return@launch
                foundPackState = packState
                if (process == null) {
                    when {
                        // Assume this means not launched before
                        packState.userAgreedDeps == null -> {
                            coroutineScope.launch(Dispatchers.IO) {
                                packState.install(state).join()
                                Launcher.launch(state, packState, state.profile)
                            }
                        }
                        updateBeforeLaunch -> {
                            dialog = Dialog.Options(
                                title = "Update before launch?",
                                message = "Updates are available for this modpack. Would you like to download them?",
                                acceptText = "Download",
                                declineText = "Skip",
                                onAccept = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        packState.install(state).join()
                                        Launcher.launch(state, packState, state.profile)
                                    }
                                },
                                onDecline = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        packState.install(state).join()
                                        Launcher.launch(state, packState, state.profile)
                                    }
                                }
                            )
                        }
                        else -> {
                            coroutineScope.launch(Dispatchers.IO) {
                                packState.ensureCurrentDepsInstalled(state).join()
                                println("Launching now!")
                                Launcher.launch(state, packState, state.profile)
                            }
                        }
                    }
                } else {
                    process.destroyForcibly()
                    state.setProcessFor(packState.instance, null)
                }
            }
        }
        val enabled = state.profile.currentProfile != null && foundPackState?.downloads?.isDownloading != true
        if (hideText) Button(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.size(52.dp).defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
            contentPadding = PaddingValues(0.dp),
            colors = buttonColors,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(buttonIcon, buttonText)
        }
        else Button(
            enabled = state.profile.currentProfile != null && foundPackState?.downloads?.isDownloading != true,
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = buttonColors
        ) {
            Icon(buttonIcon, buttonText)
            Text(buttonText)
        }
    }
}
