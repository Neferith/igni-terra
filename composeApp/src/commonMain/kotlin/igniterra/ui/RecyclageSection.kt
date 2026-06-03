package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.strings.AppStrings

@Composable
fun RecyclageSection(    recipient    : AppStrings.Recipient? = null,
                         onGameAccess : () -> Unit = {}) {
    SectionHead(AppStrings.S07.num, AppStrings.S07.title)
    Prose(AppStrings.S07.intro)
    Prose(AppStrings.S07.body)

    // ── 07.1 Chargeurs ────────────────────────────────────────────────────────
    SubHead(AppStrings.S07.Chargeur.num, AppStrings.S07.Chargeur.title)
    Prose(AppStrings.S07.Chargeur.body)

    Column(
        Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)
    ) {
        RecyclageRow(
            label    = AppStrings.S07.Chargeur.comp1Label,
            recycle  = AppStrings.S07.Chargeur.comp1Recycle,
            accent   = CFeu
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        RecyclageRow(
            label    = AppStrings.S07.Chargeur.comp2Label,
            recycle  = AppStrings.S07.Chargeur.comp2Recycle,
            accent   = CTerre
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        RecyclageRow(
            label    = AppStrings.S07.Chargeur.comp3Label,
            recycle  = AppStrings.S07.Chargeur.comp3Recycle,
            accent   = CVent
        )
    }

    // ── 07.2 Modules structurels ──────────────────────────────────────────────
    SubHead(AppStrings.S07.Modules.num, AppStrings.S07.Modules.title)

    Column(
        Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)
    ) {
        ModuleRecyclageRow(
            label   = AppStrings.S07.Modules.m1Label,
            desc    = AppStrings.S07.Modules.m1Desc,
            recycle = AppStrings.S07.Modules.m1Recycle
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        ModuleRecyclageRow(
            label   = AppStrings.S07.Modules.m2Label,
            desc    = AppStrings.S07.Modules.m2Desc,
            recycle = AppStrings.S07.Modules.m2Recycle
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        ModuleRecyclageRow(
            label   = AppStrings.S07.Modules.m3Label,
            desc    = AppStrings.S07.Modules.m3Desc,
            recycle = AppStrings.S07.Modules.m3Recycle
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        ModuleRecyclageRow(
            label   = AppStrings.S07.Modules.m4Label,
            desc    = AppStrings.S07.Modules.m4Desc,
            recycle = AppStrings.S07.Modules.m4Recycle
        )
    }

    // ── 07.3 Bombe éthérique ──────────────────────────────────────────────────
    BombeEtheriqueSection()

    // Bouton secret — Mia uniquement
    if (recipient?.displayName?.contains("Mia") == true) {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.fillMaxWidth()
                .border(1.dp, Red.copy(alpha = 0.15f))
                .clickable { onGameAccess() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Un petit jeu pour toi ! C'est une histoire de dragon et d'orphelinat. N'en parle pas à Eleanor, cela pourrait avoir des conséquences. Signé : DSV",
                fontSize = 9.sp, letterSpacing = 6.sp,
                fontFamily = Mono, color = Red.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun RecyclageRow(label: String, recycle: String, accent: Color) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(36.dp).background(accent))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 10.sp, fontFamily = Mono,
                color = accent, fontWeight = FontWeight.W500
            )
            Spacer(Modifier.height(3.dp))
            Text(
                recycle,
                fontSize = 10.sp, color = T3, lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ModuleRecyclageRow(label: String, desc: String, recycle: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            label,
            fontSize = 10.sp, fontFamily = Mono,
            color = T1, fontWeight = FontWeight.W500
        )
        Spacer(Modifier.height(4.dp))
        Text(desc, fontSize = 10.sp, color = T2, lineHeight = 15.sp)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Recyclage :".uppercase(),
                fontSize = 8.sp, letterSpacing = 2.sp, color = TealDk, fontFamily = Mono
            )
            Spacer(Modifier.width(8.dp))
            Text(recycle, fontSize = 10.sp, color = T3)
        }
    }
}

@Composable
fun BombeEtheriqueSection() {
    SubHead(AppStrings.S07.BombeEtherique.num, AppStrings.S07.BombeEtherique.title)
    Prose(AppStrings.S07.BombeEtherique.body)
    Prose(AppStrings.S07.BombeEtherique.portability)
    Prose(AppStrings.S07.BombeEtherique.detonation)
    NoteBox(AppStrings.S07.BombeEtherique.title) {
        Prose(AppStrings.S07.BombeEtherique.warning)
    }
}

// ── Couleurs élémentaires réexposées ──────────────────────────────────────────
private val CFeu   = Color(0xFFC84040)
private val CTerre = Color(0xFF8A6030)
private val CVent  = Color(0xFF38C4C4)