package com.mineinabyss.launchy.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mineinabyss.launchy.LocalLaunchyState
import com.mineinabyss.launchy.state.modpack.ModpackState
import com.mineinabyss.launchy.ui.AppTopBar
import com.mineinabyss.launchy.ui.auth.AuthDialog
import com.mineinabyss.launchy.ui.elements.LaunchyDialog
import com.mineinabyss.launchy.ui.screens.addmodpack.AddModpackDialog
import com.mineinabyss.launchy.ui.screens.home.HomeScreen
import com.mineinabyss.launchy.ui.screens.modpack.main.ModpackScreen
import com.mineinabyss.launchy.ui.screens.modpack.settings.SettingsScreen
import com.mineinabyss.launchy.ui.state.TopBar

var screen: Screen by mutableStateOf(Screen.Default)

var dialog: Dialog by mutableStateOf(Dialog.None)

var progress: Progress by mutableStateOf(Progress.Animated)

private val ModpackStateProvider = compositionLocalOf<ModpackState> { error("No local modpack provided") }

val LocalModpackState: ModpackState
    @Composable get() = ModpackStateProvider.current

@Composable
fun Screens() {
    val state = LocalLaunchyState
    val packState = state.modpackState
    if (packState != null) CompositionLocalProvider(ModpackStateProvider provides packState) {
        TransitionFade(screen is Screen.Modpack) {
            ModpackScreen()
        }
        TranslucentTopBar(screen) {
            TransitionSlideUp(screen == Screen.Settings) {
                SettingsScreen()
            }
        }
    }

    TranslucentTopBar(screen) {
        TransitionFade(screen == Screen.Default) {
            HomeScreen()
        }
    }

    AppTopBar(
        TopBar,
        screen.transparentTopBar,
        showBackButton = screen != Screen.Default,
        onBackButtonClicked = {
            screen = when (screen) {
                Screen.Modpack -> {
                    state.modpackState?.saveToConfig()
                    Screen.Default
                }

                Screen.Settings -> {
                    state.modpackState?.saveToConfig()
                    Screen.Modpack
                }

                else -> Screen.Default
            }
        }
    )

    when (val castDialog = dialog) {
        Dialog.None -> {}
        Dialog.Auth -> AuthDialog(
            onDismissRequest = { dialog = Dialog.None },
        )

        is Dialog.Error -> LaunchyDialog(
            title = { Text(castDialog.title, style = LocalTextStyle.current) },
            onAccept = { dialog = Dialog.None },
            onDecline = { dialog = Dialog.None },
            onDismiss = { dialog = Dialog.None },
            acceptText = "Close",
            declineText = null,
        ) { Text(castDialog.message, style = LocalTextStyle.current) }

        is Dialog.Options -> {
            LaunchyDialog(
                title = { Text(castDialog.title, style = LocalTextStyle.current) },
                onAccept = { castDialog.onAccept(); dialog = Dialog.None },
                onDecline = { castDialog.onDecline(); dialog = Dialog.None },
                onDismiss = { dialog = Dialog.None },
                acceptText = castDialog.acceptText,
                declineText = castDialog.declineText,
            ) { Text(castDialog.message, style = LocalTextStyle.current) }
        }

        Dialog.AddModpack -> {
            AddModpackDialog()
        }
    }
}

@Composable
fun TranslucentTopBar(currentScreen: Screen, content: @Composable () -> Unit) {
    Column {
        AnimatedVisibility(!currentScreen.transparentTopBar, enter = fadeIn(), exit = fadeOut()) {
            Spacer(Modifier.height(40.dp))
        }
        content()
    }
}

@Composable
fun TransitionFade(enabled: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(enabled, enter = fadeIn(), exit = fadeOut()) {
        content()
    }
}

@Composable
fun TransitionSlideUp(enabled: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        enabled,
        enter = fadeIn() + slideIn(initialOffset = { IntOffset(0, 100) }),
        exit = fadeOut() + slideOut(targetOffset = { IntOffset(0, 100) }),
    ) {
        content()
    }
}

