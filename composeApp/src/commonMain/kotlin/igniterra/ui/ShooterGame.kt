package igniterra.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import igniterra.CrackleSound
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
    val id      : Int,
    var x       : Float,
    var y       : Float,
    var alive   : Boolean = true,
    val dirY    : Float   = 0f,    // déviation verticale (gerbe)
    val isFlame : Boolean = false,  // balle de feu
    val damage  : Int     = 1      // dégâts
)

data class Explosion(val x: Float, val y: Float, var frames: Int = 8)

data class Grapple(
    var active  : Boolean = false,
    var x       : Float   = 0f,
    var y       : Float   = 0f,
    var targetId: Int     = -1,  // id de la pièce visée
    var retracting: Boolean = false
)

data class DamageNumber(
    val x      : Float,
    val y      : Float,
    val amount : Int,
    var frames : Int = 30
)

data class IgniPart(
    val id   : Int,
    var x    : Float,
    var y    : Float,
    var alive: Boolean = true
)

data class Missile(
    val id   : Int,
    var x    : Float,
    var y    : Float,
    val dirY : Float,  // direction verticale (-1 haut, +1 bas, 0 droit)
    var alive: Boolean = true
)

data class FinalBoss(
    var hp       : Int   = 120,
    val maxHp    : Int   = 120,
    var x        : Float = 0.88f,
    var y        : Float = 0.5f,
    var alive    : Boolean = true,
    var hitFlash   : Int   = 0,
    var shieldFlash: Int   = 0,  // flash bouclier quand immunisé
    var phase    : Int   = 0   // 0=normal, 1=enragé (hp<30)
)

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
    val missiles   = mutableListOf<Missile>()
    val parts      = mutableListOf<IgniPart>()
    val dmgNumbers = mutableListOf<DamageNumber>()
    var igniParts  by mutableStateOf(0)  // pièces collectées
    var igniCharged by mutableStateOf(false)  // gerbe disponible
    var igniLevel   by mutableStateOf(0)      // 0=normal 1=α 2=β 3=γ
    var igniTotal   by mutableStateOf(0)      // total pièces collectées
    var grapple    = Grapple()
    var finalBoss  : FinalBoss? = null
    val bullets    = mutableListOf<Bullet>()
    val explosions = mutableListOf<Explosion>()

    var score     by mutableStateOf(0)
    var lives     by mutableStateOf(3)
    var wave      by mutableStateOf(0)
    var alive     by mutableStateOf(true)
    var won       by mutableStateOf(false)
    var started   by mutableStateOf(false)
    var message      by mutableStateOf("")
    var messageTimer  = 0
    var tickCount  by mutableStateOf(0)
    var badEnding   by mutableStateOf(false)
    var girlsSaved  by mutableStateOf(0)
    var girlsLost   by mutableStateOf(0)

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
        badEnding = false
        girlsSaved = 0
        girlsLost = 0
        // Clear tout sauf la charge igni
        zombies.clear(); bullets.clear(); explosions.clear()
        civilians.clear(); parts.clear(); missiles.clear()
        grapple = Grapple()
        finalBoss = null
        civilianMessage = ""; civilianMsgTimer = 0
        message = ""; messageTimer = 0; messagePriority = 0
        // Garde igniCharged, igniLevel, igniTotal, igniParts
        started = true
        spawnWave(0)
    }

    private fun spawnWave(waveIdx: Int) {
        wave = waveIdx + 1
        showMessage("VAGUE $wave", 120)
        spawnQueue = WAVES.getOrElse(waveIdx) { WAVES.last() }.toMutableList()
    }

    private var messagePriority = 0

    private fun showMessage(msg: String, frames: Int = 180, priority: Int = 0) {
        if (priority >= messagePriority) {
            message = msg
            messageTimer = frames
            messagePriority = priority
        }
    }

    fun moveUp()   { if (alive && started) playerY = (playerY - 0.04f).coerceAtLeast(0.05f) }
    fun moveDown() { if (alive && started) playerY = (playerY + 0.04f).coerceAtMost(0.95f) }

    fun shoot() {
        if (!alive || !started) return
        if (igniCharged) {
            val angles = when (igniLevel) {
                1    -> listOf(-0.15f, 0f, 0.15f)
                2    -> listOf(-0.20f, -0.10f, 0f, 0.10f, 0.20f)
                else -> listOf(-0.25f, -0.15f, -0.05f, 0.05f, 0.15f, 0.25f)
            }
            val dmg = 1 shl igniLevel  // α=2, β=4, γ=8
            angles.forEach { da ->
                bullets.add(Bullet(id = nextId++, x = 0.10f, y = playerY, dirY = da, isFlame = true, damage = dmg))
            }
        } else {
            bullets.add(Bullet(id = nextId++, x = 0.10f, y = playerY))
        }
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
                CrackleSound.shooterPlayerHit()
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
            b.y += b.dirY * 0.015f
            if (b.x > 1.0f || b.y < 0f || b.y > 1f) { b.alive = false; return@forEach }
            zombies.filter { it.alive }.forEach { z ->
                if (b.alive && abs(b.x - z.x) < 0.05f && abs(b.y - z.y) < 0.06f) {
                    z.hp -= b.damage; z.hitFlash = 4; b.alive = false
                    CrackleSound.shooterFleshHit()
                    dmgNumbers.add(DamageNumber(z.x, z.y, b.damage))
                    if (z.hp <= 0) {
                        z.alive = false
                        score += z.type.points
                        explosions.add(Explosion(z.x, z.y))
                        // Drop aléatoire de pièce Igni Terra
                        if (rng.nextFloat() < 0.35f) {
                            parts.add(IgniPart(id = nextId++, x = z.x, y = z.y))
                        }
                    }
                }
            }

        }
        deadBullets.forEach { bullets.remove(it) }

        // Explosions
        explosions.removeAll { e -> e.frames-- <= 0 }

        // Vague suivante
        if (spawnQueue.isEmpty() && zombies.none { it.alive }) {
            if (wave == WAVES.size && finalBoss == null) {
                val nextWave = wave
                scope.launch {
                    message = "LE LÉGAT SUPRÊME ARRIVE..."
                    delay(2000L)
                    wave = WAVES.size + 1
                    showMessage("VAGUE FINALE", 150, priority = 5)
                    finalBoss = FinalBoss()
                }
                return
            }
            if (wave > WAVES.size && finalBoss?.alive == false) {
                won = true; message = "VICTOIRE !"
            } else {
                val nextWave = wave
                scope.launch {
                    showMessage("VAGUE ${nextWave + 1} EN APPROCHE...", 120)
                    delay(2000L)
                    spawnWave(nextWave)
                }
            }
        }

        // Message timers
        if (civilianMsgTimer > 0) civilianMsgTimer--
        else civilianMessage = ""
        if (messageTimer > 0) messageTimer--
        else if (messageTimer == 0 && message.isNotEmpty() && alive && !won) { message = ""; messagePriority = 0 }

        // Spawn civilian aléatoire — uniquement si le jeu est en cours
        if (alive && !won && rng.nextFloat() < 0.002f && civilians.none { it.alive }) {
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
            // Sauvée — joueur sur son chemin
            if (c.x in 0.06f..0.16f && kotlin.math.abs(c.y - playerY) < 0.08f) {
                c.saved = true
                score += 50
                girlsSaved++
                CrackleSound.shooterGirlSaved()
                civilianMessage = "SAUVÉE ! +50"
                civilianMsgTimer = 120
                deadCivilians.add(c)
            }
            // Perdue — passe à gauche sans être sauvée
            else if (c.x <= 0.05f) {
                c.saved = false
                badEnding = true
                girlsLost++
                CrackleSound.shooterGirlKilled()
                civilianMessage = "UNE ENFANT EST PERDUE..."
                civilianMsgTimer = 180
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
                    girlsLost++
                    badEnding = true
                    CrackleSound.shooterGirlKilled()
                    civilianMessage = "TU L'AS TUÉE !"
                    civilianMsgTimer = 120
                    if (lives <= 0) { alive = false; message = "GAME OVER" }
                }
            }
        }

        // Final Boss
        finalBoss?.let { boss ->
            if (!boss.alive) return@let

            if (boss.hitFlash > 0) boss.hitFlash--
            if (boss.shieldFlash > 0) boss.shieldFlash--

            // Phase enragée si PV < 30
            boss.phase = if (boss.hp < 60) 1 else 0

            // Oscillation verticale
            boss.y += ((if (boss.phase == 0) 0.004f else 0.007f) * kotlin.math.sin(tickCount * 0.05)).toFloat()

            // Tir de missiles
            val fireRate = if (boss.phase == 0) 0.015f else 0.030f
            if (rng.nextFloat() < fireRate) {
                val targetY = playerY
                val dy = (targetY - boss.y).coerceIn(-0.5f, 0.5f)
                missiles.add(Missile(id = nextId++, x = boss.x - 0.05f, y = boss.y, dirY = dy * 0.8f))
            }

            // Lance une petite fille (phase enragée)
            if (alive && !won && boss.phase == 1 && rng.nextFloat() < 0.008f && civilians.none { it.alive }) {
                civilians.add(Civilian(x = boss.x - 0.05f, y = boss.y + (rng.nextFloat() - 0.5f) * 0.2f))
                showMessage("!! IL LANCE UNE ENFANT !!", 150, priority = 8)
            }

            // Collision balles joueur → boss (immunisé sans mode γ)
            bullets.filter { it.alive }.forEach { b ->
                if (b.alive && b.x > boss.x - 0.08f && abs(b.y - boss.y) < 0.12f) {
                    b.alive = false
                    if (!b.isFlame || igniLevel < 3) {
                        // Balle rejetée — boss immunisé
                        boss.shieldFlash = 12
                        showMessage("!! IGNI TERRA γ REQUIS !!", 100, priority = 10)
                        return@forEach
                    }
                    boss.hp -= b.damage
                    boss.hitFlash = 4
                    dmgNumbers.add(DamageNumber(boss.x, boss.y - 0.1f, b.damage))
                    if (boss.hp <= 0) {
                        boss.alive = false
                        score += 1000
                        message = "LÉGAT SUPRÊME VAINCU ! +500"
                        explosions.add(Explosion(boss.x, boss.y))
                        explosions.add(Explosion(boss.x - 0.05f, boss.y - 0.05f))
                        explosions.add(Explosion(boss.x + 0.05f, boss.y + 0.05f))
                    }
                }
            }
        }

        // Grappin
        val g = grapple
        if (g.active) {
            if (!g.retracting) {
                // Le grappin avance vers la droite
                g.x += 0.035f
                // Collision avec une pièce
                val target = parts.firstOrNull { it.alive && it.id == g.targetId }
                // Grappin attrape une civile en vol
                civilians.filter { it.alive && !it.saved }.forEach { c ->
                    if (kotlin.math.abs(g.x - c.x) < 0.05f && kotlin.math.abs(g.y - c.y) < 0.07f) {
                        c.saved = true
                        score += 50
                        girlsSaved++
                        CrackleSound.shooterGirlSaved()
                        lives = (lives + 1).coerceAtMost(9)
                        civilianMessage = "SAUVEE ! +50 +VIE"
                        civilianMsgTimer = 120
                    }
                }
                if (target != null && kotlin.math.abs(g.x - target.x) < 0.04f && kotlin.math.abs(g.y - target.y) < 0.06f) {
                    g.retracting = true
                } else if (g.x >= 0.55f || target == null) {
                    // Raté — atteint la moitié de l'écran, revient à vide
                    g.retracting = true
                    g.targetId = -1
                }
            } else {
                // Rétractation — la pièce suit le grappin vers le joueur
                g.x -= 0.04f
                val target = parts.firstOrNull { it.alive && it.id == g.targetId }
                if (target != null) {
                    target.x = g.x
                    target.y = g.y
                }
                if (g.x <= 0.10f) {
                    // Ramasse une pièce si c'est la cible
                    val picked = parts.firstOrNull { it.alive && it.id == g.targetId }
                    if (picked != null) {
                        picked.alive = false
                        CrackleSound.shooterPartPickup()
                        igniParts++
                        igniTotal++
                    }
                    // Ramasse une fille si le grappin passe près d'elle
                    civilians.filter { it.alive && !it.saved }.forEach { c ->
                        if (kotlin.math.abs(g.x - c.x) < 0.06f && kotlin.math.abs(g.y - c.y) < 0.08f) {
                            c.saved = true
                            score += 50
                            girlsSaved++
                            lives = (lives + 1).coerceAtMost(9)
                            civilianMessage = "SAUVEE ! +50 +VIE"
                            civilianMsgTimer = 120
                        }
                    }
                    if (igniParts >= 5) {
                        igniParts = 0
                        igniCharged = true
                        igniLevel = when {
                            igniTotal >= 15 -> { showMessage("MODE γ — PUISSANCE MAX !", 200); 3 }
                            igniTotal >= 10 -> { showMessage("MODE β DEBLOQUE !", 180); 2 }
                            else            -> { showMessage("MODE α DEBLOQUE !", 180); 1 }
                        }
                        CrackleSound.shooterLevelUp()
                    }
                    g.active = false
                }
            }
        }

        // Ramassage pièces Igni Terra
        val deadParts = mutableListOf<IgniPart>()
        parts.forEach { p ->
            if (!p.alive) { deadParts.add(p); return@forEach }
            if (kotlin.math.abs(p.x - 0.08f) < 0.05f && kotlin.math.abs(p.y - playerY) < 0.07f) {
                p.alive = false
                CrackleSound.shooterPartPickup()
                igniParts++
                igniTotal++
                if (igniParts >= 5) {
                    igniParts = 0
                    igniCharged = true
                    igniLevel = when {
                        igniTotal >= 15 -> { showMessage("MODE γ — PUISSANCE MAX !", 200); 3 }
                        igniTotal >= 10 -> { showMessage("MODE β DEBLOQUE !", 180); 2 }
                        else            -> { showMessage("MODE α DEBLOQUE !", 180); 1 }
                    }
                    CrackleSound.shooterLevelUp()
                }
            }
        }
        deadParts.forEach { parts.remove(it) }

        // Missiles
        val deadMissiles = mutableListOf<Missile>()
        missiles.forEach { m ->
            if (!m.alive) { deadMissiles.add(m); return@forEach }
            m.x -= 0.018f
            m.y += m.dirY * 0.02f
            if (m.x < 0f || m.y < 0f || m.y > 1f) { m.alive = false; return@forEach }
            // Touche le joueur
            if (abs(m.x - 0.08f) < 0.05f && abs(m.y - playerY) < 0.05f) {
                m.alive = false
                lives = (lives - 1).coerceAtLeast(0)
                explosions.add(Explosion(m.x, m.y))
                if (lives <= 0) { alive = false; message = "GAME OVER" }
            }
        }
        deadMissiles.forEach { missiles.remove(it) }

        // Damage numbers
        dmgNumbers.removeAll { d -> d.frames-- <= 0 }

        tickCount++
    }

    fun grapple() {
        if (!alive || !started || grapple.active) return
        // Lance toujours le grappin — cherche une cible si dispo
        val nearest = parts.filter { it.alive && it.x > 0.10f }
            .minByOrNull { kotlin.math.abs(it.y - playerY) + (it.x - 0.10f) * 0.5f }
        grapple.active     = true
        grapple.retracting = false
        grapple.x          = 0.12f
        grapple.y          = playerY
        grapple.targetId   = nearest?.id ?: -1  // -1 = pas de cible, va jusqu'à mi-écran
    }

    // Clic souris — déplace le joueur vers Y puis tire
    fun clickAt(x: Float, y: Float) {
        if (!alive || !started) return
        playerY = y.coerceIn(0.05f, 0.95f)
        shoot()
    }
}