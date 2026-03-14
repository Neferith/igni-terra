package igniterra

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.exp
import kotlin.random.Random

/**
 * Sons procéduraux de l'application.
 *
 * - Bruit de fond : bruit brun + craquements aléatoires.
 * - Clic de navigation : impulsion électrique courte avec decay exponentiel.
 */
actual object CrackleSound {

    private const val SAMPLE_RATE  = 44100f
    private const val BUFFER_BYTES = 2048
    private const val BASE_VOLUME  = 0.018f
    private const val CRACKLE_PROB = 0.0015f
    private const val CRACKLE_AMP  = 0.22f

    // ── Bruit de fond ─────────────────────────────────────────────────────────

    @Volatile private var running = false

    actual fun start() {
        if (running) return
        running = true
        Thread(::bgLoop, "igniterra-crackle").also {
            it.isDaemon = true
            it.start()
        }
    }

    actual fun stop() {
        running = false
    }

    private fun bgLoop() {
        val format = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
        val line: SourceDataLine = AudioSystem.getSourceDataLine(format)
        line.open(format, BUFFER_BYTES * 2)
        line.start()

        val buf = ByteArray(BUFFER_BYTES)
        var brown      = 0.0
        var crackleLeft = 0
        var crackleAmp  = 0.0

        while (running) {
            for (i in buf.indices step 2) {
                brown = (brown + (Random.nextDouble() * 2.0 - 1.0) * 0.08).coerceIn(-1.0, 1.0)
                var sample = brown * BASE_VOLUME

                if (crackleLeft > 0) {
                    sample += crackleAmp * (Random.nextDouble() * 2.0 - 1.0)
                    crackleAmp *= 0.82
                    crackleLeft--
                } else if (Random.nextFloat() < CRACKLE_PROB) {
                    crackleLeft = Random.nextInt(8, 40)
                    crackleAmp  = (CRACKLE_AMP * (0.3f + Random.nextFloat() * 0.7f)).toDouble()
                }

                val s = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                buf[i]     = (s.toInt() and 0xFF).toByte()
                buf[i + 1] = (s.toInt() shr 8).toByte()
            }
            line.write(buf, 0, buf.size)
        }

        line.drain()
        line.close()
    }

    // ── Clic de navigation ────────────────────────────────────────────────────

    /**
     * Impulsion électrique courte : bruit blanc avec decay exponentiel rapide.
     * ~12ms, non bloquant (thread daemon dédié).
     *
     * CLICK_VOLUME : intensité du clic (0.0 – 1.0)
     * CLICK_DECAY  : vitesse de decay — plus élevé = plus court
     */
    private const val CLICK_VOLUME = 0.55f
    private const val CLICK_DECAY  = 180.0   // coefficient decay exponentiel

    actual fun click() {
        Thread({
            try {
                val format   = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
                val line     = AudioSystem.getSourceDataLine(format)
                val samples  = (SAMPLE_RATE * 0.012f).toInt()   // 12ms
                val buf      = ByteArray(samples * 2)

                line.open(format, buf.size)
                line.start()

                for (i in 0 until samples) {
                    val t      = i.toDouble() / SAMPLE_RATE
                    val env    = exp(-CLICK_DECAY * t)
                    val noise  = Random.nextDouble() * 2.0 - 1.0
                    val sample = (noise * env * CLICK_VOLUME * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    buf[i * 2]     = (sample.toInt() and 0xFF).toByte()
                    buf[i * 2 + 1] = (sample.toInt() shr 8).toByte()
                }

                line.write(buf, 0, buf.size)
                line.drain()
                line.close()
            } catch (_: Exception) {}
        }, "igniterra-click").also {
            it.isDaemon = true
            it.start()
        }
    }
}