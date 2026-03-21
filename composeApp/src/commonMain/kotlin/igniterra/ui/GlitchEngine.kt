package igniterra.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Moteur de glitches visuels — adapté depuis Magitek Remote.
 * Déclenche des effets aléatoires à intervalle irrégulier.
 */
@Stable
class GlitchEngine {

    // ── États observables ──────────────────────────────────────────────────────

    /** Décalage horizontal des scanlines en dp */
    var scanlineShift by mutableStateOf(0f)
        private set

    /** Intensité du flash d'inversion (0f = normal, 1f = blanc complet) */
    var flashIntensity by mutableStateOf(0f)
        private set

    /** Tremblement global du contenu en dp */
    var contentShake by mutableStateOf(Pair(0f, 0f))
        private set

    /** Lignes de bruit horizontales — liste de paires (positionY 0..1, hauteur dp, opacité) */
    var noiseLines by mutableStateOf<List<Triple<Float, Float, Float>>>(emptyList())
        private set

    // ── Boucle automatique ────────────────────────────────────────────────────

    fun startLoop(scope: CoroutineScope) {
        scope.launch {
            while (true) {
                // Pause aléatoire entre deux glitches : 4 à 14 secondes
                delay(Random.nextLong(3_000L, 7_000L))
                triggerGlitch(scope)
            }
        }
    }

    private fun triggerGlitch(scope: CoroutineScope) {
        val effects = listOf(
            ::effectScanlineShift,
            ::effectFlash,
            ::effectContentShake,
            ::effectNoiseLines,
        ).shuffled().take(Random.nextInt(1, 4))

        effects.forEach { scope.launch { it() } }
    }

    // ── Glitch de navigation — court et sec ─────────────────────────────────────

    fun triggerNavGlitch(scope: CoroutineScope) {
        scope.launch { effectFlashNav() }
        scope.launch { effectScanlineNav() }
    }

    private suspend fun effectFlashNav() {
        flashIntensity = 0.08f
        delay(40L)
        flashIntensity = 0f
        delay(30L)
        flashIntensity = 0.04f
        delay(30L)
        flashIntensity = 0f
    }

    private suspend fun effectScanlineNav() {
        repeat(3) {
            scanlineShift = Random.nextFloat() * 8f - 4f
            delay(35L)
        }
        scanlineShift = 0f
        noiseLines = List(2) {
            Triple(Random.nextFloat(), Random.nextFloat() * 2f + 1f, 0.2f)
        }
        delay(60L)
        noiseLines = emptyList()
    }

    // ── Effets ────────────────────────────────────────────────────────────────

    private suspend fun effectScanlineShift() {
        val steps = Random.nextInt(4, 10)
        repeat(steps) {
            scanlineShift = (Random.nextFloat() * 12f - 6f)
            delay(Random.nextLong(30L, 80L))
        }
        scanlineShift = 0f
    }

    private suspend fun effectFlash() {
        val pulses = Random.nextInt(2, 5)
        repeat(pulses) {
            flashIntensity = Random.nextFloat() * 0.12f + 0.04f
            delay(Random.nextLong(25L, 60L))
            flashIntensity = 0f
            delay(Random.nextLong(20L, 50L))
        }
        flashIntensity = 0f
    }

    private suspend fun effectContentShake() {
        val steps = Random.nextInt(5, 12)
        repeat(steps) {
            contentShake = Pair(
                Random.nextFloat() * 6f - 3f,
                Random.nextFloat() * 3f - 1.5f
            )
            delay(Random.nextLong(25L, 70L))
        }
        contentShake = Pair(0f, 0f)
    }

    private suspend fun effectNoiseLines() {
        val count = Random.nextInt(2, 6)
        val iterations = Random.nextInt(3, 8)
        repeat(iterations) {
            noiseLines = List(count) {
                Triple(
                    Random.nextFloat(),                          // posY 0..1
                    Random.nextFloat() * 3f + 1f,               // hauteur dp
                    Random.nextFloat() * 0.35f + 0.1f           // opacité
                )
            }
            delay(Random.nextLong(40L, 100L))
        }
        noiseLines = emptyList()
    }
}
