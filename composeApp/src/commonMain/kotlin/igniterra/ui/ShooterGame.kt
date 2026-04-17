package igniterra.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

enum class ZombieType(
    val char   : String,
    val label  : String,
    val hp     : Int,
    val speed  : Float,
    val points : Int
) {
    SOLDAT    ("S", "Soldat Zombifié",    1, 0.006f, 10),
    AUTOMATE  ("A", "Automate Corrompu",  2, 0.004f, 20),
    CENTURION ("C", "Centurion Mort",     3, 0.003f, 35),
    BOSS      ("X", "Légat Maudit",       8, 0.002f, 100),
}

data class Zombie(
    val id      : Int,
    val type    : ZombieType,
    var x       : Float,
    val y       : Float,
    var hp      : Int     = type.hp,
    var alive   : Boolean = true,
    var hitFlash: Int     = 0
)

data class Bullet(
    val id   : Int,
    var x    : Float,
    val y    : Float,
    var alive: Boolean = true
)

data class Explosion(val x: Float, val y: Float, var frames: Int = 8)

data class Civilian(
    var x       : Float = 1.05f,
    val y       : Float,
    var alive   : Boolean = true,
    var saved   : Boolean = false,
    var hitFlash: Int = 0
)


private val WAVES = listOf(
    listOf(ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT, ZombieType.SOLDAT),
    listOf(ZombieType.SOLDAT, ZombieType.AUTOMATE, ZombieType.SOLDAT, ZombieType.AUTOMATE, ZombieType.SOLDAT, ZombieType.AUTOMATE, ZombieType.SOLDAT, ZombieType.AUTOMATE, ZombieType.SOLDAT),
    listOf(ZombieType.AUTOMATE, ZombieType.CENTURION, ZombieType.AUTOMATE, ZombieType.CENTURION, ZombieType.AUTOMATE, ZombieType.CENTURION, ZombieType.AUTOMATE, ZombieType.CENTURION, ZombieType.AUTOMATE),
    listOf(ZombieType.SOLDAT, ZombieType.CENTURION, ZombieType.BOSS, ZombieType.CENTURION, ZombieType.SOLDAT, ZombieType.CENTURION, ZombieType.BOSS, ZombieType.CENTURION, ZombieType.SOLDAT),
    listOf(ZombieType.AUTOMATE, ZombieType.BOSS, ZombieType.CENTURION, ZombieType.BOSS, ZombieType.AUTOMATE, ZombieType.BOSS, ZombieType.CENTURION, ZombieType.BOSS, ZombieType.AUTOMATE),
)

class ShooterGame {

    val zombies    = mutableListOf<Zombie>()
    val civilians  = mutableListOf<Civilian>()
    val bullets    = mutableListOf<Bullet>()
    val explosions = mutableListOf<Explosion>()

    var score     by mutableStateOf(0)
    var lives     by mutableStateOf(3)
    var wave      by mutableStateOf(0)
    var alive     by mutableStateOf(true)
    var won       by mutableStateOf(false)
    var started   by mutableStateOf(false)
    var message   by mutableStateOf("")
    var tickCount by mutableStateOf(0)

    // Position du joueur (0.0 = haut, 1.0 = bas)
    var playerY        by mutableStateOf(0.5f)
    var civilianMessage by mutableStateOf("")  // "SAUVÉE !" ou "TU L'AS TUÉE !"
    var civilianMsgTimer = 0

    private var nextId              = 0
    private val rng        = Random.Default
    private var spawnQueue = mutableListOf<ZombieType>()

    fun start() {
        score = 0; lives = 3; wave = 0; alive = true; won = false
        playerY = 0.5f
        zombies.clear(); bullets.clear(); explosions.clear(); civilians.clear()
        civilianMessage = ""; civilianMsgTimer = 0
        started = true
        spawnWave(0)
    }

    private fun spawnWave(waveIdx: Int) {
        wave = waveIdx + 1
        message = "VAGUE $wave"
        spawnQueue = WAVES.getOrElse(waveIdx) { WAVES.last() }.toMutableList()
    }

    fun moveUp()   { if (alive && started) playerY = (playerY - 0.04f).coerceAtLeast(0.05f) }
    fun moveDown() { if (alive && started) playerY = (playerY + 0.04f).coerceAtMost(0.95f) }

    fun shoot() {
        if (!alive || !started) return
        bullets.add(Bullet(id = nextId++, x = 0.10f, y = playerY))
    }

    fun tick(scope: CoroutineScope) {
        if (!alive || !started) return

        // Spawn — max 8 zombies simultanés
        if (spawnQueue.isNotEmpty() && zombies.count { it.alive } < 8 && rng.nextFloat() < 0.012f) {
            val type = spawnQueue.removeAt(0)
            zombies.add(Zombie(
                id = nextId++, type = type,
                x  = 0.95f + rng.nextFloat() * 0.1f,
                y  = 0.10f + rng.nextFloat() * 0.80f,
            ))
        }

        // Zombies
        val deadZombies = mutableListOf<Zombie>()
        zombies.forEach { z ->
            if (!z.alive) { deadZombies.add(z); return@forEach }
            z.x -= z.type.speed
            if (z.hitFlash > 0) z.hitFlash--
            if (z.x <= 0.08f) {
                z.alive = false
                lives = (lives - 1).coerceAtLeast(0)
                explosions.add(Explosion(z.x, z.y))
                if (lives <= 0) { alive = false; message = "GAME OVER" }
            }
        }
        deadZombies.forEach { zombies.remove(it) }

        // Balles
        val deadBullets = mutableListOf<Bullet>()
        bullets.forEach { b ->
            if (!b.alive) { deadBullets.add(b); return@forEach }
            b.x += 0.030f
            if (b.x > 1.0f) { b.alive = false; return@forEach }
            zombies.filter { it.alive }.forEach { z ->
                if (b.alive && abs(b.x - z.x) < 0.05f && abs(b.y - z.y) < 0.06f) {
                    z.hp--; z.hitFlash = 4; b.alive = false
                    if (z.hp <= 0) {
                        z.alive = false
                        score += z.type.points
                        explosions.add(Explosion(z.x, z.y))
                    }
                }
            }

        }
        deadBullets.forEach { bullets.remove(it) }

        // Explosions
        explosions.removeAll { e -> e.frames-- <= 0 }

        // Vague suivante
        if (spawnQueue.isEmpty() && zombies.none { it.alive }) {
            if (wave >= WAVES.size) {
                won = true; message = "VICTOIRE !"
            } else {
                val nextWave = wave
                scope.launch {
                    message = "VAGUE ${nextWave + 1} EN APPROCHE..."
                    delay(2000L)
                    spawnWave(nextWave)
                }
            }
        }

        // Message timer
        if (civilianMsgTimer > 0) civilianMsgTimer--
        else civilianMessage = ""

        // Spawn civilian aléatoire (~toutes les 30s en moyenne)
        if (rng.nextFloat() < 0.0003f && civilians.none { it.alive }) {
            civilians.add(Civilian(
                x = 0.98f,
                y = 0.10f + rng.nextFloat() * 0.80f
            ))
        }

        // Mouvement civile
        val deadCivilians = mutableListOf<Civilian>()
        civilians.forEach { c ->
            if (!c.alive || c.saved) { deadCivilians.add(c); return@forEach }
            c.x -= 0.003f  // avance lentement
            if (c.x <= 0.08f) {
                c.saved = true
                score += 50
                civilianMessage = "SAUVÉE ! +50"
                civilianMsgTimer = 120
                deadCivilians.add(c)
            }
        }
        deadCivilians.forEach { civilians.remove(it) }

        // Collision balle — civile
        bullets.filter { it.alive }.forEach { b ->
            civilians.filter { it.alive }.forEach { c ->
                if (abs(b.x - c.x) < 0.04f && abs(b.y - c.y) < 0.05f) {
                    b.alive = false
                    c.alive = false
                    lives = (lives - 1).coerceAtLeast(0)
                    explosions.add(Explosion(c.x, c.y))
                    civilianMessage = "TU L'AS TUÉE !"
                    civilianMsgTimer = 120
                    if (lives <= 0) { alive = false; message = "GAME OVER" }
                }
            }
        }

        tickCount++
    }

    // Clic souris — déplace le joueur vers Y puis tire
    fun clickAt(x: Float, y: Float) {
        if (!alive || !started) return
        playerY = y.coerceIn(0.05f, 0.95f)
        shoot()
    }
}