package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import kotlinx.coroutines.delay

// ── Palette Garlean ───────────────────────────────────────────────────────────
private val DBg     = Color(0xFF05080E)
private val DPanel  = Color(0xFF0A0F18)
private val DCard   = Color(0xFF0D1520)
private val DRed    = Color(0xFFC84040)
private val DGold   = Color(0xFFC8A44A)
private val DTeal   = Color(0xFF38C4C4)
private val DGray   = Color(0xFF4A5568)
private val DT1     = Color(0xFFE2E8F0)
private val DT2     = Color(0xFF90A0B0)
private val DT3     = Color(0xFF4A5568)
private val DBdr    = Color(0xFF1A2535)
private val DMono   = FontFamily.Monospace

// Couleurs des tuiles ASCII
private val colorWall   = Color(0xFF2D3748)
private val colorFloor  = Color(0xFF1A2535)
private val colorStairs = DGold
private val colorPlayer = DTeal
private val colorBoss   = DRed
private val colorEnemy  = Color(0xFFE53E3E)
private val colorItem   = Color(0xFF68D391)

// Viewport : nombre de cases affichées
private const val VIEW_W = 15
private const val VIEW_H = 9

@Composable
fun DungeonOverlay(traducterMode: Boolean,onDismiss: () -> Unit) {
    val game = remember { DungeonGame(traducterMode) }

    LaunchedEffect(Unit) { game.newGame() }

    var fpsMode by remember { mutableStateOf(false) }

    if (fpsMode) {
        FpsView(game = game, onClose = { fpsMode = false })
        return
    }



    Box(
        Modifier.fillMaxSize().background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .width(480.dp)
                .heightIn(max = 520.dp)
                .background(DBg)
                .border(1.dp, DRed.copy(alpha = 0.4f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            DungeonHeader(game, onDismiss, { fpsMode = it })
            Box(Modifier.fillMaxWidth().height(1.dp).background(DBdr))

            // Corps : carte + stats
         /*   Row(Modifier.fillMaxWidth()) {
                // Carte ASCII
                Box(Modifier.weight(1f).background(DBg).padding(8.dp)) {
                    DungeonMapView(game)
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(DBdr))
                // Panneau stats + log
                Column(Modifier.width(120.dp).background(DPanel).padding(10.dp)) {
                    StatsPanel(game)
                    Spacer(Modifier.height(10.dp))
                    LogPanel(game)
                }
            }*/
            Row(Modifier.fillMaxWidth().height(200.dp)) {
                Box(Modifier.weight(1f).background(DBg).padding(8.dp)) {
                    DungeonMapView(game)
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(DBdr))
                Column(
                    Modifier.width(120.dp).fillMaxHeight().background(DPanel).padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    StatsPanel(game)
                    LogPanel(game)
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(DBdr))

            // D-pad + statut
            DungeonFooter(game)
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun DungeonHeader(game: DungeonGame, onDismiss: () -> Unit, onFpsMode: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(DPanel).padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("CITADELLE GARLEAN", fontSize = 8.sp, letterSpacing = 3.sp, fontFamily = DMono, color = DT3)
            Text("PROTOCOLE D'INFILTRATION", fontSize = 11.sp, letterSpacing = 2.sp, fontFamily = DMono, color = DRed)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!game.alive || game.won) {
                DungeonBtn("REJOUER", DRed) { game.newGame() }
            }
            DungeonBtn("FPS", DTeal) { CrackleSound.click(); onFpsMode(true) }
            DungeonBtn(
                if (game.demoMode) "DEMO ON" else "DEMO",
                if (game.demoMode) DTeal else DGray
            ) { game.toggleDemo() }
            DungeonBtn("QUITTER", DGray) { CrackleSound.click(); onDismiss() }
        }
    }
}

// ── Carte ASCII ───────────────────────────────────────────────────────────────
@Composable
private fun DungeonMapView(game: DungeonGame) {
    if (!game.started) return

    val px = game.player.pos.x
    val py = game.player.pos.y
    val map = game.map

    // Centre le viewport sur le joueur
    val startX = (px - VIEW_W / 2).coerceIn(0, map.width - VIEW_W)
    val startY = (py - VIEW_H / 2).coerceIn(0, map.height - VIEW_H)

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        for (vy in 0 until VIEW_H) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                for (vx in 0 until VIEW_W) {
                    val wx = startX + vx
                    val wy = startY + vy
                    val worldPos = DungeonPos(wx, wy)

                    val enemy  = game.enemies.firstOrNull { it.alive && it.pos == worldPos }
                    val item   = game.items.firstOrNull { !it.picked && it.pos == worldPos }
                    val isPlayer = worldPos == game.player.pos

                    val (char, color) = when {
                        isPlayer -> "@" to colorPlayer
                        enemy != null -> enemy.type.displayChar to
                                if (enemy.type.isBoss) colorBoss else colorEnemy
                        item != null  -> item.type.displayChar to colorItem
                        else -> when (map.tile(worldPos)) {
                            Tile.WALL   -> "#" to colorWall
                            Tile.FLOOR  -> "." to colorFloor
                            Tile.STAIRS -> ">" to colorStairs
                            Tile.DOOR   -> "+" to DGold
                        }
                    }

                    Text(
                        char,
                        fontSize = 10.sp,
                        fontFamily = DMono,
                        color = color,
                        modifier = Modifier.size(12.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ── Stats ─────────────────────────────────────────────────────────────────────
@Composable
private fun StatsPanel(game: DungeonGame) {
    val p = game.player
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("STATUTS", fontSize = 7.sp, letterSpacing = 3.sp, fontFamily = DMono, color = DT3)
        Spacer(Modifier.height(2.dp))
        StatRow("NIV", "${p.level}")
        StatRow("PV", "${p.hp}/${p.maxHp}", color = when {
            p.hp <= p.maxHp / 4 -> DRed
            p.hp <= p.maxHp / 2 -> DGold
            else -> DTeal
        })
        StatRow("ATK", "${p.atk}")
        StatRow("DEF", "${p.def}")
        StatRow("XP", "${p.xp}")
        StatRow("ETAGE", "${game.floor}/3")
        Spacer(Modifier.height(4.dp))
        // Barre de vie
        val ratio = p.hp.toFloat() / p.maxHp
        Box(Modifier.fillMaxWidth().height(3.dp).background(DBdr)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(ratio).background(
                when { ratio <= 0.25f -> DRed; ratio <= 0.5f -> DGold; else -> DTeal }
            ))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color = DT1) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 8.sp, fontFamily = DMono, color = DT3)
        Text(value, fontSize = 8.sp, fontFamily = DMono, color = color, fontWeight = FontWeight.W500)
    }
}

// ── Log ───────────────────────────────────────────────────────────────────────
@Composable
private fun LogPanel(game: DungeonGame) {
    val scroll = rememberScrollState()
    LaunchedEffect(game.log.size) { scroll.animateScrollTo(scroll.maxValue) }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("JOURNAL", fontSize = 7.sp, letterSpacing = 3.sp, fontFamily = DMono, color = DT3)
        Spacer(Modifier.height(2.dp))
        Column(
            Modifier.verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            game.log.forEach { line ->
                Text(line, fontSize = 7.sp, fontFamily = DMono, color = DT2, lineHeight = 10.sp)
            }
        }
    }
}

// ── Footer : D-pad ────────────────────────────────────────────────────────────
@Composable
private fun DungeonFooter(game: DungeonGame) {

    // Boucle démo
    LaunchedEffect(game.demoMode, game.alive, game.started) {
        if (!game.demoMode || !game.alive || !game.started) return@LaunchedEffect
        while (game.demoMode && game.alive) {
            delay(300L)
            game.demoMove()
        }
    }

    val statusText = when {
        !game.started -> "Chargement..."
        !game.alive   -> "MORT AU COMBAT"
        game.won      -> "VICTOIRE !"
        else          -> "Utilisez le D-pad pour vous déplacer"
    }

    Row(
        Modifier.fillMaxWidth().padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // D-pad
        DungeonDPad(enabled = game.alive && !game.won) { dx, dy ->
            CrackleSound.click()
            game.move(dx, dy)
        }

        // Légende
        Column(
            Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(statusText, fontSize = 8.sp, fontFamily = DMono,
                color = when {
                    !game.alive -> DRed
                    game.won    -> DGold
                    else        -> DT3
                }
            )
            Spacer(Modifier.height(4.dp))
            LegendRow("@", colorPlayer, "Vous")
            LegendRow("S/A/C", colorEnemy, "Ennemis")
            LegendRow("X", colorBoss, "Boss")
            LegendRow("+*/^", colorItem, "Objets")
            LegendRow(">", colorStairs, "Escalier")
        }
    }
}

@Composable
private fun LegendRow(char: String, color: Color, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(char, fontSize = 8.sp, fontFamily = DMono, color = color, modifier = Modifier.width(20.dp))
        Text(label, fontSize = 7.sp, fontFamily = DMono, color = DT3)
    }
}

// ── D-pad Donjon ──────────────────────────────────────────────────────────────
@Composable
private fun DungeonDPad(enabled: Boolean, onMove: (Int, Int) -> Unit) {
    val btnSize = 28.dp
    val btnColor = if (enabled) DCard else DBg
    val arrowColor = if (enabled) DRed else DT3.copy(alpha = 0.3f)

    fun btnMod(dx: Int, dy: Int) = Modifier
        .size(btnSize)
        .background(btnColor)
        .border(1.dp, DBdr)
        .then(if (enabled) Modifier.clickable { onMove(dx, dy) } else Modifier)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(btnMod(0, -1), contentAlignment = Alignment.Center) { DungeonArrow(0, -1, arrowColor) }
        Row {
            Box(btnMod(-1, 0), contentAlignment = Alignment.Center) { DungeonArrow(-1, 0, arrowColor) }
            Spacer(Modifier.size(btnSize).background(DBg).border(1.dp, DBdr))
            Box(btnMod(1, 0), contentAlignment = Alignment.Center)  { DungeonArrow(1,  0, arrowColor) }
        }
        Box(btnMod(0, 1), contentAlignment = Alignment.Center)  { DungeonArrow(0,  1, arrowColor) }
    }
}

@Composable
fun DungeonArrow(dx: Int, dy: Int, color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(10.dp)) {
        val w = size.width; val h = size.height
        val path = androidx.compose.ui.graphics.Path()
        when {
            dy < 0 -> { path.moveTo(w/2, 0f); path.lineTo(w, h); path.lineTo(0f, h) }
            dy > 0 -> { path.moveTo(0f, 0f); path.lineTo(w, 0f); path.lineTo(w/2, h) }
            dx < 0 -> { path.moveTo(0f, h/2); path.lineTo(w, 0f); path.lineTo(w, h) }
            dx > 0 -> { path.moveTo(w, h/2); path.lineTo(0f, 0f); path.lineTo(0f, h) }
        }
        path.close()
        drawPath(path, color)
    }
}

@Composable
private fun DungeonBtn(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .border(1.dp, color.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = DMono, color = color)
    }
}