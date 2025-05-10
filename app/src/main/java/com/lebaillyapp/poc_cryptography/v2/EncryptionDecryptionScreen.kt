package com.lebaillyapp.poc_cryptography.v2

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.lebaillyapp.poc_cryptography.screen.CryptoViewModel

/**
 * ## EncryptionDecryptionScreen
 *
 * `EncryptionDecryptionScreen` est l'écran principal de l'application permettant à l'utilisateur de sélectionner un dossier de stockage pour les fichiers chiffrés, afficher la liste des fichiers et interagir avec des actions via la `BottomAppBar`.
 *
 * ---
 *
 * ### Responsabilités principales
 * - Permet à l'utilisateur de choisir un dossier de stockage via un `AssistChip`.
 * - Affiche une liste des fichiers chiffrés dans le dossier sélectionné avec des informations de taille.
 * - Offre un `BottomAppBar` avec plusieurs icônes pour des actions (par exemple, vérifier, éditer, etc.).
 * - Inclut un `FloatingActionButton` pour ajouter de nouvelles actions.
 *
 * ---
 *
 * ### Paramètres
 * - `modifier`: Modificateur pour personnaliser l'UI (par défaut `Modifier`).
 * - `onRequestDirectorySelection`: Callback appelé pour ouvrir le sélecteur de dossier.
 * - `bottomAppBarBackgroundColor`: Couleur de fond pour la `BottomAppBar` (par défaut `MaterialTheme.colorScheme.secondaryContainer`).
 * - `fabBackgroundColor`: Couleur de fond pour le `FloatingActionButton` (par défaut `MaterialTheme.colorScheme.primary`).
 * - `iconGlobalTintColor`: Couleur d'icône globale pour les icônes de la barre inférieure (par défaut `MaterialTheme.colorScheme.onSecondaryContainer`).
 * - `viewModel`: Vue modèle pour gérer les données de l'écran. (Par défaut, un `hiltViewModel()` est utilisé).
 *
 * ---
 *
 * ### Composants utilisés
 * - `Scaffold`: Conteneur principal pour la structure de l'écran, incluant la barre inférieure et les actions flottantes.
 * - `BottomAppBar`: Barre inférieure avec des icônes pour les actions.
 * - `FloatingActionButton`: Bouton flottant pour ajouter une nouvelle action.
 * - `AssistChip`: Permet à l'utilisateur de sélectionner un dossier de stockage.
 * - `LazyColumn`: Liste paresseuse des fichiers chiffrés, avec des informations de taille et un message "Aucun fichier trouvé" si la liste est vide.
 *
 * ---
 *
 * ### Remarque technique
 * L'écran effectue un appel `LaunchedEffect` pour charger les fichiers au démarrage. Le dossier sélectionné est récupéré via le `StoragePrefs` pour gérer l'URI du dossier.
 *
 * Le sélecteur de dossier SAF est déclenché par le `AssistChip` et le résultat est affiché dans le label du chip.
 * Les actions de la `BottomAppBar` n'ont pas encore de comportement défini (`IconButton` vide pour l'instant).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EncryptionDecryptionScreen(
    modifier: Modifier = Modifier,
    onRequestDirectorySelection: () -> Unit,
    bottomAppBarBackgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    fabBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconGlobalTintColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    storagePrefs: StoragePrefs,
    viewModel: CryptoViewModel = hiltViewModel()
) {

    // Récupérer le contexte et les préférences de stockage
    val context = LocalContext.current



    // URI du dossier sélectionné et la liste des fichiers
    val encryptedDirUri by storagePrefs.encryptedDirUriFlow.collectAsState()
    val files by viewModel.files.collectAsState()

    // Charger les fichiers chiffrés après un délai de 3 secondes pour simuler le démarrage
    LaunchedEffect(encryptedDirUri) {
        encryptedDirUri?.let {
            viewModel.loadEncryptedFiles(it, context)
        }
    }


    // Structure principale avec Scaffold (qui inclut une BottomAppBar et un FloatingActionButton)
    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = bottomAppBarBackgroundColor,
                tonalElevation = 3.dp,
                actions = {
                    // Icônes de la barre inférieure (actuellement sans actions)
                    IconButton(onClick = { /* do something */ }) {
                        Icon(Icons.Filled.Check, tint = iconGlobalTintColor, contentDescription = "Localized description")
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(Icons.Filled.Edit, tint = iconGlobalTintColor, contentDescription = "Localized description")
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(Icons.Filled.Face, tint = iconGlobalTintColor, contentDescription = "Localized description")
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(Icons.Filled.Person, tint = iconGlobalTintColor, contentDescription = "Localized description")
                    }
                },
                floatingActionButton = {
                    // Bouton flottant d'ajout
                    FloatingActionButton(
                        onClick = { /* do something */ },
                        containerColor = fabBackgroundColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, "Localized description", tint = iconGlobalTintColor)
                    }
                }
            )
        }
    ) { innerPadding ->

        // Contenu principal de l'écran
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Chip pour sélectionner un dossier de stockage
            AssistChip(
                onClick = { onRequestDirectorySelection() },
                label = {
                    Text(
                        text = encryptedDirUri?.lastPathSegment?: "No directory selected",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Check, contentDescription = null)
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Liste des fichiers chiffrés
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                items(files) { file ->
                    // Affichage de chaque fichier avec son nom et sa taille
                    ListItem(
                        headlineContent = { Text(file.name) },
                        supportingContent = { Text("${file.size} bytes") },
                        leadingContent = {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null
                            )
                        }
                    )
                    Divider()
                }

                // Si aucun fichier n'est trouvé
                if (files.isEmpty()) {
                    item {
                        Text(
                            text = "Aucun fichier trouvé.",
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }


    }
}