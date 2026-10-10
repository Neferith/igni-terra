package igniterra.ui

import androidx.compose.runtime.Composable
import igniterra.strings.AppStrings
import kotlinx.coroutines.CoroutineScope

// ── Description d'un manuel ───────────────────────────────────────────────────
// Chaque destinataire pointe vers un manuel (AppStrings.ManualKind).
// ManualApp ne connaît que cette structure.

enum class PageUnlock { NONE, SECRET, TRADUCTER }

class ManualPage(
    val num     : String,
    val label   : String,
    val unlock  : PageUnlock = PageUnlock.NONE,
    val content : @Composable (ManualPageContext) -> Unit
)

class ManualSpec(
    val orgShort             : String,
    val organization         : String,
    val weaponName           : String,
    val subtitle             : String,
    val badge                : String,
    val docRef               : String,
    val coverMeta            : List<Pair<String, String>>,
    val serialNumber         : String,
    val footerDocRef         : String,
    val footerTitle          : String,
    val footerClassification : String,
    val watermark            : String,
    val pages                : List<ManualPage>,
    val certification        : CertificationTest? = null,
    val autoPlayMusic        : Boolean = false,  // musique du destinataire dès la connexion
    val transmissionNote     : String? = null,   // remplace la note du destinataire sur la couverture
)

// Test déclenché depuis le manuel (intro -> shooter -> fin)
class CertificationTest(
    val introHeader   : String,
    val introTitle    : String,
    val introSubtitle : String,
    val introText     : String,
    val scenario      : ShooterScenario,
    val endings       : EndTexts,
)

// Tout ce dont une page peut avoir besoin
class ManualPageContext(
    val spec               : ManualSpec,
    val recipient          : AppStrings.Recipient,
    val glitch             : GlitchEngine,
    val scope              : CoroutineScope,
    val translatorUnlocked : Boolean,
    val secretReveled      : Boolean,
    val onSecretReveled    : () -> Unit,
    val onEnableDungeon    : () -> Unit,
    val onGameAccess       : () -> Unit,
    val onEmblemClick      : () -> Unit,
    val onBadgeClick       : () -> Unit,
    val onDocRefClick      : () -> Unit,
    val certificationResult: EndType?,
    val certificationCode  : String? = null,
)

fun AppStrings.Recipient.manualSpec(): ManualSpec = when (manual) {
    AppStrings.ManualKind.IGNI_TERRA    -> IgniTerraManual
    AppStrings.ManualKind.DANSE_MACABRE -> DanseMacabreManual
    AppStrings.ManualKind.DANSE_MACABRE_CONSULT -> DanseMacabreConsultManual
}
