package igniterra

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.CanvasBasedWindow
import igniterra.strings.AppStrings
import igniterra.ui.ManualApp

private const val BASE_WIDTH = 1100f

@JsFun("() => window.innerWidth")
private external fun windowInnerWidth(): Int

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CrackleSound.start()

    val scale = (windowInnerWidth().toFloat() / BASE_WIDTH * 1.2f).coerceAtLeast(0.7f)
   // val scale = windowInnerWidth().toFloat() / BASE_WIDTH * 1.1f

    CanvasBasedWindow(title = AppStrings.Meta.windowTitle) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density   = scale,
                fontScale = 1.0f
            )
        ) {
            ManualApp()
        }
    }
}