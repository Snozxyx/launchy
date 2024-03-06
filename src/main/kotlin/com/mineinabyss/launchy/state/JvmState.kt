package com.mineinabyss.launchy.state

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mineinabyss.launchy.data.config.Config
import com.mineinabyss.launchy.logic.SuggestedJVMArgs
import kotlin.io.path.Path

class JvmState(
    val config: Config
) {
    var javaPath by mutableStateOf(config.javaPath?.let { Path(it) })
    var userMemoryAllocation by mutableStateOf(config.memoryAllocation)
    var userJvmArgs by mutableStateOf(config.jvmArguments)
    var useRecommendedJvmArgs by mutableStateOf(config.useRecommendedJvmArguments)
    val suggestedArgs get() = buildString {
        if("graalvm" in javaPath.toString()) {
            append(SuggestedJVMArgs.graalVM)
            append(" ")
        }
        append(SuggestedJVMArgs.clientG1GC)
    }
    val jvmArgs by derivedStateOf {
        val memory = (userMemoryAllocation ?: SuggestedJVMArgs.memory).toString()

        "-Xms${memory}M -Xmx${memory}M ${userJvmArgs?.takeIf { !useRecommendedJvmArgs } ?: suggestedArgs}"
    }
    val memory get() = userMemoryAllocation ?: SuggestedJVMArgs.memory
}
