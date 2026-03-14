package igniterra

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
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
                var sample = brown * BASE_VOLUME

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
}