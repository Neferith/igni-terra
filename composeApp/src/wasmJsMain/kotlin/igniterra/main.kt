package igniterra

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import igniterra.strings.AppStrings
import igniterra.ui.ManualApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CrackleSound.start()
    CanvasBasedWindow(title = AppStrings.Meta.windowTitle) {
        ManualApp()
    }
}