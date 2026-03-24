// commonMain/kotlin/org/angelus/magitek/model/MagitekCipher.kt

package igniterra.model

/**
 * Alphabet magitek — 64 valeurs encodées via ButtonLabelEncoder
 *
 *  0–25  → A–Z
 * 26–35  → 0–9
 * 36     → .
 * 37     → ,
 * 38     → !
 * 39     → ?
 * 40     → '
 * 41     → -
 * 42     → ■
 * 43     → ◆
 * 44     → ♦
 * 45     → ▲
 * 46     → ●
 * 47     → ▪
 * 48–63  → réservé (affiché comme █ — caractère illisible intentionnel)
 */
object MagitekCipher {

    private val ALPHABET: Array<String> = Array(64) { i ->
        when (i) {
            in 0..25  -> ('A' + i).toString()
            in 26..35 -> ('0' + (i - 26)).toString()
            36 -> "."
            37 -> ","
            38 -> "!"
            39 -> "?"
            40 -> "'"
            41 -> "-"
            42 -> "■"
            43 -> "◆"
            44 -> "♦"
            45 -> "▲"
            46 -> "●"
            47 -> "▪"
            else -> "█"   // 48–63 : illisible / réservé
        }
    }

    /** Encode un texte en liste de codes 3 lettres */
    fun encode(text: String): List<String> =
        text.uppercase().mapNotNull { c -> charToIndex(c) }
            .map { ButtonLabelEncoder.encode(it) }

    /** Décode une liste de codes en texte */
    fun decode(codes: List<String>): String =
        codes.mapNotNull { code ->
            ButtonLabelEncoder.decode(code)?.let { ALPHABET.getOrNull(it) }
        }.joinToString("")

    /** Encode en une seule string séparée par des espaces */
    fun encodeToString(text: String): String = encode(text).joinToString(" ")

    private fun charToIndex(c: Char): Int? = when (c) {
        in 'A'..'Z' -> c - 'A'
        in '0'..'9' -> 26 + (c - '0')
        '.' -> 36
        ',' -> 37
        '!' -> 38
        '?' -> 39
        '\'' -> 40
        '-' -> 41
        '■' -> 42
        '◆' -> 43
        '♦' -> 44
        '▲' -> 45
        '●' -> 46
        '▪' -> 47
        ' ' -> null  // les espaces sont ignorés / gérés séparément
        else -> null
    }

    /** Table de référence complète pour debug (mode édition seulement) */
    fun referenceTable(): String = (0..47).joinToString("\n") { i ->
        "${ButtonLabelEncoder.encode(i)} = ${ALPHABET[i]}"
    }

    /** Encode une liste d'indices en codes puis décode en texte */
    fun fromIndices(indices: List<Int>): String =
        indices.mapNotNull { ALPHABET.getOrNull(it) }.joinToString("")

    /** Récupère le caractère correspondant à un index */
    fun charAt(index: Int): String = ALPHABET.getOrNull(index) ?: ""
}

// ── Message caché — à configurer ─────────────────────────────────────────────

/**
 * Le message affiché au dos de la télécommande.
 * Les espaces dans le texte créent des lignes séparées à l'affichage.
 * Chaque mot devient une ligne de codes.
 */
data class HiddenBackMessage(
    val title : String,           // affiché en clair (ex: titre de section RP)
    val words : List<List<String>>, // mots encodés — chaque mot = liste de codes
)

fun buildHiddenBackMessage(): HiddenBackMessage {
    // ← Modifie ce texte pour changer le message caché
    val rawText = "Ceci est un avertissement. Si tu as été assez intelligente pour traduire ce message, alors sache que je ne lâcherai jamais le morceau. Je pourrais juste prendre le contrôle de ta « fiancée », mais ce ne serait pas amusant, non ? Non, non, l’empire n’est plus. En vérité, cela n’a plus réellement d’importance. Alors, autant en profiter, non ? Ce que je veux, ce que je souhaite au plus profondément, c’est que tu ne puisses plus la regarder sans te dire : est-ce vraiment elle derrière ? Qui te dit qu’au fond, elle n’est pas autre ? Et si elle ne faisait que ce que Decimus Sas Varen lui demande ? Peut-être même qu’il peut voir à travers elle, qui sait ? Après tout, c’est bien elle qui a bricolé cette télécommande, alors comment se fait-il que ce message se trouve dessus ? réfléchis bien à tout ceci, Adrila. A mes yeux, tu n’es qu’une traitresse. Je vais t’utiliser et quand nous en aurons terminé, je me révèlerai et nous pourrons régler cela entre nous. Ma première demande est simple : je veux que tu tues Aenor."

    val words = rawText.split(" ").map { word ->
        MagitekCipher.encode(word)
    }

    return HiddenBackMessage(
        title = "// Un message gravé à l'arrière de la télécommande //",
        words = words,
    )
}