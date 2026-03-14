package igniterra

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import igniterra.strings.AppStrings
import igniterra.ui.ManualApp

/**
 * Résolution de référence pour le scaling.
 * À cette taille de fenêtre, le scale factor est 1.0 — identique au design de base.
 * Sur un écran 2K (ex. 2560×1440), la fenêtre par défaut est plus grande
 * et le scale monte proportionnellement.
 */
private const val BASE_WIDTH  = 1100f
private const val BASE_HEIGHT = 760f

fun main() = application {
    CrackleSound.start()

    val windowState = rememberWindowState(width = BASE_WIDTH.dp, height = BASE_HEIGHT.dp)

    Window(
        onCloseRequest = {
            CrackleSound.stop()
            exitApplication()
        },
        title = AppStrings.Meta.windowTitle,
        state = windowState,
    ) {
        // Scale basé sur la largeur — on prend le min avec la hauteur pour éviter le clipping
        val windowWidth  = windowState.size.width.value
        val windowHeight = windowState.size.height.value
        val scaleX = windowWidth  / BASE_WIDTH
        val scaleY = windowHeight / BASE_HEIGHT
        val scale  = minOf(scaleX, scaleY).coerceAtLeast(0.5f)

        CompositionLocalProvider(
            LocalDensity provides Density(
                density   = LocalDensity.current.density * scale,
                fontScale = LocalDensity.current.fontScale
            )
        ) {
            ManualApp()
        }
    }
}