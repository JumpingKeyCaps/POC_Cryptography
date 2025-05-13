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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.lebaillyapp.poc_cryptography.screen.formatIterations
import com.lebaillyapp.poc_cryptography.ui.theme.chartreuse
import com.lebaillyapp.poc_cryptography.ui.theme.gunMetal
import kotlin.math.roundToInt

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
    val defaultConfig by viewModel.defaultConfig.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()


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
                    IconButton(onClick = {viewModel.showDialog(BottomDialogState.CryptModeSelector) }) {
                        Icon(painterResource(R.drawable.key_vertical_24px), tint = iconGlobalTintColor, contentDescription = "Encryption mode")
                    }
                    IconButton(onClick = {viewModel.showDialog(BottomDialogState.IVCountSelector) }) {
                        Icon(painterResource(R.drawable.laps_24px), tint = iconGlobalTintColor, contentDescription = "IV count")
                    }
                    IconButton(onClick = {viewModel.showDialog(BottomDialogState.Bitrate) }) {
                        Icon(painterResource(R.drawable.binary_lock), tint = iconGlobalTintColor, contentDescription = "Bits", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {viewModel.showDialog(BottomDialogState.PasswordKeySelector) }) {
                        Icon(painterResource(R.drawable.password_24px), tint = iconGlobalTintColor, contentDescription = "Password")
                    }

                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { viewModel.showDialog(BottomDialogState.MultiCrypt)},
                        containerColor = fabBackgroundColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(painterResource(R.drawable.encrypted_24px), contentDescription = null, tint = iconGlobalTintColor)
                    }
                }
            )
        }
    ) { innerPadding ->

        // Affichage des fichiers
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


        // Affichage des dialogs
        when (dialogState) {
            BottomDialogState.CryptModeSelector -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = {
                        Text("Select the encryption mode",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontFamily = FontFamily(
                            Font(R.font.jura_bold, weight = FontWeight.Bold)
                        ))
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .padding(3.dp) // Réduction du padding
                                .fillMaxWidth() // Permet au texte de remplir la largeur
                        ) {
                            // Texte explicatif sur les différentes tailles de clé AES
                            Box(
                                modifier = Modifier
                                    .heightIn(max = 240.dp) // Limite la hauteur du texte
                                    .verticalScroll(rememberScrollState()) // Rendre le texte scrollable
                            ) {
                                Text(
                                    ""+
                                            when(defaultConfig.mode){
                                                1 -> "**CBC (Cipher Block Chaining) + PKCS5**\n\n" +
                                                        "[How it works]:\n\n Each plaintext block is XORed with the previous ciphertext block before encryption. The first block uses an Initialization Vector (IV).\n" +
                                                        "\n- Requires **padding**: AES uses 16-byte blocks. If your data isn't a multiple of 16, padding fills the gap. **PKCS5 padding** adds bytes to make the data fit the block size.\n" +
                                                        "\n- Pros: widely used, relatively simple.\n" +
                                                        "- Cons: vulnerable to padding oracle attacks if not handled securely. Padding can be tampered with.\n\n\n"

                                                2 -> "**GCM (Galois/Counter Mode) – No padding**\n\n" +
                                                        "[How it works]:\n\n Builds on CTR mode by adding **authentication**. Each block is encrypted with a counter value and produces an authentication tag.\n" +
                                                        "\n- No **padding** needed: handles any input size directly.\n" +
                                                        "\n- Pros: fast, secure, ensures integrity of both message and metadata.\n" +
                                                        "- Cons: slightly more complex to implement.\n\n\n"

                                                3 -> "**CTR (Counter Mode) – No padding**\n\n" +
                                                        "[How it works]:\n\n A counter is incremented for each block, encrypted, and XORed with the plaintext.\n" +
                                                        "\n- Like GCM, **no padding** is required.\n" +
                                                        "\n- Pros: very fast, supports parallel processing.\n" +
                                                        "- Cons: **no built-in authentication**, so data integrity must be verified separately.\n\n\n"
                                                else -> "💡 **Choose according to your needs:**\n\n\n" +
                                                        "- CBC + PKCS5: simple, but verify IV handling and secure padding.\n\n" +
                                                        "- GCM: recommended for secure and authenticated encryption.\n\n" +
                                                        "- CTR: fast and flexible, but requires additional integrity checks."
                                            } +
                                            "🔎 **About Padding:**" +
                                            "\n- Padding is used to make the data size a multiple of the block size (16 bytes for AES).\n" +
                                            "- Example: if your message is 20 bytes, PKCS5 padding adds 12 bytes (0x0C) to reach 32 bytes.\n" +
                                            "- Padding isn't needed in stream-like modes such as CTR or GCM.\n\n\n",

                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily(
                                        Font(R.font.jura_regular, weight = FontWeight.Normal)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(22.dp)) // Espacement entre le texte et les boutons
                            // CBC + PKCS5 padding
                            Button(
                                colors = if(defaultConfig.mode == 1)ButtonDefaults.buttonColors() else ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    val newConfig = viewModel.defaultConfig.value.copy(mode = 1)
                                    viewModel.updateConfig(newConfig)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                            ) {
                                Text("CBC + PKCS5 padding")
                            }
                            Spacer(modifier = Modifier.height(5.dp)) // Espacement entre les boutons
                            // GCM + No padding
                            Button(
                                colors = if(defaultConfig.mode == 2)ButtonDefaults.buttonColors() else ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    val newConfig = viewModel.defaultConfig.value.copy(mode = 2)
                                    viewModel.updateConfig(newConfig)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                            ) {
                                Text("GCM + No padding ")
                            }
                            Spacer(modifier = Modifier.height(5.dp)) // Espacement entre les boutons
                            // CTR +  no padding
                            Button(
                                colors = if(defaultConfig.mode == 3)ButtonDefaults.buttonColors() else ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    val newConfig = viewModel.defaultConfig.value.copy(mode = 3)
                                    viewModel.updateConfig(newConfig)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                            ) {
                                Text("CTR + No padding")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissDialog() },
                        ) {
                            Text("Apply")
                        }
                    }
                )
            }

            BottomDialogState.PasswordKeySelector -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text("Fermer")
                        }
                    },
                    title = { Text("Paramètres") },
                    text = {
                        Text("Paramètres à venir...")
                    }
                )
            }
            BottomDialogState.IVCountSelector -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = {
                        Text("Select the number of iterations",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontFamily = FontFamily(
                                Font(R.font.jura_bold, weight = FontWeight.Bold)
                            ))
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .padding(6.dp) // Réduction du padding
                                .fillMaxWidth() // Permet au texte de remplir la largeur
                        ) {
                            // Texte explicatif sur les différentes tailles de clé AES
                            Box(
                                modifier = Modifier
                                    .heightIn(max = 190.dp) // Limite la hauteur du texte
                                    .verticalScroll(rememberScrollState()) // Rendre le texte scrollable
                            ) {
                                Text(
                                    text = """
                            Key derivation functions (KDFs) are used to transform a password into a secure cryptographic key.
                            
                            Instead of using a password directly — which is often weak or guessable — a KDF applies a series of transformations, including hashing, to make the resulting key stronger and resistant to brute-force attacks.
                            
                            The number of iterations defines how many times the KDF processes the input. A higher number of iterations increases the time required to derive the key, which makes it significantly harder for attackers to perform large-scale attacks.
                            
                            ⚠️ Keep in mind:
                            - More iterations = stronger security
                            - But also = slower performance during encryption/decryption
                            
                            A good practice is to choose the **highest number of iterations that still allows a smooth user experience**.
                        """.trimIndent(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily(
                                        Font(R.font.jura_regular, weight = FontWeight.Normal)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(22.dp)) // Espacement entre le texte et les boutons

                            val iterationOptions = listOf(
                                1_000, 1_500, 2_000, 3_000, 5_000, 7_500, 10_000, 15_000, 20_000,
                                30_000, 50_000, 75_000, 100_000, 150_000, 200_000, 300_000, 500_000,
                                750_000, 1_000_000
                            )
                            val currentIndex = iterationOptions.indexOfFirst { it >= viewModel.defaultConfig.value.iterations }.coerceAtLeast(0)

                            var sliderIndex by remember { mutableStateOf(currentIndex) }
                            val selectedIterations = iterationOptions[sliderIndex]

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Iterations: ${formatIterations(selectedIterations)}",
                                    fontFamily = FontFamily(
                                        Font(R.font.jura_medium, weight = FontWeight.Bold)
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Slider(
                                    value = sliderIndex.toFloat(),
                                    onValueChange = {
                                        sliderIndex = it.roundToInt().coerceIn(0, iterationOptions.lastIndex)
                                        viewModel.updateConfig(viewModel.defaultConfig.value.copy(iterations = iterationOptions[sliderIndex]))
                                                    },
                                    steps = iterationOptions.size - 2,
                                    valueRange = 0f..(iterationOptions.size - 1).toFloat(),

                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissDialog()
                          }
                        ) {
                            Text("Apply")
                        }
                    }

                )
            }

            BottomDialogState.Bitrate -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = {
                        Text("Select the AES Key Size",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontFamily = FontFamily(
                                Font(R.font.jura_bold, weight = FontWeight.Bold)
                            ))
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .padding(6.dp) // Réduction du padding
                                .fillMaxWidth() // Permet au texte de remplir la largeur
                        ) {
                            // Texte explicatif sur les différentes tailles de clé AES
                            Box(
                                modifier = Modifier
                                    .heightIn(max = 190.dp) // Limite la hauteur du texte
                                    .verticalScroll(rememberScrollState()) // Rendre le texte scrollable
                            ) {
                                Text(
                                    "AES (Advanced Encryption Standard) is a symmetric encryption algorithm widely used to protect sensitive data. " +
                                            "The principle of AES relies on the use of a shared secret key to encrypt and decrypt data. " +
                                            "The security of AES mainly depends on the size of the key used.\n\n" +

                                            "Here are the three common key sizes for AES, with their advantages and disadvantages:\n\n" +

                                            "- **128 bits**: Provides a good level of security for most applications. It is fast and efficient but could be vulnerable to future attacks with advances in computational power. Recommended for applications that do not need an extremely high level of security.\n\n" +

                                            "- **192 bits**: Offers better security than 128 bits, with increased resistance to attacks. It is slightly slower to encrypt and decrypt but remains a good compromise between security and performance.\n\n" +

                                            "- **256 bits**: Provides the strongest security among the three sizes. It is extremely resistant to attacks, even with very powerful computing resources. However, it is also slower to execute and may be excessive for some applications. Ideal for situations where security is the top priority, especially in sensitive environments.\n\n" +

                                            "The choice of key size depends on the balance between performance and security. The larger the key, the stronger the security, but it requires more computational resources.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily(
                                        Font(R.font.jura_regular, weight = FontWeight.Normal)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(22.dp)) // Espacement entre le texte et les boutons
                            // 128 bits
                            Button(
                                colors = if(defaultConfig.keySize == 128)ButtonDefaults.buttonColors() else ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    val newConfig = viewModel.defaultConfig.value.copy(keySize = 128)
                                    viewModel.updateConfig(newConfig)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                            ) {
                                Text("128 bits")
                            }
                            Spacer(modifier = Modifier.height(5.dp)) // Espacement entre les boutons
                            // 192 bits
                            Button(
                                colors = if(defaultConfig.keySize == 192)ButtonDefaults.buttonColors() else ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    val newConfig = viewModel.defaultConfig.value.copy(keySize = 192)
                                    viewModel.updateConfig(newConfig)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                            ) {
                                Text("192 bits")
                            }
                            Spacer(modifier = Modifier.height(5.dp)) // Espacement entre les boutons
                            // 256 bits
                            Button(
                                colors = if(defaultConfig.keySize == 256)ButtonDefaults.buttonColors() else ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    val newConfig = viewModel.defaultConfig.value.copy(keySize = 256)
                                    viewModel.updateConfig(newConfig)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                            ) {
                                Text("256 bits")
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissDialog() },
                        ) {
                            Text("Apply")
                        }
                    }
                )
            }

            BottomDialogState.MultiCrypt -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    confirmButton = {
                        TextButton(onClick = {
                            // Ajoute ici l'action de suppression
                            viewModel.dismissDialog()
                        }) {
                            Text("Supprimer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDialog() }) {
                            Text("Annuler")
                        }
                    },
                    title = { Text("Confirmation") },
                    text = {
                        Text("Souhaitez-vous vraiment désélectionner tous les fichiers ?")
                    }
                )
            }

            BottomDialogState.None -> Unit
        }


    }
}









