package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound

// ══════════════════════════════════════════════════════════════════════════════
// DANSE MACABRE — fusil compact magitek prêté par Eleanor à Sébastien
// ══════════════════════════════════════════════════════════════════════════════

private const val DM_DOC_REF = "DOC-NLRP-DM-0007 · REV.1"

private val DM_SERIAL: String = run {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789"
    val part1 = (1..4).map { chars.random() }.joinToString("")
    val part2 = (1..4).map { chars.random() }.joinToString("")
    "DM-$part1-$part2"
}

// ── Jeu : même mécanique, autre habillage ─────────────────────────────────────
val DanseMacabreScenario = ShooterScenario(
    hudTitle          = "DANSE MACABRE — TEST DE CONCENTRATION",
    civiliansLabel    = "CIVILS",
    savedLabel        = "A L'ABRI",
    lostLabel         = "VICTIMES",
    endSavedLabel     = "A l'abri",
    endLostLabel      = "Victimes",
    civilianSaved     = "A L'ABRI ! +50",
    civilianSavedLife = "A L'ABRI ! +50 +VIE",
    civilianLost      = "UN CIVIL EST PERDU...",
    civilianKilled    = "VICTIME COLLATERALE !",
    killedMessageRed  = true,
    firstLoss         = "Ce n'est pas un barbecue !",
    bossThrows        = "!! IL PROJETTE UN CIVIL !!",
    bossImmune        = "!! INCENDIAIRES MAX REQUIS !!",
    levelUp           = listOf("INCENDIAIRES α CHARGEES !", "INCENDIAIRES β CHARGEES !", "INCENDIAIRES MAX !"),
    weaponLabels      = listOf("MUNITIONS INCENDIAIRES", "INCENDIAIRES α", "INCENDIAIRES β", "INCENDIAIRES MAX"),
    partsUnit         = "cartouches",
    maxLabel          = "CHARGE MAX",
    victoryButton     = "Test termine. Viens ici.",
    bitterButton      = "Test termine... Viens ici.",
    abandonButton     = "Abandonner le test.",
    maxAttempts       = 3,
    adultCivilian     = true,
)

private val DM_INTRO = "Sébastien,\n" +
        "Tu as accepté les conditions. Reste à savoir si tu es capable de les respecter.\n" +
        "Ce qui suit est une simulation. Les morts-vivants avancent et des civils se trouvent sur leur chemin. Une munition incendiaire ne fait pas la différence entre les deux. Toi, tu dois la faire.\n" +
        "Ramasse les cartouches incendiaires en chemin et mets les civils à l'abri. Quelque chose de très gros arrive à la fin. Il ne craindra que le feu à pleine charge.\n" +
        "Tu as droit à trois tentatives. Ce que tu ramasses, tu le gardes d'une tentative à l'autre.\n" +
        "Et souviens-toi : ce n'est pas un barbecue improvisé.\n" +
        "Eleanor Dubrie\n\n"

private val DM_ENDINGS = EndTexts(
    victoryTitle = "CERTIFICATION ACCORDEE",
    bitterTitle  = "CERTIFICATION SOUS RESERVE",
    defeatTitle  = "CERTIFICATION REFUSEE",
    victoryCode  = "NLRP-DM-7K4Q",
    bitterCode   = "NLRP-DM-3R9X",
    victory = """
Sébastien,

Test réussi. Pour être honnête, je ne m'y attendais pas.
La Danse Macabre est à toi pour ce soir, avec ses munitions incendiaires. Respecte les conditions, toutes les conditions, et rapporte-moi l'arme à la fin du mandat.
Et la prochaine fois, viens me voir quelques jours avant.
Envoie-moi le code ci-dessous, tu auras ton arme en retour.

Eleanor Dubrie
""".trim(),
    bitter = """
Sébastien,

Tu as tenu jusqu'au bout. Mais pas sans dégâts.
Je te confie malgré tout la Danse Macabre pour ce soir. Je te rappelle simplement l'article 06.3 : je ne porterai aucune responsabilité pour ce que tu en feras. Chaque victime collatérale sera la tienne, pas la mienne.
Une munition incendiaire ne fait pas la différence entre un mort et un vivant. Toi, tu dois la faire.
Envoie-moi le code ci-dessous, tu auras ton arme en retour.

Eleanor Dubrie
""".trim(),
    defeat = """
Sébastien,

Échec. Conformément à l'article 06.4, l'usage de la Danse Macabre t'est refusé.
Ce n'est pas une punition. Face à des non-morts, une arme mal maîtrisée blesse plus d'alliés que d'ennemis.
Tu iras ce soir avec ce que tu sais faire. Et la prochaine fois, viens me voir bien avant. Nous prendrons le temps de t'entraîner.

Eleanor Dubrie
""".trim(),
)

// ── Manuel ────────────────────────────────────────────────────────────────────
val DanseMacabreManual: ManualSpec by lazy { buildDanseMacabre(consultation = false) }

// Exemplaire de consultation (Adrila) : même contenu, sans test, note dédiée
val DanseMacabreConsultManual: ManualSpec by lazy { buildDanseMacabre(consultation = true) }

private const val DM_CONSULT_NOTE = "Ma chérie.\n" +
        "Sébastien a eu une demande particulière aujourd’hui. Je dois avouer que je n’apprécie pas qu’il s’y prenne au dernier moment comme cela. Néanmoins, je compte lui fournir une arme ce soir accompagnée de ce manuel. Il devra juste passer un petit exercice de concentration. Rien d’impossible pour un viera qui a quarante ans d’expérience de l’arc…\n" +
        "Si tu refuses l’usage de cette arme, tiens-moi au courant.\n\n" +
        "Je t’embrasse Adrila.\n\n" +
        "Eleanor Dubrie\n\n" +
        "PS : Il y a du lait pour Victoria dans la cuisine."

private fun buildDanseMacabre(consultation: Boolean): ManualSpec =
    ManualSpec(
        orgShort             = "NLRP / DAE",
        organization         = "Compagnie de la nouvelle lune · Division Armements Éthériques",
        weaponName           = "Danse Macabre",
        subtitle             = "Fusil Compact Magitek · Munitions Incendiaires",
        badge                = "Confidentiel",
        docRef               = DM_DOC_REF,
        coverMeta            = listOf(
            "Référence"       to "DOC-NLRP-DM-0007",
            "Révision"        to "REV.1 — ACTIF",
            "Classification"  to "CONFIDENTIEL",
            "Portée efficace" to "25 YALMS",
            "Conceptrice"     to "Eleanor Dubrie",
            "Emprunteur"      to "Sébastien Soufflazur",
        ),
        serialNumber         = DM_SERIAL,
        footerDocRef         = DM_DOC_REF,
        footerTitle          = "DANSE MACABRE — MANUEL TECHNIQUE",
        footerClassification = "CONFIDENTIEL",
        watermark            = "E.D.",
        pages = listOf(
            ManualPage("00", "Couverture")     { CoverSection(it) },
            ManualPage("01", "Vue d'ensemble") { DmOverview() },
            ManualPage("02", "Spécifications") { DmSpecs() },
            ManualPage("03", "Sécurité")       { DmSafety() },
            ManualPage("04", "Munitions")      { DmAmmo() },
            ManualPage("05", "FAQ")            { DmFaq() },
            ManualPage("06", "CGU")            { DmCgu(it) },
            ManualPage("07", "Inventaire")     { DmInventory() },
        ),
        autoPlayMusic = true,
        transmissionNote = if (consultation) DM_CONSULT_NOTE else null,
        certification = if (consultation) null else CertificationTest(
            introHeader   = "COMPAGNIE DE LA NOUVELLE LUNE · DAE",
            introTitle    = "DANSE MACABRE",
            introSubtitle = "TEST DE CONCENTRATION — SIMULATION DE COMBAT",
            introText     = DM_INTRO,
            scenario      = DanseMacabreScenario,
            endings       = DM_ENDINGS,
        ),
    )

// ── 01 Vue d'ensemble ─────────────────────────────────────────────────────────
@Composable
private fun DmOverview() {
    SectionHead("01", "Vue d'ensemble")
    Prose("La Danse Macabre n'est pas un fusil d'assaut de grande envergure, mais une arme compacte. Elle tient dans un sac et peut même se dissimuler sous un manteau. Sa conception est volontairement simple : elle a été assemblée à partir de matériel de récupération, principalement du titane.")
    Prose("Elle ne fonctionne pas à la poudre. Sa propulsion est assurée par une micro-cellule magitek, qui projette ce que contient le chargeur. Associée aux munitions incendiaires fournies, elle répond à un usage précis : la destruction des créatures mortes-vivantes.")
    NoteBox("Note de conception") {
        Prose("Arme légère, faible recul, puissance de feu modérée. Elle ne fera pas de miracle face à une horde. Elle demande du sang-froid, de la précision, et un utilisateur qui sait quand ne pas tirer.")
    }
}

// ── 02 Spécifications ─────────────────────────────────────────────────────────
@Composable
private fun DmSpecs() {
    SectionHead("02", "Spécifications Techniques")
    SpecGrid(
        listOf(
            "Désignation"       to plain("DANSE MACABRE MK.I"),
            "Type"              to plain("Fusil compact magitek"),
            "Propulsion"        to plain("Micro-cellule magitek"),
            "Portée efficace"   to plain("~25 yalms"),
            "Au-delà"           to plain("Perte de stabilité"),
            "Recul"             to plain("Faible"),
            "Puissance de feu"  to plain("Modérée"),
            "Mode de tir"       to struck("Coup par coup · ", "Rafale"),
            "Capacité"          to plain("Chargeur de 5 munitions"),
            "Matériaux"         to plain("Titane de récupération"),
            "Encombrement"      to plain("Dissimulable sous un manteau"),
        )
    )
    SubHead("02.1", "Micro-cellule magitek")
    Prose("La micro-cellule doit tenir suffisamment longtemps pour un mandat. Il peut toutefois être nécessaire de la recharger ou de la remplacer. Vérifiez sa charge avant chaque sortie.")
    SubHead("02.2", "Mode rafale")
    Prose("Le modèle d'origine permet le tir en rafale. Ce mode a été retiré de cet exemplaire : il demande une technique que l'emprunteur ne possède pas encore.")
}

// ── 03 Sécurité ───────────────────────────────────────────────────────────────
@Composable
private fun DmSafety() {
    SectionHead("03", "Sécurité & Utilisation")
    WarningBox("[!] Avertissement principal") {
        Prose("Une arme chargée est une arme dangereuse. Ne jamais pointer la Danse Macabre vers ce que l'on ne souhaite pas détruire.")
    }
    Prose("La sécurité dispose de deux modes : manuel et automatique. En mode automatique, si aucune signature éthérique de l'utilisateur n'est détectée, la sécurité se réactive d'elle-même. C'est ce qui se produit, par exemple, lorsque l'arme tombe au sol.")
    val checklist = listOf(
        "Sécurité engagée en permanence hors situation de tir.",
        "Mode de sécurité automatique recommandé en mission.",
        "Tir exclusivement coup par coup.",
        "Ne pas tirer au-delà de 25 yalms : la trajectoire devient instable.",
        "Vérifier que rien n'obstrue le canon avant de charger.",
        "Aucune personne entre le tireur et la cible.",
    )
    Column(
        Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        checklist.forEachIndexed { i, item ->
            CheckItem(item)
            if (i < checklist.lastIndex)
                Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr.copy(alpha = 0.7f)))
        }
    }
    SubHead("03.1", "Surchauffe")
    Prose("En raison de sa petite taille, la Danse Macabre chauffe vite. Des tirs trop rapprochés peuvent bloquer la micro-cellule pendant plusieurs minutes. Espacez vos tirs : une arme bloquée au milieu d'une horde est une arme inutile.")
    WarningBox("[!] Défaut connu") {
        Prose("La surchauffe est un défaut connu. Il sera corrigé avec le temps. — E.D.")
    }
}

// ── 04 Munitions ──────────────────────────────────────────────────────────────
@Composable
private fun DmAmmo() {
    SectionHead("04", "Munitions")
    Prose("La propulsion étant assurée par la micro-cellule magitek, la Danse Macabre peut tirer à peu près n'importe quoi, tant que cela rentre dans le chargeur et n'obstrue pas le canon. Des munitions adaptées restent toutefois plus optimales.")
    SubHead("04.1", "Munitions standard")
    Prose("Chargeur de 5. Contre une créature morte-vivante animée, une balle standard ralentit, elle ne détruit pas.")
    SubHead("04.2", "Munitions incendiaires")
    Prose("Conçues à l'origine pour un fusil à pompe, puis adaptées à la Danse Macabre. Une fois plantée dans la chair, la munition se met à brûler de l'intérieur, jusqu'à se propager dans les articulations. C'est ce qui permet de désintégrer la structure d'une créature morte-vivante.")
    SpecGrid(
        listOf(
            "Dotation" to plain("5 munitions"),
            "Soit"     to plain("1 chargeur complet"),
            "Cible"    to plain("Morts-vivants exclusivement"),
            "Effet"    to plain("Combustion interne, articulations"),
        )
    )
    Spacer(Modifier.height(14.dp))
    WarningBox("[!] Usage restreint") {
        Prose("Très faciles à utiliser, très dangereuses. Une munition incendiaire qui manque sa cible ne s'éteint pas pour autant. Ne jamais tirer à proximité de civils, de compagnons, de bâtiments ou de végétation sèche.")
    }
}

// ── 05 FAQ ────────────────────────────────────────────────────────────────────
@Composable
private fun DmFaq() {
    SectionHead("05", "Foire aux questions")
    FaqItem("Une FAQ, c'est quoi ?", "Une Foire Aux Questions. Les questions que l'on se pose habituellement avant de demander une arme le soir même.")
    FaqItem("Et les CGU ?", "Les Conditions Générales d'Utilisation. Section 06. Elles doivent être acceptées avant toute remise de l'arme.")
    FaqItem("Pourquoi pas de tir en rafale ?", "Parce que tu n'as pas encore la technique. Le mode a été retiré de ton exemplaire.")
    FaqItem("Puis-je tirer autre chose que les munitions fournies ?", "Oui, tant que cela rentre dans le chargeur et n'obstrue pas le canon. Ce n'est pas une invitation à expérimenter.")
    FaqItem("Une balle suffit-elle contre un mort-vivant ?", "Non. Une balle le ralentit. Pour le détruire, il faut atteindre ses articulations. D'où les munitions incendiaires.")
    FaqItem("L'arme s'est bloquée, que faire ?", "Surchauffe. Mets-toi à couvert, remets la sécurité et attends quelques minutes. La secouer ne sert à rien.")
    FaqItem("J'ai fait tomber l'arme, elle ne tire plus.", "La sécurité automatique s'est réactivée. C'est voulu.")
    FaqItem("Et si j'échoue au test ?", "Tu as droit à trois tentatives. Ce que tu as ramassé, tu le gardes d'une tentative à l'autre. Au-delà, c'est non.")
    FaqItem("Et l'Igni Terra ?", "L'Igni Terra n'existe plus.")
    FaqItem("Et si je cause des dommages collatéraux ?", "Voir article 06.3.")
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            "Q. $question",
            fontSize = 11.sp, fontFamily = Mono, color = Teal, letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Prose(answer)
    }
}

// ── 06 CGU ────────────────────────────────────────────────────────────────────
@Composable
private fun DmCgu(ctx: ManualPageContext) {
    SectionHead("06", "Conditions Générales d'Utilisation")
    SubHead("06.1", "Objet")
    Prose("Les présentes conditions encadrent le prêt de la Danse Macabre et de sa dotation de munitions incendiaires par Eleanor Dubrie à Sébastien Soufflazur, pour le mandat en cours.")
    SubHead("06.2", "Propriété & prêt")
    Prose("La Danse Macabre reste la propriété exclusive d'Eleanor Dubrie. Il s'agit d'un prêt, non d'un don. L'arme doit être restituée à l'issue du mandat, avec ses chargeurs et toute munition incendiaire non utilisée.")
    SubHead("06.3", "Responsabilité")
    Prose("L'emprunteur est seul responsable de l'usage qu'il fait de l'arme. Eleanor Dubrie décline toute responsabilité en cas de blessure, de décès, d'incendie ou de dommage collatéral.")
    SubHead("06.4", "Certification")
    Prose("L'usage de la Danse Macabre est subordonné à la réussite d'un test de concentration. L'acceptation des présentes conditions déclenche ce test. Trois tentatives sont accordées. En cas d'échec, l'usage de l'arme est refusé.")
    SubHead("06.5", "Délais")
    Prose("Toute demande d'équipement doit être formulée plusieurs jours avant le mandat. La présente dérogation ne constitue pas un précédent.")
    Spacer(Modifier.height(20.dp))

    // Exemplaire de consultation : pas de test
    if (ctx.spec.certification == null) {
        Box(Modifier.border(1.dp, GoldDk).padding(horizontal = 18.dp, vertical = 8.dp)) {
            Text("EXEMPLAIRE DE CONSULTATION — NLRP / DAE", fontSize = 9.sp, letterSpacing = 3.sp, fontFamily = Mono, color = GoldDk)
        }
        return
    }

    val result = ctx.certificationResult
    if (result == null) {
        run {
            Box(
                Modifier.fillMaxWidth()
                    .background(TealDk)
                    .clickable { CrackleSound.click(); ctx.onGameAccess() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "J'ACCEPTE LES CONDITIONS · LANCER LE TEST",
                    fontSize = 9.sp, letterSpacing = 3.sp, fontFamily = Mono, color = Teal
                )
            }
        }
    } else {
        run {
            val (label, color) = when (result) {
                EndType.VICTORY        -> "CERTIFICATION ACCORDÉE" to Teal
                EndType.BITTER_VICTORY -> "CERTIFICATION ACCORDÉE SOUS RÉSERVE" to Gold
                EndType.BAD_END        -> "CERTIFICATION REFUSÉE" to Red
            }
            Box(Modifier.border(1.dp, color).padding(horizontal = 18.dp, vertical = 8.dp)) {
                Text("$label — NLRP / DAE", fontSize = 9.sp, letterSpacing = 3.sp, fontFamily = Mono, color = color)
            }
            // Rappel du code à envoyer
            ctx.certificationCode?.let { code ->
                Spacer(Modifier.height(12.dp))
                Text("CODE DE CERTIFICATION", fontSize = 8.sp, letterSpacing = 3.sp, fontFamily = Mono, color = T3)
                Spacer(Modifier.height(4.dp))
                SelectionContainer {
                    Text(code, fontSize = 14.sp, letterSpacing = 4.sp, fontFamily = Mono, color = color)
                }
            }
        }
    }
}

// ── 07 Inventaire ─────────────────────────────────────────────────────────────
@Composable
private fun DmInventory() {
    SectionHead("07", "Inventaire de l'équipement")
    val items = listOf(
        "x1" to "Fusil compact Danse Macabre MK.I",
        "x1" to "Micro-cellule magitek",
        "x1" to "Chargeur 5 coups",
        "x5" to "Munitions incendiaires",
        "x1" to "Manuel technique DOC-NLRP-DM-0007",
    )
    Column(
        Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        items.forEachIndexed { i, (qty, label) ->
            Row(Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(qty, fontSize = 11.sp, fontFamily = Mono, color = Teal, modifier = Modifier.width(36.dp))
                Text(label, fontSize = 12.sp, color = T2)
            }
            if (i < items.lastIndex)
                Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr.copy(alpha = 0.7f)))
        }
    }
}

// ── Grille de caractéristiques ───────────────────────────────────────────────
private fun plain(text: String) = AnnotatedString(text)

// Texte barré via TextDecoration (rendu par Compose, pas de caractère Unicode : OK en wasm)
private fun struck(prefix: String, barred: String) = buildAnnotatedString {
    append(prefix)
    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = Red.copy(alpha = 0.8f))) {
        append(barred)
    }
}

@Composable
private fun SpecCell(label: String, value: AnnotatedString, modifier: Modifier) {
    Column(modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
        Text(label, fontSize = 8.sp, letterSpacing = 2.sp, color = T3)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 11.sp, fontFamily = Mono, color = T1)
    }
}

@Composable
private fun SpecGrid(specs: List<Pair<String, AnnotatedString>>) {
    val rows = specs.chunked(2)
    Column(Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)) {
        rows.forEachIndexed { ri, row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEachIndexed { ci, (lbl, v) ->
                    SpecCell(lbl, v, Modifier.weight(1f))
                    if (ci == 0) Box(Modifier.width(1.dp).height(52.dp).background(Bdr))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            if (ri < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        }
    }
}
