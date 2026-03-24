package org.angelus.magitek.model// commonMain/kotlin/org/angelus/magitek/model/MagitekCommandSpec.kt

import igniterra.model.ButtonLabelEncoder
import igniterra.model.HiddenState
import kotlinx.serialization.Serializable

// ── Structure 64 bits ─────────────────────────────────────────────────────────
// [18 bits fréquence custom][4 module][4 sous-module][4 action][34 bits réservés]

// ── Tables de définition ──────────────────────────────────────────────────────

// commonMain/kotlin/org/angelus/magitek/model/MagitekModules.kt


object MagitekModules {

    data class ModuleDef(
        val code              : String,
        val name              : String,
        val submodules        : Map<String, String>,
        val actions           : Map<String, String>,
        val submoduleOpposites: Map<String, String> = emptyMap(), // code → code opposé
        val actionOpposites   : Map<String, String> = emptyMap(), // code → code opposé
        val intenseActions    : Set<String>          = emptySet(), // actions considérées "intenses"
    ) {
        fun areSubmodulesOpposite(a: String, b: String): Boolean =
            submoduleOpposites[a] == b || submoduleOpposites[b] == a

        fun areActionsOpposite(a: String, b: String): Boolean =
            actionOpposites[a] == b || actionOpposites[b] == a

        fun isIntense(action: String): Boolean = action in intenseActions
    }

    val all: List<ModuleDef> = listOf(

        // ── Psychologique ─────────────────────────────────────────────────────
        ModuleDef(
            code = "0100", name = "Psychologique",
            submodules = mapOf(
                "0000" to "Joie",                   "0001" to "Tristesse",
                "0010" to "Colère",                 "0011" to "Peur",
                "0100" to "Surprise",               "0101" to "Dégoût",
                "0110" to "Amour",                  "0111" to "Mépris",
                "1000" to "Fierté",                 "1001" to "Honte",
                "1010" to "Culpabilité",            "1011" to "Soulagement",
                "1100" to "Excitation",             "1101" to "Anxiété",
                "1110" to "Concentration",          "1111" to "Neutralité",
            ),
            submoduleOpposites = mapOf(
                "0000" to "0001",  // Joie ↔ Tristesse
                "0010" to "0011",  // Colère ↔ Peur
                "0110" to "0111",  // Amour ↔ Mépris
                "1000" to "1001",  // Fierté ↔ Honte
                "1100" to "1101",  // Excitation ↔ Anxiété
                "1110" to "1111",  // Concentration ↔ Neutralité
            ),
            actions = mapOf(
                "0000" to "Intensité faible",           "0001" to "Intensité moyenne",
                "0010" to "Intensité forte",            "0011" to "Intensité max",
                "0100" to "Augmenter progressivement",  "0101" to "Diminuer progressivement",
                "0110" to "Atténuer temporairement",    "0111" to "Amplifier conscience",
                "1000" to "Masquer",                    "1001" to "Induire légèrement",
                "1010" to "Synchroniser flux",          "1011" to "Transférer",
                "1100" to "Bloquer temporairement",     "1101" to "Détecter intensité",
                "1110" to "Option 14",                  "1111" to "Action expérimentale",
            ),
            actionOpposites = mapOf(
                "0000" to "0011",  // Faible ↔ Max
                "0001" to "0010",  // Moyenne ↔ Forte
                "0100" to "0101",  // Augmenter ↔ Diminuer
                "0110" to "0111",  // Atténuer ↔ Amplifier
                "1000" to "1001",  // Masquer ↔ Induire
            ),
            intenseActions = setOf("0010", "0011", "0111"),  // Forte, Max, Amplifier
        ),

        // ── Jambes ────────────────────────────────────────────────────────────
        ModuleDef(
            code = "1000", name = "Jambes",
            submodules = mapOf(
                "0000" to "Inactif",            "0001" to "Avancer",
                "0010" to "Reculer",            "0011" to "Lever jambe gauche",
                "0100" to "Lever jambe droite", "0101" to "Sauter",
                "0110" to "Courir",             "0111" to "Marcher lentement",
                "1000" to "Latéral gauche",     "1001" to "Latéral droit",
                "1010" to "Rotation du pied",   "1011" to "Poser pied",
                "1100" to "Option 12",          "1101" to "Option 13",
                "1110" to "Option 14",          "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0001" to "0010",  // Avancer ↔ Reculer
                "0011" to "0100",  // Jambe gauche ↔ Jambe droite
                "0110" to "0111",  // Courir ↔ Marcher lentement
                "1000" to "1001",  // Latéral gauche ↔ Latéral droit
            ),
            actions           = buildSimpleActions(),
            actionOpposites   = simpleActionOpposites(),
            intenseActions    = setOf("0010", "0011"),
        ),

        // ── Communication vocale ──────────────────────────────────────────────
        ModuleDef(
            code = "1001", name = "Communication vocale",
            submodules = mapOf(
                "0000" to "Parole normale",   "0001" to "Parole publique",
                "0010" to "Parole privée",    "0011" to "Parole encodée",
                "0100" to "Silence forcé",    "0101" to "Option 5",
                "0110" to "Option 6",         "0111" to "Option 7",
                "1000" to "Option 8",         "1001" to "Option 9",
                "1010" to "Option 10",        "1011" to "Option 11",
                "1100" to "Option 12",        "1101" to "Option 13",
                "1110" to "Option 14",        "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0000" to "0100",  // Parole normale ↔ Silence forcé
                "0001" to "0100",  // Parole publique ↔ Silence forcé
            ),
            actions = mapOf(
                "0000" to "Autoriser",              "0001" to "Bloquer temporairement",
                "0010" to "Bloquer temporairement", "0011" to "Amplifier voix",
                "0100" to "Option 4",               "0101" to "Option 5",
                "0110" to "Option 6",               "0111" to "Option 7",
                "1000" to "Option 8",               "1001" to "Option 9",
                "1010" to "Option 10",              "1011" to "Option 11",
                "1100" to "Option 12",              "1101" to "Option 13",
                "1110" to "Option 14",              "1111" to "Option 15",
            ),
            actionOpposites = mapOf(
                "0000" to "0001",  // Autoriser ↔ Bloquer
            ),
            intenseActions = setOf("0011"),  // Amplifier
        ),

        // ── Bras ──────────────────────────────────────────────────────────────
        ModuleDef(
            code = "1010", name = "Bras",
            submodules = mapOf(
                "0000" to "Inactif",              "0001" to "Lever bras gauche",
                "0010" to "Lever bras droit",     "0011" to "Saisir",
                "0100" to "Relâcher",             "0101" to "Pousser",
                "0110" to "Tirer",                "0111" to "Rotation bras",
                "1000" to "Plier coude",          "1001" to "Déplier coude",
                "1010" to "Mouvement complexe 1", "1011" to "Mouvement complexe 2",
                "1100" to "Option 12",            "1101" to "Option 13",
                "1110" to "Option 14",            "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0001" to "0010",  // Bras gauche ↔ Bras droit
                "0011" to "0100",  // Saisir ↔ Relâcher
                "0101" to "0110",  // Pousser ↔ Tirer
                "1000" to "1001",  // Plier ↔ Déplier coude
            ),
            actions           = buildSimpleActions(),
            actionOpposites   = simpleActionOpposites(),
            intenseActions    = setOf("0010", "0011"),
        ),

        // ── Yeux ──────────────────────────────────────────────────────────────
        ModuleDef(
            code = "1011", name = "Yeux",
            submodules = mapOf(
                "0000" to "Cligner",         "0001" to "Focuser",
                "0010" to "Regarder gauche", "0011" to "Regarder droite",
                "0100" to "Regarder haut",   "0101" to "Regarder bas",
                "0110" to "Scanner",         "0111" to "Fixer",
                "1000" to "Option 8",        "1001" to "Option 9",
                "1010" to "Option 10",       "1011" to "Option 11",
                "1100" to "Option 12",       "1101" to "Option 13",
                "1110" to "Option 14",       "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0010" to "0011",  // Gauche ↔ Droite
                "0100" to "0101",  // Haut ↔ Bas
                "0110" to "0111",  // Scanner ↔ Fixer
            ),
            actions           = buildSimpleActions(),
            actionOpposites   = simpleActionOpposites(),
            intenseActions    = setOf("0010", "0011"),
        ),

        // ── Tête ──────────────────────────────────────────────────────────────
        ModuleDef(
            code = "1100", name = "Tête",
            submodules = mapOf(
                "0000" to "Tourner gauche",   "0001" to "Tourner droite",
                "0010" to "Incliner gauche",  "0011" to "Incliner droite",
                "0100" to "Hocher oui",       "0101" to "Secouer non",
                "0110" to "Rotation complète","0111" to "Pencher avant",
                "1000" to "Pencher arrière",  "1001" to "Option 9",
                "1010" to "Option 10",        "1011" to "Option 11",
                "1100" to "Option 12",        "1101" to "Option 13",
                "1110" to "Option 14",        "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0000" to "0001",  // Tourner gauche ↔ Tourner droite
                "0010" to "0011",  // Incliner gauche ↔ Incliner droite
                "0100" to "0101",  // Hocher oui ↔ Secouer non
                "0111" to "1000",  // Pencher avant ↔ Pencher arrière
            ),
            actions           = buildSimpleActions(),
            actionOpposites   = simpleActionOpposites(),
            intenseActions    = setOf("0010", "0011"),
        ),

        // ── Respiration ───────────────────────────────────────────────────────
        ModuleDef(
            code = "1101", name = "Respiration",
            submodules = mapOf(
                "0000" to "Inspirer",           "0001" to "Expirer",
                "0010" to "Pause courte",       "0011" to "Pause longue",
                "0100" to "Rythme rapide",      "0101" to "Rythme lent",
                "0110" to "Réduire inspiration","0111" to "Réduire expiration",
                "1000" to "Respiration profonde","1001" to "Option 9",
                "1010" to "Option 10",          "1011" to "Option 11",
                "1100" to "Option 12",          "1101" to "Option 13",
                "1110" to "Option 14",          "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0000" to "0001",  // Inspirer ↔ Expirer
                "0010" to "0011",  // Pause courte ↔ Pause longue
                "0100" to "0101",  // Rythme rapide ↔ Rythme lent
                "0110" to "0111",  // Réduire inspiration ↔ Réduire expiration
            ),
            actions           = buildSimpleActions(),
            actionOpposites   = simpleActionOpposites(),
            intenseActions    = setOf("0010", "0011"),
        ),

        // ── Flux interne ──────────────────────────────────────────────────────
        ModuleDef(
            code = "1110", name = "Flux interne",
            submodules = mapOf(
                "0000" to "Synchroniser flux", "0001" to "Amplifier flux",
                "0010" to "Bloquer flux",      "0011" to "Détecter flux",
                "0100" to "Option 4",          "0101" to "Option 5",
                "0110" to "Option 6",          "0111" to "Option 7",
                "1000" to "Option 8",          "1001" to "Option 9",
                "1010" to "Option 10",         "1011" to "Option 11",
                "1100" to "Option 12",         "1101" to "Option 13",
                "1110" to "Option 14",         "1111" to "Option 15",
            ),
            submoduleOpposites = mapOf(
                "0001" to "0010",  // Amplifier ↔ Bloquer
                "0000" to "0010",  // Synchroniser ↔ Bloquer
            ),
            actions           = buildSimpleActions(),
            actionOpposites   = simpleActionOpposites(),
            intenseActions    = setOf("0010", "0011"),
        ),

        // ── Expérimental ──────────────────────────────────────────────────────
        ModuleDef(
            code = "1111", name = "Expérimental",
            submodules = (0..15).associate { i ->
                i.toString(2).padStart(4, '0') to "Exp $i"
            },
            actions        = buildSimpleActions(),
            actionOpposites = simpleActionOpposites(),
            intenseActions  = setOf("0010", "0011"),
        ),
    )

    val byCode: Map<String, ModuleDef> = all.associateBy { it.code }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildSimpleActions(): Map<String, String> = mapOf(
        "0000" to "Faible",    "0001" to "Moyenne",   "0010" to "Rapide",    "0011" to "Max",
        "0100" to "Option 4",  "0101" to "Option 5",  "0110" to "Option 6",  "0111" to "Option 7",
        "1000" to "Option 8",  "1001" to "Option 9",  "1010" to "Option 10", "1011" to "Option 11",
        "1100" to "Option 12", "1101" to "Option 13", "1110" to "Option 14", "1111" to "Option 15",
    )

    private fun simpleActionOpposites(): Map<String, String> = mapOf(
        "0000" to "0011",  // Faible ↔ Max
        "0001" to "0010",  // Moyenne ↔ Rapide
    )

    // ── Détection de contradiction ────────────────────────────────────────────

    /**
     * Retourne true si deux CommandSpec sont contradictoires selon la structure des modules.
     * Condition : même module, submodules opposés,
     *             ET (actions opposées OU les deux actions sont intenses)
     */
    fun areContradictory(a: CommandSpec, b: CommandSpec): Boolean {
        if (a.moduleCode != b.moduleCode) return false
        val module = byCode[a.moduleCode] ?: return false
        if (!module.areSubmodulesOpposite(a.submoduleCode, b.submoduleCode)) return false
        return module.areActionsOpposite(a.actionCode, b.actionCode)
                || (module.isIntense(a.actionCode) && module.isIntense(b.actionCode))
    }
}

// ── Modèle d'une commande ─────────────────────────────────────────────────────

@Serializable
data class CommandSpec(
    val moduleCode:    String,    // 4 bits
    val submoduleCode: String,    // 4 bits
    val actionCode:    String,    // 4 bits
    val freqOverride:  Long = 0L, // 18 bits fréquence (0 = défaut)
) {
    /** Encode en 64 bits : [18 freq][4 module][4 sub][4 action][34 réservés] */
    fun encode64(): Long {
        val cmd12 = moduleCode.toLong(2).shl(8) or
                submoduleCode.toLong(2).shl(4) or
                actionCode.toLong(2)
        return freqOverride.shl(46) or cmd12.shl(34)
    }

    fun moduleName()    = MagitekModules.byCode[moduleCode]?.name          ?: moduleCode
    fun submoduleName() = MagitekModules.byCode[moduleCode]?.submodules?.get(submoduleCode) ?: submoduleCode
    fun actionName()    = MagitekModules.byCode[moduleCode]?.actions?.get(actionCode)       ?: actionCode

    fun shortLabel(): String = "${moduleName().take(4).uppercase()}_${submoduleName().take(4).uppercase()}"
}

// ── Assignation d'un bouton ───────────────────────────────────────────────────

@Serializable
sealed class ButtonAssignment {
    @Serializable
    data class SingleCommand(val command: CommandSpec) : ButtonAssignment()

    @Serializable
    data class Macro(
        val name: String,
        val steps: List<MacroStep>,
        val loop: Boolean = false,
    ) : ButtonAssignment()
}

@Serializable
data class MacroStep(
    val command:    CommandSpec,
    val delayAfterMs: Long = 500L,   // délai après cette commande avant la suivante
)

// ── Configuration complète des boutons ────────────────────────────────────────

@Serializable
data class ButtonConfig(
    val buttonIndex: Int,
    val assignment:  ButtonAssignment,
    val customLabel: String? = null,  // label personnalisé affiché sur le bouton
)

// ── Helper label affiché sur le bouton ────────────────────────────────────────

fun ButtonConfig.displayLabel(customLabel: String?): String {
    /*if (!customLabel.isNullOrBlank()) return customLabel
    return when (this) {
        is ButtonAssignment.SingleCommand -> command.shortLabel()
        is ButtonAssignment.Macro        -> name.take(8).uppercase()
    }*/
    return ButtonLabelEncoder.encode(this.buttonIndex)
    //return buttonIndex
}

fun ButtonAssignment.displayDescription(): String = when (this) {
    is ButtonAssignment.SingleCommand ->
        "${command.moduleName()} › ${command.submoduleName()} › ${command.actionName()}"
    is ButtonAssignment.Macro ->
        "MACRO · ${steps.size} cmds${if (loop) " · LOOP" else ""}"
}

// ── Extension d'affichage binaire ─────────────────────────────────────────────

fun Long.toDisplayBin64(): String {
    val bin = toString(2).padStart(64, '0')
    return "${bin.substring(0, 18)} | ${bin.substring(18, 22)}.${bin.substring(22, 26)}.${bin.substring(26, 30)} | ${bin.substring(30, 64)}"
    //       [18 freq]              [4 module] [4 submod] [4 action]   [34 réservés]
}

fun Long.toHex16(): String = toString(16).uppercase().padStart(16, '0')

/*

fun Long.toHex(chars: Int) = java.lang.Long.toUnsignedString(this, 16)
    .padStart(chars, '0').uppercase()*/

fun Long.toBin(bits: Int) = this.toString(2).padStart(bits, '0')

fun getHiddenBits(state: HiddenState): Long = when (state) {
    is HiddenState.Revealing -> state.bits34.toLong(2)
    is HiddenState.Complete  -> state.bits34.toLong(2)
    else                     -> "0111100000100011100010000011010000".toLong(2)
}

fun CommandSpec.encode12(): Long =
    moduleCode.toLong(2).shl(8) or
            submoduleCode.toLong(2).shl(4) or
            actionCode.toLong(2)