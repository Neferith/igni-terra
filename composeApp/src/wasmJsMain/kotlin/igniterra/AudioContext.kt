package igniterra

/**
 * Implémentation Web (wasmJs) — Web Audio API.
 * Toutes les sources passent par un GainNode central pour le contrôle du volume.
 */

// Crée le contexte + GainNode central au premier appel
@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) {
            window._igniterra_ctx = new (window.AudioContext || window.webkitAudioContext)();
            window._igniterra_gain = window._igniterra_ctx.createGain();
            window._igniterra_gain.gain.value = 1.0;
            window._igniterra_gain.connect(window._igniterra_ctx.destination);
        }
        if (window._igniterra_ctx.state === 'suspended') window._igniterra_ctx.resume();
    } catch(e) {}
}""")
private external fun jsInitCtx()

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) { jsInitCtx(); return; }
        const ctx = window._igniterra_ctx;
        if (ctx.state === 'suspended') { ctx.resume(); return; }
        const out = window._igniterra_gain || ctx.destination;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.012);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            data[i] = (Math.random() * 2 - 1) * Math.exp(-180.0 * (i / sr)) * 0.55;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(out); src.start();
    } catch(e) {}
}""")
private external fun jsClick()

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) return;
        const ctx = window._igniterra_ctx;
        const out = window._igniterra_gain || ctx.destination;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.4);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t = i / sr;
            data[i] = (Math.sin(2*Math.PI*80*t)*Math.exp(-3*t)*0.6 + (Math.random()*2-1)*Math.exp(-8*t)*0.3) * 0.7;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(out); src.start();
    } catch(e) {}
}""")
private external fun jsOpenDocument()

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) return;
        const ctx = window._igniterra_ctx;
        const out = window._igniterra_gain || ctx.destination;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.6);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t = i / sr;
            const freq = t < 0.3 ? 110 + (440-110)*(t/0.3) : 440;
            const env  = t < 0.3 ? t/0.3 : Math.exp(-4*(t-0.3));
            data[i] = Math.sin(2*Math.PI*freq*t)*env*0.7 + (Math.random()*2-1)*Math.exp(-10*t)*0.15;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(out); src.start();
    } catch(e) {}
}""")
private external fun jsUnlockSecret()

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) { jsInitCtx(); }
        const ctx = window._igniterra_ctx;
        if (ctx.state === 'suspended') { ctx.resume(); return; }
        const out = window._igniterra_gain || ctx.destination;
        const melody = [523,494,440,392,330,392,440,0,392,330,294,330,392,440,392,0,349,392,440,523,440,392,349,0,392,440,494,440,392,330,294,0,523,494,440,392,330,392,440,0,392,330,294,330,392,440,392,0,349,392,440,523,440,392,349,0,392,440,494,440,392,330,294,0,523,587,659,587,523,494,440,0,440,523,587,523,440,392,330,0,523,494,440,523,587,523,440,0,494,440,392,330,294,330,262,0,523,494,440,392,330,392,440,0,392,330,294,330,392,440,392,0,349,392,440,523,440,392,349,0,392,440,494,440,392,330,294,0];
        let noteIdx = 0;
        window._igniterra_music_running = true;
        function playNote() {
            if (!window._igniterra_music_running) return;
            const freq = melody[noteIdx++ % melody.length];
            const samples = Math.floor(ctx.sampleRate * 0.12);
            const buf = ctx.createBuffer(1, samples, ctx.sampleRate);
            const data = buf.getChannelData(0);
            for (let i = 0; i < samples; i++) {
                const t = i / ctx.sampleRate;
                const env = i < samples * 0.05 ? i / (samples * 0.05) : 1.0;
                if (freq > 0) {
                    const vib = 1.0 + 0.005 * Math.sin(2*Math.PI*5*t);
                    data[i] = Math.sin(2*Math.PI*freq*vib*t) * env * 0.25;
                }
            }
            const src = ctx.createBufferSource();
            src.buffer = buf; src.connect(out); src.start();
            src.onended = playNote;
        }
        playNote();
    } catch(e) {}
}""")
private external fun jsSnakeMusicStart()

@JsFun("() => { window._igniterra_music_running = false; }")
private external fun jsSnakeMusicStop()

@JsFun("""(volume) => {
    try {
        if (window._igniterra_gain) window._igniterra_gain.gain.value = volume;
    } catch(e) {}
}""")
private external fun jsSetVolume(volume: Float)

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) return;
        const ctx = window._igniterra_ctx;
        const out = window._igniterra_gain || ctx.destination;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.08);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t = i / sr;
            data[i] = ((( (t < 0.04 ? 523 : 784) * t) % 1 < 0.5) ? 1 : -1) * 0.35;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(out); src.start();
    } catch(e) {}
}""")
private external fun jsSnakeEat()

@JsFun("""() => {
    try {
        if (!window._igniterra_ctx) return;
        const ctx = window._igniterra_ctx;
        const out = window._igniterra_gain || ctx.destination;
        const sr = ctx.sampleRate;
        const samples = Math.floor(sr * 0.5);
        const buf = ctx.createBuffer(1, samples, sr);
        const data = buf.getChannelData(0);
        for (let i = 0; i < samples; i++) {
            const t = i / sr;
            data[i] = (((440 * Math.pow(0.5, t*2) * t) % 1 < 0.5) ? 1 : -1) * Math.exp(-3*t) * 0.4;
        }
        const src = ctx.createBufferSource();
        src.buffer = buf; src.connect(out); src.start();
    } catch(e) {}
}""")
private external fun jsSnakeDie()

@JsFun("""(filename, loop) => {
    try {
        if (!window._igniterra_ctx) return;
        const ctx = window._igniterra_ctx;
        const out = window._igniterra_gain || ctx.destination;
        if (window._igniterra_wav_source) {
            window._igniterra_wav_source.stop();
            window._igniterra_wav_source = null;
        }
        fetch('./composeResources/igniterra.composeapp.generated.resources/files/' + filename)
            .then(r => r.arrayBuffer())
            .then(ab => ctx.decodeAudioData(ab))
            .then(decoded => {
                const src = ctx.createBufferSource();
                src.buffer = decoded;
                src.loop = loop;
                src.connect(out);
                src.start();
                window._igniterra_wav_source = src;
            }).catch(e => console.warn('playWav error:', e));
    } catch(e) {}
}""")
private external fun jsPlayWav(filename: String, loop: Boolean)

@JsFun("""() => {
    try {
        if (window._igniterra_wav_source) {
            window._igniterra_wav_source.stop();
            window._igniterra_wav_source = null;
        }
    } catch(e) {}
}""")
private external fun jsStopWav()

@JsFun("""(notes, vol, wave) => {
    try {
        if (!window._igniterra_ctx) return;
        const ctx = window._igniterra_ctx;
        const out = window._igniterra_gain || ctx.destination;
        const sr = ctx.sampleRate;
        let timeOffset = ctx.currentTime + 0.01;
        for (const [freq, ms] of notes) {
            const dur = ms / 1000;
            const samples = Math.floor(sr * dur);
            const buf = ctx.createBuffer(1, samples, sr);
            const data = buf.getChannelData(0);
            for (let i = 0; i < samples; i++) {
                const t = i / sr;
                data[i] = wave === 'sine'
                    ? Math.sin(2 * Math.PI * freq * t) * vol
                    : ((freq * t) % 1 < 0.5 ? 1 : -1) * vol;
            }
            const src = ctx.createBufferSource();
            src.buffer = buf; src.connect(out);
            src.start(timeOffset);
            timeOffset += dur;
        }
    } catch(e) {}
}""")
private external fun jsPlayTones(notes: JsAny, vol: Float, wave: String)

@JsFun("""(notes, vol, wave) => {
    const arr = [];
    return arr;
}""")
private external fun jsEmptyArray(): JsAny

@JsFun("(freq, ms) => [freq, ms]")
private external fun jsNote(freq: Float, ms: Int): JsAny

@JsFun("(arr, note) => { arr.push(note); return arr; }")
private external fun jsPush(arr: JsAny, note: JsAny): JsAny

private fun playTonesWasm(notes: List<Pair<Float, Int>>, vol: Float, wave: String) {
    var arr = jsEmptyArray()
    for ((freq, ms) in notes) arr = jsPush(arr, jsNote(freq, ms))
    jsPlayTones(arr, vol, wave)
}

actual object CrackleSound {
    actual fun start()                      {}
    actual fun stop()                       {}
    actual fun click()                      { jsInitCtx(); jsClick() }
    actual fun openDocument()               { jsInitCtx(); jsOpenDocument() }
    actual fun unlockSecret()               { jsInitCtx(); jsUnlockSecret() }
    actual fun snakeMusicStart()            { jsInitCtx(); jsSnakeMusicStart() }
    actual fun snakeMusicStop()             = jsSnakeMusicStop()
    actual fun setVolume(volume: Float)     = jsSetVolume(volume)
    actual fun snakeEat()                   = jsSnakeEat()
    actual fun snakeDie()                   = jsSnakeDie()

    actual fun playWav(filename: String, loop: Boolean) = jsPlayWav(filename, loop)
    actual fun stopWav()                        = jsStopWav()

    actual fun dungeonHit()        = playTonesWasm(listOf(220f to 30, 180f to 20), 0.4f, "square")
    actual fun dungeonEnemyDie()   = playTonesWasm(listOf(440f to 60, 330f to 50, 220f to 80), 0.3f, "square")
    actual fun dungeonItemPickup() = playTonesWasm(listOf(523f to 60, 659f to 60, 784f to 100), 0.3f, "sine")
    actual fun dungeonLevelUp()    = playTonesWasm(listOf(262f to 80, 330f to 80, 392f to 80, 523f to 80, 659f to 80, 784f to 160), 0.35f, "sine")
    actual fun dungeonGameOver()   = playTonesWasm(listOf(330f to 150, 294f to 150, 262f to 150, 220f to 300), 0.35f, "square")
    actual fun dungeonVictory()    = playTonesWasm(listOf(523f to 80, 523f to 80, 523f to 80, 415f to 240, 466f to 80, 523f to 320, 466f to 80, 523f to 400), 0.4f, "square")
}