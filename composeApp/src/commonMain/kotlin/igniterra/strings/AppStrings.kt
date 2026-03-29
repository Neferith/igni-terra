package igniterra.strings

import igniterra.model.buildHiddenBackMessage

object AppStrings {

    // ── Destinataires ────────────────────────────────────────────────────────────
    data class Recipient(
        val displayName : String,
        val password    : String,
        val note        : String,

        val hasSecretAccess: Boolean = false,
        val musicFile      : String? = null  // null = pas de musique
    )


    val recipients = listOf(
        Recipient(
            displayName = "Aurèle - Officière de la Nouvelle Lune",
            password    = "viera_fatale",
            note        = "Aurele,\n\n" +
                    "Je suis prise d’un affreux doute, car j’ai l’intime persuasion que l’Igni Terra pourrait être très mal utilisé à l’avenir." +
                    " Ma plus grande peur serait de carboniser d’innocentes victimes, suite à un excès de zèle.\n" +
                    "J’avais rédigé des conditions d’usage que j’ai partagées avec Adrila, elle les a acceptées sans discuter, " +
                    "mais j’ignore si elle a eu le temps de les partager avec l’ensemble des officiers de la compagnie.\n" +
                    "Si tel n’est pas le cas, voilà un début de réparation. Mettez-vous bien à l’aise pour étudier ce document. " +
                    "Vous pouvez appuyer sur play pour écouter de la musique, cela vous aidera à vous détendre durant la lecture. Lorsque vous en aurez terminé, " +
                    "vous pouvez cliquer cinq fois sur la référence du document en bas de l’écran.\n\n\n" +
                    "— Eleanor Dubrie",
            musicFile = "kalenda_maya.mp3"
        ),
        Recipient(
            displayName = "Keryth - Samourai",
            password    = "balfir",
            note        = "Keryth,\n\n" +
                    "Par-delà beffroi \n" +
                    "son ouragan s'efface\n" +
                    "l’oiseau s’envole\n\n" +
                    "— Eleanor Dubrie",
            musicFile = "AFathersPride.mp3"
        ),

        Recipient(
            displayName = "Winifred de Honeystone — Pupille Hauterive",
            password    = "thepooh",
            note        = "Winifred,\n\n" +
                    "On ne se connaît pas vraiment, mais je vous assure que c’est un plaisir de vous avoir dans la compagnie.\n" +
                    "Mon rôle au sein de cette compagnie est multiple, bien que je sois rarement présente en mission. Mon seul objectif est d’être utile quand c’est nécessaire.\n" +
                    "L’Igni Terra n’est pas juste un lance-flamme, il est le résultat d’une fusion de deux esprits. Je n’aime pas les armes, je ne me plais pas à les utiliser, malheureusement je me plais à les concevoir.\n" +
                    "Faites très attention lors de son usage, ce n’est pas un jouet.\n\n" +
                    "— Eleanor Dubrie",
            musicFile = "ishgardAnthem8bits.mp3"
        ),
        Recipient(
            displayName = "Galaad Chasten — Aventurier",
            password    = "ultima",
            note        = "Galaad,\n\n" +
                    "Je compte sur votre grande expérience.\n\n" +
                    "— Eleanor Dubrie",
            musicFile = "ultima8bits.mp3"
        ),
        Recipient(
            displayName = "Lothaire — Novice parmi la Nouvelle Lune depuis le 23/12/2025",
            password    = "lotus",
            note        = "Lothaire,\n\n" +
                    "Déjà j’espère que tu vas bien. Ne pense pas que ta disparition ne m’a pas inquiété, mais, quelles que soient les raisons, je n’ai aucun doute sur tes convictions et ta volonté. Tu as la capacité de te sortir des pires situations.\n" +
                    "Maintenant, pour ce qui est de l’Igni Terra, je te prie d’en faire un usage modéré." +
                    " Avec Damian, nous l’avons pensé comme un outil servant à calciner le mal, mais il peut être à double tranchant." +
                    " Une arme reste une arme. Plus elle est puissante, plus elle doit être utilisée avec parcimonie.\n\n" +
                    "— Eleanor Dubrie",
            musicFile = "ishgardAnthem8bits.mp3"
        ),
        Recipient(
            displayName = "Adrila — Officière de la Nouvelle Lune",
            password    = "adrenaline",
            note        = "Mon amour,\n\n" +
                    "J’espère que tu apprécies tous les efforts que je fais pour la Compagnie. C’est aussi un cadeau pour toi.\n\n" +
                    "Mon corps est passion.\n" +
                    "Mais mon cœur reste raison.\n" +
                    "À nous l'avenir.\n\n" +
                    "— Eleanor Dubrie\n" +
            "PS : J'ai une surprise pour toi, clique cinq fois sur le logo et tu pourras la découvrir.",
            hasSecretAccess = true,
            musicFile   = "garleananthem.mp3"
        ),
        Recipient(
            displayName = "Damian — Ingénieur de la Nouvelle Lune",
            password    = "GardienG",
            note        = "Damian,\n\n" +
                    "J’espère que notre arme sera bien utilisée. Le défi de créer une telle arme m’excite au plus haut point, mais la seule idée de son usage m’effraie.\n" +
                    "Je crois que je ne suis pas normal…\n\n" +
                    "— Eleanor Dubrie",
            hasSecretAccess = false,
            musicFile = "TheEwerBrimmeth8bit.mp3"
        ),
        Recipient(
            displayName = "Alyx — Occultiste",
            password    = "akala",
            note        = "Alyx,\n\n" +
                    "Je n’ai pas encore eu le plaisir de discourir avec vous, même si à l’occasion ce sera avec plaisir.\n" +
                    "L'Igni Terra n'est pas une arme ordinaire. Elle est surtout très dangereuse. Faites attention avec.\n\n" +
                    "— Eleanor Dubrie\n",
            musicFile = "endwalker8bits.mp3"
        ),
        Recipient(
            displayName = "Wellan — Chasseur",
            password    = "piegeachocobo",
            note        = "Wellan,\n\n" +
                    "Je me souviens encore de l’échec de notre piège avec le chocobo rouge. " +
                    "Mais l’Igni Terra ne se retournera pas contre nous, si nous respectons les règles de sécurité.\n\n" +
                    "— Eleanor Dubrie\n",
            musicFile = "endwalker8bits.mp3"
        ),
        Recipient(
            displayName = "Kalyra — Pictomancienne",
            password    = "bravoure",
            note        = "Kalyra,\n\n" +
                    "Je sais que vous êtes une artiste et nous voilà avec un outil qui est loin de faire dans la dentelle." +
                    " Ce manuel doit vous aider à l’utiliser dans de bonnes conditions.\n\n" +
                    "— Eleanor Dubrie\n",
            musicFile = "endwalker8bits.mp3"
        ),
    )

    val serialNumber: String = run {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789"
        val part1 = (1..4).map { chars.random() }.joinToString("")
        val part2 = (1..4).map { chars.random() }.joinToString("")
        "IT-$part1-$part2"
    }

    object Meta {
        const val windowTitle = "Igni Terra — Manuel Technique"
    }

    object Header {
        const val organization = "Compagnie de la nouvelle lune · Division Armements Éthériques"
        const val orgShort     = "NLRP / DAE"
        const val weaponName   = "Igni Terra"
        const val badge        = "Confidentiel"
        const val docRef       = "DOC-NLRP-IT-0042 · REV.2"
    }

    object Cover {
        const val subtitle = "Lance-Flamme Magitek Éthérique · Série Expérimentale"

        object Meta {
            const val refLabel    = "Référence"
            const val refValue    = "DOC-NLRP-IT-0042"
            const val revLabel    = "Révision"
            const val revValue    = "REV.2 — ACTIF"
            const val classLabel  = "Classification"
            const val classValue  = "CONFIDENTIEL"
            const val rangeLabel  = "Portée validée"
            const val rangeValue  = "15 YALMS"
            const val authorLabel = "Ingénieur principal"
            const val authorValue = "Damian Ashwood"
            const val author2Label = "Muse technique"
            const val author2Value = "Eleanor Dubrie"
        }
    }

    object S01 {
        const val num   = "01"
        const val title = "Vue d'ensemble"
        const val body1 = "L'objectif de l'Igni Terra est de calciner intégralement tout mort réanimé. L'idée est de profiter des propriétés des lentilles en verre de Phaenna, ainsi que d'un système d'arc en boucle fermé, pour projeter un agrégat élémentaire, dont l'objectif est de calciner intégralement nos adversaires."
        const val body2 = "L'arme est ainsi prévue dans ce cas précis et doit être utilisée de manière calculée. Elle est extrêmement destructrice."

        object Note {
            const val title = "Note de conception"
            const val body  = "L'arme n'est qu'à l'état de prototype, mais elle est d'ores et déjà utilisable en condition réelle, à condition de respecter les règles de sécurité. La priorité doit être : 0 DOMMAGE COLLATÉRAL."
        }
    }

    object S02 {
        const val num   = "02"
        const val title = "Spécifications Techniques"

        object Spec {
            const val nameLabel       = "Désignation"
            const val nameValue       = "IGNI TERRA MK.II"
            const val energyLabel     = "Source d'énergie"
            const val energyValue     = "Cristal de Foudre"
            const val fuelLabel       = "Carburant"
            const val fuelValue       = "Agrégat cristallin tri-composant"
            const val rangeLabel      = "Portée validée"
            const val rangeValue      = "5 – 15 yalms"
            const val controlLabel    = "Contrôle débit"
            const val controlValue    = "Résistance variable mécanique"
            const val ignitionLabel   = "Allumage"
            const val ignitionValue   = "Arc électrique permanent"
            const val archLabel       = "Architecture électrique"
            const val archValue       = "Boucle fermée EM réinjection"
            const val combustionLabel = "Combustion"
            const val combustionValue = "Harmonisation cristalline éthérique"
            const val dimensionsLabel  = "Dimensions"
            const val dimensionsValue  = "12 ilms × 1,8 ilms × 1,4 ilms"
            const val weightEmptyLabel = "Poids (vide)"
            const val weightEmptyValue = "8 ponzes 4 onzes"
            const val weightFullLabel  = "Poids (chargé)"
            const val weightFullValue  = "13 ponzes 6 onzes"
            const val materialsLabel   = "Matériaux"
            const val materialsValue   = "Alliage de titane magitek, caoutchouc traité, verre de Phaenna"
            const val autonomyLabel    = "Autonomie"
            const val autonomyValue    = "~45 secondes de tir continu"
        }

        object Sub1 {
            const val num        = "02.1"
            const val title      = "Agrégat Éthérique — Composition"
            const val tableTitle = "Composition validée — REV.2"

            const val comp1Label  = "Sable éthéréen [FEU]"
            const val comp1Effect = "Combustion intense"
            const val comp1Ratio  = 0.70f

            const val comp2Label  = "Sable éthéréen [TERRE]"
            const val comp2Effect = "Adhérence aux surfaces"
            const val comp2Ratio  = 0.52f

            const val comp3Label  = "Poudre cristal [VENT]"
            const val comp3Effect = "Propulsion / portée"
            const val comp3Ratio  = 0.38f

            const val body = "L'arme peut utiliser de nombreux agrégats, mais l'idée actuelle est de propulser un mélange de sable éthéré, dopé à l'éther de feu. Le mélange est ainsi à la fois brûlant et collant. Pour prolonger la distance de tir, l'ensemble est mélangé à une poudre de cristal de vent."
        }
    }

    object S03 {
        const val num   = "03"
        const val title = "Composants & Architecture Interne"

        const val diagram = ""

        object Sub1 {
            const val num   = "03.1"
            const val title = "Système d'Électrodes"
            const val body  = "Une bobine génère un arc électrique, qui servira d'allumage au point de sortie du carburant. Le champ électromagnétique résiduel sera ensuite réinjecté en partie à l'intérieur. L'intérêt est avant tout de ralentir la consommation du cristal de foudre. L'avantage de ce procédé est qu'il propose une bonne autonomie. Le défi est de rendre cela suffisamment compact. Une fois les problèmes de chaleur résolus, son usage devrait être relativement sécurisé.\n" +
                    "À la sortie de la buse, deux électrodes créent un arc électrique permanent, tant que la gâchette est enfoncée. L'arc servira à enflammer l'amalgame de cristaux de vent et de feu, via une méthode d'harmonisation.\n" +
                    "L'angle de convergences doit donc être mesuré très précisément.\n" +
                    "Pour générer l'arc, nous utiliserons un cristal de foudre, enchâssée dans un logement en caoutchouc traité pour lui laisser juste suffisamment d'espace pour ne pas se briser sous le coup des vibrations provoquées."
        }

        object Warning {
            const val title           = "[!] Paramètres Critiques — Électrodes"
            const val arcUnstable     = "Arc instable"
            const val arcUnstableDesc = "Projections de flammes non contrôlées, risque de brûlures opérateur."
            const val arcStrong       = "Arc trop puissant"
            const val arcStrongDesc   = "Fusion des électrodes, mise hors service définitive du module."
            const val arcWeak         = "Arc trop faible"
            const val arcWeakDesc     = "Absence de résonance avec l'agrégat, pas de combustion."
        }
    }

    object S04 {
        const val num   = "04"
        const val title = "Modes de Tir"
        const val intro = "Différents modes de tir, pour plus de polyvalence. Avant tout usage, il est très IMPORTANT de commencer par le mode α. L'appareil doit monter progressivement en puissance."

        object Alpha {
            const val code     = "MODE α"
            const val name     = "Contact"
            const val range    = "5"
            const val unit     = "Yalms"
            const val desc     = "Destruction massive à faible distance, économie d'énergie, faible risque collatéral."
            const val barRatio = 0.34f
        }

        object Beta {
            const val code     = "MODE β"
            const val name     = "Intermédiaire"
            const val range    = "10"
            const val unit     = "Yalms"
            const val desc     = "Destruction massive, consommation modérée, risque collatéral."
            const val barRatio = 0.67f
        }

        object Gamma {
            const val code     = "MODE γ"
            const val name     = "Destruction"
            const val range    = "15"
            const val unit     = "Yalms"
            const val desc     = "Destruction totale, consommation massive, risque collatéral important."
            const val barRatio = 1.00f
        }

        object Note {
            const val title = "Effet Feu-Terre — Mode γ"
            const val body  = "Le Mode γ pourrait être un pivot central dans une stratégie bien pensée, mais il doit être réfléchi doublement. Les dégâts engendrés pourraient provoquer des situations particulièrement dramatiques."
        }
    }

    object S05 {
        const val num   = "05"
        const val title = "Sécurité & Instructions d'Utilisation"
        const val intro = "La sécurité est la base de l'utilisation de l'Igni Terra. L'opérateur du prototype doit agir de manière pragmatique, sans se laisser déborder par ses émotions, avec toujours ce principe en tête : Tuer n'est pas jouer."

        object MainWarning {
            const val title = "[!] Avertissement Principal"
            const val body  = "Ne pas confier à des utilisateurs non formés, ne pas donner à des enfants."
        }

        val checklist = listOf(
            "Remettre systématiquement la sécurité entre deux utilisations, même écartées d'une trentaine de secondes.",
            "Combinaison ignifugée intégrale portée par l'opérateur lors de l'apprentissage. Non nécessaire en mission.",
            "Dissipateur thermique installé et vérifié.",
            "Zone dégagée sur au moins 20 yalms dans la direction de tir.",
            "Absence de personnel non protégé dans un rayon de 5 yalms.",
            "Intégrité des électrodes vérifiée visuellement avant activation.",
            "Cristal de foudre correctement enchâssé dans son logement en caoutchouc traité.",
            "Réserve d'agrégat éthérique scellée jusqu'au moment du tir.",
            "Le premier qui appuie cinq fois sur le bouton confidentiel, avant la fin de l'entraînement n'obtiendra pas sa certification."
        )

        object Activation {
            const val num   = "05.1"
            const val title = "Procédure d'Activation"
            const val steps = "Ne jamais enlever la sécurité avant usage. Toujours activer l'arme en MODE α" +
                    " et monter progressivement en puissance. "
        }

        object ArcWarning {
            const val title = "[!] Anomalie d'Arc"
            const val body  = "En cas d'anomalie de l'arc en boucle fermé. SURTOUT NE PAS S'AFFOLER. SOUFFLEZ UN GRAND COUP, LÂCHEZ LA GÂCHETTE ET RÉTABLISSEZ LA SÉCURITÉ."
        }
    }

    object S06 {
        const val num   = "06"
        const val title = "Dispositions Légales & Réglementaires"

        const val sub1Num   = "06.1"
        const val sub1Title = "Propriété intellectuelle & matérielle"
        const val p1 = "L'Igni Terra, dans l'ensemble de ses composants, plans et prototypes, est la propriété intellectuelle et matérielle de Damian Ashwood et Eleanor Dubrie. Aucune reproduction, modification ou démontage n'est autorisé sans leur consentement explicite."

        const val sub2Num   = "06.2"
        const val sub2Title = "Cession d'usage"
        const val p2 = "Damian Ashwood et Eleanor Dubrie confient à la Compagnie de la Nouvelle Lune l'usage exclusif de l'Igni Terra, dans le cadre unique et limité de la neutralisation du groupe de fanatiques visé. Cette autorisation ne constitue pas un transfert de propriété et peut être révoquée à tout moment, unilatéralement, par l'un ou l'autre des propriétaires, sans préavis ni justification."

        const val sub3Num   = "06.3"
        const val sub3Title = "Clause de responsabilité"
        const val p3 = "Tout dommage collatéral, blessure, décès ou destruction de bien résultant d'un usage non conforme aux présentes dispositions engage la responsabilité exclusive de l'opérateur et du commandement ayant autorisé l'opération. Les concepteurs déclinent toute responsabilité en cas de non-respect des protocoles de sécurité établis en section 05."

        const val sub4Num   = "06.4"
        const val sub4Title = "Sanctions & protocole de destruction"
        const val p4 = "Tout usage en dehors du cadre défini entraînera la confiscation immédiate de l'ensemble des prototypes et composants. En cas d'impossibilité de récupération, l'opérateur désigné est tenu d'activer le protocole de destruction : immersion complète dans l'eau salée pendant 24 heures, suivi d'un démantèlement physique du cristal de foudre. Damian Ashwood se réserve le droit de récupérer personnellement tout matériel confisqué."

        const val stamp = "Document validé — NLRP / DAE"
    }

    object Footer {
        const val docRef         = "DOC-NLRP-IT-0042 · REV.2"
        const val title          = "IGNI TERRA — MANUEL TECHNIQUE"
        const val classification = "CONFIDENTIEL"
    }

    // ── Section secrète ─────────────────────────────────────────────────────────
    object Secret {
        const val num   = "07"
        const val label = "Classifié"
        const val title = "Données Classifiées"
        val body  = buildHiddenBackMessage().words
    }
}