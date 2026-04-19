package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

enum class EndType { VICTORY, BITTER_VICTORY, BAD_END }


private val GOOD_ENDING_TEXT = """
Je dois dire que tu m’impressionnes chaque fois un peu plus Adrila. Je ne pensais pas que tu t’en sortirais aussi bien à mon petit jeu.
J’ai une nouvelle demande pour toi, Adrila. J’aurais besoin que tu te réserves un Igni Terra pour réaliser une tâche personnelle. Surtout, n’en parle pas à ton robot de compagnie, je suis presque certaine qu’elle n’apprécierait pas.
Je veux ensuite que tu retournes finir le travail que nous n’avons pas pu finir à cause de toi. Tu sauras tout une fois sur place avec l’igni terra.
Oh ! J’y pense. J’ai pu voir les jolis portraits qu’ils ont pu afficher de toi en Noscea dans le Coerthas. Très réussi. Fais attention… à un moment ou à un autre, ça risque de remonter un peu plus haut. Ça pourrait nuire à ta réputation. Ça ferait mauvais genre qu'on apprenne que la cheffe de la nouvelle lune est une meurtrière, non ? Ne t’en fais pas. Je suis sûre qu’on pourra régler cette situation si tu fais bien ton travail.
Oh… et inutile d’envisager de me refuser quoi que ce soit. Tu as peut-être pu sauver Elea dans un jeu. Mais est-ce que tu y parviendras mille fois dans la vraie vie ?

Decimus Sas Varen

""".trim()

private val BAD_ENDING_TEXT  = """
C’est donc tout ce que vaut la vie d’Elea à tes yeux ? Je suis déçue, véritablement déçue Adrila. Je devrais peut-être la tuer pour de vrai ?
Tu y penses ?
Réfléchis Adrila… est ce que tout ceci est bien raisonnable ? Parviendras-tu à sauver Elea une seule fois ? Nous allons voir.
J’ai une nouvelle demande pour toi, Adrila. J’aurais besoin que tu te réserves un Igni Terra pour réaliser une tâche personnelle. Surtout, n’en parle pas à ton petit robot de compagnie, je suis presque certaine qu’elle n’apprécierait pas.
Je veux ensuite que tu retournes finir le travail que nous n’avons pas pu finir à cause de toi dans le Coerthas. Tu sauras tout une fois sur place avec l’igni terra.
Oh ! J’y pense. J’ai pu voir les jolis portraits qu’ils ont pu afficher de toi en Noscea. Très réussi. Fais attention… à un moment ou à un autre, ça risque de remonter un peu plus haut. Ça pourrait nuire à ta réputation. Ça ferait mauvais genre qu'on apprenne que la cheffe de la nouvelle lune est une meurtrière, non ? Ne t’en fais pas. Je suis sûre qu’on pourra régler cette situation si tu fais bien ton travail.

Decimus Sas Varen
""".trim()

private val DEFEAT_TEXT = """
Aucune persévérance. C’est vraiment très décevant Adrila. Tu ne sais pas le temps que j’ai dû prendre pour mettre tout ceci en place et tu jettes l’ensemble comme un malpropre. Je suis outré. Terriblement outré. Tu n’as vraiment aucune valeur à mes yeux. Traitresse. Mais j’ai encore besoin de toi, si tu es intelligente, tu t’en serviras comme levier pour remonter dans mon estime.
J’ai une nouvelle demande pour toi, Adrila. J’aurais besoin que tu te réserves un Igni Terra pour réaliser une tâche personnelle. Surtout, n’en parle pas à ton robot de compagnie, je suis presque certaine qu’elle n’apprécierait pas.
Je veux ensuite que tu retournes finir le travail que nous n’avons pas pu finir à cause de toi dans le Coerthas. Tu sauras tout une fois sur place avec l’igni terra.
Oh ! J’y pense. J’ai pu voir les jolis portraits qu’ils ont pu afficher de toi en Noscea. Très réussi. Fais attention… à un moment ou à un autre, ça risque de remonter un peu plus haut. Ça pourrait nuire à ta réputation. Ça ferait mauvais genre qu'on apprenne que la cheffe de la nouvelle lune est une meurtrière, non ? Ne t’en fais pas. Je suis sûre qu’on pourra régler cette situation si tu fais bien ton travail.

Decimus Sas Varen

""".trim()

@Composable
fun EndScreen(
    endType     : EndType,
    onQuit      : () -> Unit
) {
    var displayedText by remember { mutableStateOf("") }
    val fullText = when (endType) {
        EndType.VICTORY        -> GOOD_ENDING_TEXT
        EndType.BITTER_VICTORY -> BAD_ENDING_TEXT
        EndType.BAD_END        -> DEFEAT_TEXT
    }
    val accentColor = when (endType) {
        EndType.VICTORY        -> EGold
        EndType.BITTER_VICTORY -> EOrange
        EndType.BAD_END        -> ERed
    }

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
            modifier = Modifier
                .widthIn(max = 480.dp)
                .padding(40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Titre
            Text(
                when {
                    endType == EndType.BAD_END  -> "GAME OVER"
                    endType == EndType.BITTER_VICTORY -> "VICTOIRE AMERE"
                    else         -> "VICTOIRE !"
                },
                fontSize = 10.sp, fontFamily = EMono,
                color = accentColor, letterSpacing = 8.sp
            )
            Box(Modifier.width(160.dp).height(1.dp).background(accentColor.copy(alpha = 0.5f)))

            Spacer(Modifier.height(8.dp))

            // Texte typewriter
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF080F18))
                    .border(1.dp, accentColor.copy(alpha = 0.3f))
                    .padding(18.dp)
            ) {
                Text(
                    displayedText,
                    fontSize = 11.sp, fontFamily = EMono,
                    color = ETeal.copy(alpha = 0.9f),
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Start
                )
            }

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