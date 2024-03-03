package com.mineinabyss.launchy.ui.screens.modpack.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mineinabyss.launchy.LocalLaunchyState
import com.mineinabyss.launchy.data.Dirs
import com.mineinabyss.launchy.ui.elements.LaunchyDialog
import com.mineinabyss.launchy.ui.screens.Screen
import com.mineinabyss.launchy.ui.screens.screen
import kotlin.io.path.copyTo
import kotlin.io.path.div

@Composable
fun HandleImportSettings() {
    val state = LocalLaunchyState
    AnimatedVisibility(
        !state.handledImportOptions && state.onboardingComplete,
        enter = fadeIn(), exit = fadeOut(),
    ) {
        ImportSettingsDialog(
            onAccept = {
                try {
                    (Dirs.minecraft / "options.txt").copyTo(Dirs.mineinabyss / "options.txt")
                } catch (e: Exception) {
                    // TODO: Show error message
                    e.printStackTrace()
                }
                screen = Screen.Settings
                state.handledImportOptions = true
            },
            onDecline = {
                screen = Screen.Settings
                state.handledImportOptions = true
            }
        )
    }
}

@Composable
fun ImportSettingsDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    LaunchyDialog(
        title = { Text("Import Settings") },
        onAccept = onAccept,
        onDecline = onDecline,
        onDismiss = onDecline,
        acceptText = "Import",
        declineText = "Skip",
        content = {
            Text("This will import the options.txt file from your .minecraft directory.")
        }
    )
}
