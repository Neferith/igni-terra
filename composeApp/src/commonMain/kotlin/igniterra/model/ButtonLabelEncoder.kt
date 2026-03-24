// commonMain/kotlin/org/angelus/magitek/model/ButtonLabelEncoder.kt

package igniterra.model

/**
 * Encode l'index d'un bouton (0–63) en un code 3 caractères.
 *
 * Structure : 6 bits → [2 bits][2 bits][2 bits]
 *   Position 1 (bits 5–4) : X=00  N=01  M=10  R=11
 *   Position 2 (bits 3–2) : A=00  O=01  E=10  I=11
 *   Position 3 (bits 1–0) : R=00  L=01  F=10  K=11
 *
 * Exemples :
 *   0  = 000000 → X-A-R = "XAR"
 *   1  = 000001 → X-A-L = "XAL"
 *   5  = 000101 → X-O-L = "XOL"
 *   32 = 100000 → M-A-R = "MAR"
 *   63 = 111111 → R-I-K = "RIK"
 *
 * Le code est réversible : decodeLabel("XAR") == 0
 */
object ButtonLabelEncoder {

    private val CHARS_1 = charArrayOf('X', 'N', 'M', 'R')   // bits 5–4
    private val CHARS_2 = charArrayOf('A', 'O', 'E', 'I')   // bits 3–2
    private val CHARS_3 = charArrayOf('R', 'L', 'F', 'K')   // bits 1–0

    fun encode(index: Int): String {
        require(index in 0..63) { "Index must be 0–63" }
        val c1 = CHARS_1[(index shr 4) and 0x3]
        val c2 = CHARS_2[(index shr 2) and 0x3]
        val c3 = CHARS_3[(index shr 0) and 0x3]
        return "$c1$c2$c3"
    }

    fun decode(label: String): Int? {
        if (label.length != 3) return null
        val i1 = CHARS_1.indexOf(label[0]).takeIf { it >= 0 } ?: return null
        val i2 = CHARS_2.indexOf(label[1]).takeIf { it >= 0 } ?: return null
        val i3 = CHARS_3.indexOf(label[2]).takeIf { it >= 0 } ?: return null
        return (i1 shl 4) or (i2 shl 2) or i3
    }

    /** Table complète des 64 codes pour référence */
    val allLabels: List<String> = (0..63).map { encode(it) }
}