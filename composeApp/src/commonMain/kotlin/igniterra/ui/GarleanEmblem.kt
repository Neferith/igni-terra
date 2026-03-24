package igniterra.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private val GRed     = Color(0xFFC84040)
private val GGray    = Color(0xFF555555)
private val GDark    = Color(0xFF111111)
private val GDeepRed = Color(0xFF3A0000)
private val GMono    = FontFamily.Monospace

private fun DrawScope.drawDiamond(
    cx: Float, cy: Float, half: Float,
    outerStroke: Float, outerColor: Color,
    innerStroke: Float, innerColor: Color,
    notch: Float = 0f
) {
    val path = Path().apply {
        moveTo(cx,        cy - half)
        lineTo(cx + half, cy)
        lineTo(cx,        cy + half)
        lineTo(cx - half, cy)
        close()
    }
    drawPath(path, outerColor, style = Stroke(outerStroke))
    drawPath(path, innerColor, style = Stroke(innerStroke))
    if (notch > 0f) {
        val n = notch; val h = notch * 0.7f
        listOf(
            Offset(cx - n/2, cy - half - h) to Size(n, h),
            Offset(cx - n/2, cy + half)      to Size(n, h),
            Offset(cx - half - h, cy - n/2)  to Size(h, n),
            Offset(cx + half,     cy - n/2)  to Size(h, n),
        ).forEach { (tl, sz) -> drawRect(outerColor, topLeft = tl, size = sz) }
    }
}

@Composable
fun GarleanEmblem(
    modifier    : Modifier = Modifier,
    canvasSize  : Dp       = 200.dp,
    loaderDuration: Long   = 3000L,
    onComplete  : (() -> Unit)? = null
) {
    val alphaLeft    = remember { Animatable(0f) }
    val alphaRight   = remember { Animatable(0f) }
    val alphaDiamond = remember { Animatable(0f) }
    val alphaLabel   = remember { Animatable(0f) }
    val alphaLines   = remember { Animatable(0f) }
    var glitchX      by remember { mutableStateOf(0f) }
    var loaderProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Fade in séquentiel
        delay(300L)
        alphaLeft.animateTo(1f, tween(500))
        delay(150L)
        alphaRight.animateTo(1f, tween(500))
        delay(200L)
        repeat(4) { alphaLines.snapTo(0.5f); delay(60L); alphaLines.snapTo(0f); delay(50L) }
        alphaDiamond.animateTo(1f, tween(400))
        alphaLines.animateTo(0.08f, tween(300))
        delay(300L)
        alphaLabel.animateTo(1f, tween(500))

        // Loader
        val steps = 60
        val stepDelay = loaderDuration / steps
        repeat(steps) { i ->
            loaderProgress = (i + 1).toFloat() / steps
            delay(stepDelay)
        }
        loaderProgress = 1f
        delay(200L)
        onComplete?.invoke()
    }

    // Glitch loop séparé
    LaunchedEffect(Unit) {
        delay(2000L)
        while (true) {
            delay(Random.nextLong(2000L, 5000L))
            glitchX = (Random.nextFloat() - 0.5f) * 10f
            delay(80L)
            glitchX = 0f
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Canvas plus large que le logo pour que les chaînes ne débordent pas
        Canvas(Modifier.size(canvasSize * 1.5f, canvasSize)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Proportions basées sur h (hauteur), pas w — évite le débordement latéral
            val half  = h * 0.36f   // demi-diagonale chaînes
            val halfD = h * 0.28f   // demi-diagonale diamant
            val gap   = h * 0.38f   // écart depuis le centre
            val os    = h * 0.09f
            val ins   = h * 0.055f
            val notch = h * 0.07f

            // Chaîne gauche — clippée à droite du centre
            clipRect(left = 0f, top = 0f, right = cx, bottom = h) {
                drawDiamond(
                    cx = cx - gap + glitchX, cy = cy, half = half,
                    outerStroke = os, outerColor = GDark,
                    innerStroke = ins, innerColor = GGray.copy(alpha = alphaLeft.value),
                    notch = notch
                )
            }

            // Chaîne droite — clippée à gauche du centre
            clipRect(left = cx, top = 0f, right = w, bottom = h) {
                drawDiamond(
                    cx = cx + gap - glitchX, cy = cy, half = half,
                    outerStroke = os, outerColor = GDark,
                    innerStroke = ins, innerColor = GGray.copy(alpha = alphaRight.value),
                    notch = notch
                )
            }

            // Diamant rouge central
            drawDiamond(
                cx = cx, cy = cy, half = halfD,
                outerStroke = h * 0.08f, outerColor = GDeepRed.copy(alpha = alphaDiamond.value),
                innerStroke = h * 0.045f, innerColor = GRed.copy(alpha = alphaDiamond.value)
            )

            // Scanlines
            val la = alphaLines.value
            if (la > 0f) {
                var y = 0f
                while (y < h) {
                    drawLine(GRed.copy(alpha = la * 0.5f), Offset(0f, y), Offset(w, y), 0.7f)
                    y += h * 0.09f
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            "EMPIRE DE GARLEMALD",
            fontSize = 8.sp, letterSpacing = 4.sp,
            fontFamily = GMono, color = GRed.copy(alpha = alphaLabel.value)
        )

        Spacer(Modifier.height(10.dp))

        // Loader
        if (loaderProgress > 0f) {
            Box(
                Modifier
                    .width(canvasSize)
                    .height(2.dp)
                    .background(GRed.copy(alpha = 0.15f))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(loaderProgress)
                        .background(GRed)
                )
            }
        }
    }
}