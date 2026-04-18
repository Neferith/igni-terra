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

private val SBg       = Color(0xFF02050A)
private val SPanel    = Color(0xFF080F18)
private val SBdr      = Color(0xFF1A2535)
private val STeal     = Color(0xFF38C4C4)
private val SRed      = Color(0xFFC84040)
private val SGold     = Color(0xFFC8A44A)
private val ST3       = Color(0xFF4A5568)
private val SMono     = FontFamily.Monospace
private val SBullet   = Color(0xFFFFD700)
private val SBoss     = Color(0xFFFF4444)
private val SEnemy    = Color(0xFF8B4444)
private val SExplosion= Color(0xFFFF8C00)

// ── Overlay — structure identique à FpsView ───────────────────────────────────
@Composable
fun ShooterOverlay(onDismiss: () -> Unit) {
    val game           = remember { ShooterGame() }
    val scope          = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
        Column(
            Modifier.width(600.dp).background(SBg).border(1.dp, SRed.copy(alpha = 0.4f))
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
                ShooterHud("IGNI", when (game.igniLevel) {
                    0 -> "${game.igniParts}/5 → α"
                    1 -> "α ×2 | ${game.igniParts}/5 → β"
                    2 -> "β ×4 | ${game.igniParts}/5 → γ"
                    else -> "γ ×8 🔥"
                })
                Text("CLIC TIRER  ·  CLIC DROIT / LONG PRESS GRAPPIN", fontSize = 7.sp, fontFamily = SMono, color = ST3)
                if (game.message.isNotEmpty()) {
                    Text(game.message, fontSize = 9.sp, fontFamily = SMono, letterSpacing = 2.sp,
                        color = when { !game.alive -> SRed; game.won -> SGold; else -> STeal })
                }
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
                    Text("♥", fontSize = 10.sp, color = Color(0xFFFFB3C6))
                    Text("SAUVÉES : ${game.girlsSaved}", fontSize = 8.sp, fontFamily = SMono, color = Color(0xFFFFB3C6))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("✕", fontSize = 10.sp, color = SRed)
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
                                Text("♥ Sauvées : ${game.girlsSaved}", fontSize = 9.sp, fontFamily = SMono, color = Color(0xFFFFB3C6))
                                Text("✕ Perdues : ${game.girlsLost}", fontSize = 9.sp, fontFamily = SMono,
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
                            Text("APPUYEZ SUR DÉMARRER", fontSize = 10.sp, fontFamily = SMono,
                                color = STeal.copy(alpha = 0.6f), letterSpacing = 3.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("↑ / ↓  ou  W / S  —  déplacer", fontSize = 8.sp, fontFamily = SMono, color = ST3)
                            Text("ESPACE  —  tirer", fontSize = 8.sp, fontFamily = SMono, color = ST3)
                            Text("ÉCHAP  —  quitter", fontSize = 8.sp, fontFamily = SMono, color = ST3)
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(SBdr))

            // Légende
            Row(
                Modifier.fillMaxWidth().background(SPanel).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data class Legend(val label: String, val hp: Int, val pts: Int, val color: Color, val shape: String)
                listOf(
                    Legend("Soldat",    1,  10, Color(0xFF8B4444), "▭"),
                    Legend("Automate",  2,  20, Color(0xFF6B8CFF), "●"),
                    Legend("Centurion", 3,  35, Color(0xFFB86BFF), "◆"),
                    Legend("Légat",     8, 100, SBoss,             "✕"),
                ).forEach { l ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(l.shape, fontSize = 11.sp, color = l.color)
                        Text(l.label, fontSize = 7.sp, fontFamily = SMono, color = ST3)
                        Text("(${l.hp}PV +${l.pts})", fontSize = 6.sp, fontFamily = SMono, color = ST3.copy(alpha = 0.6f))
                    }
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
        CrackleSound.playWav("ninjagaiden.mp3", loop = true)
    }
    Box(Modifier.fillMaxSize().background(SBg), contentAlignment = Alignment.Center) {
        ShooterOverlay(onDismiss = { CrackleSound.stopWav(); onQuit() })
    }
}

// ── Rendu Canvas ──────────────────────────────────────────────────────────────

private fun DrawScope.drawShooterField(game: ShooterGame) {
    val w = size.width
    val h = size.height

    // Scanlines
    var scanY = 0f
    while (scanY < h) {
        drawLine(Color(0xFF0A1520), Offset(0f, scanY), Offset(w, scanY), 0.5f)
        scanY += h * 0.04f
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
            // Gerbe de feu — flamme orange/rouge
            drawCircle(Color(0xFFFF4500), radius = 5f, center = Offset(bx, by))
            drawCircle(Color(0xFFFFD700), radius = 2.5f, center = Offset(bx, by))
            drawLine(Color(0xFFFF6B00).copy(alpha = 0.6f), Offset(bx - 14f, by), Offset(bx, by), 3f)
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
                // Rectangle simple
                val sz = 10f
                drawRect(color.copy(alpha = 0.2f), Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f))
                drawRect(color, Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f), style = Stroke(1.5f))
            }
            ZombieType.AUTOMATE -> {
                // Cercle
                val r = 8f
                drawCircle(color.copy(alpha = 0.2f), r, Offset(zx, zy))
                drawCircle(color, r, Offset(zx, zy), style = Stroke(1.5f))
                // Antenne
                drawLine(color, Offset(zx, zy - r), Offset(zx, zy - r - 5f), 1.5f)
                drawCircle(color, 2f, Offset(zx, zy - r - 5f))
            }
            ZombieType.CENTURION -> {
                // Losange
                val sz = 10f
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(zx,      zy - sz * 1.4f)
                    lineTo(zx + sz, zy)
                    lineTo(zx,      zy + sz * 1.4f)
                    lineTo(zx - sz, zy)
                    close()
                }
                drawPath(path, color.copy(alpha = 0.2f))
                drawPath(path, color, style = Stroke(1.5f))
            }
            ZombieType.BOSS -> {
                // Croix / X
                val sz = 14f
                drawRect(color.copy(alpha = 0.15f), Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f))
                drawRect(color, Offset(zx - sz/2, zy - sz), Size(sz, sz * 2f), style = Stroke(2f))
                // Croix intérieure
                drawLine(color, Offset(zx - sz/2, zy - sz), Offset(zx + sz/2, zy + sz), 1.5f)
                drawLine(color, Offset(zx + sz/2, zy - sz), Offset(zx - sz/2, zy + sz), 1.5f)
            }
        }

        // Barre de vie commune
        if (z.type.hp > 1) {
            val barW = 20f
            val hpR  = z.hp.toFloat() / z.type.hp
            val barY = zy - (if (z.type == ZombieType.BOSS) 18f else 14f)
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
        val hpR  = boss.hp.toFloat() / 60f
        drawRect(Color(0xFF1A1A1A), Offset(bx - barW/2, by - sz - 20f), Size(barW, 6f))
        drawRect(if (hpR > 0.5f) Color(0xFFFF4444) else Color(0xFFFF0000),
            Offset(bx - barW/2, by - sz - 20f), Size(barW * hpR, 6f))
        // Label
        drawRect(Color(0xFF8B0000).copy(alpha = 0.6f),
            Offset(bx - barW/2 - 2f, by - sz - 30f), Size(barW + 4f, 12f))
    }

    // Petite fille
    game.civilians.filter { it.alive && !it.saved }.forEach { c ->
        val cx = c.x * w
        val cy = c.y * h
        val skin  = Color(0xFFFFD5B0)
        val dress = Color(0xFFFF80AB)
        val hair  = Color(0xFF8B4513)

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

    // Explosions
    game.explosions.forEach { e ->
        val ex = e.x * w; val ey = e.y * h
        val alpha = e.frames / 8f
        val r = (8 - e.frames) * 5f + 4f
        drawCircle(SExplosion.copy(alpha = alpha * 0.8f), r, Offset(ex, ey))
        drawCircle(Color.White.copy(alpha = alpha * 0.4f), r * 0.5f, Offset(ex, ey))
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