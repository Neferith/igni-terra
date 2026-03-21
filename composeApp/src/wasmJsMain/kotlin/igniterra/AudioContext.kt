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

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) {
            window._igniterra_ctx = new (window.AudioContext || window.webkitAudioContext)();
        }
        const ctx = window._igniterra_ctx;
        if (ctx.state === 'suspended') { ctx.resume(); return; }
        const sr      = ctx.sampleRate;
        const samples = Math.floor(sr * 0.4);
        const buf     = ctx.createBuffer(1, samples, sr);
        const data    = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t     = i / sr;
            const tone  = Math.sin(2 * Math.PI * 80 * t) * Math.exp(-3 * t);
            const noise = (Math.random() * 2 - 1) * Math.exp(-8 * t) * 0.3;
            data[i]     = (tone * 0.6 + noise) * 0.7;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf;
        src.connect(ctx.destination);
        src.start();
    } catch(e) {}
}""")
private external fun jsOpenDocument()

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) {
            window._igniterra_ctx = new (window.AudioContext || window.webkitAudioContext)();
        }
        const ctx = window._igniterra_ctx;
        if (ctx.state === 'suspended') { ctx.resume(); return; }
        const sr      = ctx.sampleRate;
        const samples = Math.floor(sr * 0.6);
        const buf     = ctx.createBuffer(1, samples, sr);
        const data    = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t    = i / sr;
            const freq = t < 0.3 ? 110 + (440 - 110) * (t / 0.3) : 440;
            const env  = t < 0.3 ? t / 0.3 : Math.exp(-4 * (t - 0.3));
            const tone = Math.sin(2 * Math.PI * freq * t) * env * 0.7;
            const noise = (Math.random() * 2 - 1) * Math.exp(-10 * t) * 0.15;
            data[i] = tone + noise;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf;
        src.connect(ctx.destination);
        src.start();
    } catch(e) {}
}""")
private external fun jsUnlockSecret()

actual object CrackleSound {
    actual fun start() {}
    actual fun stop()  {}
    actual fun click() = jsClick()

    actual fun openDocument() = jsOpenDocument()

    actual fun unlockSecret() = jsUnlockSecret()
}