package com.lebaillyapp.poc_cryptography.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.poc_cryptography.R
import com.lebaillyapp.poc_cryptography.ui.theme.chartreuse
import com.lebaillyapp.poc_cryptography.ui.theme.gunMetal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoBottomSheet(
    isSheetOpen: Boolean,
    onDismiss: () -> Unit,
    pagerState: PagerState,
    onEncryptDecryptClick: (password: String, extension: String, obfuscateFileName: Boolean) -> Unit,
    generatedSalt: String?,
    generatedHash: String?,
    onPasswordChanged: (String) -> Unit
) {
    /**
     * Récupère la couleur de fond en fonction de la page actuelle du Pager.
     * Gun Metal pour la page d'encryption, Chartreuse pour la page de decryption.
     */
    val backgroundColor = if (pagerState.currentPage == 0) gunMetal else chartreuse
    /**
     * Récupère la couleur du bouton en fonction de la page actuelle du Pager.
     * Chartreuse pour le bouton d'encryption, Gun Metal pour le bouton de decryption.
     */
    val buttonColor = if (pagerState.currentPage == 0) chartreuse else gunMetal
    /**
     * Récupère la couleur du texte principal en fonction de la page actuelle du Pager.
     * Gun Metal pour le texte d'encryption, Chartreuse pour le texte de decryption.
     */
    val textColor = if (pagerState.currentPage == 0) gunMetal else chartreuse
    /**
     * Récupère la couleur du texte inversée (pour les titres) en fonction de la page actuelle du Pager.
     * Chartreuse pour le titre d'encryption, Gun Metal pour le titre de decryption.
     */
    val textColorInv = if (pagerState.currentPage == 0) chartreuse else gunMetal

    /**
     * Etat local pour stocker la valeur du mot de passe saisi par l'utilisateur.
     */
    var password by remember { mutableStateOf("") }
    /**
     * Etat local pour stocker l'extension de fichier personnalisée pour l'encryption.
     * La valeur par défaut est "crypt".
     */
    var extension by remember { mutableStateOf("crypt") }
    /**
     * Etat local pour contrôler la visibilité du mot de passe (afficher/masquer).
     */
    var passwordVisible by remember { mutableStateOf(false) }


    var obfuscateFileName by remember { mutableStateOf(false) }


    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(isSheetOpen) {
        if (isSheetOpen) {
            sheetState.expand() // Animer directement à l'état étendu
        } else {
            sheetState.hide()
        }
    }


    /**
     * Affiche la ModalBottomSheet si [isSheetOpen] est true.
     */
    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = backgroundColor.copy(alpha = 0.98f),
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(1f).padding(top = 46.dp), // Augmentez la hauteur pour afficher le sel et le hash
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Secret Key",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily(Font(R.font.sairacondensed_black)),
                    color = textColorInv
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    if (pagerState.currentPage == 0) "Setup the decryption key pass !" else "Enter the encryption key pass.",
                    color = textColorInv
                )


                Spacer(Modifier.height(24.dp))

                /**
                 * Affiche le sel généré si la valeur n'est pas nulle.
                 * Ceci est uniquement à des fins de démonstration (POC).
                 */
                generatedSalt?.let { salt ->
                    Text("Generated Salt (Hex):", color = textColorInv, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(salt, color = textColorInv, style = MaterialTheme.typography.bodySmall,modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(8.dp))
                }

                /**
                 * Affiche un aperçu du hash généré si la valeur n'est pas nulle.
                 * Affiche uniquement les 32 premiers octets (64 caractères hexadécimaux) pour éviter un affichage trop long.
                 * Ceci est uniquement à des fins de démonstration (POC).
                 */
                generatedHash?.let { hash ->
                    Text("Generated Hash (First 32 bytes):", color = textColorInv, fontWeight = FontWeight.Bold,modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text(hash.take(64),
                        color = textColorInv,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(start = 36.dp, end = 36.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                }




                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onPasswordChanged(it) // maj temps reel du hash et de l'iv
                    },
                    label = { Text("Password", color = textColorInv) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Rounded.KeyboardArrowLeft else Icons.Rounded.KeyboardArrowRight
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = icon, contentDescription = null, tint = textColorInv)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor,
                        focusedTextColor = textColorInv,
                        unfocusedTextColor = textColorInv,
                        focusedLabelColor = textColorInv,
                        unfocusedLabelColor = textColorInv,
                        focusedIndicatorColor = textColorInv,
                        unfocusedIndicatorColor = textColorInv,
                        cursorColor = textColorInv
                    )
                )


                /**
                 * Affiche le champ d'extension personnalisée uniquement sur la page d'encryption.
                 */
                if (pagerState.currentPage == 0) {
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = extension,
                        onValueChange = { extension = it },
                        label = { Text("Custom Extension", color = textColorInv) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = backgroundColor,
                            unfocusedContainerColor = backgroundColor,
                            focusedTextColor = textColorInv,
                            unfocusedTextColor = textColorInv,
                            focusedLabelColor = textColorInv,
                            unfocusedLabelColor = textColorInv,
                            focusedIndicatorColor = textColorInv,
                            unfocusedIndicatorColor = textColorInv,
                            cursorColor = textColorInv
                        )
                    )


                }
                Spacer(Modifier.height(24.dp))
                //ofuscate file name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, end = 36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Obfuscate File Name",
                        fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                        color = chartreuse,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = obfuscateFileName,
                        onCheckedChange = { obfuscateFileName = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = chartreuse,
                            checkedTrackColor = chartreuse.copy(alpha = 0.6f),
                            uncheckedThumbColor = chartreuse.copy(alpha = 0.3f),
                            uncheckedTrackColor = chartreuse.copy(alpha = 0.2f)
                        )
                    )
                }
                Spacer(Modifier.height(64.dp))

                /**
                 * Bouton d'action pour lancer l'encryption ou le decryption.
                 * L'état [enabled] dépend de si le mot de passe et (pour l'encryption) l'extension sont renseignés.
                 */
                Button(
                    modifier = Modifier.width(250.dp).height(56.dp),
                    onClick = {
                        onEncryptDecryptClick(password, extension, obfuscateFileName)
                        onDismiss()
                    },
                    enabled = password.isNotBlank() && (pagerState.currentPage != 0 || extension.isNotBlank()),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    Text(
                        if (pagerState.currentPage == 0) "Encrypt!" else "Decrypt!",
                        fontSize = 25.sp,
                        color = backgroundColor,
                        fontFamily = FontFamily(Font(R.font.bytesized_regular))
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}