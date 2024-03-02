package com.mineinabyss.launchy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mineinabyss.launchy.data.config.Config
import com.mineinabyss.launchy.data.Dirs
import com.mineinabyss.launchy.data.modpacks.Mods
import com.mineinabyss.launchy.state.LaunchyState
import com.mineinabyss.launchy.ui.colors.AppTheme
import com.mineinabyss.launchy.ui.screens.Screens
import com.mineinabyss.launchy.ui.state.TopBarProvider
import com.mineinabyss.launchy.ui.state.TopBarState
import com.mineinabyss.launchy.util.OS

private val LaunchyStateProvider = compositionLocalOf<LaunchyState> { error("No local versions provided") }
val LocalLaunchyState: LaunchyState
    @Composable
    get() = LaunchyStateProvider.current

fun main() {
    application {
        val windowState = rememberWindowState(placement = WindowPlacement.Floating)
        val icon = painterResource("mia_profile_icon.png")
        val launchyState by produceState<LaunchyState?>(null) {
            val config = Config.read()
            value = LaunchyState(config)
        }
        val onClose: () -> Unit = {
            exitApplication()
            launchyState?.saveToConfig()
        }

        Window(
            state = windowState,
            title = "Mine in Abyss - Launcher",
            icon = icon,
            onCloseRequest = onClose,
            undecorated = true,
            transparent = OS.get() == OS.WINDOWS, // Windows 11 shows a white bar on the bottom without this
        ) {
            val topBarState = remember { TopBarState(onClose, windowState, this) }
            val ready = launchyState != null
            AppTheme {
                CompositionLocalProvider(TopBarProvider provides topBarState) {
                    Scaffold {
                        AnimatedVisibility(!ready, exit = fadeOut()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Getting latest plugin versions...")
                            }
                        }
                        AnimatedVisibility(ready, enter = fadeIn()) {
                            CompositionLocalProvider(
                                LaunchyStateProvider provides launchyState!!,
                            ) {
                                Dirs.createDirs()
                                Screens()
                            }
                        }
                    }
                }
            }
        }
    }
}
