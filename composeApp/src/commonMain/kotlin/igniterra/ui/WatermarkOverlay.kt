package igniterra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

// ── Filigrane D.A. / E.D. ────────────────────────────────────────────────────
@Composable
fun WatermarkOverlay(modifier: Modifier = Modifier) {
    Layout(
        modifier = modifier,
        content = {
            repeat(120) {
                Text(
                    "D.A. / E.D.",
                    fontSize      = 11.sp,
                    fontFamily    = Mono,
                    color         = T3.copy(alpha = 0.06f),
                    letterSpacing = 2.sp,
                    modifier      = Modifier.graphicsLayer { rotationZ = -25f }
                )
            }
        }
    ) { measurables, constraints ->
        val free = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(free) }
        val w = constraints.maxWidth.coerceAtLeast(1)
        // Force la hauteur à remplir le parent — si pas de contrainte, fallback 1200
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight else 1200
        layout(w, h) {
            val tw       = placeables.firstOrNull()?.width ?: 100
            val th       = placeables.firstOrNull()?.height ?: 20
            val spacingX = tw + 80
            val spacingY = th + 60
            var idx = 0
            var y = -spacingY
            while (y < h + spacingY && idx < placeables.size) {
                var x = -spacingX
                while (x < w + spacingX && idx < placeables.size) {
                    placeables[idx++].place(x, y)
                    x += spacingX
                }
                y += spacingY
            }
        }
    }
}