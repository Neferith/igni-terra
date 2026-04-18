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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

private val IBg    = Color(0xFF02050A)
private val ITeal  = Color(0xFF38C4C4)
private val IT3    = Color(0xFF365470)
private val IPanel = Color(0xFF080F18)
private val IMono  = FontFamily.Monospace

@Composable
fun IgniTerraIntro(
    introText  : String = "",
    onComplete : () -> Unit
) {
    var loaderProgress by remember { mutableStateOf(0f) }
    var displayedText  by remember { mutableStateOf("") }
    var phase          by remember { mutableStateOf(0) }
    var countdown      by remember { mutableStateOf(-1) }
    var tick           by remember { mutableStateOf(0) }

    // Tick animation
    LaunchedEffect(Unit) {
        while (true) { delay(16L); tick++ }
    }

    // Animations continues
    val crystalPulse by animateFloatAsState(
        targetValue = if (tick % 2 == 0) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse)
    )
    val flamePulse by animateFloatAsState(
        targetValue = if (tick % 3 == 0) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse)
    )

    // Séquence
    LaunchedEffect(Unit) {
        // Chargement — 16 secondes avec boite à musique
        CrackleSound.playWav("boiteamusique.mp3", loop = false)
        val steps = 100
        repeat(steps) { i ->
            loaderProgress = i.toFloat() / steps
            delay(160L)  // 100 × 170ms = 17 secondes
        }
        loaderProgress = 1f
        delay(400L)

        // Texte intro
        if (introText.isNotEmpty()) {
            phase = 1
            val charDelay = 14000L / introText.length
            val laughTrigger = "on ne peut la tuer qu"  // trigger robuste sans apostrophe
            var laughTriggered = false
            for (i in introText.indices) {
                displayedText = introText.substring(0, i + 1)
                // Déclenche le rire quand la phrase clé est complète
                if (!laughTriggered && displayedText.contains(laughTrigger)) {
                    laughTriggered = true
                    // Lance le son sans bloquer le typewriter
                    kotlinx.coroutines.GlobalScope.launch {
                        delay(300L)
                        CrackleSound.stopWav()
                        delay(200L)
                        CrackleSound.playWav("rire.mp3", loop = false)
                    }
                }
                delay(charDelay)
            }
            delay(2000L)
        }

        phase = 2
        delay(300L)
        // Compte à rebours
        for (i in 5 downTo 1) {
            countdown = i
            delay(1000L)
        }
        countdown = 0
        delay(200L)
        onComplete()
    }

    Box(
        Modifier.fillMaxSize().background(IBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "AUTORITÉ MAGITEK GARLEAN",
                fontSize = 8.sp, letterSpacing = 4.sp,
                fontFamily = IMono, color = IT3
            )

            Canvas(Modifier.size(420.dp, 200.dp)) {
                drawCanonScene(tick, crystalPulse, flamePulse)
            }

            Text(
                "IGNI TERRA",
                fontSize = 26.sp, letterSpacing = 10.sp,
                fontFamily = IMono, color = ITeal
            )
            Text(
                "LANCE-FLAMME MAGITEK — CLASSE ΓΔ",
                fontSize = 8.sp, letterSpacing = 3.sp,
                fontFamily = IMono, color = IT3
            )

            Spacer(Modifier.height(8.dp))

            // Barre de chargement
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(Modifier.width(280.dp).height(4.dp).background(Color(0xFF0A1520))) {
                    Box(
                        Modifier.fillMaxHeight().fillMaxWidth(loaderProgress)
                            .background(ITeal)
                    )
                }
                Text(
                    if (loaderProgress < 1f)
                        "INITIALISATION SYSTÈME... ${(loaderProgress * 100).toInt()}%"
                    else "SYSTÈME OPÉRATIONNEL",
                    fontSize = 7.sp, letterSpacing = 2.sp,
                    fontFamily = IMono,
                    color = if (loaderProgress < 1f) IT3 else ITeal
                )
            }

            // Texte intro typewriter
            // Compte à rebours
            if (countdown > 0) {
                Box(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$countdown",
                            fontSize = 48.sp,
                            fontFamily = IMono,
                            color = if (countdown <= 2) Color(0xFFFF4400) else ITeal,
                            letterSpacing = 4.sp
                        )
                        Text(
                            "MISSION EN APPROCHE...",
                            fontSize = 7.sp,
                            fontFamily = IMono,
                            color = IT3,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            if (phase >= 1 && displayedText.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier.widthIn(max = 400.dp)
                        .background(IPanel)
                        .padding(18.dp)
                ) {
                    Text(
                        displayedText,
                        fontSize = 11.sp, lineHeight = 19.sp,
                        fontFamily = IMono, color = ITeal.copy(alpha = 0.9f),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

// ── Dessin Canvas ─────────────────────────────────────────────────────────────

private fun randF(seed: Float, min: Float, max: Float): Float {
    val x = sin(seed * 127.1f + 311.7f) * 43758.5453f
    return min + (x - kotlin.math.floor(x)) * (max - min)
}

private fun DrawScope.drawCanonScene(tick: Int, crystalPulse: Float, flamePulse: Float) {
    val w  = size.width
    val h  = size.height
    val cy = h * 0.52f
    val t  = tick.toFloat()

    // Scanlines
    var scanY = 0f
    while (scanY < h) {
        drawLine(Color(0xFF0A1520).copy(alpha = 0.5f), Offset(0f, scanY), Offset(w, scanY), 0.5f)
        scanY += h * 0.055f
    }

    // ── Tube canon ───────────────────────────────────────────────────────────
    val tL = 20f; val tR = w * 0.42f
    val tT = cy - 18f; val tB = cy + 18f

    drawRect(Color(0xFF1A2535), Offset(tL, tT), Size(tR - tL, tB - tT))
    drawRect(Color(0xFF38C4C4).copy(alpha = 0.6f), Offset(tL, tT), Size(tR - tL, tB - tT), style = Stroke(1.5f))

    // Rainures
    listOf(tL + 30f, tL + 70f, tL + 110f).forEach { x ->
        drawLine(Color(0xFF38C4C4).copy(alpha = 0.2f), Offset(x, tT), Offset(x, tB), 1f)
    }
    drawRect(Color(0xFF38C4C4).copy(alpha = 0.08f), Offset(tL, cy - 4f), Size(tR - tL, 8f))

    // ── Module buse ───────────────────────────────────────────────────────────
    val nL = tR; val nR = tR + 38f
    val nT = cy - 26f; val nB = cy + 26f

    drawRect(Color(0xFF0E2040), Offset(nL, nT), Size(nR - nL, nB - nT))
    drawRect(Color(0xFF38C4C4).copy(alpha = 0.7f), Offset(nL, nT), Size(nR - nL, nB - nT), style = Stroke(2f))
    drawLine(Color(0xFF38C4C4).copy(alpha = 0.3f), Offset(nL, cy), Offset(nR, cy), 1f)

    // ── Cristal ───────────────────────────────────────────────────────────────
    val pulse = 0.5f + 0.5f * sin(t * 0.07f)
    val cX = nL + 18f

    drawCircle(Color(0xFF6BAAFF).copy(alpha = pulse * 0.12f), 16f + pulse * 5f, Offset(cX, cy))
    drawCircle(Color(0xFF0D1A2A), 10f, Offset(cX, cy))
    drawCircle(Color(0xFF38C4C4).copy(alpha = 0.4f), 10f, Offset(cX, cy), style = Stroke(1f))

    val crystPath = Path().apply {
        moveTo(cX, cy - 8f); lineTo(cX + 6f, cy)
        lineTo(cX, cy + 8f); lineTo(cX - 6f, cy); close()
    }
    drawPath(crystPath, Color(0xFF6BAAFF).copy(alpha = 0.5f + pulse * 0.5f))
    drawPath(crystPath, Color(0xFFC8E8FF).copy(alpha = 0.4f + pulse * 0.4f), style = Stroke(1f))

    // ── Électrodes ────────────────────────────────────────────────────────────
    val eX = nR + 8f
    val e1Y = cy - 20f; val e2Y = cy + 20f

    drawLine(Color(0xFF38C4C4), Offset(nR, cy - 14f), Offset(eX + 4f, e1Y - 4f), 2.5f)
    drawLine(Color(0xFF38C4C4), Offset(nR, cy + 14f), Offset(eX + 4f, e2Y + 4f), 2.5f)
    drawCircle(Color(0xFF38C4C4), 4f, Offset(eX + 4f, e1Y - 4f))
    drawCircle(Color(0xFF38C4C4), 4f, Offset(eX + 4f, e2Y + 4f))

    // ── Arc électrique ────────────────────────────────────────────────────────
    val a1 = Offset(eX + 4f, e1Y - 4f)
    val a2 = Offset(eX + 4f, e2Y + 4f)

    // Halo
    drawLine(Color(0xFF6BAAFF).copy(alpha = 0.12f), a1, a2, 10f)

    // Arc principal
    val arcPath = Path()
    arcPath.moveTo(a1.x, a1.y)
    for (i in 1..8) {
        val tt = i / 9f
        val mx = a1.x + (a2.x - a1.x) * tt + randF(t * 0.5f + i, -7f, 7f)
        val my = a1.y + (a2.y - a1.y) * tt + randF(t * 0.5f + i + 20f, -5f, 5f)
        arcPath.lineTo(mx, my)
    }
    arcPath.lineTo(a2.x, a2.y)
    drawPath(arcPath, Color(0xFFB4E6FF).copy(alpha = 0.95f), style = Stroke(1.5f))

    // Arc secondaire
    val arcPath2 = Path()
    arcPath2.moveTo(a1.x, a1.y)
    for (i in 1..8) {
        val tt = i / 9f
        val mx = a1.x + (a2.x - a1.x) * tt + randF(t * 0.5f + i + 50f, -9f, 9f)
        val my = a1.y + (a2.y - a1.y) * tt + randF(t * 0.5f + i + 70f, -6f, 6f)
        arcPath2.lineTo(mx, my)
    }
    arcPath2.lineTo(a2.x, a2.y)
    drawPath(arcPath2, Color(0xFF6BAAFF).copy(alpha = 0.5f), style = Stroke(0.8f))

    // ── Flamme ────────────────────────────────────────────────────────────────
    val fX = eX + 14f
    val fp  = 0.7f + 0.3f * sin(t * 0.18f)
    val fp2 = 0.6f + 0.4f * sin(t * 0.11f + 1.2f)

    data class Flame(val yOff: Float, val len: Float, val yw: Float, val color: Color)
    listOf(
        Flame(0f,    180f * fp,  40f, Color(0xFFB41E00).copy(alpha = 0.10f)),
        Flame(0f,    160f * fp,  30f, Color(0xFFCC3200).copy(alpha = 0.18f)),
        Flame(-22f,   80f * fp2,  7f, Color(0xFFFF3C00).copy(alpha = 0.55f)),
        Flame( 22f,   80f * fp2,  7f, Color(0xFFFF3C00).copy(alpha = 0.55f)),
        Flame(-12f,  120f * fp2, 10f, Color(0xFFFF5000).copy(alpha = 0.75f)),
        Flame( 12f,  120f * fp2, 10f, Color(0xFFFF5000).copy(alpha = 0.75f)),
        Flame(0f,    160f * fp,  16f, Color(0xFFFF6400).copy(alpha = 0.85f)),
        Flame(0f,    130f * fp,   6f, Color(0xFFFFF0B4).copy(alpha = 0.95f)),
    ).forEach { f ->
        val tipX = fX + f.len + randF(t * 0.13f + f.yOff, -8f, 8f)
        val tipY = cy + f.yOff + randF(t * 0.09f + f.yOff + 3f, -5f, 5f)
        val flamePath = Path().apply {
            moveTo(fX, cy + f.yOff - f.yw * 0.5f)
            quadraticBezierTo(fX + f.len * 0.4f, cy + f.yOff - f.yw, tipX, tipY)
            quadraticBezierTo(fX + f.len * 0.4f, cy + f.yOff + f.yw, fX, cy + f.yOff + f.yw * 0.5f)
            close()
        }
        drawPath(flamePath, f.color)
    }

    // Particules
    for (i in 0 until 6) {
        val px = fX + randF(t * 0.2f + i, 20f, 150f * fp)
        val py = cy + randF(t * 0.2f + i + 30f, -30f, 30f)
        val pr = randF(t * 0.2f + i + 60f, 1.5f, 4f)
        val pa = randF(t * 0.2f + i + 90f, 0.3f, 0.8f)
        drawCircle(Color(0xFFFFB400).copy(alpha = pa), pr, Offset(px, py))
    }
}