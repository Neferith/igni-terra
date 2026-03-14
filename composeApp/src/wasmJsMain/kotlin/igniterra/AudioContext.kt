package igniterra

/**
 * Implémentation Web (wasmJs) — Web Audio API.
 * Toute la logique audio est déléguée à des fonctions JS via @JsFun
 * pour éviter le type `dynamic` non supporté en wasmJs.
 */

@JsFun("""() => {
    try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)();
        window._igniterraBrown      = 0;
        window._igniterraCrackleLeft = 0;
        window._igniterraCrackleAmp  = 0;
        window._igniterra_processor  = null;

        const node = ctx.createScriptProcessor(2048, 0, 1);
        node.onaudioprocess = function(e) {
            const out   = e.outputBuffer.getChannelData(0);
            const len   = out.length;
            const BASE  = 0.018;
            const PROB  = 0.0015;
            const AMP   = 0.22;
            for (let i = 0; i < len; i++) {
                window._igniterraBrown = Math.max(-1, Math.min(1,
                    window._igniterraBrown + (Math.random() * 2 - 1) * 0.08));
                let s = window._igniterraBrown * BASE;
                if (window._igniterraCrackleLeft > 0) {
                    s += window._igniterraCrackleAmp * (Math.random() * 2 - 1);
                    window._igniterraCrackleAmp  *= 0.82;
                    window._igniterraCrackleLeft -= 1;
                } else if (Math.random() < PROB) {
                    window._igniterraCrackleLeft = Math.floor(Math.random() * 32 + 8);
                    window._igniterraCrackleAmp  = AMP * (0.3 + Math.random() * 0.7);
                }
                out[i] = Math.max(-1, Math.min(1, s));
            }
        };
        node.connect(ctx.destination);
        window._igniterra_ctx       = ctx;
        window._igniterra_processor = node;
    } catch(e) {}
}""")
private external fun jsStartCrackle()

@JsFun("""() => {
    try {
        if (window._igniterra_processor) {
            window._igniterra_processor.disconnect();
            window._igniterra_processor = null;
        }
        if (window._igniterra_ctx) {
            window._igniterra_ctx.close();
            window._igniterra_ctx = null;
        }
    } catch(e) {}
}""")
private external fun jsStopCrackle()

@JsFun("""() => {
    try {
        const ctx = window._igniterra_ctx;
        if (!ctx) return;
        const sr      = ctx.sampleRate;
        const samples = Math.floor(sr * 0.012);
        const buf     = ctx.createBuffer(1, samples, sr);
        const data    = buf.getChannelData(0);
        const DECAY   = 180.0;
        const VOL     = 0.55;
        for (let i = 0; i < samples; i++) {
            const t   = i / sr;
            const env = Math.exp(-DECAY * t);
            data[i]   = (Math.random() * 2 - 1) * env * VOL;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf;
        src.connect(ctx.destination);
        src.start();
    } catch(e) {}
}""")
private external fun jsClick()

actual object CrackleSound {
    actual fun start() = jsStartCrackle()
    actual fun stop()  = jsStopCrackle()
    actual fun click() = jsClick()
}