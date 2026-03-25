package igniterra.ui


import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun HexEmblem(
    modifier        : Modifier = Modifier,
    animRotation    : Boolean  = false,
    animPulse       : Boolean  = true,
    animLines       : Boolean  = false,
    animFadeIn      : Boolean  = true,
    animGlitch      : Boolean  = false,
) {
    val alpha1 = remember { Animatable(if (animFadeIn) 0f else 1f) }
    val alpha2 = remember { Animatable(if (animFadeIn) 0f else 1f) }
    val alpha3 = remember { Animatable(if (animFadeIn) 0f else 1f) }

    val infiniteRotation = rememberInfiniteTransition()
    val rotation2 = if (animRotation) infiniteRotation
        .animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing))).value
    else 0f
    val rotation3 = if (animRotation) infiniteRotation
        .animateFloat(360f, 0f, infiniteRepeatable(tween(8000, easing = LinearEasing))).value
    else 0f

    val infiniteLines = rememberInfiniteTransition()
    val rotLines = if (animLines) infiniteLines
        .animateFloat(0f, 360f, infiniteRepeatable(tween(20000, easing = LinearEasing))).value
    else 0f

    val infinitePulse = rememberInfiniteTransition()
    val scale = if (animPulse) infinitePulse.animateFloat(
        0.97f, 1.03f, infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    ).value else 1f

    var glitchAlpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        if (animFadeIn) {
            alpha1.animateTo(1f, tween(400))
            delay(150L)
            alpha2.animateTo(1f, tween(400))
            delay(150L)
            alpha3.animateTo(1f, tween(400))
        }
        if (animGlitch) {
            while (true) {
                delay(Random.nextLong(3000L, 8000L))
                repeat(3) {
                    glitchAlpha = Random.nextFloat() * 0.4f
                    delay(60L)
                    glitchAlpha = 0f
                    delay(40L)
                }
            }
        }
    }

    Canvas(modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        fun hex(r: Float, col: Color, sw: Float, rotDeg: Float = 0f) {
            val path = Path()
            for (i in 0..5) {
                val a = (PI / 180.0 * (60.0 * i - 30.0 + rotDeg)).toFloat()
                val x = cx + r * cos(a)
                val y = cy + r * sin(a)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, col, style = Stroke(sw))
        }

        val r1 = size.minDimension / 2f - 2f

        hex(r1, Teal.copy(alpha = alpha1.value), 1.5f)
        hex(r1 * 0.72f, TealDk.copy(alpha = alpha2.value), 0.8f, rotation2)
        hex(r1 * 0.46f, GoldDk.copy(alpha = alpha3.value), 0.8f, rotation3)

        for (i in 0..2) {
            val a = (PI / 180.0 * (60.0 * i + rotLines)).toFloat()
            drawLine(
                Teal.copy(alpha = 0.12f * alpha1.value),
                start = Offset(cx - r1 * cos(a), cy - r1 * sin(a)),
                end   = Offset(cx + r1 * cos(a), cy + r1 * sin(a)),
                strokeWidth = 0.5f
            )
        }

        if (animGlitch && glitchAlpha > 0f) {
            var y = 0f
            while (y < size.height) {
                drawLine(
                    Teal.copy(alpha = glitchAlpha),
                    Offset(0f, y), Offset(size.width, y), 0.5f
                )
                y += size.height * 0.08f
            }
        }
    }
}