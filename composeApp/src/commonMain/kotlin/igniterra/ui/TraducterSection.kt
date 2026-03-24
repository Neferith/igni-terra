package igniterra.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import igniterra.model.ButtonLabelEncoder
import igniterra.model.MagitekCipher
import igniterra.strings.AppStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.angelus.magitek.model.ButtonAssignment
import org.angelus.magitek.model.ButtonConfig

/*@Composable
fun TraducterSection(recipient: AppStrings.Recipient?, glitch: GlitchEngine, scope: CoroutineScope) {
    Column(Modifier.sizeIn(maxWidth = 450.dp).fillMaxHeight().background(Panel)){

    AutoScrollScreen(lines = emptyList())



            ButtonGrid(
                buttonConfigs = emptyMap(),
                runningMacroIndex = 0,
                isEditMode = false,
                glitchEngine = glitch,
                onTap = {

                },
                onLongPress = {

                },
                // modifier = Modifier.fillMaxSize(450f)
            )

    }
}*/

@Composable
fun TraducterSection(recipient: AppStrings.Recipient?, glitch: GlitchEngine, scope: CoroutineScope) {
    var lines by remember { mutableStateOf(listOf("")) }

    fun appendChar(char: String) {
        val currentLine = lines.last()
        lines = lines.dropLast(1) + (currentLine + char)
    }

    fun newLine() {
        lines = lines + ""
    }



    Column(Modifier.sizeIn(maxWidth = 450.dp).fillMaxHeight().background(Panel)) {

        AutoScrollScreen(
            lines    = lines,
            modifier = Modifier.fillMaxWidth()
        )

        ButtonGrid(
            buttonConfigs     = emptyMap(),
            runningMacroIndex = null,
            isEditMode        = false,
            glitchEngine      = glitch,
            onTap             = { index ->
                // À toi de mapper index → caractère selon ta config
                appendChar(MagitekCipher.charAt(index))
            },
            onLongPress = {},
        )

        // Bouton entrée
        Box(
            Modifier
                .fillMaxWidth()
                .background(Panel)
                .border(1.dp, Bdr)
                .clickable { newLine(); CrackleSound.click() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "↵  ENTRÉE",
                fontSize      = 9.sp,
                letterSpacing = 3.sp,
                fontFamily    = Mono,
                color         = TealDk
            )
        }
    }
}

@Composable
fun AutoScrollScreen(
    lines   : List<String>,
    modifier: Modifier = Modifier
) {
    val lineHeight   = 18.dp
    val visibleLines = 4
    val scope        = rememberCoroutineScope()
    val scrollState  = rememberScrollState()

    // Scroll vers le bas à chaque nouvelle ligne
    LaunchedEffect(lines.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(lineHeight * visibleLines)
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            lines.forEach { line ->
                Text(
                    text          = line,
                    fontSize      = 9.sp,
                    fontFamily    = Mono,
                    color         = TealDk,
                    letterSpacing = 1.sp,
                    maxLines      = 1,
                )
            }
        }
    }
}


// ── Grille de 64 boutons ──────────────────────────────────────────────────────

@Composable
fun ButtonGrid(
    buttonConfigs: Map<Int, ButtonConfig>,
    runningMacroIndex: Int?,
    isEditMode: Boolean,
    glitchEngine: GlitchEngine,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = glitchEngine.gridShake.dp, y = 0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        (0 until 8).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (0 until 8).forEach { col ->
                    val index = row * 8 + col
                    MagitekButton(
                        modifier  = Modifier.weight(1f),
                        index     = index,
                        config    = buttonConfigs[index],
                        isRunning = runningMacroIndex == index,
                        isEditMode = isEditMode,
                        onTap     = { onTap(index) },
                        onLongPress = { onLongPress(index) },
                    )
                }
            }
        }
    }
}
// ── MagitekButton — ajouter isRunning ────────────────────────────────────────

// Dans MagitekRemoteScreen.kt — remplacer MagitekButton par cette version

@Composable
fun MagitekButton(
    modifier: Modifier = Modifier,
    index: Int,
    config: ButtonConfig?,
    isRunning: Boolean,
    isEditMode: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "running_$index")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_$index",
    )

    val isMacro = config?.assignment is ButtonAssignment.Macro
    val isAssigned = config != null

    val bgColor = when {
        isPressed -> GarlemaldColors.ImperialRedDark
        isRunning -> GarlemaldColors.MagitekBlueDim
        isMacro -> GarlemaldColors.MagitekBlueDim.copy(alpha = 0.5f)
        isAssigned -> GarlemaldColors.SurfaceVariant
        isEditMode -> GarlemaldColors.ScreenBackground   // non assigné en mode édition
        else -> GarlemaldColors.Background
    }
    val borderColor = when {
        isPressed -> GarlemaldColors.ImperialRedGlow
        isRunning -> GarlemaldColors.MagitekBlue.copy(alpha = pulseAlpha)
        isMacro -> GarlemaldColors.MagitekBlue
        isAssigned -> GarlemaldColors.Border
        isEditMode -> GarlemaldColors.ScreenGreenDim.copy(alpha = 0.4f)  // contour vert discret
        else -> GarlemaldColors.MetalDark.copy(alpha = 0.4f)
    }
    val textColor = when {
        isPressed -> GarlemaldColors.OnImperialRed
        isAssigned -> GarlemaldColors.OnSurface
        else -> GarlemaldColors.MetalDark
    }

    val label = ButtonLabelEncoder.encode(index)/*config?.assignment?.displayLabel(config.customLabel)
        ?: index.toString().padStart(2, '0')*/

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgColor)
            .border(1.dp, borderColor)
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onPress = { offset ->
                        // Émettre immédiatement le press → visuel rouge instantané
                        val press = PressInteraction.Press(offset)
                        scope.launch { interactionSource.emit(press) }
                        val released = tryAwaitRelease()
                        scope.launch {
                            if (released) interactionSource.emit(PressInteraction.Release(press))
                            else interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    },
                    onTap = { onTap() },
                    onLongPress = { onLongPress() },
                )
            }
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(borderColor.copy(alpha = 0.5f)),
        )
        if (isRunning) {
            Text(
                text = "■",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp,
                    color = GarlemaldColors.OnSurface,
                ).copy(
                    color = GarlemaldColors.MagitekBlue.copy(alpha = pulseAlpha),
                    fontSize = 10.sp,
                ),
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp,
                    color = GarlemaldColors.OnSurface,
                ).copy(
                    color = textColor,
                    fontSize = 7.sp,
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}