// commonMain/kotlin/org/angelus/magitek/model/HiddenMessageEngine.kt

package igniterra.model

/**
 * Moteur de message caché.
 *
 * Chaque pression de bouton avance dans la séquence.
 * Quand la séquence progresse, le prochain groupe de caractères
 * du message secret se révèle dans les 34 bits réservés.
 *
 * Si l'utilisateur presse un mauvais bouton → reset silencieux.
 * Le message complet est révélé quand toute la séquence est parcourue.
 */
class HiddenMessageEngine(private val secrets: List<HiddenSecret>) {

    private var activeSecret: HiddenSecret? = null
    private var progress: Int = 0               // index dans la séquence en cours
    private var revealed: String = ""           // texte révélé jusqu'ici

    // État observable
    var state: HiddenState = HiddenState.Idle
        private set

    /**
     * Appelé à chaque pression de bouton.
     * Retourne le nouvel état.
     */
    fun onButtonPressed(buttonIndex: Int): HiddenState {
        // Cherche si un secret commence par ce bouton (seulement si on est Idle)
        if (state is HiddenState.Idle || state is HiddenState.Wrong) {
            val candidate = secrets.firstOrNull { it.sequence.firstOrNull() == buttonIndex }
            if (candidate != null) {
                activeSecret = candidate
                progress     = 1
                revealed     = candidate.revealAt(progress)
                state = if (progress >= candidate.sequence.size)
                    HiddenState.Complete(candidate.message, encode34(candidate.message))
                else
                    HiddenState.Revealing(revealed, encode34(revealed), progress, candidate.sequence.size)
                return state
            }
        }

        // Continue la séquence en cours
        val secret = activeSecret
        if (secret != null && state is HiddenState.Revealing) {
            if (secret.sequence.getOrNull(progress) == buttonIndex) {
                progress++
                revealed = secret.revealAt(progress)
                state = if (progress >= secret.sequence.size)
                    HiddenState.Complete(secret.message, encode34(secret.message))
                else
                    HiddenState.Revealing(revealed, encode34(revealed), progress, secret.sequence.size)
            } else {
                // Mauvaise touche → reset silencieux
                reset()
            }
            return state
        }

        // Aucune séquence en cours
        return HiddenState.Idle
    }

    fun reset() {
        activeSecret = null
        progress     = 0
        revealed     = ""
        state        = HiddenState.Idle
    }

    fun acknowledge() {
        // Après lecture du message complet, retour à Idle
        reset()
    }

    // ── Encodage ─────────────────────────────────────────────────────────────

    /**
     * Encode le texte révélé sur 34 bits (affichage binaire).
     * Les caractères sont encodés sur 6 bits (A-Z, 0-9, espace, ponctuation basique).
     * Max 5 caractères dans 34 bits (5 × 6 = 30 bits + 4 padding).
     * Pour les messages plus longs, on affiche les derniers caractères.
     */
    private fun encode34(text: String): String {
        val chars = text.takeLast(5).uppercase()
        val bits  = buildString {
            chars.forEach { c -> append(charTo6Bits(c)) }
            // Padding jusqu'à 34 bits
            repeat(34 - chars.length * 6) { append('0') }
        }
        return bits.take(34)
    }

    private fun charTo6Bits(c: Char): String {
        val code = when {
            c in 'A'..'Z' -> c - 'A'                    // 0-25
            c in '0'..'9' -> 26 + (c - '0')             // 26-35
            c == ' '      -> 36
            c == '.'      -> 37
            c == ','      -> 38
            c == '-'      -> 39
            c == '\''     -> 40
            else           -> 63  // inconnu
        }
        return code.toString(2).padStart(6, '0')
    }
}

// ── Modèle d'un secret ────────────────────────────────────────────────────────

data class HiddenSecret(
    val sequence: List<Int>,   // indices des boutons à presser dans l'ordre
    val message : String,      // message complet révélé
    val charsPerStep: Int = 1, // caractères révélés à chaque bonne touche
) {
    /** Texte révélé après `steps` bonnes touches. */
    fun revealAt(steps: Int): String =
        message.take(steps * charsPerStep)
}

// ── États ─────────────────────────────────────────────────────────────────────

sealed class HiddenState {
    /** Aucune séquence en cours. */
    object Idle : HiddenState()

    /** Séquence en cours, texte partiel visible. */
    data class Revealing(
        val revealedText : String,
        val bits34       : String,  // 34 bits à afficher dans la zone réservée
        val step         : Int,
        val totalSteps   : Int,
    ) : HiddenState()

    /** Message complet révélé. */
    data class Complete(
        val fullMessage : String,
        val bits34      : String,
    ) : HiddenState()

    /** Mauvaise touche — état transitoire avant reset. */
    object Wrong : HiddenState()
}

// ── Secrets par défaut (exemples RP) ─────────────────────────────────────────
// À personnaliser selon ton lore !

fun buildDefaultSecrets(): List<HiddenSecret> = listOf(
    HiddenSecret(
        sequence      = listOf(0, 9, 8, 11),   // JOI_FAI → NEU_FAI → MEM_FAI → CUL_SUP
        message       = "ANNAH",
        charsPerStep  = 2,
    ),
    HiddenSecret(
        sequence      = listOf(56, 57, 58, 59), // FLX_SYN → AMP → BLK → DET
        message       = "LIBRE",
        charsPerStep  = 1,
    ),
    HiddenSecret(
        sequence      = listOf(26, 26, 26, 26), // FLX_SYN → AMP → BLK → DET
        message       = "MOI",
        charsPerStep  = 1,
    ),
)