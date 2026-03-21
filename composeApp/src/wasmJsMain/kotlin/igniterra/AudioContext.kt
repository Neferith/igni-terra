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

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) window._igniterra_ctx = new (window.AudioContext || window.webkitAudioContext)();
        const ctx = window._igniterra_ctx;
        if (ctx.state === 'suspended') { ctx.resume(); return; }
        const melody = [523,494,440,392,330,392,440,0,392,330,294,330,392,440,392,0,349,392,440,523,440,392,349,0,392,440,494,440,392,330,294,0,523,494,440,392,330,392,440,0,392,330,294,330,392,440,392,0,349,392,440,523,440,392,349,0,392,440,494,440,392,330,294,0,523,587,659,587,523,494,440,0,440,523,587,523,440,392,330,0,523,494,440,523,587,523,440,0,494,440,392,330,294,330,262,0,523,494,440,392,330,392,440,0,392,330,294,330,392,440,392,0,349,392,440,523,440,392,349,0,392,440,494,440,392,330,294,0];
        const noteDur = 0.12;
        let noteIdx = 0;
        window._igniterra_music_running = true;
        function playNote() {
            if (!window._igniterra_music_running) return;
            const freq = melody[noteIdx % melody.length];
            noteIdx++;
            const samples = Math.floor(ctx.sampleRate * noteDur);
            const buf = ctx.createBuffer(1, samples, ctx.sampleRate);
            const data = buf.getChannelData(0);
            for (let i = 0; i < samples; i++) {
                const t = i / ctx.sampleRate;
                const env = i < samples * 0.05 ? i / (samples * 0.05) : 1.0;
                if (freq > 0) {
                    const vibrato = 1.0 + 0.005 * Math.sin(2 * Math.PI * 5 * t);
                    data[i] = Math.sin(2 * Math.PI * freq * vibrato * t) * env * 0.25;
                }
            }
            const src = ctx.createBufferSource();
            src.buffer = buf;
            src.connect(ctx.destination);
            src.start();
            src.onended = playNote;
        }
        playNote();
    } catch(e) {}
}""")
private external fun jsSnakeMusicStart()

@JsFun("() => { window._igniterra_music_running = false; }")
private external fun jsSnakeMusicStop()

@JsFun("""() => {
    try {
        const ctx = window._igniterra_ctx;
        if (!ctx || ctx.state === 'suspended') return;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.08);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t = i / sr;
            const freq = t < 0.04 ? 523 : 784;
            data[i] = ((freq * t) % 1 < 0.5 ? 1 : -1) * 0.35;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(ctx.destination); src.start();
    } catch(e) {}
}""")
private external fun jsSnakeEat()

@JsFun("""() => {
    try {
        const ctx = window._igniterra_ctx;
        if (!ctx || ctx.state === 'suspended') return;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.5);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t = i / sr;
            const freq = 440 * Math.pow(0.5, t * 2);
            const env = Math.exp(-3 * t);
            data[i] = ((freq * t) % 1 < 0.5 ? 1 : -1) * env * 0.4;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(ctx.destination); src.start();
    } catch(e) {}
}""")
private external fun jsSnakeDie()

actual object CrackleSound {
    actual fun start() {}
    actual fun stop()  {}
    actual fun click() = jsClick()

    actual fun openDocument() = jsOpenDocument()

    actual fun unlockSecret() = jsUnlockSecret()

    actual fun snakeMusicStart()  = jsSnakeMusicStart()
    actual fun snakeMusicStop()   = jsSnakeMusicStop()
    actual fun snakeEat()         = jsSnakeEat()
    actual fun snakeDie()         = jsSnakeDie()
}