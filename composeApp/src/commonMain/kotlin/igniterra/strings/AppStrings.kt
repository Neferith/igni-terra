package igniterra.strings

/**
 * Toutes les chaînes de l'application externalisées ici.
 * Les valeurs "[À compléter]" sont des placeholders — remplacer par le contenu final.
 * Les valeurs structurelles (labels, codes, noms) sont déjà remplies.
 */
object AppStrings {

    object Meta {
        const val windowTitle = "Igni Terra — Manuel Technique"
    }

    object Header {
        const val organization = "Autorité Technique Sharlayenne · Division Armements Éthériques"
        const val orgShort     = "ATE / DAE"
        const val weaponName   = "Igni Terra"
        const val badge        = "Confidentiel"
        const val docRef       = "DOC-ATE-IT-0042 · REV.2"
    }

    // ── Page de couverture ────────────────────────────────────────────────────
    object Cover {
        const val subtitle = "Lance-Flamme Magitek Éthérique · Série Expérimentale"

        object Meta {
            const val refLabel   = "Référence"
            const val refValue   = "DOC-ATE-IT-0042"
            const val revLabel   = "Révision"
            const val revValue   = "REV.2 — ACTIF"
            const val classLabel = "Classification"
            const val classValue = "CONFIDENTIEL"
            const val rangeLabel = "Portée validée"
            const val rangeValue = "15 YALMS"
        }
    }

    // ── 01 Vue d'ensemble ─────────────────────────────────────────────────────
    object S01 {
        const val num   = "01"
        const val title = "Vue d'ensemble"
        const val body1 = "[À compléter]"
        const val body2 = "[À compléter]"

        object Note {
            const val title = "Note de conception"
            const val body  = "[À compléter]"
        }
    }

    // ── 02 Spécifications techniques ─────────────────────────────────────────
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

            const val body = "[À compléter]"
        }
    }

    // ── 03 Composants ─────────────────────────────────────────────────────────
    object S03 {
        const val num   = "03"
        const val title = "Composants & Architecture Interne"

        const val diagram = """IGNI TERRA — SCHÉMA FONCTIONNEL
═══════════════════════════════════════

  ┌─ BUSE DE SORTIE ──────────────────┐
  │  ─┤ ├─  ← Électrodes (angle calibré)
  │    │
  │  ══╪══  ← Arc électrique permanent
  └───────────────────────────────────┘
         │
  ┌─ MODULE PROPULSION ───────────────┐
  │  [RÉSISTANCE VARIABLE MÉCA.]      │
  │  Gâchette → Débit faible ►fort    │
  │  (pression)  α-MODE     γ-MODE    │
  └───────────────────────────────────┘
         │
  ┌─ MODULE ÉNERGIE ──────────────────┐
  │  ┌──────────┐                     │
  │  │  BOBINE  │ → génère l'arc      │
  │  │  MAGITEK │                     │
  │  └────┬─────┘                     │
  │  ┌────┴─────┐                     │
  │  │ CRISTAL  │← logement caoutch.  │
  │  │DE FOUDRE │  (anti-vibrations)  │
  │  └──────────┘                     │
  │  [RÉINJECTION EM] → boucle fermée │
  └───────────────────────────────────┘"""

        object Sub1 {
            const val num   = "03.1"
            const val title = "Système d'Électrodes"
            const val body  = "[À compléter]"
        }

        object Warning {
            const val title           = "⚠ Paramètres Critiques — Électrodes"
            const val arcUnstable     = "Arc instable"
            const val arcUnstableDesc = "Projections de flammes non contrôlées, risque de brûlures opérateur."
            const val arcStrong       = "Arc trop puissant"
            const val arcStrongDesc   = "Fusion des électrodes, mise hors service définitive du module."
            const val arcWeak         = "Arc trop faible"
            const val arcWeakDesc     = "Absence de résonance avec l'agrégat, pas de combustion."
        }
    }

    // ── 04 Modes de tir ───────────────────────────────────────────────────────
    object S04 {
        const val num   = "04"
        const val title = "Modes de Tir"
        const val intro = "[À compléter]"

        object Alpha {
            const val code     = "MODE α"
            const val name     = "Contact"
            const val range    = "5"
            const val unit     = "Yalms"
            const val desc     = "[À compléter]"
            const val barRatio = 0.34f
        }

        object Beta {
            const val code     = "MODE β"
            const val name     = "Intermédiaire"
            const val range    = "10"
            const val unit     = "Yalms"
            const val desc     = "[À compléter]"
            const val barRatio = 0.67f
        }

        object Gamma {
            const val code     = "MODE γ"
            const val name     = "Destruction"
            const val range    = "15"
            const val unit     = "Yalms"
            const val desc     = "[À compléter]"
            const val barRatio = 1.00f
        }

        object Note {
            const val title = "Effet Feu-Terre — Mode γ"
            const val body  = "[À compléter]"
        }
    }

    // ── 05 Sécurité ───────────────────────────────────────────────────────────
    object S05 {
        const val num   = "05"
        const val title = "Sécurité & Instructions d'Utilisation"
        const val intro = "[À compléter]"

        object MainWarning {
            const val title = "⚠ Avertissement Principal"
            const val body  = "[À compléter]"
        }

        val checklist = listOf(
            "Combinaison ignifugée intégrale portée par l'opérateur",
            "Dissipateur thermique installé et vérifié",
            "Zone dégagée sur au moins 20 yalms dans la direction de tir",
            "Absence de personnel non protégé dans un rayon de 5 yalms",
            "Intégrité des électrodes vérifiée visuellement avant activation",
            "Cristal de foudre correctement enchâssé dans son logement en caoutchouc traité",
            "Réserve d'agrégat éthérique scellée jusqu'au moment du tir"
        )

        object Activation {
            const val num   = "05.1"
            const val title = "Procédure d'Activation"
            const val steps = "[À compléter]"
        }

        object ArcWarning {
            const val title = "⚠ Anomalie d'Arc"
            const val body  = "[À compléter]"
        }
    }

    // ── 06 Dispositions légales ───────────────────────────────────────────────
    object S06 {
        const val num   = "06"
        const val title = "Dispositions Légales & Réglementaires"
        const val p1    = "[À compléter]"
        const val p2    = "[À compléter]"
        const val p3    = "[À compléter]"
        const val p4    = "[À compléter]"
        const val stamp = "Document validé — ATE / DAE"
    }

    // ── Pied de page ──────────────────────────────────────────────────────────
    object Footer {
        const val docRef         = "DOC-ATE-IT-0042 · REV.2"
        const val title          = "IGNI TERRA — MANUEL TECHNIQUE"
        const val classification = "CONFIDENTIEL"
    }
}
