package igniterra

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.exp
import kotlin.math.sin
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

    /**
     * Son d'ouverture du document — grave et solennel.
     * Bruit brun court + tonalité basse avec decay lent (~400ms).
     */
    actual fun openDocument() {
        Thread({
            try {
                val format  = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
                val line    = AudioSystem.getSourceDataLine(format)
                val samples = (SAMPLE_RATE * 0.4f).toInt()  // 400ms
                val buf     = ByteArray(samples * 2)

                line.open(format, buf.size * 2)
                line.start()

                for (i in 0 until samples) {
                    val t       = i.toDouble() / SAMPLE_RATE
                    // Ton grave à 80Hz avec decay lent
                    val tone    = kotlin.math.sin(2.0 * kotlin.math.PI * 80.0 * t) * exp(-3.0 * t)
                    // Bruit brun léger par-dessus
                    val noise   = (Random.nextDouble() * 2.0 - 1.0) * exp(-8.0 * t) * 0.3
                    val sample  = ((tone * 0.6 + noise) * 0.7 * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    buf[i * 2]     = (sample.toInt() and 0xFF).toByte()
                    buf[i * 2 + 1] = (sample.toInt() shr 8).toByte()
                }

                line.write(buf, 0, buf.size)
                line.drain()
                line.close()
            } catch (_: Exception) {}
        }, "igniterra-open").also {
            it.isDaemon = true
            it.start()
        }
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

    /**
     * Son de déverrouillage — montée rapide + double tonalité (220Hz puis 440Hz).
     * ~600ms total.
     */
    actual fun unlockSecret() {
        Thread({
            try {
                val format  = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
                val line    = AudioSystem.getSourceDataLine(format)
                val samples = (SAMPLE_RATE * 0.6f).toInt()
                val buf     = ByteArray(samples * 2)
                line.open(format, buf.size * 2)
                line.start()
                for (i in 0 until samples) {
                    val t    = i.toDouble() / SAMPLE_RATE
                    // Montée de fréquence : 110Hz → 440Hz sur 300ms
                    val freq = if (t < 0.3) 110.0 + (440.0 - 110.0) * (t / 0.3) else 440.0
                    val env  = if (t < 0.3) t / 0.3 else exp(-4.0 * (t - 0.3))
                    val tone = sin(2.0 * Math.PI * freq * t) * env * 0.7
                    val noise = (Random.nextDouble() * 2.0 - 1.0) * exp(-10.0 * t) * 0.15
                    val s = ((tone + noise) * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    buf[i * 2]     = (s.toInt() and 0xFF).toByte()
                    buf[i * 2 + 1] = (s.toInt() shr 8).toByte()
                }
                line.write(buf, 0, buf.size)
                line.drain()
                line.close()
            } catch (_: Exception) {}
        }, "igniterra-unlock").also { it.isDaemon = true; it.start() }
    }

    // ── Sons Snake ────────────────────────────────────────────────────────────


    private const val NOTE_DUR  = 0.12f  // durée d'une note en secondes
    private const val MUSIC_VOL = 0.18f

    @Volatile private var musicRunning = false

    // Mélodie 8-bit — notes en Hz
    private val MELODY = floatArrayOf(
        523f, 494f, 440f, 392f, 330f, 392f, 440f, 0f, 392f, 330f, 294f, 330f, 392f, 440f, 392f, 0f, 349f, 392f, 440f, 523f, 440f, 392f, 349f, 0f, 392f, 440f, 494f, 440f, 392f, 330f, 294f, 0f, 523f, 494f, 440f, 392f, 330f, 392f, 440f, 0f, 392f, 330f, 294f, 330f, 392f, 440f, 392f, 0f, 349f, 392f, 440f, 523f, 440f, 392f, 349f, 0f, 392f, 440f, 494f, 440f, 392f, 330f, 294f, 0f, 523f, 587f, 659f, 587f, 523f, 494f, 440f, 0f, 440f, 523f, 587f, 523f, 440f, 392f, 330f, 0f, 523f, 494f, 440f, 523f, 587f, 523f, 440f, 0f, 494f, 440f, 392f, 330f, 294f, 330f, 262f, 0f, 523f, 494f, 440f, 392f, 330f, 392f, 440f, 0f, 392f, 330f, 294f, 330f, 392f, 440f, 392f, 0f, 349f, 392f, 440f, 523f, 440f, 392f, 349f, 0f, 392f, 440f, 494f, 440f, 392f, 330f, 294f, 0f
    )


    actual fun snakeMusicStart() {
        if (musicRunning) return
        musicRunning = true
        Thread({
            try {
                val format = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
                val line   = AudioSystem.getSourceDataLine(format)
                val noteSamples = (SAMPLE_RATE * NOTE_DUR).toInt()
                val buf = ByteArray(noteSamples * 2)
                line.open(format, buf.size * 4)
                line.start()
                var noteIdx = 0
                while (musicRunning) {
                    val freq = MELODY[noteIdx % MELODY.size]
                    for (i in 0 until noteSamples) {
                        val t   = i.toDouble() / SAMPLE_RATE
                        val env = if (i < noteSamples * 0.1) i / (noteSamples * 0.1) else 1.0
                        val s   = if (freq > 0f) {
                            // Sinus avec vibrato léger 5Hz, depth 0.5%
                            val vibrato = 1.0 + 0.005 * sin(2.0 * Math.PI * 5.0 * t)
                            sin(2.0 * Math.PI * freq * vibrato * t) * env * MUSIC_VOL
                        } else 0.0
                        val sample = (s * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        buf[i * 2]     = (sample.toInt() and 0xFF).toByte()
                        buf[i * 2 + 1] = (sample.toInt() shr 8).toByte()
                    }
                    line.write(buf, 0, buf.size)
                    noteIdx++
                }
                line.drain()
                line.close()
            } catch (_: Exception) {}
        }, "igniterra-snake-music").also { it.isDaemon = true; it.start() }
    }

    actual fun snakeMusicStop() { musicRunning = false }

    actual fun snakeEat() {
        Thread({
            try {
                val format  = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
                val line    = AudioSystem.getSourceDataLine(format)
                val samples = (SAMPLE_RATE * 0.08f).toInt()
                val buf     = ByteArray(samples * 2)
                line.open(format, buf.size)
                line.start()
                // Deux bips montants rapides
                for (i in 0 until samples) {
                    val t    = i.toDouble() / SAMPLE_RATE
                    val freq = if (t < 0.04) 523.0 else 784.0
                    val phase = (freq * t) % 1.0
                    val s = (if (phase < 0.5) 1.0 else -1.0) * 0.35 * Short.MAX_VALUE
                    buf[i * 2]     = (s.toInt() and 0xFF).toByte()
                    buf[i * 2 + 1] = (s.toInt() shr 8).toByte()
                }
                line.write(buf, 0, buf.size)
                line.drain()
                line.close()
            } catch (_: Exception) {}
        }, "igniterra-eat").also { it.isDaemon = true; it.start() }
    }

    actual fun snakeDie() {
        Thread({
            try {
                val format  = AudioFormat(SAMPLE_RATE, 16, 1, true, false)
                val line    = AudioSystem.getSourceDataLine(format)
                val samples = (SAMPLE_RATE * 0.5f).toInt()
                val buf     = ByteArray(samples * 2)
                line.open(format, buf.size)
                line.start()
                // Descente chromatique
                for (i in 0 until samples) {
                    val t    = i.toDouble() / SAMPLE_RATE
                    val freq = 440.0 * Math.pow(0.5, t * 2.0)  // descente sur 2 octaves
                    val env  = exp(-3.0 * t)
                    val phase = (freq * t) % 1.0
                    val s = (if (phase < 0.5) 1.0 else -1.0) * env * 0.4 * Short.MAX_VALUE
                    buf[i * 2]     = (s.toInt() and 0xFF).toByte()
                    buf[i * 2 + 1] = (s.toInt() shr 8).toByte()
                }
                line.write(buf, 0, buf.size)
                line.drain()
                line.close()
            } catch (_: Exception) {}
        }, "igniterra-die").also { it.isDaemon = true; it.start() }
    }
}