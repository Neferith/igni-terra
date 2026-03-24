package igniterra.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.*
import igniterra.CrackleSound
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.math.roundToInt

// ── Palette FPS ───────────────────────────────────────────────────────────────
private val FBg      = Color(0xFF000000)
private val FCeiling = Color(0xFF0A0F18)
private val FFloor   = Color(0xFF050A10)
private val FWall1   = Color(0xFF1A2535)
private val FWall2   = Color(0xFF0D1520)
private val FEnemy   = Color(0xFFC84040)
private val FItem    = Color(0xFF68D391)
private val FBoss    = Color(0xFFFF4444)
private val FHud     = Color(0xFF0C1828)
private val FTeal    = Color(0xFF38C4C4)
private val FGold    = Color(0xFFC8A44A)
private val FRed     = Color(0xFFC84040)
private val FT1      = Color(0xFFE2E8F0)
private val FT3      = Color(0xFF4A5568)
private val FMono    = FontFamily.Monospace

// ── Raycaster ─────────────────────────────────────────────────────────────────

private const val FOV       = PI / 3.0   // 60°
private const val HALF_FOV  = FOV / 2.0
private const val NUM_RAYS  = 120        // colonnes de rendu
private const val MAX_DEPTH = 20.0

data class RayHit(
    val dist     : Double,
    val wallType : Tile,
    val side     : Int    // 0 = NS, 1 = EW
)

fun castRay(map: DungeonMap, px: Double, py: Double, angle: Double): RayHit {
    // DDA algorithm
    val rayDirX = cos(angle)
    val rayDirY = sin(angle)

    var mapX = px.toInt()
    var mapY = py.toInt()

    val deltaDistX = if (rayDirX == 0.0) Double.MAX_VALUE else abs(1.0 / rayDirX)
    val deltaDistY = if (rayDirY == 0.0) Double.MAX_VALUE else abs(1.0 / rayDirY)

    val stepX: Int
    val stepY: Int
    var sideDistX: Double
    var sideDistY: Double

    if (rayDirX < 0) { stepX = -1; sideDistX = (px - mapX) * deltaDistX }
    else             { stepX =  1; sideDistX = (mapX + 1.0 - px) * deltaDistX }

    if (rayDirY < 0) { stepY = -1; sideDistY = (py - mapY) * deltaDistY }
    else             { stepY =  1; sideDistY = (mapY + 1.0 - py) * deltaDistY }

    var side = 0
    var depth = 0

    while (depth < MAX_DEPTH) {
        if (sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0 }
        else                       { sideDistY += deltaDistY; mapY += stepY; side = 1 }

        if (mapX !in 0 until map.width || mapY !in 0 until map.height) break
        val tile = map.tiles[mapY][mapX]
        if (tile == Tile.WALL) {
            val dist = if (side == 0) sideDistX - deltaDistX else sideDistY - deltaDistY
            return RayHit(dist.coerceAtLeast(0.1), Tile.WALL, side)
        }
        depth++
    }
    return RayHit(MAX_DEPTH, Tile.FLOOR, 0)
}

// ── Sprite (ennemi/objet) dans la vue FPS ─────────────────────────────────────

data class Sprite(
    val x: Double, val y: Double,
    val color: Color, val label: String
)

// ── Composable principal ──────────────────────────────────────────────────────

@Composable
fun FpsView(
    game    : DungeonGame,
    onClose : () -> Unit
) {
    // Caméra
    var camX  by remember { mutableStateOf(game.player.pos.x + 0.5) }
    var camY  by remember { mutableStateOf(game.player.pos.y + 0.5) }
    var angle by remember { mutableStateOf(0.0) }
    val focusRequester = remember { FocusRequester() }
    val glitch = remember { GlitchEngine() }
    val scope  = rememberCoroutineScope()
    LaunchedEffect(Unit) { glitch.startLoop(scope) }

    // Sync caméra sur le joueur
    LaunchedEffect(game.player.pos) {
        camX = game.player.pos.x + 0.5
        camY = game.player.pos.y + 0.5
    }

    // Focus auto pour le clavier
    LaunchedEffect(Unit) { focusRequester.requestFocus() }


    LaunchedEffect(game.alive, game.started) {
        if (!game.alive || !game.started) return@LaunchedEffect
        while (game.alive && game.started) {
            delay(600L)
            // Tick passif : ennemis se rapprochent même sans action joueur
            game.passiveEnemyTick()
        }
    }

    // Game over → retour automatique après 3s
    LaunchedEffect(game.alive) {
        if (!game.alive) {
            delay(3000L)
            onClose()
        }
    }

    // Sprites — recalculés à chaque recomposition
    val sprites = buildList {
        game.enemies.forEach { e ->
            if (e.alive) add(Sprite(e.pos.x + 0.5, e.pos.y + 0.5,
                if (e.type.isBoss) FBoss else FEnemy, e.type.displayChar))
        }
        game.items.forEach { i ->
            if (!i.picked) add(Sprite(i.pos.x + 0.5, i.pos.y + 0.5, FItem, i.type.displayChar))
        }
        // Escalier visible comme sprite
        for (y in 0 until game.map.height)
            for (x in 0 until game.map.width)
                if (game.map.tiles[y][x] == Tile.STAIRS)
                    add(Sprite(x + 0.5, y + 0.5, FGold, ">"))
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xDD000000))
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val step = 0.15
                val rot  = 0.12
                when (event.key) {
                    Key.W, Key.DirectionUp -> {
                        val dx = cos(angle).roundToInt().coerceIn(-1, 1)
                        val dy = sin(angle).roundToInt().coerceIn(-1, 1)
                        val (nx, ny) = game.fpsMove(camX, camY, dx, dy)
                        camX = nx; camY = ny; CrackleSound.click(); true
                    }
                    Key.S, Key.DirectionDown -> {
                        val dx = (-cos(angle)).roundToInt().coerceIn(-1, 1)
                        val dy = (-sin(angle)).roundToInt().coerceIn(-1, 1)
                        val (nx, ny) = game.fpsMove(camX, camY, dx, dy)
                        camX = nx; camY = ny; CrackleSound.click(); true
                    }
                    Key.A, Key.DirectionLeft  -> { angle -= rot; true }
                    Key.D, Key.DirectionRight -> { angle += rot; true }
                    Key.Spacebar, Key.Enter   -> { game.fpsAttack(camX, camY, angle); CrackleSound.dungeonHit(); true }
                    Key.Escape -> { onClose(); true }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(520.dp).background(FBg).border(1.dp, FRed.copy(alpha = 0.4f))
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().background(FHud).padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VUE FPS — CITADELLE GARLEAN",
                    fontSize = 9.sp, letterSpacing = 2.sp, fontFamily = FMono, color = FRed)
                Box(
                    Modifier.border(1.dp, FT3).clickable { CrackleSound.click(); onClose() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("FERMER", fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = FMono, color = FT3)
                }
            }

            // Rendu raycasting
            Box(Modifier.fillMaxWidth().height(220.dp).background(FBg)) {
                RaycastCanvas(
                    map     = game.map,
                    camX    = camX, camY = camY, angle = angle,
                    sprites = sprites,
                    modifier = Modifier.fillMaxSize()
                )
                // Glitch overlay
                GlitchOverlay(glitch, Modifier.matchParentSize())
                // Game over / Victoire overlay
                if (!game.alive || game.won) {
                    Box(
                        Modifier.fillMaxSize()
                            .background(Color(if (!game.alive) 0xCC000000.toInt() else 0xCC000510.toInt())),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (!game.alive) "VOUS ÊTES MORT" else "VICTOIRE !",
                                fontSize = 18.sp, fontFamily = FMono, letterSpacing = 4.sp,
                                color = if (!game.alive) FRed else FGold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Retour dans 3s...", fontSize = 9.sp, fontFamily = FMono, color = FT3)
                        }
                    }
                }
                // Réticule
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(8.dp, 1.dp).background(FTeal.copy(alpha = 0.6f)))
                    Box(Modifier.size(1.dp, 8.dp).background(FTeal.copy(alpha = 0.6f)))
                }
            }

            // HUD
            Row(
                Modifier.fillMaxWidth().background(FHud).padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val p = game.player
                HudStat("PV",  "${p.hp}/${p.maxHp}", when {
                    p.hp <= p.maxHp/4 -> FRed
                    p.hp <= p.maxHp/2 -> FGold
                    else -> FTeal
                })
                HudStat("NIV", "${p.level}", FT1)
                HudStat("ATK", "${p.atk}", FT1)
                HudStat("DEF", "${p.def}", FT1)
                HudStat("XP",  "${p.xp}", FGold)
                HudStat("ÉTG", "${game.floor}/3", FT1)
            }

            // Contrôles FPS
            FpsControls(
                onAttack   = { game.fpsAttack(camX, camY, angle); CrackleSound.dungeonHit() },
                onForward  = { val dx = cos(angle).roundToInt().coerceIn(-1,1); val dy = sin(angle).roundToInt().coerceIn(-1,1); val (nx,ny) = game.fpsMove(camX, camY, dx, dy); camX = nx; camY = ny; CrackleSound.click() },
                onBackward = { val dx = (-cos(angle)).roundToInt().coerceIn(-1,1); val dy = (-sin(angle)).roundToInt().coerceIn(-1,1); val (nx,ny) = game.fpsMove(camX, camY, dx, dy); camX = nx; camY = ny; CrackleSound.click() },
                onLeft     = { angle -= 0.12; CrackleSound.click() },
                onRight    = { angle += 0.12; CrackleSound.click() }
            )

            // Log
            Box(
                Modifier.fillMaxWidth().background(FHud).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    game.log.lastOrNull() ?: "",
                    fontSize = 8.sp, fontFamily = FMono, color = FT3
                )
            }
        }
    }
}


// ── Rendu Canvas ──────────────────────────────────────────────────────────────

@Composable
private fun RaycastCanvas(
    map     : DungeonMap,
    camX    : Double,
    camY    : Double,
    angle   : Double,
    sprites : List<Sprite>,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val colW = w / NUM_RAYS

        // Plafond
        drawRect(FCeiling, Offset(0f, 0f), Size(w, h / 2))
        // Sol
        drawRect(FFloor, Offset(0f, h / 2), Size(w, h / 2))

        // Murs
        val zBuffer = DoubleArray(NUM_RAYS)
        for (ray in 0 until NUM_RAYS) {
            val rayAngle = angle - HALF_FOV + (ray.toDouble() / NUM_RAYS) * FOV
            val hit = castRay(map, camX, camY, rayAngle)
            zBuffer[ray] = hit.dist

            // Correction fish-eye
            val dist = hit.dist * cos(rayAngle - angle)
            val wallH = (h / dist).coerceAtMost(h.toDouble())
            val top    = (h - wallH) / 2f
            val bright = (1.0 - dist / MAX_DEPTH).coerceIn(0.0, 1.0).toFloat()
            val shade  = if (hit.side == 1) bright * 0.6f else bright
            val color  = if (hit.side == 1)
                FWall2.copy(alpha = shade)
            else
                FWall1.copy(alpha = shade)

            drawRect(
                color   = color,
                topLeft = Offset(ray * colW, top.toFloat()),
                size    = Size(colW + 1f, wallH.toFloat())
            )
        }

        // Sprites
        sprites.sortedByDescending { hypot(it.x - camX, it.y - camY) }.forEach { sprite ->
            val dx = sprite.x - camX
            val dy = sprite.y - camY
            val dist = hypot(dx, dy)
            if (dist < 0.5) return@forEach

            // Angle du sprite relatif à la caméra
            val spriteAngle = atan2(dy, dx) - angle
            val normalizedAngle = atan2(sin(spriteAngle), cos(spriteAngle))
            if (abs(normalizedAngle) > HALF_FOV + 0.2) return@forEach

            val screenX = ((normalizedAngle + HALF_FOV) / FOV * NUM_RAYS).toFloat()
            val spriteH = (h / dist).toFloat().coerceAtMost(h.toFloat())
            val spriteW = spriteH * 0.6f
            val top     = (h - spriteH) / 2f
            val left    = screenX * colW - spriteW / 2

            // Vérifie le z-buffer
            val rayIdx = screenX.toInt().coerceIn(0, NUM_RAYS - 1)
            if (dist >= zBuffer[rayIdx]) return@forEach

            val alpha = (1.0 - dist / MAX_DEPTH).coerceIn(0.2, 1.0).toFloat()
            drawRect(
                color   = sprite.color.copy(alpha = alpha * 0.8f),
                topLeft = Offset(left, top),
                size    = Size(spriteW, spriteH)
            )
        }
    }
}

// ── HUD ───────────────────────────────────────────────────────────────────────

@Composable
private fun HudStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 6.sp, fontFamily = FMono, color = FT3, letterSpacing = 1.sp)
        Text(value, fontSize = 9.sp, fontFamily = FMono, color = color)
    }
}

// ── Contrôles FPS ─────────────────────────────────────────────────────────────

@Composable
private fun FpsControls(
    onForward : () -> Unit,
    onBackward: () -> Unit,
    onLeft    : () -> Unit,
    onRight   : () -> Unit,
    onAttack  : () -> Unit
) {
    val btnSize = 36.dp

    fun btnMod(onClick: () -> Unit, color: Color = Color(0xFF0D1520)) = Modifier
        .size(btnSize)
        .background(color)
        .border(1.dp, Color(0xFF1A2535))
        .clickable(onClick = onClick)

    Row(
        Modifier.fillMaxWidth().background(FHud).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // D-pad mouvement
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(btnMod(onForward), contentAlignment = Alignment.Center) {
                DungeonArrow(0, -1, FTeal)
            }
            Row {
                Box(btnMod(onLeft), contentAlignment = Alignment.Center) {
                    RotateArrow(left = true, FTeal)
                }
                Spacer(Modifier.size(btnSize).background(FBg))
                Box(btnMod(onRight), contentAlignment = Alignment.Center) {
                    RotateArrow(left = false, FTeal)
                }
            }
            Box(btnMod(onBackward), contentAlignment = Alignment.Center) {
                DungeonArrow(0, 1, FTeal)
            }
        }
        // Bouton attaque
        Box(
            Modifier.size(60.dp)
                .background(FRed.copy(alpha = 0.15f))
                .border(1.dp, FRed.copy(alpha = 0.5f))
                .clickable(onClick = onAttack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ATK", fontSize = 8.sp, fontFamily = FMono, color = FRed, letterSpacing = 2.sp)
                Text("[SPC]", fontSize = 6.sp, fontFamily = FMono, color = FT3)
            }
        }
    }
}

@Composable
private fun RotateArrow(left: Boolean, color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(14.dp)) {
        val w = size.width; val h = size.height
        val path = androidx.compose.ui.graphics.Path()
        if (left) {
            path.moveTo(w * 0.2f, h / 2)
            path.lineTo(w * 0.8f, h * 0.1f)
            path.lineTo(w * 0.8f, h * 0.9f)
        } else {
            path.moveTo(w * 0.8f, h / 2)
            path.lineTo(w * 0.2f, h * 0.1f)
            path.lineTo(w * 0.2f, h * 0.9f)
        }
        path.close()
        drawPath(path, color)
    }
}