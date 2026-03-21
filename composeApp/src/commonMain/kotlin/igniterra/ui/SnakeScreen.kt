package igniterra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Palette Sharlayan ─────────────────────────────────────────────────────────
private val SBg      = Color(0xFF050E1A)
private val SPanel   = Color(0xFF0A1828)
private val STeal    = Color(0xFF38C4C4)
private val STealDk  = Color(0xFF1C7070)
private val ST1      = Color(0xFFD6EDF6)
private val ST2      = Color(0xFF6EA8C0)
private val ST3      = Color(0xFF365470)
private val SBdr     = Color(0xFF152640)
private val SRed     = Color(0xFFC84040)
private val SGold    = Color(0xFFC8A44A)
private val SMono    = FontFamily.Monospace

@Composable
fun SnakeOverlay(onDismiss: () -> Unit) {
    val game  = remember { SnakeGame() }
    val scope = rememberCoroutineScope()

    // Game loop
    LaunchedEffect(game.alive, game.started, game.demoMode) {
        if (!game.alive || !game.started) return@LaunchedEffect
        while (game.alive && game.started) {
            delay(if (game.demoMode) 100L else 150L)
            if (game.demoMode) game.demoTick() else game.tick()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .width(600.dp)
                .background(SBg)
                .border(1.dp, STealDk),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().background(SPanel).padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("IGNI TERRA", fontSize = 8.sp, letterSpacing = 4.sp, fontFamily = SMono, color = ST3)
                    Text("MODULE RÉCRÉATIF", fontSize = 10.sp, letterSpacing = 3.sp, fontFamily = SMono, color = STeal)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SCORE", fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = SMono, color = ST3)
                        Text("${game.score}", fontSize = 16.sp, fontFamily = SMono, fontWeight = FontWeight.W300, color = ST1)
                    }
                    Box(
                        Modifier
                            .border(1.dp, if (game.demoMode) STeal else SBdr)
                            .clickable { CrackleSound.click(); game.toggleDemo() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("DEMO", fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = SMono,
                            color = if (game.demoMode) STeal else ST3)
                    }
                    Box(
                        Modifier
                            .border(1.dp, SBdr)
                            .clickable { CrackleSound.click(); onDismiss() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("FERMER", fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = SMono, color = ST3)
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(SBdr))

            // Grille + D-pad côte à côte
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GameGrid(game)
                // D-pad + status à droite de la grille
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DPad(enabled = !game.demoMode) { dir ->
                        CrackleSound.click()
                        val head = game.snake.first()
                        val target = when (dir) {
                            Direction.UP    -> Cell(head.x, 0)
                            Direction.DOWN  -> Cell(head.x, game.rows - 1)
                            Direction.LEFT  -> Cell(0, head.y)
                            Direction.RIGHT -> Cell(game.cols - 1, head.y)
                        }
                        game.steer(target)
                    }
                    Text(
                        if (!game.started) "D-pad"
                        else if (!game.alive) "HORS LIGNE"
                        else "SCORE${game.score}",
                    fontSize = 8.sp, letterSpacing = 2.sp, fontFamily = SMono,
                    color = if (!game.alive) SRed else ST3,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    if (!game.alive) {
                        Box(
                            Modifier
                                .border(1.dp, STealDk)
                                .clickable { CrackleSound.click(); game.reset() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text("RESET", fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = SMono, color = STeal)
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(SBdr))
        }
    }
}

@Composable
private fun GameGrid(game: SnakeGame) {
    val cellSize = 20.dp

    Canvas(
        Modifier
            .size(cellSize * game.cols, cellSize * game.rows)
    ) {
        val cw = size.width / game.cols
        val ch = size.height / game.rows
        val pad = 1.5f

        // Grille de fond
        for (x in 0 until game.cols) {
            for (y in 0 until game.rows) {
                drawRect(
                    color    = SPanel,
                    topLeft  = Offset(x * cw + pad, y * ch + pad),
                    size     = Size(cw - pad * 2, ch - pad * 2)
                )
            }
        }

        // Lignes de grille discrètes
        for (x in 0..game.cols) {
            drawLine(SBdr, Offset(x * cw, 0f), Offset(x * cw, size.height), strokeWidth = 0.5f)
        }
        for (y in 0..game.rows) {
            drawLine(SBdr, Offset(0f, y * ch), Offset(size.width, y * ch), strokeWidth = 0.5f)
        }

        // Nourriture — croix teal
        val fx = game.food.x * cw + cw / 2
        val fy = game.food.y * ch + ch / 2
        val fr = cw * 0.25f
        drawLine(SGold, Offset(fx - fr, fy), Offset(fx + fr, fy), strokeWidth = 2f)
        drawLine(SGold, Offset(fx, fy - fr), Offset(fx, fy + fr), strokeWidth = 2f)
        drawRect(
            color   = SGold.copy(alpha = 0.15f),
            topLeft = Offset(game.food.x * cw + pad, game.food.y * ch + pad),
            size    = Size(cw - pad * 2, ch - pad * 2)
        )

        // Serpent
        game.snake.forEachIndexed { i, cell ->
            val isHead  = i == 0
            val alpha   = if (isHead) 1f else (1f - i.toFloat() / game.snake.size * 0.5f)
            val color   = if (isHead) STeal else STealDk.copy(alpha = alpha)
            drawRect(
                color    = color,
                topLeft  = Offset(cell.x * cw + pad, cell.y * ch + pad),
                size     = Size(cw - pad * 2, ch - pad * 2)
            )
            if (isHead) {
                drawRect(
                    color    = STeal.copy(alpha = 0.3f),
                    topLeft  = Offset(cell.x * cw + pad, cell.y * ch + pad),
                    size     = Size(cw - pad * 2, ch - pad * 2),
                    style    = Stroke(1.5f)
                )
            }
        }

        // Game over overlay
        if (!game.alive) {
            drawRect(Color(0x88000000))
        }
    }
}

// ── D-pad ─────────────────────────────────────────────────────────────────────
@Composable
private fun DPad(enabled: Boolean = true, onDirection: (Direction) -> Unit) {
    val btnSize = 28.dp
    val totalW  = btnSize * 3

    Column(
        modifier            = Modifier.width(totalW),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DPadButton(
            Modifier.size(btnSize).background(SPanel).border(1.dp, SBdr).clickable { onDirection(Direction.UP) }
        ) { ArrowCanvas(Direction.UP) }

        Row(Modifier.width(totalW)) {
            DPadButton(
                Modifier.size(btnSize).background(SPanel).border(1.dp, SBdr).clickable { onDirection(Direction.LEFT) }
            ) { ArrowCanvas(Direction.LEFT) }
            Box(Modifier.size(btnSize).background(Color(0xFF030810)).border(1.dp, SBdr))
            DPadButton(
                Modifier.size(btnSize).background(SPanel).border(1.dp, SBdr).clickable { onDirection(Direction.RIGHT) }
            ) { ArrowCanvas(Direction.RIGHT) }
        }

        DPadButton(
            Modifier.size(btnSize).background(SPanel).border(1.dp, SBdr).clickable { onDirection(Direction.DOWN) }
        ) { ArrowCanvas(Direction.DOWN) }
    }
}

@Composable
private fun ArrowCanvas(dir: Direction, color: Color = STeal) {
    Canvas(Modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path()
        when (dir) {
            Direction.UP -> {
                path.moveTo(w / 2, 0f)
                path.lineTo(w, h)
                path.lineTo(0f, h)
            }
            Direction.DOWN -> {
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w / 2, h)
            }
            Direction.LEFT -> {
                path.moveTo(0f, h / 2)
                path.lineTo(w, 0f)
                path.lineTo(w, h)
            }
            Direction.RIGHT -> {
                path.moveTo(w, h / 2)
                path.lineTo(0f, 0f)
                path.lineTo(0f, h)
            }
        }
        path.close()
        drawPath(path, color)
    }
}

@Composable
private fun DPadButton(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center, content = content)
}