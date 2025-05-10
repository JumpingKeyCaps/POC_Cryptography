package com.lebaillyapp.poc_cryptography

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lebaillyapp.poc_cryptography.ui.theme.POC_CryptographyTheme
import com.lebaillyapp.poc_cryptography.ui.theme.chartreuse
import com.lebaillyapp.poc_cryptography.ui.theme.gunMetal
import com.lebaillyapp.poc_cryptography.v2.EncryptionDecryptionScreen
import com.lebaillyapp.poc_cryptography.v2.StoragePrefs
import dagger.hilt.android.AndroidEntryPoint


/**
 * ##  MainActivity
 *
 * `MainActivity` est le point d’entrée principal de l’application.
 * Elle initialise les préférences de stockage, gère la sélection initiale d’un dossier via le **Storage Access Framework (SAF)**,
 * et charge l’interface utilisateur principale avec Jetpack Compose.
 *
 * ---
 *
 * ###  Responsabilités principales
 * - Afficher un **dialogue SAF** au premier lancement pour choisir le dossier de sauvegarde.
 * - Sauvegarder l’URI sélectionné dans les `SharedPreferences` via `StoragePrefs`.
 * - Appliquer le thème de l’application (couleurs système, barres de navigation, etc.).
 * - Afficher l’écran principal de chiffrement avec Jetpack Compose.
 *
 * ---
 *
 * ###  Composants utilisés
 * - `StoragePrefs` : classe utilitaire pour gérer l'URI sauvegardé.
 * - `POC_CryptographyTheme` : thème Jetpack Compose.
 * - `EncryptionDecryptionScreen` : écran principal de l’app.
 *
 * ---
 *
 * ### ️ Gestion du SAF
 * Si aucun dossier de sauvegarde n’est configuré, une **boîte de dialogue** s'affiche pour permettre à l’utilisateur de choisir un dossier. L’URI obtenu est ensuite persisté avec les bons droits d'accès.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var storagePrefs: StoragePrefs
    private lateinit var openDocumentTreeLauncher: ActivityResultLauncher<Intent>

    private val requestFlags =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storagePrefs = StoragePrefs(this)

        // Couleurs UI de la fenêtre
        initAppColorUI(window)

        // Permissions pour Android < 13
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                123
            )
        }

        // Initialiser l'ActivityResultLauncher
        openDocumentTreeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    contentResolver.takePersistableUriPermission(
                        uri,
                        requestFlags
                    )
                    storagePrefs.saveEncryptedDirUri(uri)
                }
            }
        }

        // Chargement de l’UI principale avec Jetpack Compose
        setContent {
            POC_CryptographyTheme {
                window.navigationBarColor = gunMetal.toArgb()

                // Si aucun dossier de sauvegarde n'est configuré, demander à l'utilisateur de le sélectionner
                if (!storagePrefs.hasEncryptedDirUri()) {
                    ShowStorageSetupDialog()
                }

                EncryptionDecryptionScreen(
                    onRequestDirectorySelection = { launchDirectorySelection() },
                    bottomAppBarBackgroundColor = gunMetal,
                    fabBackgroundColor = chartreuse,
                    iconGlobalTintColor = Color.White,
                    storagePrefs = storagePrefs
                )
            }
        }
    }

    /**
     * Lance l’Intent pour choisir un dossier avec SAF
     */
    private fun launchDirectorySelection() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(requestFlags)
        }
        openDocumentTreeLauncher.launch(intent)
    }

    /**
     * Affiche une boîte de dialogue Material 3 pour que l’utilisateur choisisse un dossier SAF.
     */
    @Composable
    fun ShowStorageSetupDialog() {
        // Gestion de l'état du dialogue
        var openDialog by remember { mutableStateOf(true) }

        if (openDialog) {
            AlertDialog(
                onDismissRequest = { /* L'utilisateur a fermé la boîte de dialogue, on le laisse fermer si nécessaire */ },
                title = { Text(text = "Dossier de sauvegarde") },
                text = { Text(text = "Veuillez choisir un dossier pour stocker vos fichiers chiffrés.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            launchDirectorySelection()
                            openDialog = false // Ferme le dialogue après l'action
                        }
                    ) {
                        Text("Choisir")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            finish() // Ferme l'application si l'utilisateur annule
                            openDialog = false
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
    }

    /**
     * Applique les couleurs système à la fenêtre de l’application.
     */
    private fun initAppColorUI(window: android.view.Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
    }
}
