package igniterra.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import igniterra.model.buildHiddenBackMessage
import igniterra.strings.AppStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.compareTo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ── Palette ───────────────────────────────────────────────────────────────────
val Bg      = Color(0xFF07101E)
val Panel   = Color(0xFF0C1B30)
public val Card    = Color(0xFF0E2040)
val Teal    = Color(0xFF38C4C4)
val TealDk  = Color(0xFF1C7070)
private val Gold    = Color(0xFFC8A44A)
private val GoldDk  = Color(0xFF7A6028)
val T1      = Color(0xFFD6EDF6)
val T2      = Color(0xFF6EA8C0)
public val T3      = Color(0xFF365470)
val Bdr     = Color(0xFF152640)
public val Red     = Color(0xFFC84040)
private val RedBg   = Color(0x14C84040)
private val RedBdr  = Color(0x40C84040)
private val GoldBg  = Color(0x12C8A44A)
private val GoldBdr = Color(0x40C8A44A)
private val CFeu    = Color(0xFFC84040)
private val CTerre  = Color(0xFF8A6030)
private val CVent   = Color(0xFF38C4C4)
public val Mono    = FontFamily.Monospace

// ── Navigation ────────────────────────────────────────────────────────────────
enum class ManualSection(val num: String, val label: String) {
    COVER("00", "Couverture"),
    OVERVIEW("01", "Vue d'ensemble"),
    SPECS("02", "Spécifications"),
    COMPONENTS("03", "Composants"),
    FIRE_MODES("04", "Modes de tir"),
    SAFETY("05", "Sécurité"),
    LEGAL("06", "Dispositions légales"),
    SECRET("07", "Classifié")
}

// ── Root ──────────────────────────────────────────────────────────────────────
// Seuil en dp en dessous duquel on bascule en mode portrait (drawer)
private const val PORTRAIT_THRESHOLD_DP = 600

@Composable
fun ManualApp() {
    var recipient by remember { mutableStateOf<AppStrings.Recipient?>(null) }

    if (recipient == null) {
        LoginScreen { recipient = it }
        return
    }

    ManualContent_Internal(recipient!!)
}

@Composable
private fun ManualContent_Internal(recipient: AppStrings.Recipient) {
    val glitch = remember { GlitchEngine() }
    val scope  = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        glitch.startLoop(scope)
        CrackleSound.openDocument()
      //  recipient.musicFile?.let { CrackleSound.playWav(it, loop = true) }
    }


    var selected       by remember { mutableStateOf(ManualSection.COVER) }
    LaunchedEffect(selected) { glitch.triggerNavGlitch(scope) }
    var drawerOpen     by remember { mutableStateOf(false) }
    var secretUnlocked by remember { mutableStateOf(false) }
    var emblemClicks   by remember { mutableStateOf(0) }
    var snakeVisible   by remember { mutableStateOf(false) }
    var badgeClicks    by remember { mutableStateOf(0) }
    var dungeonVisible by remember { mutableStateOf(false) }
    var docRefClicks   by remember { mutableStateOf(0) }
    val (shakeX, shakeY) = glitch.contentShake

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isPortrait = maxWidth < PORTRAIT_THRESHOLD_DP.dp

        Box(Modifier.fillMaxSize()) {
            if (isPortrait) {
                // ── Mode portrait : contenu plein écran + drawer overlay ──────
                PortraitLayout(
                    scope = scope,
                    glitch          = glitch,
                    selected        = selected,
                    drawerOpen      = drawerOpen,
                    shakeX          = shakeX,
                    shakeY          = shakeY,
                    recipient       = recipient,
                    secretUnlocked  = secretUnlocked && recipient.hasSecretAccess,
                    onSelect        = { selected = it; drawerOpen = false },
                    onToggle        = { drawerOpen = !drawerOpen },
                    onDismiss       = { drawerOpen = false },
                    onEmblemClick   = {
                        if (recipient.hasSecretAccess) {
                            emblemClicks++
                            if (emblemClicks >= 5) {
                                secretUnlocked = true
                                emblemClicks = 0
                                CrackleSound.unlockSecret()
                            }
                        }
                    },
                    onBadgeClick = {
                        badgeClicks++
                        if (badgeClicks >= 5) {
                            snakeVisible = true
                            badgeClicks = 0
                            CrackleSound.click()
                        }
                    }
                )
            } else {
                // ── Mode paysage / desktop : sidebar fixe ────────────────────
                Row(
                    Modifier.fillMaxSize().background(Bg)
                        .offset(shakeX.dp, shakeY.dp)
                ) {
                    ManualSidebar(
                        selected        = selected,
                        secretUnlocked  = secretUnlocked && recipient.hasSecretAccess,
                        onSelect        = { selected = it },
                        recipient = recipient,
                        onBadgeClick = {
                            badgeClicks++
                            if (badgeClicks >= 5) {
                                snakeVisible = true
                                badgeClicks = 0
                                CrackleSound.click()
                            }
                        }
                    )
                    Box(Modifier.width(1.dp).fillMaxHeight().background(Bdr))
                    ManualContent(
                        section    = selected,
                        modifier   = Modifier.weight(1f).fillMaxHeight(),
                        recipient  = recipient,
                        emblemClicks    = emblemClicks,
                        glitch = glitch,
                        scope = scope,
                        onEmblemClick   = {
                            if (recipient.hasSecretAccess) {
                                emblemClicks++
                                if (emblemClicks >= 5) {
                                    secretUnlocked = true
                                    emblemClicks = 0

                                    CrackleSound.unlockSecret()
                                }
                            }
                        },
                        onDocRefClick = {
                            docRefClicks++
                            if (docRefClicks >= 5) {
                                dungeonVisible = true
                                docRefClicks = 0
                                CrackleSound.click()
                            }
                        }
                    )
                }
            }
            GlitchOverlay(glitch)
        }
        if (snakeVisible) {
            SnakeOverlay(onDismiss = { snakeVisible = false; badgeClicks = 0 })
        }
        if (dungeonVisible) {
            DungeonOverlay(onDismiss = { dungeonVisible = false; docRefClicks = 0 })
        }
    }
}

@Composable
private fun PortraitLayout(
    scope: CoroutineScope,
    glitch         : GlitchEngine,
    selected       : ManualSection,
    drawerOpen     : Boolean,
    shakeX         : Float,
    shakeY         : Float,
    recipient      : AppStrings.Recipient,
    secretUnlocked : Boolean = false,
    onSelect       : (ManualSection) -> Unit,
    onToggle       : () -> Unit,
    onDismiss      : () -> Unit,
    onEmblemClick  : () -> Unit = {},
    onBadgeClick: () -> Unit = {},
) {
    val drawerWidth = 220.dp
    val offsetX by animateDpAsState(
        targetValue = if (drawerOpen) 0.dp else -drawerWidth,
        animationSpec = tween(durationMillis = 220),
        label = "drawerOffset"
    )

    Box(Modifier.fillMaxSize().background(Bg)) {
        // Contenu principal avec offset shake
        Box(Modifier.fillMaxSize().offset(shakeX.dp, shakeY.dp)) {
            Column(Modifier.fillMaxSize()) {
                // Header mobile avec bouton menu
                PortraitHeader(selected, onToggle, onBadgeClick)
                Box(Modifier.weight(1f)) {
                    ManualContent(selected, Modifier.fillMaxSize(), recipient, glitch = glitch, scope = scope, onEmblemClick = onEmblemClick)
                }
            }
        }

        // Scrim sombre quand le drawer est ouvert
        if (drawerOpen) {
            Box(
                Modifier.fillMaxSize()
                    .background(Color(0x99000000))
                    .pointerInput(Unit) { detectTapGestures { onDismiss() } }
            )
        }

        // Drawer qui slide depuis la gauche
        Box(Modifier.offset(x = offsetX).width(drawerWidth).fillMaxHeight()) {
            ManualSidebar(
                selected       = selected,
                onSelect       = onSelect,
                recipient = recipient,
                secretUnlocked = secretUnlocked
            )
        }
    }
}

@Composable
private fun PortraitHeader(selected: ManualSection, onMenuClick: () -> Unit,
                           onBadgeClick  : () -> Unit = {},) {
    Row(
        Modifier.fillMaxWidth().background(Panel)
            .border(BorderStroke(1.dp, Bdr))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton hamburger ☰
        Box(
            Modifier.size(32.dp)
                .clickable(onClick = { onMenuClick(); CrackleSound.click() }),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(Modifier.width(18.dp).height(1.5f.dp).background(Teal))
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                AppStrings.Header.weaponName.uppercase(),
                fontSize = 13.sp, fontWeight = FontWeight.W300,
                letterSpacing = 4.sp, color = Teal
            )
            Text(
                "${selected.num} — ${selected.label}",
                fontSize = 9.sp, fontFamily = Mono, color = T3, letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.border(1.dp, GoldDk, RoundedCornerShape(2.dp))
                .clickable { onBadgeClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(AppStrings.Header.badge.uppercase(), fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = Mono, color = Gold)
        }
    }
}

// ── Sidebar ───────────────────────────────────────────────────────────────────
@Composable
private fun ManualSidebar(
    selected       : ManualSection,
    onSelect       : (ManualSection) -> Unit,
    recipient: AppStrings.Recipient,
    secretUnlocked : Boolean = false,
    onBadgeClick  : () -> Unit = {},
) {
    var volume by remember { mutableStateOf(1f) }
    var muted  by remember { mutableStateOf(false) }

    Column(Modifier.width(220.dp).fillMaxHeight().background(Panel)) {
        Column(Modifier.padding(18.dp)) {
            Text(AppStrings.Header.orgShort, fontSize = 8.sp, letterSpacing = 3.sp, fontFamily = Mono, color = T3)
            Spacer(Modifier.height(4.dp))
            Text(
                AppStrings.Header.weaponName.uppercase(),
                fontSize = 15.sp, fontWeight = FontWeight.W300, letterSpacing = 4.sp, color = Teal
            )
            Spacer(Modifier.height(3.dp))
            Text(AppStrings.Header.docRef, fontSize = 8.sp, fontFamily = Mono, color = T3)
        }
        HRule()
        ManualSection.entries.forEach { s ->
            if (s == ManualSection.SECRET) return@forEach
            SideNavItem(s, s == selected) { onSelect(s) }
        }
        if (secretUnlocked) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Red.copy(alpha = 0.3f)))
            SideNavItem(ManualSection.SECRET, selected == ManualSection.SECRET, accent = Red) {
                onSelect(ManualSection.SECRET)
            }
        }
        Spacer(Modifier.weight(1f))
        HRule()
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SON", fontSize = 7.sp, letterSpacing = 3.sp, fontFamily = Mono, color = T3)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Bouton Play/Stop musique
                    var musicPlaying by remember { mutableStateOf(false) }
                    Box(
                        Modifier
                            .border(1.dp, if (musicPlaying) Teal else Bdr, RoundedCornerShape(2.dp))
                            .clickable {
                                CrackleSound.click()
                                if (musicPlaying) {
                                    CrackleSound.stopWav()
                                } else {
                                    recipient.musicFile?.let { CrackleSound.playWav(it, loop = true) }
                                }
                                musicPlaying = !musicPlaying
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (musicPlaying) "STOP" else "PLAY",
                            fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = Mono,
                            color = if (musicPlaying) Teal else T3
                        )
                    }
                    Box(
                        Modifier
                            .border(1.dp, if (muted) Red.copy(alpha = 0.5f) else Bdr, RoundedCornerShape(2.dp))
                            .clickable {
                                muted = !muted
                                CrackleSound.setVolume(if (muted) 0f else volume)
                                CrackleSound.click()
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (muted) "MUET" else "ON",
                            fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = Mono,
                            color = if (muted) Red.copy(alpha = 0.5f) else TealDk
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Slider(
                value = if (muted) 0f else volume,
                onValueChange = { v ->
                    volume = v
                    if (muted && v > 0f) muted = false
                    CrackleSound.setVolume(v)
                },
                modifier = Modifier.fillMaxWidth().height(24.dp),
                colors = androidx.compose.material.SliderDefaults.colors(
                    thumbColor         = Teal,
                    activeTrackColor   = Teal,
                    inactiveTrackColor = Bdr
                )
            )
        }
        HRule()
        Row(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .border(1.dp, GoldDk, RoundedCornerShape(2.dp))
                    .clickable { onBadgeClick() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    AppStrings.Header.badge.uppercase(),
                    fontSize = 8.sp, letterSpacing = 3.sp, fontFamily = Mono, color = Gold
                )
            }
        }
    }
}

@Composable
private fun SideNavItem(section: ManualSection, active: Boolean, accent: Color = Teal, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(onClick = { onClick(); CrackleSound.click() })
            .background(if (active) Color(0x1538C4C4) else Color.Transparent)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(16.dp).background(if (active) accent else Color.Transparent))
        Spacer(Modifier.width(12.dp))
        Text(
            section.num, fontSize = 9.sp, fontFamily = Mono,
            color = if (active) accent else T3, modifier = Modifier.width(22.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            section.label, fontSize = 11.sp, letterSpacing = 1.sp,
            color = if (active) accent else T2,
            fontWeight = if (active) FontWeight.W500 else FontWeight.Normal
        )
    }
}

// ── Content ───────────────────────────────────────────────────────────────────
@Composable
private fun ManualContent(
    section       : ManualSection,
    modifier      : Modifier,
    recipient     : AppStrings.Recipient? = null,
    emblemClicks  : Int = 0,
    glitch: GlitchEngine,
    scope: CoroutineScope,
    onEmblemClick : () -> Unit = {},
    onBadgeClick  : () -> Unit = {},
    onDocRefClick  : () -> Unit = {}
) {
    val scroll = rememberScrollState()
    LaunchedEffect(section) { scroll.scrollTo(0) }
    Box(modifier) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(40.dp)) {
            when (section) {
                ManualSection.COVER      -> CoverSection(recipient, onEmblemClick, onBadgeClick)
                ManualSection.OVERVIEW   -> OverviewSection()
                ManualSection.SPECS      -> SpecsSection()
                ManualSection.COMPONENTS -> ComponentsSection()
                ManualSection.FIRE_MODES -> FireModesSection()
                ManualSection.SAFETY     -> SafetySection()
                ManualSection.LEGAL      -> LegalSection()
                ManualSection.SECRET     -> SecretSection(recipient,glitch, scope)
            }
            Spacer(Modifier.height(28.dp))
            HRule()
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    AppStrings.Footer.docRef, fontSize = 9.sp, fontFamily = Mono, color = T3,
                    modifier = Modifier.clickable {
                       onDocRefClick()
                    }
                )
                Text(AppStrings.Footer.title, fontSize = 9.sp, fontFamily = Mono, color = T3)
                Text(AppStrings.Footer.classification, fontSize = 9.sp, fontFamily = Mono, color = T3)
            }
        }
        WatermarkOverlay(Modifier.matchParentSize())
    }
}



// ── 00 Cover ──────────────────────────────────────────────────────────────────
@Composable
private fun CoverSection(recipient: AppStrings.Recipient? = null, onEmblemClick: () -> Unit = {}, onBadgeClick: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(20.dp))
        HexEmblem(Modifier.size(80.dp).clickable { onEmblemClick() })
        Spacer(Modifier.height(22.dp))
        Text(AppStrings.Header.organization, fontSize = 9.sp, letterSpacing = 3.sp, color = T3)
        Spacer(Modifier.height(10.dp))
        Text(
            AppStrings.Header.weaponName.uppercase(),
            fontSize = 44.sp, fontWeight = FontWeight.W100, letterSpacing = 14.sp, color = Teal
        )
        Spacer(Modifier.height(6.dp))
        Text(AppStrings.Cover.subtitle, fontSize = 10.sp, letterSpacing = 5.sp, color = Gold)
        Spacer(Modifier.height(36.dp))
        Column(Modifier.widthIn(max = 440.dp).border(1.dp, Bdr)) {
            Row(Modifier.fillMaxWidth()) {
                CoverMetaCell(AppStrings.Cover.Meta.refLabel, AppStrings.Cover.Meta.refValue, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(52.dp).background(Bdr))
                CoverMetaCell(AppStrings.Cover.Meta.revLabel, AppStrings.Cover.Meta.revValue, Modifier.weight(1f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
            Row(Modifier.fillMaxWidth()) {
                CoverMetaCell(AppStrings.Cover.Meta.classLabel, AppStrings.Cover.Meta.classValue, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(52.dp).background(Bdr))
                CoverMetaCell(AppStrings.Cover.Meta.rangeLabel, AppStrings.Cover.Meta.rangeValue, Modifier.weight(1f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
            Row(Modifier.fillMaxWidth()) {
                CoverMetaCell(AppStrings.Cover.Meta.authorLabel, AppStrings.Cover.Meta.authorValue, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(52.dp).background(Bdr))
                CoverMetaCell(AppStrings.Cover.Meta.author2Label, AppStrings.Cover.Meta.author2Value, Modifier.weight(1f))
            }
        }
        // Note de transmission
        if (recipient != null) {
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier.widthIn(max = 440.dp)
                    .background(GoldBg)
                    .border(1.dp, GoldBdr)
                    .padding(16.dp)
            ) {
                Text("NOTE DE TRANSMISSION", fontSize = 7.sp, letterSpacing = 3.sp, color = Gold, fontFamily = Mono)
                Spacer(Modifier.height(8.dp))
                Text(recipient.note, fontSize = 11.sp, lineHeight = 18.sp, color = T2, fontStyle = FontStyle.Italic)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "N° SÉRIE : ${AppStrings.serialNumber}",
            fontSize = 9.sp, fontFamily = Mono,
            color = T3, letterSpacing = 3.sp
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun HexEmblem(modifier: Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        fun hex(r: Float, col: Color, sw: Float) {
            val path = Path()
            for (i in 0..5) {
                val a = (PI / 180.0 * (60.0 * i - 30.0)).toFloat()
                val x = cx + r * cos(a)
                val y = cy + r * sin(a)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, col, style = Stroke(sw))
        }
        val r1 = size.minDimension / 2f - 2f
        hex(r1, Teal, 1.5f)
        hex(r1 * 0.72f, TealDk, 0.8f)
        hex(r1 * 0.46f, GoldDk, 0.8f)
        for (i in 0..2) {
            val a = (PI / 180.0 * (60.0 * i)).toFloat()
            drawLine(
                Teal.copy(alpha = 0.12f),
                start = Offset(cx - r1 * cos(a), cy - r1 * sin(a)),
                end   = Offset(cx + r1 * cos(a), cy + r1 * sin(a)),
                strokeWidth = 0.5f
            )
        }
    }
}

@Composable
private fun CoverMetaCell(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(label, fontSize = 8.sp, letterSpacing = 2.sp, color = T3)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 11.sp, fontFamily = Mono, color = T1)
    }
}

// ── 01 Vue d'ensemble ─────────────────────────────────────────────────────────
@Composable
private fun OverviewSection() {
    SectionHead(AppStrings.S01.num, AppStrings.S01.title)
    Prose(AppStrings.S01.body1)
    Prose(AppStrings.S01.body2)
    NoteBox(AppStrings.S01.Note.title) {
        Prose(AppStrings.S01.Note.body)
    }
}

// ── 02 Spécifications ─────────────────────────────────────────────────────────
@Composable
private fun SpecsSection() {
    SectionHead(AppStrings.S02.num, AppStrings.S02.title)
    val specs = listOf(
        AppStrings.S02.Spec.nameLabel       to AppStrings.S02.Spec.nameValue,
        AppStrings.S02.Spec.energyLabel     to AppStrings.S02.Spec.energyValue,
        AppStrings.S02.Spec.fuelLabel       to AppStrings.S02.Spec.fuelValue,
        AppStrings.S02.Spec.rangeLabel      to AppStrings.S02.Spec.rangeValue,
        AppStrings.S02.Spec.controlLabel    to AppStrings.S02.Spec.controlValue,
        AppStrings.S02.Spec.ignitionLabel   to AppStrings.S02.Spec.ignitionValue,
        AppStrings.S02.Spec.archLabel       to AppStrings.S02.Spec.archValue,
        AppStrings.S02.Spec.combustionLabel to AppStrings.S02.Spec.combustionValue,
        AppStrings.S02.Spec.dimensionsLabel  to AppStrings.S02.Spec.dimensionsValue,
        AppStrings.S02.Spec.weightEmptyLabel to AppStrings.S02.Spec.weightEmptyValue,
        AppStrings.S02.Spec.weightFullLabel  to AppStrings.S02.Spec.weightFullValue,
        AppStrings.S02.Spec.materialsLabel   to AppStrings.S02.Spec.materialsValue,
        AppStrings.S02.Spec.autonomyLabel    to AppStrings.S02.Spec.autonomyValue,
    )
    val rows = specs.chunked(2)
    Column(Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)) {
        rows.forEachIndexed { ri, row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEachIndexed { ci, (lbl, v) ->
                    DataCell(lbl, v, Modifier.weight(1f))
                    if (ci == 0) Box(Modifier.width(1.dp).height(52.dp).background(Bdr))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            if (ri < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        }
    }
    SubHead(AppStrings.S02.Sub1.num, AppStrings.S02.Sub1.title)
    Column(Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr).padding(14.dp)) {
        Text(
            AppStrings.S02.Sub1.tableTitle,
            fontSize = 8.sp, letterSpacing = 3.sp, color = T3,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        MixRow(AppStrings.S02.Sub1.comp1Label, AppStrings.S02.Sub1.comp1Ratio, CFeu,   AppStrings.S02.Sub1.comp1Effect)
        Spacer(Modifier.height(10.dp))
        MixRow(AppStrings.S02.Sub1.comp2Label, AppStrings.S02.Sub1.comp2Ratio, CTerre, AppStrings.S02.Sub1.comp2Effect)
        Spacer(Modifier.height(10.dp))
        MixRow(AppStrings.S02.Sub1.comp3Label, AppStrings.S02.Sub1.comp3Ratio, CVent,  AppStrings.S02.Sub1.comp3Effect)
    }
    Spacer(Modifier.height(14.dp))
    Prose(AppStrings.S02.Sub1.body)
}

@Composable
private fun DataCell(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
        Text(label, fontSize = 8.sp, letterSpacing = 2.sp, color = T3)
        Spacer(Modifier.height(3.dp))
        Text(value, fontSize = 11.sp, fontFamily = Mono, color = T1)
    }
}

@Composable
private fun MixRow(label: String, ratio: Float, color: Color, effect: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 10.sp, fontFamily = Mono, color = T2, modifier = Modifier.width(180.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(5.dp).background(Bdr)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(ratio).background(color))
        }
        Spacer(Modifier.width(12.dp))
        Text(effect, fontSize = 9.sp, color = T3, modifier = Modifier.width(140.dp))
    }
}

// ── 03 Composants ─────────────────────────────────────────────────────────────
@Composable
private fun ComponentsSection() {
    SectionHead(AppStrings.S03.num, AppStrings.S03.title)
    ComponentDiagram()
    Spacer(Modifier.height(14.dp))
    SubHead(AppStrings.S03.Sub1.num, AppStrings.S03.Sub1.title)
    Prose(AppStrings.S03.Sub1.body)
    WarningBox(AppStrings.S03.Warning.title) {
        Text(
            buildAnnotatedString {
                pushStyle(SpanStyle(color = Color(0xFFE06060), fontWeight = FontWeight.W500))
                append(AppStrings.S03.Warning.arcUnstable); pop()
                append(" — ${AppStrings.S03.Warning.arcUnstableDesc}\n")
                pushStyle(SpanStyle(color = Color(0xFFE06060), fontWeight = FontWeight.W500))
                append(AppStrings.S03.Warning.arcStrong); pop()
                append(" — ${AppStrings.S03.Warning.arcStrongDesc}\n")
                pushStyle(SpanStyle(color = Color(0xFFE06060), fontWeight = FontWeight.W500))
                append(AppStrings.S03.Warning.arcWeak); pop()
                append(" — ${AppStrings.S03.Warning.arcWeakDesc}")
            },
            fontSize = 12.sp, lineHeight = 20.sp, color = T2
        )
    }
}

// ── 04 Modes de tir ───────────────────────────────────────────────────────────
@Composable
private fun FireModesSection() {
    SectionHead(AppStrings.S04.num, AppStrings.S04.title)
    Prose(AppStrings.S04.intro)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ModeCard(
            code = AppStrings.S04.Alpha.code, name = AppStrings.S04.Alpha.name,
            range = AppStrings.S04.Alpha.range, unit = AppStrings.S04.Alpha.unit,
            desc = AppStrings.S04.Alpha.desc, barRatio = AppStrings.S04.Alpha.barRatio,
            accent = Teal, modifier = Modifier.weight(1f)
        )
        ModeCard(
            code = AppStrings.S04.Beta.code, name = AppStrings.S04.Beta.name,
            range = AppStrings.S04.Beta.range, unit = AppStrings.S04.Beta.unit,
            desc = AppStrings.S04.Beta.desc, barRatio = AppStrings.S04.Beta.barRatio,
            accent = Gold, modifier = Modifier.weight(1f)
        )
        ModeCard(
            code = AppStrings.S04.Gamma.code, name = AppStrings.S04.Gamma.name,
            range = AppStrings.S04.Gamma.range, unit = AppStrings.S04.Gamma.unit,
            desc = AppStrings.S04.Gamma.desc, barRatio = AppStrings.S04.Gamma.barRatio,
            accent = Red, modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(14.dp))
    NoteBox(AppStrings.S04.Note.title) {
        Prose(AppStrings.S04.Note.body)
    }
}

@Composable
private fun ModeCard(
    code: String, name: String, range: String, unit: String,
    desc: String, barRatio: Float, accent: Color, modifier: Modifier
) {
    Column(modifier.background(Card).border(1.dp, Bdr)) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(accent))
        Column(Modifier.padding(14.dp)) {
            Text(code, fontSize = 9.sp, letterSpacing = 3.sp, fontFamily = Mono, color = accent)
            Spacer(Modifier.height(4.dp))
            Text(name.uppercase(), fontSize = 11.sp, letterSpacing = 3.sp, fontWeight = FontWeight.W500, color = accent)
            Spacer(Modifier.height(12.dp))
            Text(range, fontSize = 30.sp, fontWeight = FontWeight.W100, color = T1, lineHeight = 32.sp)
            Text(unit.uppercase(), fontSize = 9.sp, letterSpacing = 2.sp, color = T3)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(3.dp).background(Bdr)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(barRatio).background(accent))
            }
            Spacer(Modifier.height(10.dp))
            Prose(desc)
        }
    }
}

// ── 05 Sécurité ───────────────────────────────────────────────────────────────
@Composable
private fun SafetySection() {
    SectionHead(AppStrings.S05.num, AppStrings.S05.title)
    WarningBox(AppStrings.S05.MainWarning.title) {
        Prose(AppStrings.S05.MainWarning.body)
    }
    Prose(AppStrings.S05.intro)
    Column(
        Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        AppStrings.S05.checklist.forEachIndexed { i, item ->
            CheckItem(item)
            if (i < AppStrings.S05.checklist.lastIndex)
                Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr.copy(alpha = 0.7f)))
        }
    }
    SubHead(AppStrings.S05.Activation.num, AppStrings.S05.Activation.title)
    Prose(AppStrings.S05.Activation.steps)
    WarningBox(AppStrings.S05.ArcWarning.title) {
        Prose(AppStrings.S05.ArcWarning.body)
    }
}

@Composable
private fun CheckItem(text: String) {
    Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Text(">", fontSize = 8.sp, color = TealDk, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 12.sp, lineHeight = 19.sp, color = T2)
    }
}

// ── 06 Dispositions légales ───────────────────────────────────────────────────
@Composable
private fun LegalSection() {
    SectionHead(AppStrings.S06.num, AppStrings.S06.title)
    SubHead(AppStrings.S06.sub1Num, AppStrings.S06.sub1Title)
    Prose(AppStrings.S06.p1)
    SubHead(AppStrings.S06.sub2Num, AppStrings.S06.sub2Title)
    Prose(AppStrings.S06.p2)
    SubHead(AppStrings.S06.sub3Num, AppStrings.S06.sub3Title)
    Prose(AppStrings.S06.p3)
    SubHead(AppStrings.S06.sub4Num, AppStrings.S06.sub4Title)
    Prose(AppStrings.S06.p4)
    Spacer(Modifier.height(20.dp))
    Box(Modifier.border(1.dp, GoldDk).padding(horizontal = 18.dp, vertical = 7.dp)) {
        Text(AppStrings.S06.stamp, fontSize = 9.sp, letterSpacing = 4.sp, fontFamily = Mono, color = GoldDk)
    }
}

// ── Composants réutilisables ──────────────────────────────────────────────────
@Composable
private fun SectionHead(num: String, title: String) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(num, fontSize = 10.sp, letterSpacing = 2.sp, fontFamily = Mono, color = TealDk)
        Spacer(Modifier.width(12.dp))
        Text(title.uppercase(), fontSize = 11.sp, letterSpacing = 4.sp, fontWeight = FontWeight.W500, color = T1)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Bdr))
    }
}

@Composable
private fun SubHead(num: String, title: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(num, fontSize = 9.sp, letterSpacing = 1.sp, fontFamily = Mono, color = TealDk)
        Spacer(Modifier.width(10.dp))
        Text(title.uppercase(), fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.W500, color = T2)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Bdr))
    }
}

/**
 * Texte de corps. Les placeholders "[À compléter]" s'affichent en italique/dim.
 */
@Composable
private fun Prose(text: String, color: Color = T2) {
    val isPlaceholder = text.trim().startsWith("[")
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 20.sp,
        color = if (isPlaceholder) T3 else color,
        fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
        modifier = Modifier.padding(bottom = 14.dp).fillMaxWidth()
    )
}

@Composable
private fun WarningBox(title: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp).height(IntrinsicSize.Min)) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(Red))
        Column(
            Modifier.weight(1f).background(RedBg).border(1.dp, RedBdr).padding(12.dp)
        ) {
            Text(title, fontSize = 8.sp, letterSpacing = 3.sp, color = Red, modifier = Modifier.padding(bottom = 6.dp))
            content()
        }
    }
}

@Composable
private fun NoteBox(title: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp).height(IntrinsicSize.Min)) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(GoldDk))
        Column(
            Modifier.weight(1f).background(GoldBg).border(1.dp, GoldBdr).padding(12.dp)
        ) {
            Text(title, fontSize = 8.sp, letterSpacing = 3.sp, color = Gold, modifier = Modifier.padding(bottom = 6.dp))
            content()
        }
    }
}

@Composable
private fun HRule() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
}

// ── Glitch overlay ────────────────────────────────────────────────────────────
@Composable
fun GlitchOverlay(engine: GlitchEngine, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        // Lignes de bruit horizontales
        engine.noiseLines.forEach { (posY, heightDp, alpha) ->
            drawRect(
                color = Teal.copy(alpha = alpha),
                topLeft = Offset(engine.scanlineShift.dp.toPx(), posY * size.height),
                size    = androidx.compose.ui.geometry.Size(size.width, heightDp.dp.toPx())
            )
        }
        // Flash de surface
        if (engine.flashIntensity > 0f) {
            drawRect(color = T1.copy(alpha = engine.flashIntensity))
        }
    }
}

// ── Diagramme composants — rendu Compose natif (pas d'ASCII) ─────────────────
@Composable
private fun ComponentDiagram() {
    Column(
        Modifier.fillMaxWidth().background(Card).border(1.dp, Bdr).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "IGNI TERRA — SCHÉMA FONCTIONNEL",
            fontSize = 9.sp, fontFamily = Mono, color = Teal,
            letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 8.dp)
        )

        DiagramBlock(
            title    = "BUSE DE SORTIE",
            color    = Teal,
            entries  = listOf(
                "Électrodes" to "Angle de convergence calibré",
                "Arc"        to "Arc électrique permanent (gâchette enfoncée)"
            )
        )

        DiagramConnector()

        DiagramBlock(
            title    = "MODULE PROPULSION",
            color    = Gold,
            entries  = listOf(
                "Résistance variable" to "Mécanique, pilotée par la gâchette",
                "Débit"              to "Pression légère → α  |  Pression forte → γ"
            )
        )

        DiagramConnector()

        DiagramBlock(
            title    = "MODULE ÉNERGIE",
            color    = TealDk,
            entries  = listOf(
                "Bobine Magitek"    to "Génère l'arc électrique",
                "Cristal de Foudre" to "Logement caoutchouc anti-vibrations",
                "Réinjection EM"    to "Boucle fermée — réduit la consommation du cristal"
            )
        )
    }
}

@Composable
private fun DiagramBlock(
    title   : String,
    color   : Color,
    entries : List<Pair<String, String>>
) {
    Column(
        Modifier.fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.4f))
    ) {
        // Header du bloc
        Box(
            Modifier.fillMaxWidth()
                .background(color.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(title, fontSize = 8.sp, fontFamily = Mono, color = color, letterSpacing = 2.sp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(color.copy(alpha = 0.25f)))
        // Lignes de données
        entries.forEachIndexed { i, (key, value) ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    key,
                    fontSize = 9.sp, fontFamily = Mono, color = color.copy(alpha = 0.7f),
                    modifier = Modifier.width(130.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    value,
                    fontSize = 10.sp, color = T2, lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            if (i < entries.lastIndex)
                Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
        }
    }
}

@Composable
private fun DiagramConnector() {
    Column(
        Modifier.padding(start = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        repeat(3) {
            Box(Modifier.width(1.dp).height(5.dp).background(TealDk.copy(alpha = 0.5f)))
            Spacer(Modifier.height(2.dp))
        }
    }
}

// ── Section secrète ───────────────────────────────────────────────────────────


// ── Séparateur horizontal gravé ───────────────────────────────────────────────

@Composable
fun EngravedHorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .drawBehind {
                drawLine(
                    color       = Color(0xFF000000),
                    start       = Offset(0f, size.height / 2f),
                    end         = Offset(size.width, size.height / 2f),
                    strokeWidth = 1f,
                )
                drawLine(
                    color       = Color(0x22FFFFFF),
                    start       = Offset(0f, size.height / 2f + 1f),
                    end         = Offset(size.width, size.height / 2f + 1f),
                    strokeWidth = 1f,
                )
            },
    )
}

// ── Séparateur vertical gravé ─────────────────────────────────────────────────

@Composable
fun EngravedVerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(4.dp)
            .drawBehind {
                drawLine(
                    color       = Color(0xFF000000),
                    start       = Offset(size.width / 2f, 0f),
                    end         = Offset(size.width / 2f, size.height),
                    strokeWidth = 1f,
                )
                drawLine(
                    color       = Color(0x22FFFFFF),
                    start       = Offset(size.width / 2f + 1f, 0f),
                    end         = Offset(size.width / 2f + 1f, size.height),
                    strokeWidth = 1f,
                )
            },
    )
}

@Composable
fun EngravedText(
    text         : String,
    fontSize     : TextUnit,
    modifier     : Modifier = Modifier,
    textAlign    : TextAlign = TextAlign.Start,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    alpha        : Float = 1f,
) {
    Text(
        text          = text,
        textAlign     = textAlign,
        fontSize      = fontSize,
        letterSpacing = letterSpacing,
        fontFamily    = Mono,
        color         = TealDk.copy(alpha = alpha),
        modifier      = modifier,
    )
}
enum class SecretPhase { ELEANOR, GLITCH, LOGO, DECIMUS, CIPHER }
@Composable
fun SecretSection(recipient: AppStrings.Recipient?, glitch: GlitchEngine, scope: CoroutineScope) {


    LaunchedEffect(Unit) {

       //  recipient?.musicFile?.let { CrackleSound.playWav(it, loop = true) }
    }



    var phase         by remember { mutableStateOf(SecretPhase.ELEANOR) }

    val eleanorText = "Ma chérie si tu savais comme je suis heureuse de t'avoir rencontré. Dans le fond, je crois que j'étais obscurité me voilà lumière. Avec toi, je me sens véritablement complète. Je voudrais pouvoir passer tellement plus de temps avec toi. Tout mon temps. Alors volons toutes les deux, volons loin de ce monde fou. Emportons tout avec nous. Ne reste que notre amour. Attend un peu, après ce message, ce n'est pas encore terminé."

    val decimusText = "J’ai placé une bombe quelque part. Alors, si tu lis ce message, Adrila, garde le sourire et ne fais rien. N'en parle à personne et surtout pas à ton petit robot de compagnie, Eleanor, sinon je fais tout exploser. Fais un grand sourire dérangeant à tous tes petits compagnons et jouons. Je dispose de tout le temps nécessaire, mais ce n’est pas le cas pour toi, alors sois prudente. Quand vous aurez fini votre entrainement au lance-flamme, tu pourras t'atteler à déchiffrer mon message."

    var displayedText by remember { mutableStateOf("") }
    var revealed      by remember { mutableStateOf(false) }
    var loaderProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        recipient?.musicFile?.let { CrackleSound.playWav(it, loop = true) }

        // ── Eleanor ───────────────────────────────────────────────────────────
        phase = SecretPhase.ELEANOR
        val eleanorDelay = 12000L / eleanorText.length
        for (i in eleanorText.indices) {
            displayedText = eleanorText.substring(0, i + 1)
            delay(eleanorDelay)
        }

        // ── Glitches ──────────────────────────────────────────────────────────
       /* phase = SecretPhase.GLITCH
        displayedText = ""
        repeat(6) {
            glitch.triggerNavGlitch(scope)
            CrackleSound.click()
            delay(250L)
        }*/
        // Phase GLITCH
        phase = SecretPhase.GLITCH
        displayedText = ""
        var glitchTick = 0
        repeat(6) {
            glitch.triggerNavGlitch(scope)
            CrackleSound.click()
            // Texte aléatoire qui change à chaque glitch
            displayedText = buildString {
                repeat(Random.nextInt(20, 60)) {
                    append("ABCDEFGHIJKLMNOPQRSTUVWXYZαβγδΩΨΦ∆∇∑∏#@!%&*<>".random())
                }
            }
            delay(250L)
        }
        displayedText = ""

        // ── Logo Garlemald ────────────────────────────────────────────────────
        phase = SecretPhase.LOGO
        val loaderSteps = 40
        repeat(loaderSteps) { step ->
            loaderProgress = step.toFloat() / loaderSteps
            delay(50L)
        }
        loaderProgress = 1f
        delay(3500L)

        // ── Decimus ───────────────────────────────────────────────────────────
        phase = SecretPhase.DECIMUS
        val decimusDelay = 8000L / decimusText.length
        for (i in decimusText.indices) {
            displayedText = decimusText.substring(0, i + 1)
            delay(decimusDelay)
        }
        delay(1500L)

        // ── Cipher ────────────────────────────────────────────────────────────
        repeat(2) { glitch.triggerNavGlitch(scope); delay(200L) }
        phase = SecretPhase.CIPHER
        revealed = true
    }


    val message = remember {
        try {
            buildHiddenBackMessage()
        } catch (e: Exception) {
            println("ERROR in buildHiddenBackMessage: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    if (message == null) {
        Prose("Erreur de chargement.")
        return
    }
    val scrollState = rememberScrollState()

    SectionHead(AppStrings.Secret.num, AppStrings.Secret.title)
    WarningBox("ACCÈS RESTREINT") {
        Prose("Ce document contient des informations classifiées. Toute divulgation non autorisée est passible de sanctions.")
    }
    Spacer(Modifier.height(8.dp))

    // Texte progressif
  /*  if (displayedText.isNotEmpty()) {
        Prose(
            displayedText,
            // Couleur différente selon la phase
            color = if (phase >= 2) Red.copy(alpha = 0.9f) else T2
        )
        Spacer(Modifier.height(8.dp))
    }*/
   /* when (phase) {
        SecretPhase.ELEANOR -> {
            Prose(displayedText, color = T2)
        }
        SecretPhase.GLITCH -> {
            // Rien — juste les glitches visuels
        }
        SecretPhase.LOGO -> {
            Box(
                Modifier.fillMaxWidth().height(220.dp).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                GarleanEmblem(
                    canvasSize     = 160.dp,
                    loaderDuration = 2000L,
                    onComplete     = { scope.launch { phase = SecretPhase.DECIMUS } }
                )
            }
        }
        SecretPhase.DECIMUS -> {
            Prose(displayedText, color = Red.copy(alpha = 0.9f))
        }
        SecretPhase.CIPHER -> {
            Prose(decimusText, color = Red.copy(alpha = 0.9f))
            Spacer(Modifier.height(16.dp))
            // Ton bloc cipher existant
        }
    }*/


    // Phase ELEANOR — texte tendre, disparaît au glitch
    if (phase == SecretPhase.ELEANOR && displayedText.isNotEmpty()) {
        Prose(displayedText, color = T2)
        Spacer(Modifier.height(16.dp))
    }

// Phase GLITCH — rien à afficher, juste les effets visuels
    if (phase == SecretPhase.GLITCH && displayedText.isNotEmpty()) {
        Text(
            displayedText,
            fontSize      = 11.sp,
            fontFamily    = Mono,
            color         = Red.copy(alpha = 0.7f),
            letterSpacing = 3.sp,
            lineHeight    = 16.sp,
            modifier      = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
    }

// Logo — visible dès LOGO jusqu'à la fin
    if (phase >= SecretPhase.LOGO) {
        Box(
            Modifier.fillMaxWidth().height(220.dp).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            GarleanEmblem(
                canvasSize     = 160.dp,
                loaderDuration = 2000L,
                onComplete     = { scope.launch {
                    if (phase == SecretPhase.LOGO) phase = SecretPhase.DECIMUS
                }}
            )
        }
        Spacer(Modifier.height(16.dp))
    }

// Texte Décimus — visible dès DECIMUS
    if (phase >= SecretPhase.DECIMUS && displayedText.isNotEmpty()) {
        Prose(displayedText, color = Red.copy(alpha = 0.9f))
        Spacer(Modifier.height(16.dp))
    }

// Cipher — visible en phase CIPHER
    if (phase == SecretPhase.CIPHER) {
     //   Prose(displayedText, color = T2)
       // Spacer(Modifier.height(16.dp))

        val chunks = message.words.reversed().chunked(12)

        if (revealed) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Card)
                    .border(1.dp, Bdr)
                    //.verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                chunks.forEachIndexed { chunkIdx, chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top,
                    ) {
                        chunk.forEachIndexed { colIdx, codes ->
                            EngravedCodeColumn(
                                codes = codes,
                                modifier = Modifier.widthIn(min = 60.dp)
                            )
                            if (colIdx < chunk.size - 1) {
                                EngravedVerticalDivider()
                            }
                        }
                        repeat(4 - chunk.size) {
                            Spacer(Modifier.widthIn(min = 60.dp))
                        }
                    }
                    if (chunkIdx < chunks.size - 1) {
                        EngravedHorizontalDivider()
                    }
                }
            }
        }
    }


    // Groupes de 4 colonnes par ligne

}

@Composable
fun EngravedCodeColumn(codes: List<String>, modifier: Modifier = Modifier) {
    Column(
        modifier                = modifier.padding(horizontal = 8.dp),
        verticalArrangement     = Arrangement.spacedBy(6.dp),
        horizontalAlignment     = Alignment.CenterHorizontally,
    ) {
        codes.forEach { code ->
            EngravedText(
                text          = code,
                fontSize      = 11.sp,
                letterSpacing = 2.sp,
                textAlign     = TextAlign.Center,
            )
        }
    }


}