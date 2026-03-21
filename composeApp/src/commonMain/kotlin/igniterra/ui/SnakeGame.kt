package igniterra.ui

import androidx.compose.runtime.*

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Cell(val x: Int, val y: Int)

@Stable
class SnakeGame(val cols: Int = 20, val rows: Int = 16) {

    var snake  by mutableStateOf(listOf(Cell(cols / 2, rows / 2)))
        private set
    var food   by mutableStateOf(randomFood(listOf(Cell(cols / 2, rows / 2))))
        private set
    var dir    by mutableStateOf(Direction.RIGHT)
        private set
    var score  by mutableStateOf(0)
        private set
    var alive  by mutableStateOf(true)
        private set
    var started by mutableStateOf(false)
        private set

    fun steer(target: Cell) {
        if (!alive) return
        val head = snake.first()
        val dx = target.x - head.x
        val dy = target.y - head.y
        val newDir = if (kotlin.math.abs(dx) >= kotlin.math.abs(dy)) {
            if (dx > 0) Direction.RIGHT else Direction.LEFT
        } else {
            if (dy > 0) Direction.DOWN else Direction.UP
        }
        // Prevent 180° reversal
        val forbidden = when (dir) {
            Direction.UP    -> Direction.DOWN
            Direction.DOWN  -> Direction.UP
            Direction.LEFT  -> Direction.RIGHT
            Direction.RIGHT -> Direction.LEFT
        }
        if (newDir != forbidden) dir = newDir
        if (!started) started = true
    }

    fun tick() {
        if (!alive || !started) return
        val head = snake.first()
        val next = when (dir) {
            Direction.UP    -> Cell(head.x, head.y - 1)
            Direction.DOWN  -> Cell(head.x, head.y + 1)
            Direction.LEFT  -> Cell(head.x - 1, head.y)
            Direction.RIGHT -> Cell(head.x + 1, head.y)
        }
        // Wall collision
        if (next.x !in 0 until cols || next.y !in 0 until rows) { alive = false; return }
        // Self collision
        if (next in snake) { alive = false; return }

        val ate = next == food
        snake = if (ate) listOf(next) + snake else listOf(next) + snake.dropLast(1)
        if (ate) {
            score += 10
            food = randomFood(snake)
        }
    }

    fun reset() {
        val start = Cell(cols / 2, rows / 2)
        snake   = listOf(start)
        food    = randomFood(listOf(start))
        dir     = Direction.RIGHT
        score   = 0
        alive   = true
        started = false
    }

    private fun randomFood(occupied: List<Cell>): Cell {
        val free = (0 until cols).flatMap { x -> (0 until rows).map { y -> Cell(x, y) } } - occupied.toSet()
        return if (free.isEmpty()) Cell(0, 0) else free.random()
    }
}