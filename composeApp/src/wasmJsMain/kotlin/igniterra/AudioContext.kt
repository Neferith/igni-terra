package igniterra

/**
 * Implémentation Web (wasmJs) — clic uniquement, pas de bruit de fond.
 */

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) {
            window._igniterra_ctx = new (window.AudioContext || window.webkitAudioContext)();
        }
        const ctx = window._igniterra_ctx;
        if (ctx.state === 'suspended') { ctx.resume(); return; }
        const sr      = ctx.sampleRate;
        const samples = Math.floor(sr * 0.012);
        const buf     = ctx.createBuffer(1, samples, sr);
        const data    = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const env = Math.exp(-180.0 * (i / sr));
            data[i]   = (Math.random() * 2 - 1) * env * 0.55;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf;
        src.connect(ctx.destination);
        src.start();
    } catch(e) {}
}""")
private external fun jsClick()

actual object CrackleSound {
    actual fun start() {}
    actual fun stop()  {}
    actual fun click() = jsClick()
}