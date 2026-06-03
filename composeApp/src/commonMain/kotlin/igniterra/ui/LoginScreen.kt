package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import igniterra.CrackleSound
import igniterra.strings.AppStrings

// ── Écran de connexion ────────────────────────────────────────────────────────
@Composable
fun LoginScreen(onSuccess: (AppStrings.Recipient) -> Unit) {
    var selectedRecipient by remember { mutableStateOf<AppStrings.Recipient?>(null) }
    var password          by remember { mutableStateOf("") }
    var error             by remember { mutableStateOf(false) }
    var expanded          by remember { mutableStateOf(false) }
    var searchQuery       by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String>("Code incorrect.") }
    Box(
        Modifier.fillMaxSize().background(Bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(380.dp).background(Panel).border(1.dp, Bdr),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(Modifier.fillMaxWidth().background(Color(0xFF09162A)).padding(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(AppStrings.Header.orgShort, fontSize = 8.sp, letterSpacing = 3.sp, fontFamily = Mono, color = T3)
                    Spacer(Modifier.height(6.dp))
                    Text("IGNI TERRA", fontSize = 18.sp, fontWeight = FontWeight.W300, letterSpacing = 8.sp, color = Teal)
                    Spacer(Modifier.height(4.dp))
                    Text("Accès sécurisé", fontSize = 9.sp, letterSpacing = 2.sp, color = T3)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))

            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Dropdown destinataire
                Text("Destinataire", fontSize = 8.sp, letterSpacing = 2.sp, color = T3)

                // Champ de recherche
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; expanded = true; selectedRecipient = null },
                    singleLine = true,
                    placeholder = { Text("Rechercher...", fontSize = 10.sp, color = T3) },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor         = Card,
                        textColor               = T1,
                        cursorColor             = Teal,
                        focusedIndicatorColor   = Teal,
                        unfocusedIndicatorColor = Bdr,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Liste filtrée
                val filtered = AppStrings.recipients.filter {
                    searchQuery.isBlank() || it.displayName.contains(searchQuery, ignoreCase = true)
                }
                if (expanded && filtered.isNotEmpty()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .border(1.dp, Bdr)
                            .background(Card)
                            .verticalScroll(rememberScrollState())
                    ) {
                        filtered.forEach { r ->
                            Box(
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        selectedRecipient = r
                                        searchQuery = r.displayName
                                        expanded = false
                                        password = ""
                                        error = false
                                        CrackleSound.click()
                                    }
                                    .background(if (r == selectedRecipient) Color(0x1538C4C4) else Color.Transparent)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(r.displayName, fontSize = 11.sp, fontFamily = Mono, color = T2)
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
                        }
                    }
                }

                // Champ mot de passe
                if (selectedRecipient != null) {
                    Text("Mot de passe", fontSize = 8.sp, letterSpacing = 2.sp, color = T3)
                    TextField(
                        value         = password,
                        onValueChange = { password = it; error = false },
                        singleLine    = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor         = Card,
                            textColor               = T1,
                            cursorColor             = Teal,
                            focusedIndicatorColor   = Teal,
                            unfocusedIndicatorColor = Bdr,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error) {
                        Text(errorMessage, fontSize = 9.sp, color = Red, letterSpacing = 1.sp)
                    }

                    // Bouton valider
                    Box(
                        Modifier.fillMaxWidth()
                            .background(TealDk)
                            .clickable {
                                CrackleSound.click()
                                val r = selectedRecipient ?: return@clickable
                                if (password == r.password) {
                                    onSuccess(r)
                                } else {
                                    error = true
                                    password = ""
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ACCÉDER", fontSize = 9.sp, letterSpacing = 4.sp, fontFamily = Mono, color = Teal)
                    }
                }

                // Slider volume
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Bdr))
                Spacer(Modifier.height(10.dp))
                var volume by remember { mutableStateOf(1f) }
                var muted  by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth(0.5f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("SON", fontSize = 7.sp, letterSpacing = 2.sp, fontFamily = Mono, color = T3)
                        Spacer(Modifier.weight(1f))
                        var musicPlaying by remember { mutableStateOf(false) }
                        Box(
                            Modifier
                                .border(1.dp, if (musicPlaying) Teal else Bdr, RoundedCornerShape(2.dp))
                                .clickable {
                                    CrackleSound.click()
                                    if (musicPlaying) CrackleSound.stopWav()
                                    else CrackleSound.playWav("endwalker8bits.mp3", loop = true)
                                    musicPlaying = !musicPlaying
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (musicPlaying) "STOP" else "TEST", fontSize = 7.sp, fontFamily = Mono,
                                color = if (musicPlaying) Teal else T3)
                        }
                        Box(
                            Modifier
                                .border(1.dp, if (muted) Red.copy(alpha = 0.5f) else Bdr, RoundedCornerShape(2.dp))
                                .clickable { muted = !muted; CrackleSound.setVolume(if (muted) 0f else volume); CrackleSound.click() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (muted) "MUET" else "ON", fontSize = 7.sp, fontFamily = Mono,
                                color = if (muted) Red.copy(alpha = 0.5f) else TealDk)
                        }
                    }
                    Slider(
                        value = if (muted) 0f else volume,
                        onValueChange = { v -> volume = v; if (muted && v > 0f) muted = false; CrackleSound.setVolume(v) },
                        modifier = Modifier.fillMaxWidth().height(20.dp),
                        colors = androidx.compose.material.SliderDefaults.colors(thumbColor = Teal, activeTrackColor = Teal, inactiveTrackColor = Bdr)
                    )
                }
            }
        }
    }
}