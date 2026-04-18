package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound

private val EBg   = Color(0xFF02050A)
private val ETeal = Color(0xFF7EC8E3)
private val EGold = Color(0xFFCFE8FF)
private val ERed  = Color(0xFFC84040)
private val EOrange = Color(0xFFFF6B00)
private val ET3   = Color(0xFF4A6880)
private val EMono = FontFamily.Monospace

private val GOOD_ENDING_TEXT = """
[PLACEHOLDER — message bonne fin]
""".trim()

private val BAD_ENDING_TEXT = """
[PLACEHOLDER — message mauvaise fin]
""".trim()

@Composable
fun EndScreen(
    isBadEnding : Boolean,
    onQuit      : () -> Unit
) {
    var displayedText by remember { mutableStateOf("") }
    val fullText = if (isBadEnding) BAD_ENDING_TEXT else GOOD_ENDING_TEXT
    val accentColor = if (isBadEnding) EOrange else EGold

    LaunchedEffect(Unit) {
        val charDelay = 12000L / fullText.length
        for (i in fullText.indices) {
            displayedText = fullText.substring(0, i + 1)
            kotlinx.coroutines.delay(charDelay)
        }
    }

    Box(
        Modifier.fillMaxSize().background(EBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.widthIn(max = 480.dp).padding(40.dp)
        ) {
            // Titre
            Text(
                if (isBadEnding) "FIN AMERE" else "FIN",
                fontSize = 10.sp, fontFamily = EMono,
                color = accentColor, letterSpacing = 8.sp
            )
            Box(Modifier.width(160.dp).height(1.dp).background(accentColor.copy(alpha = 0.5f)))

            Spacer(Modifier.height(8.dp))

            // Texte typewriter
            Text(
                displayedText,
                fontSize = 11.sp, fontFamily = EMono,
                color = ETeal.copy(alpha = 0.9f),
                lineHeight = 19.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .background(Color(0xFF080F18))
                    .border(1.dp, accentColor.copy(alpha = 0.3f))
                    .padding(18.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Bouton quitter — visible uniquement quand le texte est fini
            if (displayedText == fullText) {
                Box(Modifier.width(160.dp).height(1.dp).background(accentColor.copy(alpha = 0.3f)))
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .border(1.dp, accentColor.copy(alpha = 0.6f))
                        .clickable {
                            CrackleSound.click()
                            onQuit()
                        }
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        "FERMER",
                        fontSize = 8.sp, fontFamily = EMono,
                        color = accentColor, letterSpacing = 4.sp
                    )
                }
            }
        }
    }
}
