package com.mineinabyss.launchy.ui.screens.modpack.main.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mineinabyss.launchy.ui.screens.Screen
import com.mineinabyss.launchy.ui.screens.screen

@Composable
fun SettingsButton() {
    Button(onClick = { screen = Screen.InstanceSettings }) {
        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
        Text("Settings")
    }
}
