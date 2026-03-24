package igniterra

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.math.exp
import kotlin.random.Random

/**
 * Implémentation Android — AudioTrack PCM 16bit mono.
 * Même comportement que la version desktop.
 */
actual object CrackleSound {

    private const val SAMPLE_RATE   = 44100
    private const val BASE_VOLUME   = 0.018f
    private const val CRACKLE_PROB  = 0.0015f
    private const val CRACKLE_AMP   = 0.22f
    private const val CLICK_VOLUME  = 0.55f
    private const val CLICK_DECAY   = 180.0

    @Volatile private var running = false
    @Volatile var globalVolume: Float = 1.0f
        private set

    private fun audioAttributes() = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private fun monoFormat() = AudioFormat.Builder()
        .setSampleRate(SAMPLE_RATE)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    // ── Bruit de fond ─────────────────────────────────────────────────────────

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
        val bufSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val track = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes())
            .setAudioFormat(monoFormat())
            .setBufferSizeInBytes(bufSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        track.play()

        val buf          = ShortArray(bufSize / 2)
        var brown        = 0.0
        var crackleLeft  = 0
        var crackleAmp   = 0.0

        while (running) {
            for (i in buf.indices) {
                brown = (brown + (Random.nextDouble() * 2.0 - 1.0) * 0.08).coerceIn(-1.0, 1.0)
                var sample = brown * BASE_VOLUME * globalVolume

                if (crackleLeft > 0) {
                    sample += crackleAmp * (Random.nextDouble() * 2.0 - 1.0)
                    crackleAmp *= 0.82
                    crackleLeft--
                } else if (Random.nextFloat() < CRACKLE_PROB) {
                    crackleLeft = Random.nextInt(8, 40)
                    crackleAmp  = (CRACKLE_AMP * (0.3f + Random.nextFloat() * 0.7f)).toDouble()
                }

                buf[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
            }
            track.write(buf, 0, buf.size)
        }

        track.stop()
        track.release()
    }

    // ── Clic de navigation ────────────────────────────────────────────────────

    actual fun setVolume(volume: Float) { globalVolume = volume.coerceIn(0f, 1f) }

    actual fun unlockSecret() {
        Thread({
            val samples = (SAMPLE_RATE * 0.6).toInt()
            val buf     = ShortArray(samples)
            for (i in buf.indices) {
                val t    = i.toDouble() / SAMPLE_RATE
                val freq = if (t < 0.3) 110.0 + (440.0 - 110.0) * (t / 0.3) else 440.0
                val env  = if (t < 0.3) t / 0.3 else kotlin.math.exp(-4.0 * (t - 0.3))
                val tone = kotlin.math.sin(2.0 * kotlin.math.PI * freq * t) * env * 0.7
                val noise = (Random.nextDouble() * 2.0 - 1.0) * kotlin.math.exp(-10.0 * t) * 0.15
                buf[i] = ((tone + noise) * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes())
                .setAudioFormat(monoFormat())
                .setBufferSizeInBytes(samples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buf, 0, buf.size)
            track.play()
            Thread.sleep(700)
            track.stop()
            track.release()
        }, "igniterra-unlock").also { it.isDaemon = true; it.start() }
    }

    actual fun openDocument() {
        Thread({
            val samples = (SAMPLE_RATE * 0.4).toInt()
            val buf     = ShortArray(samples)
            for (i in buf.indices) {
                val t      = i.toDouble() / SAMPLE_RATE
                val tone   = kotlin.math.sin(2.0 * kotlin.math.PI * 80.0 * t) * kotlin.math.exp(-3.0 * t)
                val noise  = (Random.nextDouble() * 2.0 - 1.0) * kotlin.math.exp(-8.0 * t) * 0.3
                buf[i]     = ((tone * 0.6 + noise) * 0.7 * Short.MAX_VALUE)
                    .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes())
                .setAudioFormat(monoFormat())
                .setBufferSizeInBytes(samples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buf, 0, buf.size)
            track.play()
            Thread.sleep(500)
            track.stop()
            track.release()
        }, "igniterra-open").also { it.isDaemon = true; it.start() }
    }

    actual fun click() {
        Thread({
            val samples = (SAMPLE_RATE * 0.012).toInt()   // 12ms
            val buf     = ShortArray(samples)

            for (i in buf.indices) {
                val t   = i.toDouble() / SAMPLE_RATE
                val env = exp(-CLICK_DECAY * t)
                buf[i]  = ((Random.nextDouble() * 2.0 - 1.0) * env * CLICK_VOLUME * Short.MAX_VALUE)
                    .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes())
                .setAudioFormat(monoFormat())
                .setBufferSizeInBytes(samples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buf, 0, buf.size)
            track.play()
            Thread.sleep(20)
            track.stop()
            track.release()
        }, "igniterra-click").also {
            it.isDaemon = true
            it.start()
        }
    }

    @Volatile private var musicRunning = false

    private val melody = floatArrayOf(
        523f, 494f, 440f, 392f, 330f, 392f, 440f, 0f, 392f, 330f, 294f, 330f, 392f, 440f, 392f, 0f, 349f, 392f, 440f, 523f, 440f, 392f, 349f, 0f, 392f, 440f, 494f, 440f, 392f, 330f, 294f, 0f, 523f, 494f, 440f, 392f, 330f, 392f, 440f, 0f, 392f, 330f, 294f, 330f, 392f, 440f, 392f, 0f, 349f, 392f, 440f, 523f, 440f, 392f, 349f, 0f, 392f, 440f, 494f, 440f, 392f, 330f, 294f, 0f, 523f, 587f, 659f, 587f, 523f, 494f, 440f, 0f, 440f, 523f, 587f, 523f, 440f, 392f, 330f, 0f, 523f, 494f, 440f, 523f, 587f, 523f, 440f, 0f, 494f, 440f, 392f, 330f, 294f, 330f, 262f, 0f, 523f, 494f, 440f, 392f, 330f, 392f, 440f, 0f, 392f, 330f, 294f, 330f, 392f, 440f, 392f, 0f, 349f, 392f, 440f, 523f, 440f, 392f, 349f, 0f, 392f, 440f, 494f, 440f, 392f, 330f, 294f, 0f
    )

    actual fun snakeMusicStart() {
        if (musicRunning) return
        musicRunning = true
        Thread({
            val noteSamples = (SAMPLE_RATE * 0.12).toInt()
            var noteIdx = 0
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes())
                .setAudioFormat(monoFormat())
                .setBufferSizeInBytes(noteSamples * 2 * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            track.play()
            while (musicRunning) {
                val freq = melody[noteIdx % melody.size]
                val buf = ShortArray(noteSamples)
                for (i in buf.indices) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = if (i < noteSamples * 0.05) i / (noteSamples * 0.05) else 1.0
                    buf[i] = if (freq > 0f) {
                        val vibrato = 1.0 + 0.005 * kotlin.math.sin(2.0 * kotlin.math.PI * 5.0 * t)
                        (kotlin.math.sin(2.0 * kotlin.math.PI * freq * vibrato * t) * env * 0.25 * globalVolume * Short.MAX_VALUE).toInt().toShort()
                    } else 0
                }
                track.write(buf, 0, buf.size)
                noteIdx++
            }
            track.stop(); track.release()
        }, "igniterra-snake-music").also { it.isDaemon = true; it.start() }
    }

    actual fun snakeMusicStop() { musicRunning = false }

    actual fun snakeEat() {
        Thread({
            val samples = (SAMPLE_RATE * 0.08).toInt()
            val buf = ShortArray(samples)
            for (i in buf.indices) {
                val t = i.toDouble() / SAMPLE_RATE
                val freq = if (t < 0.04) 523.0 else 784.0
                val phase = (freq * t) % 1.0
                buf[i] = ((if (phase < 0.5) 1.0 else -1.0) * 0.35 * Short.MAX_VALUE).toInt().toShort()
            }
            val track = AudioTrack.Builder().setAudioAttributes(audioAttributes()).setAudioFormat(monoFormat())
                .setBufferSizeInBytes(samples * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
            track.write(buf, 0, buf.size); track.play()
            Thread.sleep(100); track.stop(); track.release()
        }, "igniterra-eat").also { it.isDaemon = true; it.start() }
    }

    actual fun snakeDie() {
        Thread({
            val samples = (SAMPLE_RATE * 0.5).toInt()
            val buf = ShortArray(samples)
            for (i in buf.indices) {
                val t = i.toDouble() / SAMPLE_RATE
                val freq = 440.0 * kotlin.math.exp(-t * kotlin.math.ln(2.0) * 2)
                val env = kotlin.math.exp(-3.0 * t)
                val phase = (freq * t) % 1.0
                buf[i] = ((if (phase < 0.5) 1.0 else -1.0) * env * 0.4 * Short.MAX_VALUE).toInt().toShort()
            }
            val track = AudioTrack.Builder().setAudioAttributes(audioAttributes()).setAudioFormat(monoFormat())
                .setBufferSizeInBytes(samples * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
            track.write(buf, 0, buf.size); track.play()
            Thread.sleep(600); track.stop(); track.release()
        }, "igniterra-die").also { it.isDaemon = true; it.start() }
    }

    // ── Context (initialisé depuis MainActivity) ──────────────────────────────
    // ── Lecture WAV ───────────────────────────────────────────────────────────
    private var mediaPlayer: MediaPlayer? = null

    actual fun playWav(filename: String, loop: Boolean) {
        Thread({
            try {
                stopWav()
                val stream = CrackleSound::class.java.classLoader
                    ?.getResourceAsStream(filename)
                    ?: return@Thread
                val tmp = java.io.File.createTempFile("igniterra_audio", null).apply {
                    deleteOnExit()
                    outputStream().use { stream.copyTo(it) }
                }
                val mp = MediaPlayer().apply {
                    setDataSource(tmp.absolutePath)
                    isLooping = loop
                    setOnCompletionListener { if (!loop) mediaPlayer = null }
                    prepare()
                    start()
                }
                mediaPlayer = mp
            } catch (_: Exception) {}
        }, "igniterra-wav").also { it.isDaemon = true; it.start() }
    }

    actual fun stopWav() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun dungeonHit() { playTones(listOf(220f to 30, 180f to 20), 0.4f, "square") }
    actual fun dungeonEnemyDie() { playTones(listOf(440f to 60, 330f to 50, 220f to 80), 0.3f, "square") }
    actual fun dungeonItemPickup() { playTones(listOf(523f to 60, 659f to 60, 784f to 100), 0.3f, "sine") }
    actual fun dungeonLevelUp() { playTones(listOf(262f to 80, 330f to 80, 392f to 80, 523f to 80, 659f to 80, 784f to 160), 0.35f, "sine") }
    actual fun dungeonGameOver() { playTones(listOf(330f to 150, 294f to 150, 262f to 150, 220f to 300), 0.35f, "square") }
    actual fun dungeonVictory() { playTones(listOf(523f to 80, 523f to 80, 523f to 80, 415f to 240, 466f to 80, 523f to 320, 466f to 80, 523f to 400), 0.4f, "square") }

    private fun playTones(notes: List<Pair<Float, Int>>, vol: Float, wave: String) {
        Thread({
            val totalSamples = notes.sumOf { (_, ms) -> (SAMPLE_RATE * ms / 1000.0).toInt() }
            val buf = ShortArray(totalSamples)
            var i = 0
            for ((freq, ms) in notes) {
                val n = (SAMPLE_RATE * ms / 1000.0).toInt()
                repeat(n) { j ->
                    val t = (i + j).toDouble() / SAMPLE_RATE
                    val s = if (wave == "sine")
                        kotlin.math.sin(2.0 * kotlin.math.PI * freq * t)
                    else
                        if ((freq * t) % 1.0 < 0.5) 1.0 else -1.0
                    buf[i + j] = (s * vol * globalVolume * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                i += n
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes())
                .setAudioFormat(monoFormat())
                .setBufferSizeInBytes(totalSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(buf, 0, buf.size)
            track.play()
            Thread.sleep((totalSamples * 1000L / SAMPLE_RATE) + 50)
            track.stop(); track.release()
        }, "igniterra-dungeon").also { it.isDaemon = true; it.start() }
    }

}