package igniterra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import igniterra.strings.AppStrings
import kotlinx.coroutines.delay

private val SBg       = Color(0xFF050A12)   // bleu nuit glacé
private val SPanel    = Color(0xFF0A1220)   // panel nuit
private val SBdr      = Color(0xFF1A2840)   // bordure bleue
private val STeal     = Color(0xFF7EC8E3)   // bleu glace
private val SRed      = Color(0xFFC84040)
private val SGold     = Color(0xFFCFE8FF)   // blanc glacé
private val ST3       = Color(0xFF4A6880)   // bleu gris
private val SMono     = FontFamily.Monospace
private val SBullet   = Color(0xFFE8F4FF)   // blanc feu bleu
private val SBoss     = Color(0xFF8B0000)   // rouge sang boss
private val SEnemy    = Color(0xFF4A7040)   // vert putride
private val SExplosion= Color(0xFF8B1A1A)   // sang
private val SSnow     = Color(0xFFE8F0FF)   // neige
private val SPutrid   = Color(0xFF6B8B3A)   // vert pourriture
private val SBlood    = Color(0xFFAA1111)   // sang sombre

// ── Overlay — structure identique à FpsView ───────────────────────────────────
@Composable
fun ShooterOverlay(onDismiss: () -> Unit, autoStart: Boolean = false) {
    val game           = remember { ShooterGame() }
    val scope          = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        if (autoStart) game.start()
    }

    // Game loop
    LaunchedEffect(game.alive, game.started) {
        if (!game.started || !game.alive) return@LaunchedEffect
        while (game.alive && game.started) {
            game.tick(scope)
            delay(16L)
        }
    }

    // Retour auto
    LaunchedEffect(game.alive, game.won) {
        if (!game.alive || game.won) {
            delay(3000L)
            onDismiss()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xDD000000))
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.W, Key.DirectionUp   -> { game.moveUp();   true }
                    Key.S, Key.DirectionDown -> { game.moveDown(); true }
                    Key.Spacebar             -> { game.shoot(); CrackleSound.dungeonHit(); true }
                    Key.Escape               -> { onDismiss(); true }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val borderColor = when (game.igniLevel) {
            3    -> Color(0xFFFF4400)  // gamma — orange flamme vif
            2    -> Color(0xFFCC6600)  // beta — orange
            1    -> Color(0xFF884400)  // alpha — orange sombre
            else -> SRed.copy(alpha = 0.4f)
        }
        Column(
            Modifier.width(600.dp).background(SBg).border(1.dp, borderColor)
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().background(SPanel)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text("PROTOCOLE DE DÉFENSE", fontSize = 7.sp, letterSpacing = 3.sp, fontFamily = SMono, color = ST3)
                    Text("IGNI TERRA — MODE COMBAT", fontSize = 11.sp, letterSpacing = 2.sp, fontFamily = SMono, color = SRed)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!game.started)           ShooterBtn("DÉMARRER", STeal) { game.start(); CrackleSound.click() }
                    if (!game.alive || game.won) ShooterBtn("REJOUER",  SRed)  { game.start(); CrackleSound.click() }
                    ShooterBtn("QUITTER", ST3) { CrackleSound.click(); onDismiss() }
                }
            }

            // HUD
            Row(
                Modifier.fillMaxWidth().background(SPanel.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShooterHud("VAGUE", "${game.wave}/5")
                ShooterHud("SCORE", "${game.score}")
                ShooterHud("VIES",  "${game.lives}")

                Text("CLIC TIRER  ·  CLIC DROIT / LONG PRESS GRAPPIN", fontSize = 7.sp, fontFamily = SMono, color = ST3)

            }

            // Stats filles
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF0A0510))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CIVILES", fontSize = 6.sp, letterSpacing = 2.sp, fontFamily = SMono, color = ST3)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("(+)", fontSize = 10.sp, color = Color(0xFFFFB3C6))
                    Text("SAUVÉES : ${game.girlsSaved}", fontSize = 8.sp, fontFamily = SMono, color = Color(0xFFFFB3C6))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("(x)", fontSize = 10.sp, color = SRed)
                    Text("PERDUES : ${game.girlsLost}", fontSize = 8.sp, fontFamily = SMono, color = SRed)
                }
                if (game.civilianMessage.isNotEmpty()) {
                    Text(game.civilianMessage, fontSize = 8.sp, fontFamily = SMono,
                        color = if (game.civilianMessage.contains("PERDUE")) SRed else Color(0xFFFFB3C6),
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(SBdr))

            // Zone de jeu
            Box(
                Modifier.width(600.dp).height(340.dp).background(SBg)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                game.clickAt(
                                    offset.x / size.width.toFloat(),
                                    offset.y / size.height.toFloat()
                                )
                                CrackleSound.dungeonHit()
                            },
                            onLongPress = { offset ->
                                game.playerY = (offset.y / size.height.toFloat()).coerceIn(0.05f, 0.95f)
                                game.grapple()
                                CrackleSound.click()
                            }
                        )
                    }
                    .pointerInput("mousemove") {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Move) {
                                    val ny = event.changes.firstOrNull()?.position?.y
                                        ?.div(size.height.toFloat()) ?: return@awaitPointerEventScope
                                    game.playerY = ny.coerceIn(0.05f, 0.95f)
                                }
                            }
                        }
                    }
                    .pointerInput("rightclick") {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                                    val change = event.changes.firstOrNull() ?: continue
                                    if (event.buttons.isSecondaryPressed) {
                                        change.consume()
                                        val ny = change.position.y / size.height.toFloat()
                                        game.playerY = ny.coerceIn(0.05f, 0.95f)
                                        game.grapple()
                                        CrackleSound.click()
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val tick = game.tickCount
                    drawShooterField(game)
                }

                // Dégâts flottants — Text par-dessus le Canvas
                game.dmgNumbers.forEach { d ->
                    val alpha = (d.frames / 30f).coerceIn(0f, 1f)
                    val rise  = (30 - d.frames) * 0.6f
                    val color = when {
                        d.amount >= 8 -> Color(0xFFFF4500)
                        d.amount >= 4 -> Color(0xFFFFAA00)
                        d.amount >= 2 -> Color(0xFFFFD700)
                        else          -> Color(0xFFE2E8F0)
                    }
                    Text(
                        "×${d.amount}",
                        fontSize = if (d.amount >= 4) 13.sp else 10.sp,
                        fontFamily = SMono,
                        color = color.copy(alpha = alpha),
                        modifier = Modifier.offset(
                            x = (d.x * 600 - 10).dp,
                            y = (d.y * 340 - rise - 14).dp
                        )
                    )
                }

                // Overlay message central
                if (game.message.isNotEmpty() && game.alive && !game.won) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val msgColor = when {
                            game.message.contains("LEGAT") || game.message.contains("FINALE") -> SRed
                            game.message.contains("VAINCU") -> SGold
                            game.message.contains("REQUIS") -> Color(0xFFFF6600)
                            game.message.contains("MAX") || game.message.contains("γ") -> Color(0xFFFF4400)
                            game.message.contains("β") -> Color(0xFFCC6600)
                            game.message.contains("α") -> Color(0xFF884400)
                            game.message.contains("VAGUE") -> STeal
                            else -> STeal
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(SPanel.copy(alpha = 0.85f))
                                .border(1.dp, msgColor.copy(alpha = 0.6f))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            // Ligne déco haut
                            Box(Modifier.width(120.dp).height(1.dp).background(msgColor.copy(alpha = 0.5f)))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                game.message,
                                fontSize = 13.sp,
                                fontFamily = SMono,
                                color = msgColor,
                                letterSpacing = 3.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            // Ligne déco bas
                            Box(Modifier.width(120.dp).height(1.dp).background(msgColor.copy(alpha = 0.5f)))
                        }
                    }
                }

                if (!game.alive || game.won) {
                    val isBadEnding = game.won && game.badEnding
                    Box(
                        Modifier.fillMaxSize().background(
                            when {
                                !game.alive    -> Color(0xCC000000)
                                isBadEnding    -> Color(0xCC1A0000)
                                else           -> Color(0xCC000510)
                            }
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                when {
                                    !game.alive  -> "GAME OVER"
                                    isBadEnding  -> "VICTOIRE AMÈRE"
                                    else         -> "VICTOIRE !"
                                },
                                fontSize = 22.sp, fontFamily = SMono, letterSpacing = 4.sp,
                                color = when {
                                    !game.alive -> SRed
                                    isBadEnding -> Color(0xFFFF6B00)
                                    else        -> SGold
                                }
                            )
                            Spacer(Modifier.height(6.dp))
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("(+) Sauvees : ${game.girlsSaved}", fontSize = 9.sp, fontFamily = SMono, color = Color(0xFFFFB3C6))
                                Text("(x) Perdues : ${game.girlsLost}", fontSize = 9.sp, fontFamily = SMono,
                                    color = if (game.girlsLost > 0) SRed else ST3)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Score final : ${game.score}", fontSize = 11.sp, fontFamily = SMono, color = ST3)
                            Text("Retour dans 3s...", fontSize = 8.sp, fontFamily = SMono, color = ST3)
                        }
                    }
                }

                if (!game.started) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("APPUYEZ SUR DEMARRER", fontSize = 10.sp, fontFamily = SMono,
                                color = STeal.copy(alpha = 0.6f), letterSpacing = 3.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("CLIC  --  viser et tirer", fontSize = 8.sp, fontFamily = SMono, color = ST3)
                            Text("CLIC DROIT / LONG PRESS  --  grappin", fontSize = 8.sp, fontFamily = SMono, color = ST3)
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(SBdr))

            // Barre Igni Terra
            val igniColor = when (game.igniLevel) {
                1    -> Color(0xFF884400)
                2    -> Color(0xFFCC6600)
                3    -> Color(0xFFFF4400)
                else -> ST3
            }
            val igniLabel = when (game.igniLevel) {
                1    -> "IGNI TERRA α"
                2    -> "IGNI TERRA β"
                3    -> "IGNI TERRA γ MAX"
                else -> "IGNI TERRA"
            }
            Column(
                Modifier.fillMaxWidth().background(SPanel)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(igniLabel, fontSize = 7.sp, fontFamily = SMono, color = igniColor, letterSpacing = 3.sp)
                    Text(
                        if (game.igniLevel >= 3) "PUISSANCE MAX" else "${game.igniParts}/5 pieces",
                        fontSize = 7.sp, fontFamily = SMono, color = igniColor.copy(alpha = 0.7f)
                    )
                }
                // Barre de progression
                Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF0A1520))) {
                    // Paliers franchis (segments pleins)
                    val totalProgress = (game.igniLevel * 5 + game.igniParts) / 15f
                    Box(
                        Modifier.fillMaxHeight()
                            .fillMaxWidth(totalProgress.coerceIn(0f, 1f))
                            .background(igniColor)
                    )
                    // Séparateurs de palier
                    listOf(1f/3f, 2f/3f).forEach { sep ->
                        Box(
                            Modifier.fillMaxHeight().width(2.dp)
                                .offset(x = (sep * 600 - 1).dp)
                                .background(SPanel)
                        )
                    }
                }
                // Labels paliers
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("α", fontSize = 7.sp, fontFamily = SMono,
                        color = if (game.igniLevel >= 1) Color(0xFF884400) else ST3.copy(alpha = 0.4f))
                    Text("β", fontSize = 7.sp, fontFamily = SMono,
                        color = if (game.igniLevel >= 2) Color(0xFFCC6600) else ST3.copy(alpha = 0.4f))
                    Text("γ", fontSize = 7.sp, fontFamily = SMono,
                        color = if (game.igniLevel >= 3) Color(0xFFFF4400) else ST3.copy(alpha = 0.4f))
                }
            }
        }
    }
}

// ── Plein écran ───────────────────────────────────────────────────────────────
@Composable
fun ShooterFullScreen(
    recipient : AppStrings.Recipient,
    onQuit    : () -> Unit
) {
    LaunchedEffect(Unit) {
        recipient.musicFile?.let { CrackleSound.playWav(it, loop = true) }
    }
    Box(Modifier.fillMaxSize().background(SBg), contentAlignment = Alignment.Center) {
        ShooterOverlay(onDismiss = { CrackleSound.stopWav(); onQuit() }, autoStart = true)
    }
}

// ── Rendu Canvas ──────────────────────────────────────────────────────────────

private fun DrawScope.drawShooterField(game: ShooterGame) {
    val tick = game.tickCount
    val w = size.width
    val h = size.height


    drawRect(Color(0xFF0A1828), Offset(0f, 0f), Size(w, h))

    val particleCount = 200

    for (i in 0 until particleCount) {
        val speedY = 0.4f + (i % 7) * 0.15f
        val speedX = ((i % 9) - 4) * 0.07f
        val fx = ((i * 127 + tick * speedX) % w + w) % w
        val fy = ((i * 91  + tick * speedY) % h + h) % h
        val radius = if (i % 5 == 0) 2.5f else if (i % 3 == 0) 1.5f else 1f
        val alpha  = if (i % 5 == 0) 0.5f else if (i % 3 == 0) 0.3f else 0.15f
        drawCircle(SSnow.copy(alpha = alpha), radius, Offset(fx, fy))
    }

    // Ligne de danger
    drawLine(SRed.copy(alpha = 0.3f), Offset(w * 0.08f, 0f), Offset(w * 0.08f, h), 1f)

    // Joueur
    val px = w * 0.06f
    val py = game.playerY * h
    drawRect(STeal.copy(alpha = 0.9f), Offset(px - 6f, py - 8f), Size(16f, 16f))
    drawRect(STeal, Offset(px + 10f, py - 3f), Size(12f, 6f))
    drawRect(Color(0xFF1A6060), Offset(px - 6f, py - 8f), Size(16f, 16f), style = Stroke(1f))
    drawRect(SGold.copy(alpha = 0.5f), Offset(px - 12f, py - 3f), Size(8f, 6f))

    // Balles
    game.bullets.filter { it.alive }.forEach { b ->
        val bx = b.x * w; val by = b.y * h
        if (b.isFlame) {
            // Gerbe Igni Terra — flamme orange sur fond de glace
            drawCircle(Color(0xFFFF6600), radius = 5f, center = Offset(bx, by))
            drawCircle(Color(0xFFFFCC00), radius = 2.5f, center = Offset(bx, by))
            drawLine(Color(0xFFFF4400).copy(alpha = 0.7f), Offset(bx - 14f, by), Offset(bx, by), 3f)
            // Halo de chaleur (contraste avec le froid)
            drawCircle(Color(0xFFFF6600).copy(alpha = 0.2f), radius = 9f, center = Offset(bx, by))
        } else {
            drawCircle(SBullet, radius = 3f, center = Offset(bx, by))
            drawLine(SBullet.copy(alpha = 0.4f), Offset(bx - 10f, by), Offset(bx, by), 1.5f)
        }
    }

    // Zombies — forme différente par type
    game.zombies.filter { it.alive }.forEach { z ->
        val zx    = z.x * w
        val zy    = z.y * h
        val color = if (z.hitFlash > 0) Color.White else when (z.type) {
            ZombieType.SOLDAT    -> SEnemy
            ZombieType.AUTOMATE  -> Color(0xFF6B8CFF)  // bleu
            ZombieType.CENTURION -> Color(0xFFB86BFF)  // violet
            ZombieType.BOSS      -> SBoss              // rouge vif
        }

        when (z.type) {
            ZombieType.SOLDAT -> {
                // Cadavre animé — corps traînant, tête penchée
                val sz = 10f
                // Corps décharné
                drawRect(color.copy(alpha = 0.2f), Offset(zx - sz/2, zy - sz * 0.8f), Size(sz, sz * 1.8f))
                drawRect(color, Offset(zx - sz/2, zy - sz * 0.8f), Size(sz, sz * 1.8f), style = Stroke(1.5f))
                // Tête penchée (légèrement décalée — mort)
                drawCircle(color.copy(alpha = 0.25f), sz * 0.55f, Offset(zx + 2f, zy - sz * 1.2f))
                drawCircle(color, sz * 0.55f, Offset(zx + 2f, zy - sz * 1.2f), style = Stroke(1f))
                // Yeux creux rouges
                drawCircle(SBlood, 1.5f, Offset(zx - 1f, zy - sz * 1.25f))
                drawCircle(SBlood, 1.5f, Offset(zx + 4f, zy - sz * 1.25f))
                // Bras traînant
                drawLine(color, Offset(zx - sz/2, zy - sz * 0.4f), Offset(zx - sz/2 - 5f, zy + 3f), 1.5f)
            }
            ZombieType.AUTOMATE -> {
                // Cadavre en armure rouillée — reste d'un soldat
                val sz = 10f
                drawRect(color.copy(alpha = 0.2f), Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f))
                drawRect(color, Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f), style = Stroke(1.5f))
                // Casque fendu
                drawRect(color.copy(alpha = 0.3f), Offset(zx - sz/2 + 1f, zy - sz * 1.6f), Size(sz - 2f, sz * 0.7f))
                drawRect(color, Offset(zx - sz/2 + 1f, zy - sz * 1.6f), Size(sz - 2f, sz * 0.7f), style = Stroke(1f))
                // Fissure dans le casque
                drawLine(SBlood, Offset(zx, zy - sz * 1.6f), Offset(zx + 2f, zy - sz * 0.95f), 1.5f)
                // Yeux rougeoyants
                drawCircle(SBlood.copy(alpha = 0.9f), 2f, Offset(zx - 2f, zy - sz * 1.25f))
                drawCircle(SBlood.copy(alpha = 0.9f), 2f, Offset(zx + 3f, zy - sz * 1.25f))
            }
            ZombieType.CENTURION -> {
                // Cadavre massif — en décomposition avancée
                val sz = 12f
                drawRect(color.copy(alpha = 0.2f), Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f))
                drawRect(color, Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f), style = Stroke(2f))
                // Tête crâne visible
                drawCircle(color.copy(alpha = 0.25f), sz * 0.5f, Offset(zx, zy - sz * 1.3f))
                drawCircle(color, sz * 0.5f, Offset(zx, zy - sz * 1.3f), style = Stroke(1.5f))
                // Orbites vides
                drawCircle(Color(0xFF000000).copy(alpha = 0.8f), 2.5f, Offset(zx - 3f, zy - sz * 1.35f))
                drawCircle(Color(0xFF000000).copy(alpha = 0.8f), 2.5f, Offset(zx + 3f, zy - sz * 1.35f))
                // Lueur putride dans les orbites
                drawCircle(SPutrid.copy(alpha = 0.7f), 1.5f, Offset(zx - 3f, zy - sz * 1.35f))
                drawCircle(SPutrid.copy(alpha = 0.7f), 1.5f, Offset(zx + 3f, zy - sz * 1.35f))
                // Taches putrides
                drawCircle(SPutrid.copy(alpha = 0.3f), 3f, Offset(zx + 3f, zy))
                drawCircle(SPutrid.copy(alpha = 0.25f), 2f, Offset(zx - 4f, zy + 4f))
            }
            ZombieType.BOSS -> {
                // Colosse de chair — cadavre géant recousu
                val sz = 16f
                drawRect(color.copy(alpha = 0.2f), Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f))
                drawRect(color, Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f), style = Stroke(2.5f))
                // Tête déformée
                drawCircle(color.copy(alpha = 0.25f), sz * 0.55f, Offset(zx, zy - sz * 1.3f))
                drawCircle(color, sz * 0.55f, Offset(zx, zy - sz * 1.3f), style = Stroke(2f))
                // Yeux rouges brillants
                drawCircle(SBlood, 3.5f, Offset(zx - 4f, zy - sz * 1.35f))
                drawCircle(SBlood, 3.5f, Offset(zx + 4f, zy - sz * 1.35f))
                drawCircle(Color.White.copy(alpha = 0.6f), 1.5f, Offset(zx - 4f, zy - sz * 1.35f))
                drawCircle(Color.White.copy(alpha = 0.6f), 1.5f, Offset(zx + 4f, zy - sz * 1.35f))
                // Cicatrices / sutures
                drawLine(SBlood.copy(alpha = 0.6f), Offset(zx - sz/2, zy - 4f), Offset(zx + sz/2, zy - 4f), 1.5f)
                drawLine(SBlood.copy(alpha = 0.4f), Offset(zx - sz/2, zy + 4f), Offset(zx + sz/2, zy + 4f), 1f)
                drawLine(SBlood.copy(alpha = 0.5f), Offset(zx, zy - sz), Offset(zx, zy + sz), 1f)
                // Bras épais
                drawLine(color, Offset(zx - sz/2, zy - sz * 0.3f), Offset(zx - sz/2 - 8f, zy + 4f), 3f)
                drawLine(color, Offset(zx + sz/2, zy - sz * 0.3f), Offset(zx + sz/2 + 8f, zy + 4f), 3f)
            }
        }

        // Barre de vie commune (sauf BOSS qui a sa propre barre)
        if (z.type.hp > 1 && z.type != ZombieType.BOSS) {
            val barW = 20f
            val hpR  = z.hp.toFloat() / z.type.hp
            val barY = zy - 14f
            drawRect(Color(0xFF1A1A1A), Offset(zx - barW/2, barY), Size(barW, 3f))
            drawRect(if (hpR > 0.5f) STeal else SRed, Offset(zx - barW/2, barY), Size(barW * hpR, 3f))
        }
    }

    // Grappin — toujours visible quand actif
    val g = game.grapple
    if (g.active) {
        val gx = g.x * w; val gy = g.y * h
        val px = w * 0.08f; val py = game.playerY * h
        drawLine(Color(0xFFC8A44A).copy(alpha = 0.8f), Offset(px, py), Offset(gx, gy), 1.5f)
        drawCircle(Color(0xFFFFD700), 4f, Offset(gx, gy))
        drawCircle(Color(0xFFC8A44A), 4f, Offset(gx, gy), style = Stroke(1.5f))
    }

    // Pièces Igni Terra
    game.parts.filter { it.alive }.forEach { p ->
        val px = p.x * w; val py = p.y * h
        drawCircle(Color(0xFFC8A44A).copy(alpha = 0.9f), 5f, Offset(px, py))
        drawCircle(Color(0xFFFFD700), 5f, Offset(px, py), style = Stroke(1.5f))
        drawLine(Color(0xFFFFD700).copy(alpha = 0.8f), Offset(px - 3f, py), Offset(px + 3f, py), 1.5f)
        drawLine(Color(0xFFFFD700).copy(alpha = 0.8f), Offset(px, py - 3f), Offset(px, py + 3f), 1.5f)
    }

    // Missiles
    game.missiles.filter { it.alive }.forEach { m ->
        val mx = m.x * w; val my = m.y * h
        // Fusée allongée
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(mx + 12f, my)
            lineTo(mx, my - 4f)
            lineTo(mx - 4f, my)
            lineTo(mx, my + 4f)
            close()
        }
        drawPath(path, Color(0xFFFF6B00))
        drawPath(path, Color(0xFFFFAA00), style = Stroke(1f))
        // Flamme arrière
        drawLine(Color(0xFFFFD700).copy(alpha = 0.7f), Offset(mx - 4f, my), Offset(mx - 12f, my), 2f)
    }

    // Boss final
    game.finalBoss?.let { boss ->
        if (!boss.alive) return@let
        val bx = boss.x * w; val by = boss.y * h
        val color = if (boss.hitFlash > 0) Color.White
        else if (boss.phase == 1) Color(0xFFFF2200) else Color(0xFF8B0000)
        val sz = 40f

        // Corps massif irrégulier
        val bodyPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(bx - sz * 0.4f, by - sz * 0.8f)
            lineTo(bx + sz * 0.5f, by - sz * 0.6f)
            lineTo(bx + sz * 0.6f, by)
            lineTo(bx + sz * 0.5f, by + sz * 0.6f)
            lineTo(bx - sz * 0.3f, by + sz * 0.8f)
            lineTo(bx - sz * 0.6f, by + sz * 0.3f)
            lineTo(bx - sz * 0.6f, by - sz * 0.3f)
            close()
        }
        drawPath(bodyPath, color.copy(alpha = 0.3f))
        drawPath(bodyPath, color, style = Stroke(3f))

        // Yeux rouges menaçants
        drawCircle(Color(0xFFFF0000), 6f, Offset(bx - 10f, by - 10f))
        drawCircle(Color(0xFFFF0000), 6f, Offset(bx + 10f, by - 10f))
        drawCircle(Color.White, 2f, Offset(bx - 10f, by - 10f))
        drawCircle(Color.White, 2f, Offset(bx + 10f, by - 10f))

        // Bouche grimaçante
        val mouthPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(bx - 12f, by + 8f)
            lineTo(bx - 6f,  by + 14f)
            lineTo(bx,       by + 8f)
            lineTo(bx + 6f,  by + 14f)
            lineTo(bx + 12f, by + 8f)
        }
        drawPath(mouthPath, Color(0xFFFF4444), style = Stroke(2f))

        // Cornes
        drawLine(color, Offset(bx - 15f, by - sz * 0.8f), Offset(bx - 20f, by - sz * 1.1f), 3f)
        drawLine(color, Offset(bx + 15f, by - sz * 0.8f), Offset(bx + 20f, by - sz * 1.1f), 3f)

        // Barre de vie boss
        val barW = 80f
        val hpR  = boss.hp.toFloat() / boss.maxHp.toFloat()
        drawRect(Color(0xFF1A1A1A), Offset(bx - barW/2, by - sz - 20f), Size(barW, 6f))
        drawRect(if (hpR > 0.5f) Color(0xFFFF4444) else Color(0xFFFF0000),
            Offset(bx - barW/2, by - sz - 20f), Size(barW * hpR, 6f))
        // Label
        drawRect(Color(0xFF8B0000).copy(alpha = 0.6f),
            Offset(bx - barW/2 - 2f, by - sz - 30f), Size(barW + 4f, 12f))

        // Bouclier — ligne verticale bleue avec effet de dispersion
        // Bouclier toujours visible — γ le traverse mais ne le supprime pas
        if (true) {
            val shieldX = bx - sz * 0.7f
            val shieldAlpha = if (boss.shieldFlash > 0)
                (boss.shieldFlash / 12f).coerceIn(0f, 1f)
            else 0.25f
            val shieldH = sz * 2.8f

            // Halo du bouclier
            drawRect(
                Color(0xFF4488FF).copy(alpha = shieldAlpha * 0.15f),
                Offset(shieldX - 10f, by - shieldH / 2),
                Size(20f, shieldH)
            )
            // Ligne principale
            drawLine(
                Color(0xFF88AAFF).copy(alpha = shieldAlpha * 0.9f),
                Offset(shieldX, by - shieldH / 2),
                Offset(shieldX, by + shieldH / 2),
                3f
            )
            // Lignes secondaires ondulées
            val waveAmp = if (boss.shieldFlash > 0) 6f else 2f
            for (seg in 0..8) {
                val y1 = by - shieldH / 2 + seg * shieldH / 8
                val y2 = by - shieldH / 2 + (seg + 1) * shieldH / 8
                val xOff1 = kotlin.math.sin(seg * 1.2 + tick * 0.15).toFloat() * waveAmp
                val xOff2 = kotlin.math.sin((seg + 1) * 1.2 + tick * 0.15).toFloat() * waveAmp
                drawLine(
                    Color(0xFF4488FF).copy(alpha = shieldAlpha * 0.5f),
                    Offset(shieldX + xOff1, y1),
                    Offset(shieldX + xOff2, y2),
                    1.5f
                )
            }
            // Particules de dispersion quand un tir est bloqué
            if (boss.shieldFlash > 0) {
                val sparkAlpha = shieldAlpha
                for (j in 0..5) {
                    val angle = (j * 60f + tick * 8f) * (kotlin.math.PI / 180f)
                    val dist  = (12 - boss.shieldFlash) * 2f
                    val sx = shieldX + dist * kotlin.math.cos(angle).toFloat()
                    val sy = by + (j - 2.5f) * 12f + dist * kotlin.math.sin(angle).toFloat() * 0.3f
                    drawCircle(Color(0xFF88CCFF).copy(alpha = sparkAlpha * 0.8f), 2.5f, Offset(sx, sy))
                }
            }
        }
    }

    // Petite fille
    game.civilians.filter { it.alive && !it.saved }.forEach { c ->
        val cx = c.x * w
        val cy = c.y * h
        val skin  = Color(0xFFFFD5B0)
        val dress = Color(0xFF4A90D9)   // robe bleue (contrast Coerthas)
        val hair  = Color(0xFFCC4400)   // roux vif

        // Nattes — deux petits cercles de chaque côté de la tête
        drawCircle(hair, 3f, Offset(cx - 6f, cy - 12f))
        drawCircle(hair, 3f, Offset(cx + 6f, cy - 12f))
        // Tête — plus grosse proportionnellement (enfant)
        drawCircle(skin, 7f, Offset(cx, cy - 10f))
        drawCircle(hair, 7f, Offset(cx, cy - 14f))  // cheveux sur le dessus
        // Yeux
        drawCircle(Color(0xFF333333), 1f, Offset(cx - 2.5f, cy - 11f))
        drawCircle(Color(0xFF333333), 1f, Offset(cx + 2.5f, cy - 11f))
        // Corps court
        drawLine(skin, Offset(cx, cy - 3f), Offset(cx, cy + 4f), 2.5f)
        // Petits bras levés (enfant qui court)
        drawLine(skin, Offset(cx, cy - 1f), Offset(cx - 6f, cy - 5f), 1.5f)
        drawLine(skin, Offset(cx, cy - 1f), Offset(cx + 6f, cy - 5f), 1.5f)
        // Robe évasée
        val robe = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - 2f, cy - 2f)
            lineTo(cx - 8f, cy + 7f)
            lineTo(cx + 8f, cy + 7f)
            lineTo(cx + 2f, cy - 2f)
            close()
        }
        drawPath(robe, dress)
        drawPath(robe, dress.copy(alpha = 0.6f), style = Stroke(1f))
        // Petites jambes
        drawLine(skin, Offset(cx - 2f, cy + 7f), Offset(cx - 4f, cy + 14f), 1.5f)
        drawLine(skin, Offset(cx + 2f, cy + 7f), Offset(cx + 4f, cy + 14f), 1.5f)
        // Petites chaussures
        drawCircle(Color(0xFF333333), 2f, Offset(cx - 4f, cy + 15f))
        drawCircle(Color(0xFF333333), 2f, Offset(cx + 4f, cy + 15f))
        // Indicateur ⚠ pulsant
        drawCircle(Color(0xFFFFD700).copy(alpha = 0.9f), 3f, Offset(cx, cy - 22f))
    }

    // Explosions — sang et putréfaction
    game.explosions.forEach { e ->
        val ex = e.x * w; val ey = e.y * h
        val alpha = (e.frames / 8f).coerceIn(0f, 1f)
        val r = (8 - e.frames) * 4f + 4f
        // Halo sanguin
        drawCircle(SBlood.copy(alpha = alpha * 0.4f), r * 1.4f, Offset(ex, ey))
        drawCircle(SExplosion.copy(alpha = alpha * 0.7f), r, Offset(ex, ey))
        // Matière putride au centre
        drawCircle(SPutrid.copy(alpha = alpha * 0.5f), r * 0.5f, Offset(ex, ey))
        // Éclaboussures
        for (j in 0..7) {
            val angle = (j * 45f) * (kotlin.math.PI / 180f)
            val dist  = r * (0.8f + (j % 3) * 0.3f)
            val ex2 = ex + dist * kotlin.math.cos(angle).toFloat()
            val ey2 = ey + dist * kotlin.math.sin(angle).toFloat()
            drawCircle(SBlood.copy(alpha = alpha * 0.5f), 2f, Offset(ex2, ey2))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun ShooterHud(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 6.sp, fontFamily = SMono, color = ST3, letterSpacing = 2.sp)
        Text(value, fontSize = 10.sp, fontFamily = SMono, color = STeal)
    }
}

@Composable
private fun ShooterBtn(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.border(1.dp, color.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = SMono, color = color)
    }
}