package igniterra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                Box(
                    Modifier.fillMaxWidth().border(1.dp, if (expanded) Teal else Bdr)
                        .clickable { expanded = !expanded; CrackleSound.click() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        selectedRecipient?.displayName ?: "Sélectionner...",
                        fontSize = 11.sp, fontFamily = Mono,
                        color = if (selectedRecipient != null) T1 else T3
                    )
                }
                if (expanded) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .border(1.dp, Bdr)
                            .background(Card)
                            .verticalScroll(rememberScrollState())
                    ) {
                        AppStrings.recipients.forEach { r ->
                            Box(
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        selectedRecipient = r
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
                            backgroundColor    = Card,
                            textColor          = T1,
                            cursorColor        = Teal,
                            focusedIndicatorColor   = Teal,
                            unfocusedIndicatorColor = Bdr,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error) {
                        Text("Code incorrect.", fontSize = 9.sp, color = Red, letterSpacing = 1.sp)
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
            }
        }
    }
}