package igniterra.ui

import androidx.compose.runtime.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import igniterra.CrackleSound
import kotlin.random.Random

// ── Types de base ─────────────────────────────────────────────────────────────

data class DungeonPos(val x: Int, val y: Int) {
    operator fun plus(other: DungeonPos) = DungeonPos(x + other.x, y + other.y)
}

enum class Tile { WALL, FLOOR, DOOR, STAIRS }

enum class EnemyType(
    val displayChar: String,
    val displayName: String,
    val hp: Int,
    val atk: Int,
    val def: Int,
    val xp: Int,
    val isBoss: Boolean = false
) {
    SOLDIER    ("S", "Soldat Magitek",      8,  3, 1, 10),
    AUTOMATON  ("A", "Automate de Garde",  14,  4, 2, 20),
    CENTURION  ("C", "Centurion Garlean",  20,  6, 3, 35),
    BOSS       ("X", "Valens van Varro",        60, 10, 4, 100, isBoss = true),
}

enum class ItemType(val displayChar: String, val displayName: String) {
    POTION   ("+", "Potion d'Éther"),
    CRYSTAL  ("*", "Cristal de Foudre"),
    ARMOR    ("^", "Armure Magitek"),
}

data class Enemy(
    val type: EnemyType,
    var pos : DungeonPos,
    var hp  : Int = type.hp,
    var alive: Boolean = true
)

data class Item(
    val type: ItemType,
    val pos : DungeonPos,
    var picked: Boolean = false
)

data class PlayerState(
    var pos   : DungeonPos,
    var hp    : Int    = 20,
    val maxHp : Int    = 20,
    var atk   : Int    = 4,
    var def   : Int    = 1,
    var xp    : Int    = 0,
    var level : Int    = 1,
    var gold  : Int    = 0
)

// ── Génération du donjon ──────────────────────────────────────────────────────

data class Room(val x: Int, val y: Int, val w: Int, val h: Int) {
    val cx get() = x + w / 2
    val cy get() = y + h / 2
    fun contains(px: Int, py: Int) = px in x until x + w && py in y until y + h
}

class DungeonMap(val width: Int = 40, val height: Int = 22) {
    val tiles = Array(height) { Array(width) { Tile.WALL } }
    val rooms = mutableListOf<Room>()

    fun tile(pos: DungeonPos) = tiles[pos.y.coerceIn(0, height - 1)][pos.x.coerceIn(0, width - 1)]
    fun setTile(pos: DungeonPos, t: Tile) {
        if (pos.y in 0 until height && pos.x in 0 until width) tiles[pos.y][pos.x] = t
    }
    fun isWalkable(pos: DungeonPos) = tile(pos) != Tile.WALL

    fun generate(rng: Random) {
        val attempts = 40
        repeat(attempts) {
            val w = rng.nextInt(4, 9)
            val h = rng.nextInt(3, 7)
            val x = rng.nextInt(1, width - w - 1)
            val y = rng.nextInt(1, height - h - 1)
            val room = Room(x, y, w, h)
            if (rooms.none { overlap(it, room) }) {
                carveRoom(room)
                if (rooms.isNotEmpty()) carveCorridor(rooms.last(), room)
                rooms.add(room)
            }
        }
        // Escalier dans la dernière salle
        rooms.lastOrNull()?.let { setTile(DungeonPos(it.cx, it.cy), Tile.STAIRS) }
    }

    private fun overlap(a: Room, b: Room) =
        a.x < b.x + b.w + 1 && a.x + a.w + 1 > b.x &&
                a.y < b.y + b.h + 1 && a.y + a.h + 1 > b.y

    private fun carveRoom(r: Room) {
        for (y in r.y until r.y + r.h)
            for (x in r.x until r.x + r.w)
                setTile(DungeonPos(x, y), Tile.FLOOR)
    }

    private fun carveCorridor(a: Room, b: Room) {
        var cx = a.cx; var cy = a.cy
        while (cx != b.cx) {
            setTile(DungeonPos(cx, cy), Tile.FLOOR)
            cx += if (b.cx > cx) 1 else -1
        }
        while (cy != b.cy) {
            setTile(DungeonPos(cx, cy), Tile.FLOOR)
            cy += if (b.cy > cy) 1 else -1
        }
    }
}

// ── Moteur de jeu ─────────────────────────────────────────────────────────────

@Stable
class DungeonGame {

    var map     by mutableStateOf(DungeonMap())
    var player  by mutableStateOf(PlayerState(DungeonPos(0, 0)))
    var enemies = mutableStateListOf<Enemy>()
    var items   = mutableStateListOf<Item>()
    var log     = mutableStateListOf<String>()
    var alive   by mutableStateOf(true)
    var won     by mutableStateOf(false)
    var floor   by mutableStateOf(1)
    var started by mutableStateOf(false)

    private val rng = Random.Default

    // ── Init ──────────────────────────────────────────────────────────────────

    fun newGame() {
        floor   = 1
        alive   = true
        won     = false
        started = true
        log.clear()
        player = PlayerState(DungeonPos(0, 0))
        generateFloor()
        log.add("Bienvenue, soldat. Bonne chance.")
    }

    fun generateFloor() {
        val m = DungeonMap()
        m.generate(rng)
        map = m
        enemies.clear()
        items.clear()

        val startRoom = m.rooms.firstOrNull() ?: return
        player = player.copy(pos = DungeonPos(startRoom.cx, startRoom.cy))

        // Ennemis
        val isBossFloor = floor >= 3
        m.rooms.drop(1).forEachIndexed { i, room ->
            if (isBossFloor && i == m.rooms.size - 2) {
                enemies.add(Enemy(EnemyType.BOSS, DungeonPos(room.cx, room.cy)))
            } else {
                val type = when {
                    floor == 1 -> EnemyType.SOLDIER
                    floor == 2 -> if (rng.nextBoolean()) EnemyType.SOLDIER else EnemyType.AUTOMATON
                    else       -> EnemyType.values().filter { !it.isBoss }.random(rng)
                }
                if (rng.nextFloat() > 0.3f)
                    enemies.add(Enemy(type, DungeonPos(room.cx, room.cy)))
            }
        }

        // Objets
        m.rooms.drop(1).forEach { room ->
            if (rng.nextFloat() < 0.4f) {
                val type = ItemType.values().random(rng)
                val px = room.x + rng.nextInt(1, room.w - 1)
                val py = room.y + rng.nextInt(1, room.h - 1)
                items.add(Item(type, DungeonPos(px, py)))
            }
        }

        log.add("-- Niveau $floor --")
    }

    // ── Mouvement ─────────────────────────────────────────────────────────────

    fun move(dx: Int, dy: Int) {
        if (!alive || !started) return
        val next = DungeonPos(player.pos.x + dx, player.pos.y + dy)
        if (!map.isWalkable(next)) return

        // Combat si ennemi sur la case
        val enemy = enemies.firstOrNull { it.alive && it.pos == next }
        if (enemy != null) { attack(enemy); return }

        player = player.copy(pos = next)

        // Ramasser objet
        items.filter { !it.picked && it.pos == next }.forEach { pickItem(it) }

        // Escalier
        if (map.tile(next) == Tile.STAIRS) {
            if (enemies.any { it.alive && it.type.isBoss }) {
                log.add("Le boss doit être vaincu avant de passer !")
            } else {
                floor++
                if (floor > 3) {
                    won = true
                    log.add("Victoire ! Vous avez conquis la citadelle !")
                    CrackleSound.dungeonVictory()
                } else {
                    log.add("Vous descendez au niveau $floor...")
                    generateFloor()
                }
            }
        }

        // Tour ennemis
        enemyTurn()
        trimLog()
    }

    // ── Combat ────────────────────────────────────────────────────────────────

    private fun attack(enemy: Enemy) {
        val dmg = max(1, player.atk - enemy.type.def + rng.nextInt(-1, 2))
        enemy.hp -= dmg
        log.add("Vous attaquez ${enemy.type.displayName} : $dmg dégâts.")
        CrackleSound.dungeonHit()
        if (enemy.hp <= 0) {
            enemy.alive = false
            player = player.copy(xp = player.xp + enemy.type.xp)
            log.add("${enemy.type.displayName} vaincu ! +${enemy.type.xp} XP")
            CrackleSound.dungeonEnemyDie()
            checkLevelUp()
            if (enemy.type.isBoss) {
                log.add("Le boss est tombé ! Trouvez l'escalier.")
            }
        } else {
            enemyAttack(enemy)
        }
        trimLog()
    }

    private fun enemyAttack(enemy: Enemy) {
        val dmg = max(1, enemy.type.atk - player.def + rng.nextInt(-1, 2))
        player = player.copy(hp = player.hp - dmg)
        log.add("${enemy.type.displayName} vous attaque : $dmg dégâts.")
        if (player.hp <= 0) {
            alive = false
            log.add("Vous êtes mort. Game over.")
            CrackleSound.dungeonGameOver()
        }
    }

    private fun enemyTurn() {
        enemies.filter { it.alive }.forEach { enemy ->
            val dp = enemy.pos
            val pp = player.pos
            val dist = abs(dp.x - pp.x) + abs(dp.y - pp.y)
            if (dist == 1) {
                enemyAttack(enemy)
            } else if (dist <= 6) {
                // Déplacement vers le joueur
                val dx = (pp.x - dp.x).coerceIn(-1, 1)
                val dy = (pp.y - dp.y).coerceIn(-1, 1)
                val next1 = DungeonPos(dp.x + dx, dp.y)
                val next2 = DungeonPos(dp.x, dp.y + dy)
                val target = listOf(next1, next2)
                    .filter { map.isWalkable(it) && enemies.none { e -> e.alive && e.pos == it } && it != player.pos }
                    .firstOrNull()
                if (target != null) enemy.pos = target
            }
        }
    }

    private fun pickItem(item: Item) {
        item.picked = true
        when (item.type) {
            ItemType.POTION  -> {
                val heal = 8
                player = player.copy(hp = min(player.maxHp, player.hp + heal))
                log.add("Potion d'Éther : +$heal PV.")
                CrackleSound.dungeonItemPickup()
            }
            ItemType.CRYSTAL -> {
                player = player.copy(atk = player.atk + 1)
                log.add("Cristal de Foudre : +1 ATK !")
                CrackleSound.dungeonItemPickup()
            }
            ItemType.ARMOR   -> {
                player = player.copy(def = player.def + 1)
                log.add("Armure Magitek : +1 DEF !")
                CrackleSound.dungeonItemPickup()
            }
        }
    }

    private fun checkLevelUp() {
        val threshold = player.level * 50
        if (player.xp >= threshold) {
            player = player.copy(
                level = player.level + 1,
                hp    = min(player.maxHp, player.hp + 5),
                atk   = player.atk + 1
            )
            log.add("Niveau ${player.level} ! +1 ATK, +5 PV.")
            CrackleSound.dungeonLevelUp()
        }
    }

    private fun trimLog() {
        while (log.size > 6) log.removeAt(0)
    }

    // ── Mode démo ─────────────────────────────────────────────────────────────

    var demoMode by mutableStateOf(false)
        private set

    fun toggleDemo() { demoMode = !demoMode }

    // ── A* Pathfinding ────────────────────────────────────────────────────────

    private fun heuristic(a: DungeonPos, b: DungeonPos) = abs(a.x - b.x) + abs(a.y - b.y)

    private fun findPath(from: DungeonPos, to: DungeonPos): List<DungeonPos> {
        data class Node(val pos: DungeonPos, val g: Int, val f: Int)

        val open     = mutableListOf(Node(from, 0, heuristic(from, to)))
        val closed   = mutableSetOf<DungeonPos>()
        val cameFrom = mutableMapOf<DungeonPos, DungeonPos>()
        val gScore   = mutableMapOf(from to 0)
        val dirs     = listOf(DungeonPos(0,-1), DungeonPos(0,1), DungeonPos(-1,0), DungeonPos(1,0))

        while (open.isNotEmpty()) {
            val current = open.minByOrNull { it.f } ?: break
            open.remove(current)
            if (current.pos == to) {
                val path = mutableListOf<DungeonPos>()
                var cur: DungeonPos? = to
                while (cur != null && cur != from) { path.add(0, cur); cur = cameFrom[cur] }
                return path
            }
            closed.add(current.pos)
            for (dir in dirs) {
                val next = current.pos + dir
                if (next in closed) continue
                if (!map.isWalkable(next) && next != to) continue
                val tentativeG = (gScore[current.pos] ?: Int.MAX_VALUE) + 1
                if (tentativeG < (gScore[next] ?: Int.MAX_VALUE)) {
                    cameFrom[next] = current.pos
                    gScore[next]   = tentativeG
                    open.removeAll { it.pos == next }
                    open.add(Node(next, tentativeG, tentativeG + heuristic(next, to)))
                }
            }
            if (closed.size > 800) break
        }
        return emptyList()
    }

    fun demoMove() {
        if (!alive || !started) return
        val p = player.pos

        // Priorités : potion si PV bas → boss → ennemis → objets → escalier
        val needsHeal = player.hp.toFloat() / player.maxHp < 0.4f
        val potion = if (needsHeal)
            items.filter { !it.picked && it.type == ItemType.POTION }
                .minByOrNull { heuristic(p, it.pos) } else null

        val target: DungeonPos = when {
            potion != null ->
                potion.pos
            enemies.any { it.alive && it.type.isBoss } ->
                enemies.filter { it.alive && it.type.isBoss }
                    .minByOrNull { heuristic(p, it.pos) }!!.pos
            enemies.any { it.alive } ->
                enemies.filter { it.alive }
                    .minByOrNull { heuristic(p, it.pos) }!!.pos
            items.any { !it.picked } ->
                items.filter { !it.picked }
                    .minByOrNull { heuristic(p, it.pos) }!!.pos
            else -> {
                // Cherche l'escalier
                var stairsPos: DungeonPos? = null
                outer@ for (y in 0 until map.height)
                    for (x in 0 until map.width)
                        if (map.tiles[y][x] == Tile.STAIRS) { stairsPos = DungeonPos(x, y); break@outer }
                stairsPos ?: return
            }
        }

        val path = findPath(p, target)
        if (path.isNotEmpty()) {
            val next = path.first()
            move(next.x - p.x, next.y - p.y)
        }
    }

}