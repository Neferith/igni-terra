package igniterra.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val NLTeal      = Color(0xFF2AACAC)
private val NLTealDk    = Color(0xFF1A9494)
private val NLTealBright= Color(0xFF38C4C4)
private val NLCrystalHi = Color(0xFFC8F4FA)
private val NLCrystalMid= Color(0xFF4DC4D8)
private val NLCrystalDk = Color(0xFF2A8A9A)
private val NLCrystalStr= Color(0xFF7EEAF0)
private val NLBlack     = Color(0xFF090D11)

@Composable
fun NouvelLuneLogo(
    modifier   : Modifier = Modifier,
    canvasSize : Dp       = 220.dp
) {
    // Heartbeat
    val infiniteAnim = rememberInfiniteTransition()
    val heartbeat = infiniteAnim.animateFloat(
        initialValue   = 1f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = keyframes {
                durationMillis = 1600
                1f    at 0
                1.22f at 224
                1f    at 448
                1.18f at 672
                1f    at 1120
                1f    at 1600
            }
        )
    )
    val glowAlpha = infiniteAnim.animateFloat(
        initialValue  = 0.15f,
        targetValue   = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1600
                0.15f at 0
                0.5f  at 224
                0.15f at 448
                0.35f at 672
                0.15f at 1120
                0.15f at 1600
            }
        )
    )

    // Fade in
    val alpha = remember { Animatable(0f) }
    var glitchX by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(1200))
        delay(1000L)
        while (true) {
            delay(Random.nextLong(3000L, 8000L))
            glitchX = (Random.nextFloat() - 0.5f) * 6f
            delay(80L)
            glitchX = 0f
        }
    }

    Canvas(modifier.size(canvasSize)) {
        val w  = size.width
        val h  = size.height
        val cx = w / 2f + glitchX
        val cy = h / 2f
        val r  = w * 0.46f   // rayon cercle principal
        val a  = alpha.value

        // ── Anneaux teal effacés ──────────────────────────────────────────────
        val ringR1 = w * 0.52f
        val ringR2 = w * 0.50f
        val ringR3 = w * 0.54f

        // Anneau 1 — tirets irréguliers
        val dashPaint1 = floatArrayOf(4f,7f,10f,4f,15f,5f,8f,12f,3f,9f,6f,13f)
        drawArc(
            color       = NLTeal.copy(alpha = a * 0.28f),
            startAngle  = 0f,
            sweepAngle  = 360f,
            useCenter   = false,
            topLeft     = Offset(cx - ringR1, cy - ringR1),
            size        = androidx.compose.ui.geometry.Size(ringR1*2, ringR1*2),
            style       = Stroke(width = w * 0.07f)
        )
        drawArc(
            color       = NLTealDk.copy(alpha = a * 0.18f),
            startAngle  = 30f,
            sweepAngle  = 330f,
            useCenter   = false,
            topLeft     = Offset(cx - ringR2, cy - ringR2),
            size        = androidx.compose.ui.geometry.Size(ringR2*2, ringR2*2),
            style       = Stroke(width = w * 0.03f)
        )
        drawArc(
            color       = NLTealBright.copy(alpha = a * 0.15f),
            startAngle  = 60f,
            sweepAngle  = 300f,
            useCenter   = false,
            topLeft     = Offset(cx - ringR3, cy - ringR3),
            size        = androidx.compose.ui.geometry.Size(ringR3*2, ringR3*2),
            style       = Stroke(width = w * 0.015f)
        )

        // ── Halo pulsant ──────────────────────────────────────────────────────
        drawCircle(
            color  = NLTealBright.copy(alpha = a * glowAlpha.value),
            radius = w * 0.55f,
            center = Offset(cx, cy),
            style  = Stroke(width = w * 0.02f)
        )

        // ── Cercle principal noir ─────────────────────────────────────────────
        drawCircle(color = NLBlack, radius = r, center = Offset(cx, cy))

        // ── Lune : croissant noir (fond) + croissant blanc (dessus) ─────────
        val innerR = r * 0.84f

        // 1. Disque entier en sombre — devient le croissant noir de base
        drawCircle(color = Color(0xFF1C2535), radius = innerR, center = Offset(cx, cy))

        // 2. Croissant blanc par-dessus — cercle offset à gauche soustrait du disque
        //    Le décalage fort vers la gauche = croissant blanc large à droite
        //    Le croissant noir restant est mince et naturellement à gauche
        val whiteMaskR  = r * 0.68f
        val whiteMaskCx = cx - r * 0.38f  // fort décalage gauche → blanc large à droite
        val whiteMaskCy = cy - r * 0.08f

        val moonPath = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(cx - innerR, cy - innerR, cx + innerR, cy + innerR))
        }
        val whiteMaskPath = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(
                whiteMaskCx - whiteMaskR, whiteMaskCy - whiteMaskR,
                whiteMaskCx + whiteMaskR, whiteMaskCy + whiteMaskR
            ))
        }
        val whiteCrescent = Path().apply {
            op(moonPath, whiteMaskPath, androidx.compose.ui.graphics.PathOperation.Difference)
        }
        drawPath(whiteCrescent, Color.White)

        // ── Cristal asymétrique ───────────────────────────────────────────────
        val scale  = heartbeat.value
        val crystalCx = cx
        val crystalCy = cy - r * 0.02f

        // Points du cristal — segments haut plus longs
        val topY    = crystalCy - r * 0.50f * scale
        val midY    = crystalCy + r * 0.03f
        val bottomY = crystalCy + r * 0.36f * scale
        val wideX   = r * 0.18f * scale

        val crystal = Path().apply {
            moveTo(crystalCx,          topY)
            lineTo(crystalCx + wideX,  midY)
            lineTo(crystalCx,          bottomY)
            lineTo(crystalCx - wideX,  midY)
            close()
        }

        // Remplissage dégradé simulé — couches
        drawPath(crystal, NLCrystalDk.copy(alpha = a * 0.95f))
        val crystalTop = Path().apply {
            moveTo(crystalCx,              topY)
            lineTo(crystalCx + wideX*0.6f, midY - r*0.15f)
            lineTo(crystalCx,              midY - r*0.03f)
            lineTo(crystalCx - wideX*0.6f, midY - r*0.15f)
            close()
        }
        drawPath(crystalTop, NLCrystalMid.copy(alpha = a * 0.9f))

        // Reflet haut
        val highlight = Path().apply {
            moveTo(crystalCx,              topY)
            lineTo(crystalCx + wideX*0.35f, midY - r*0.25f)
            lineTo(crystalCx,              midY - r*0.1f)
            lineTo(crystalCx - wideX*0.35f, midY - r*0.25f)
            close()
        }
        drawPath(highlight, NLCrystalHi.copy(alpha = a * 0.30f))

        // Contour
        drawPath(crystal, NLCrystalStr.copy(alpha = a * 0.7f), style = Stroke(1.5f))

        // ── Bordure intérieure subtile ────────────────────────────────────────
        drawCircle(
            color  = NLTealDk.copy(alpha = a * 0.3f),
            radius = r,
            center = Offset(cx, cy),
            style  = Stroke(1.5f)
        )
    }
}
