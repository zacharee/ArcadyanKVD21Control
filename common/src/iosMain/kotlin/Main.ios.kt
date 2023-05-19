@file:Suppress("FunctionName", "unused")

import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import dev.zwander.common.App

fun MainViewController() = ComposeUIViewController {
    App(
        modifier = Modifier
    )
}
