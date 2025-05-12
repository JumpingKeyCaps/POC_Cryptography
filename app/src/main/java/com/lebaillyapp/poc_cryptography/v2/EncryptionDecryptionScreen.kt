package com.lebaillyapp.poc_cryptography.v2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lebaillyapp.poc_cryptography.R
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptionDecryptionScreen(
    onRequestDirectorySelection: () -> Unit,
    appBarBackgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    fabBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconGlobalTintColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    storagePrefs: StoragePrefs,
    viewModel: CryptoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val encryptedDirUri by storagePrefs.encryptedDirUriFlow.collectAsState()
    val files by viewModel.files.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(encryptedDirUri) {
        encryptedDirUri?.let {
            viewModel.loadDirectoryFiles(it, context)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val collapsedFraction = scrollBehavior.state.collapsedFraction
            val elevation by animateDpAsState(
                targetValue = lerp(0.dp, 4.dp, collapsedFraction),
                label = "AppBarElevation"
            )
            val showTitle = collapsedFraction > 0.95f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation, shape = MaterialTheme.shapes.medium)
            ) {
                LargeTopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp),
                        ) {
                            AnimatedVisibility(
                                visible = collapsedFraction < 0.3f,
                                enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                                    animationSpec = tween(500),
                                    initialScale = 0.95f
                                ),
                                exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                                    animationSpec = tween(300),
                                    targetScale = 0.95f
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                                    Image(
                                        painter = painterResource(R.drawable.doodle_two),
                                        contentDescription = "Zencrypt Logo",
                                        modifier = Modifier
                                            .size(150.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Secure your files with symmetric encryption",
                                        fontFamily = FontFamily(Font(R.font.jura_medium)),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Select one or more files to encrypt/decrypt",
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily(Font(R.font.jura_regular)),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))
                                    AssistChip(
                                        modifier = Modifier.padding(16.dp),
                                        onClick = onRequestDirectorySelection,
                                        label = {
                                            Text(
                                                text = encryptedDirUri?.lastPathSegment ?: "No directory selected",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(R.drawable.folder_supervised_24px),
                                                contentDescription = null
                                            )
                                        },
                                        shape = MaterialTheme.shapes.small,
                                        border = AssistChipDefaults.assistChipBorder(false)
                                    )


                                }
                            }

                            if (showTitle) {
                                Text(
                                    text = "Zencrypt",
                                    fontFamily = FontFamily(Font(R.font.jura_bold, FontWeight.Bold)),
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterStart)

                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {},
                    expandedHeight = 360.dp
                )
            }
        },
        bottomBar = {
            val elevation = 8.dp
            BottomAppBar(
                containerColor = appBarBackgroundColor,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .shadow(elevation, shape = RoundedCornerShape(0.dp)),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(painterResource(R.drawable.key_vertical_24px), tint = iconGlobalTintColor, contentDescription = null)
                    }
                    IconButton(onClick = { }) {
                        Icon(painterResource(R.drawable.laps_24px), tint = iconGlobalTintColor, contentDescription = null)
                    }
                    IconButton(onClick = { }) {
                        Icon(painterResource(R.drawable.password_24px), tint = iconGlobalTintColor, contentDescription = null)
                    }

                    IconButton(onClick = { }) {
                        Icon(painterResource(R.drawable.folder_zip_24px), tint = iconGlobalTintColor, contentDescription = null)
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { },
                        containerColor = fabBackgroundColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(painterResource(R.drawable.encrypted_24px), contentDescription = null, tint = iconGlobalTintColor)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 0.dp)
        ) {

            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment  = Alignment.CenterHorizontally) {


                    Text(
                        text = "${files.size} files in this directory.",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily(Font(R.font.jura_bold, FontWeight.Bold)),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start).padding(start = 16.dp,top = 16.dp),
                        color = appBarBackgroundColor
                    )

                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            items(
                items = files,
                key = { it.uri.toString() }
            ) { file ->
                val isSelected = viewModel.selectedUris.collectAsState().value.contains(file.uri)
                val isExpanded = viewModel.expandedUris.collectAsState().value.contains(file.uri)

                SAFFileItem(
                    file = file,
                    isSelected = isSelected,
                    isExpanded = isExpanded,
                    progress = 0.0f, //progressMap[file.uri] ?: 0f,
                    onClick = {
                        viewModel.toggleFileSelection(file.uri)
                    },
                    onEncryptDecryptClick = {
                       // launchMockEncryption(file)
                    },
                    onExpandClick = {
                        viewModel.toggleExpand(file.uri)
                    }
                )
                Divider()
            }

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









