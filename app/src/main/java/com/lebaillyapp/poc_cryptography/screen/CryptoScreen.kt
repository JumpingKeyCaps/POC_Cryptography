package com.lebaillyapp.poc_cryptography.screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.lebaillyapp.poc_cryptography.R
import com.lebaillyapp.poc_cryptography.model.SelectedFile
import com.lebaillyapp.poc_cryptography.ui.theme.chartreuse
import com.lebaillyapp.poc_cryptography.ui.theme.gunMetal
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CryptoScreen(modifier: Modifier = Modifier, viewModel: CryptoViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val config by viewModel.defaultConfig.collectAsState()

    val isEncryptionCancelled by viewModel.isEncryptionCancelled.collectAsState()
    val encryptState by viewModel.encryptState.collectAsState()
    val encryptProgress by viewModel.encryptProgress.collectAsState()
    val isProcessing = encryptProgress in 0.1f..1f && !isEncryptionCancelled
    val encryptedFilePath by viewModel.encryptedFilePath.collectAsState()
    val generatedSalt by viewModel.generatedSalt.collectAsState()
    val generatedHash by viewModel.generatedHash.collectAsState()

    val pageCount = 2
    val pagerState = rememberPagerState { pageCount }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val topBarHeight = screenHeight * (if (pagerState.currentPage == 0) 0.5f else 0.5f)


    // Handle Permissions
    val permissionState = rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)

    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    // Show permission request rationale if needed
    if (!permissionState.status.isGranted) {
        // You can show a rationale here if needed for the user
        // Example: Snackbar or Dialog to explain why the app needs this permission
    }



    // State for Dialog visibility
    var isKeySizeDialogOpen by remember { mutableStateOf(false) }

    // Dialog to select AES key size
    if (isKeySizeDialogOpen) {
        AlertDialog(
            onDismissRequest = { isKeySizeDialogOpen = false },
            title = {
                Text("Select the AES Key Size", color = chartreuse,fontFamily = FontFamily(
                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                ))
            },
            text = {
                Column(
                    modifier = Modifier
                        .padding(16.dp) // Réduction du padding
                        .fillMaxWidth() // Permet au texte de remplir la largeur
                ) {
                    // Texte explicatif sur les différentes tailles de clé AES
                    Box(
                        modifier = Modifier
                            .heightIn(max = 280.dp) // Limite la hauteur du texte
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
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(26.dp)) // Espacement entre le texte et les boutons
                    // Bouton 128 bits
                    Button(
                        onClick = {
                            val newConfig = viewModel.defaultConfig.value.copy(keySize = 128)
                            viewModel.updateConfig(newConfig)
                            isKeySizeDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(chartreuse),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                    ) {
                        Text("128 bits", color = gunMetal,fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ))
                    }
                    Spacer(modifier = Modifier.height(8.dp)) // Espacement entre les boutons
                    // Bouton 192 bits
                    Button(
                        onClick = {
                            val newConfig = viewModel.defaultConfig.value.copy(keySize = 192)
                            viewModel.updateConfig(newConfig)
                            isKeySizeDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(chartreuse),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                    ) {
                        Text("192 bits", color = gunMetal,fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ))
                    }
                    Spacer(modifier = Modifier.height(8.dp)) // Espacement entre les boutons
                    // Bouton 256 bits
                    Button(
                        onClick = {
                            val newConfig = viewModel.defaultConfig.value.copy(keySize = 256)
                            viewModel.updateConfig(newConfig)
                            isKeySizeDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(chartreuse),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                    ) {
                        Text("256 bits", color = gunMetal,fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isKeySizeDialogOpen = false },
                    colors = ButtonDefaults.buttonColors(chartreuse)
                ) {
                    Text("Close", color = gunMetal,fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    ))
                }
            },
            containerColor = gunMetal,
            titleContentColor = chartreuse
        )
    }
    var isModeDialogOpen by remember { mutableStateOf(false) }
    // Dialog to select Mode of Encryption
    if (isModeDialogOpen) {
        AlertDialog(
            onDismissRequest = { isModeDialogOpen = false },
            title = {
                Text("Select the encryption mode", color = chartreuse,fontFamily = FontFamily(
                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                ))
            },
            text = {
                Column(
                    modifier = Modifier
                        .padding(16.dp) // Réduction du padding
                        .fillMaxWidth() // Permet au texte de remplir la largeur
                ) {
                    // Texte explicatif sur les différentes tailles de clé AES
                    Box(
                        modifier = Modifier
                            .heightIn(max = 280.dp) // Limite la hauteur du texte
                            .verticalScroll(rememberScrollState()) // Rendre le texte scrollable
                    ) {
                        Text(
                            "🔐 **Available AES Encryption Modes:**\n\n" +
                                    "1. **CBC (Cipher Block Chaining) + PKCS5**\n\n" +
                                    "[How it works]:\n\n Each plaintext block is XORed with the previous ciphertext block before encryption. The first block uses an Initialization Vector (IV).\n" +
                                    "\n- Requires **padding**: AES uses 16-byte blocks. If your data isn't a multiple of 16, padding fills the gap. **PKCS5 padding** adds bytes to make the data fit the block size.\n" +
                                    "\n- Pros: widely used, relatively simple.\n" +
                                    "- Cons: vulnerable to padding oracle attacks if not handled securely. Padding can be tampered with.\n\n\n" +
                                    "2. **GCM (Galois/Counter Mode) – No padding**\n\n" +
                                    "[How it works]:\n\n Builds on CTR mode by adding **authentication**. Each block is encrypted with a counter value and produces an authentication tag.\n" +
                                    "\n- No **padding** needed: handles any input size directly.\n" +
                                    "\n- Pros: fast, secure, ensures integrity of both message and metadata.\n" +
                                    "- Cons: slightly more complex to implement.\n\n\n" +
                                    "3. **CTR (Counter Mode) – No padding**\n\n" +
                                    "[How it works]:\n\n A counter is incremented for each block, encrypted, and XORed with the plaintext.\n" +
                                    "\n- Like GCM, **no padding** is required.\n" +
                                    "\n- Pros: very fast, supports parallel processing.\n" +
                                    "- Cons: **no built-in authentication**, so data integrity must be verified separately.\n\n\n" +
                                    "🔎 **About Padding:**\n\n" +
                                    "\n- Padding is used to make the data size a multiple of the block size (16 bytes for AES).\n" +
                                    "- Example: if your message is 20 bytes, PKCS5 padding adds 12 bytes (0x0C) to reach 32 bytes.\n" +
                                    "- Padding isn't needed in stream-like modes such as CTR or GCM.\n\n\n" +
                                    "💡 **Choose according to your needs:**\n\n\n" +
                                    "- CBC + PKCS5: simple, but verify IV handling and secure padding.\n\n" +
                                    "- GCM: recommended for secure and authenticated encryption.\n\n" +
                                    "- CTR: fast and flexible, but requires additional integrity checks.",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(26.dp)) // Espacement entre le texte et les boutons
                    // CBC + PKCS5 padding
                    Button(
                        onClick = {
                            val newConfig = viewModel.defaultConfig.value.copy(mode = 1)
                            viewModel.updateConfig(newConfig)
                            isModeDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(chartreuse),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                    ) {
                        Text("CBC + PKCS5 padding", color = gunMetal,fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ))
                    }
                    Spacer(modifier = Modifier.height(8.dp)) // Espacement entre les boutons
                    // GCM + No padding
                    Button(
                        onClick = {
                            val newConfig = viewModel.defaultConfig.value.copy(mode = 2)
                            viewModel.updateConfig(newConfig)
                            isModeDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(chartreuse),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                    ) {
                        Text("GCM + No padding ", color = gunMetal,fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ))
                    }
                    Spacer(modifier = Modifier.height(8.dp)) // Espacement entre les boutons
                    // CTR +  no padding
                    Button(
                        onClick = {
                            val newConfig = viewModel.defaultConfig.value.copy(mode = 3)
                            viewModel.updateConfig(newConfig)
                            isModeDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(chartreuse),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(start = 30.dp, end = 30.dp).fillMaxWidth()
                    ) {
                        Text("CTR + No padding", color = gunMetal,fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isModeDialogOpen = false },
                    colors = ButtonDefaults.buttonColors(chartreuse)
                ) {
                    Text("Close", color = gunMetal,fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    ))
                }
            },
            containerColor = gunMetal,
            titleContentColor = chartreuse
        )
    }

    var isKeyDerivationDialogOpen by remember { mutableStateOf(false) }
    // Dialog for Key Derivation with SeekBar
    if (isKeyDerivationDialogOpen) {
        AlertDialog(
            onDismissRequest = { isKeyDerivationDialogOpen = false },
            title = {
                Text(
                    "Select the number of iterations",
                    color = chartreuse,
                    fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
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
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                            color = chartreuse,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Slider(
                            value = sliderIndex.toFloat(),
                            onValueChange = { sliderIndex = it.roundToInt().coerceIn(0, iterationOptions.lastIndex) },
                            steps = iterationOptions.size - 2,
                            valueRange = 0f..(iterationOptions.size - 1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = chartreuse,
                                activeTrackColor = chartreuse,
                                inactiveTrackColor = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { isKeyDerivationDialogOpen = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = chartreuse)
                        ) {
                            Text(
                                "Close",
                                color = chartreuse,
                                fontFamily = FontFamily(
                                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val newConfig = viewModel.defaultConfig.value.copy(iterations = selectedIterations)
                                viewModel.updateConfig(newConfig)
                                isKeyDerivationDialogOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(chartreuse)
                        ) {
                            Text(
                                "Apply",
                                color = gunMetal,
                                fontFamily = FontFamily(
                                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                )
                            )
                        }
                    }
                }
            },
            containerColor = gunMetal,
            titleContentColor = chartreuse,
            confirmButton = {}, // Les boutons sont déjà gérés dans text -> Row
            dismissButton = {}
        )
    }




    // Dialog d'Encryption
    if (isProcessing) {
        AlertDialog(
            onDismissRequest = { /* Do nothing to prevent dismissing the dialog */ },
            title = {
                if(encryptProgress < 1f){
                    Text(
                        "Encryption in progress...",
                        color = chartreuse,
                        fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        )
                    )
                }else{
                    Text(
                        "The file has been successfully encrypted!",
                        color = chartreuse,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily(Font(R.font.sairacondensed_black, FontWeight.Bold))
                    )
                }

            },
            text = {
                Column {
                    // Afficher la barre de progression seulement si la progression est inférieure à 100%
                    if (encryptProgress < 1f) {
                        LinearProgressIndicator(progress = encryptProgress)
                        Text(
                            "Progression: ${"%.1f".format(encryptProgress * 100)}%",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                            )
                        )
                    } else {
                        // Lorsque la progression atteint 100%, afficher un message de fin
                        Text(
                            "Saved to: $encryptedFilePath",
                            color = chartreuse,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily(Font(R.font.sairacondensed_black, FontWeight.Normal))
                        )
                    }
                }
            },
            containerColor = gunMetal,
            titleContentColor = chartreuse,
            confirmButton = {
                TextButton(onClick = { viewModel.cancelEncryption() }) {
                    Text(if(encryptProgress < 1f) "Cancel" else "Close")
                }

                //clean the file selector at the end
                if(encryptProgress >= 1f){
                    viewModel.resetFile()
                }

            },
            dismissButton = {
                if (encryptProgress >= 1f && !encryptedFilePath.isNullOrEmpty()) {
                    TextButton(onClick = {
                        val externalStorageDir = Environment.getExternalStorageDirectory()
                        val encryptedDir = File(externalStorageDir, "Documents/Encrypted")
                        Log.d("EncryptionDialog", "Encrypted Dir Path (using root): ${encryptedDir.absolutePath}")

                        val folderUri: Uri? = FileProvider.getUriForFile(
                            context,
                            "${context.applicationContext.packageName}.fileprovider",
                            encryptedDir
                        )

                        folderUri?.let { uri ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(uri, "resource/folder")
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No app found to open the file explorer.", Toast.LENGTH_SHORT).show()
                                Log.e("EncryptionDialog", "Error opening file explorer: ${e.message}")
                            }
                        } ?: run {
                            Log.e("EncryptionDialog", "Could not get content URI for folder.")
                            Toast.makeText(context, "Error accessing folder.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Open Folder")
                    }
                }
            }
        )

    }


//



    //nav bar color
    val activity = LocalActivity.current
    val navBarColor = if (pagerState.currentPage == 0) gunMetal else chartreuse
    SideEffect {
        val window = activity?.window
        if (window != null) {
            window.navigationBarColor = navBarColor.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        }
    }
    // BottomSheet
    CryptoBottomSheet(
        isSheetOpen = isSheetOpen,
        onDismiss = { isSheetOpen = false },
        pagerState = pagerState,
        onEncryptDecryptClick = { password, extension, obfuscateFileName -> // Récupérer l'état du switch
            selectedFile?.let { file ->
                if (pagerState.currentPage == 0) {
                    viewModel.encryptFile(password, extension, context, obfuscateFileName) // Passer l'état au ViewModel
                } else {
                    Log.d("CryptoScreen", "Decrypt button clicked with password: $password")
                     viewModel.decryptFile(file.uri, password, context)
                }
            }
        },
        generatedSalt = generatedSalt,
        generatedHash = generatedHash,
        onPasswordChanged = { password ->
            viewModel.generateSaltAndHash(password)
        }
    )

    //main compo
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isSheetOpen = true
                    scope.launch { bottomSheetState.show() }
                },
                containerColor = if (pagerState.currentPage == 0) gunMetal else chartreuse
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(
                        id = if (pagerState.currentPage == 0) R.drawable.clef else R.drawable.clef
                    ),
                    contentDescription = "GO",
                    tint = if (pagerState.currentPage == 0) chartreuse else gunMetal,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        topBar = {
            val title = if (pagerState.currentPage == 0) "Encrypt!" else "Decrypt!"
            TopAppBar(
                modifier = Modifier.height(topBarHeight),
                title = {
                    Column(modifier = Modifier.fillMaxWidth()
                        .padding(end = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(25.dp))
                        //Title
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(
                                Font(R.font.bytesized_regular, weight = FontWeight.Bold)
                            )
                        )
                        Spacer(Modifier.height(20.dp))
                        //description
                        Text(
                            text = if(pagerState.currentPage == 0){
                                "Symmetric encryption method is widely used for its speed and strength, providing a high level of confidentiality."
                            } else{
                                "Decrypting is a fast and efficient process that ensures the confidentiality of the original data by reversing the encryption, providing secure access to the information."
                            },
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_black, weight = FontWeight.Normal)
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )



                        if(pagerState.currentPage == 0){
                            Spacer(Modifier.height(50.dp))
                            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {

                                //AES key size info
                                Card(
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            isKeySizeDialogOpen = true
                                        },
                                    colors = CardDefaults.cardColors(containerColor = chartreuse),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp, vertical = 8.dp),
                                    ) {

                                        Card(
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = gunMetal),
                                            shape = RoundedCornerShape(40.dp),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {

                                            Text(
                                                text = "${config.keySize} bits",
                                                fontSize = 16.sp,
                                                color = chartreuse, // ou une autre couleur d’accent
                                                fontFamily = FontFamily(
                                                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                                ),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    .padding(vertical = 4.dp, horizontal = 14.dp),

                                                )
                                        }

                                        Spacer(Modifier.height(0.dp))

                                        Text(
                                            text = "AES key size",
                                            fontSize = 13.sp,
                                            color = gunMetal,
                                            fontFamily = FontFamily(
                                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                            ),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                    }
                                }


                                //mode of encryption
                                Card(
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            isModeDialogOpen = true
                                        },
                                    colors = CardDefaults.cardColors(containerColor = chartreuse),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp, vertical = 8.dp),
                                    ) {

                                        Card(
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = gunMetal),
                                            shape = RoundedCornerShape(40.dp),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {

                                            Text(
                                                text =
                                                when (config.mode) {
                                                    1 -> "CBC/PKCS5Padding"
                                                    2 -> "GCM/NoPadding"
                                                    3 -> "CTR/NoPadding"
                                                    else -> "CBC/PKCS5Padding"
                                                },
                                                fontSize = 16.sp,
                                                color = chartreuse, // ou une autre couleur d’accent
                                                fontFamily = FontFamily(
                                                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                                ),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    .padding(vertical = 4.dp, horizontal = 14.dp),

                                                )
                                        }

                                        Spacer(Modifier.height(0.dp))

                                        Text(
                                            text = "Mode of encryption",
                                            fontSize = 13.sp,
                                            color = gunMetal,
                                            fontFamily = FontFamily(
                                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                            ),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                    }
                                }


                                //iterations for key derivation
                                Card(
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            isKeyDerivationDialogOpen = true
                                        },
                                    colors = CardDefaults.cardColors(containerColor = chartreuse),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp, vertical = 8.dp),
                                    ) {

                                        Card(
                                            modifier = Modifier.padding(horizontal = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = gunMetal),
                                            shape = RoundedCornerShape(40.dp),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {

                                            Text(
                                                text = "x ${config.iterations.toPrettyFormat()}",
                                                fontSize = 16.sp,
                                                color = chartreuse, // ou une autre couleur d’accent
                                                fontFamily = FontFamily(
                                                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                                ),
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    .padding(vertical = 4.dp, horizontal = 14.dp),

                                                )
                                        }

                                        Spacer(Modifier.height(0.dp))

                                        Text(
                                            text = "key derivation",
                                            fontSize = 13.sp,
                                            color = gunMetal,
                                            fontFamily = FontFamily(
                                                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                            ),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                    }
                                }



                            }
                        }else{
                            Spacer(Modifier.height(20.dp))
                            Button(
                                onClick = { /* ton action ici */ },
                                modifier = Modifier

                                    .height(86.dp)
                                    .padding( top = 10.dp, start = 30.dp, end = 30.dp,bottom = 5.dp)
                                    .align(Alignment.CenterHorizontally),
                                shape = RoundedCornerShape(50), // arrondi complet
                                colors = ButtonDefaults.buttonColors(containerColor = gunMetal) // exemple de couleur
                            ) {
                                Text("Choose a file to decrypt !",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = chartreuse,
                                    fontFamily = FontFamily(
                                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                                    )
                                )
                            }
                        }


                        Spacer(Modifier.height(10.dp))
                        Text(
                            text =
                            if(pagerState.currentPage == 0){
                                "Setup your encryption options, pick a file and encrypt it with a single tap."
                            }else{
                                "Choose a file to decrypt with a single tap !"
                            },
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(
                                Font(R.font.sairacondensed_light, weight = FontWeight.Normal)
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp)
                        )

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (pagerState.currentPage == 0) gunMetal else chartreuse,
                    titleContentColor = if (pagerState.currentPage == 0) chartreuse else gunMetal,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { page ->
            when (page) {
                0 -> EncryptScreen(viewModel,selectedFile)
                1 -> DecryptScreen()
            }
        }
    }
}



@Composable
fun FileInfoCard(selectedFile: SelectedFile, modifier: Modifier = Modifier) {
    val fileName = selectedFile.name
    val fileExtension = selectedFile.extension
    val fileSize = selectedFile.size
    val fileUri = selectedFile.uri


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 8.dp, top = 10.dp, bottom = 20.dp)
            .background(gunMetal, RoundedCornerShape(
                topStart = 16.dp,   // Coin supérieur gauche
                topEnd = 0.dp,     // Coin supérieur droit
                bottomStart = 16.dp, // Coin inférieur gauche
                bottomEnd = 0.dp  // Coin inférieur droit
            ))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        //le nom du fichier
        Card(
            modifier = Modifier.padding(horizontal = 4.dp).align(Alignment.CenterHorizontally),
            colors = CardDefaults.cardColors(containerColor = gunMetal),
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(0.dp),

        ) {
            Text(
                text = fileName,
                fontSize = 14.sp,
                color = chartreuse,
                fontFamily = FontFamily(
                    Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(vertical = 2.dp, horizontal = 20.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp,start = 1.dp,end = 1.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Liste des extensions valides
            val validExtensions = setOf("jpg", "png", "mp4")

            // Vérifie si l'extension est valide
            val isValidFile = fileExtension.lowercase() in validExtensions
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(105.dp)
                    .padding(end = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = gunMetal)
            ) {
                if (isValidFile) {
                    // Configuration de la requête pour l'image
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                        .data(fileUri)
                        .apply {
                            if (fileExtension.equals("mp4", ignoreCase = true)) {
                                decoderFactory(VideoFrameDecoder.Factory())
                            }
                        }
                        .crossfade(true)
                        .build()

                    // Affichage de l'image ou vidéo
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Affichage de l'icône "folder" pour les fichiers non valides
                    Box(Modifier.fillMaxSize()){
                        Image(
                            painter = painterResource(id = R.drawable.folder),
                            contentDescription = "Top Bar Decoration",
                            modifier = Modifier
                                .size(56.dp)
                                .padding(bottom = 0.dp)
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                            colorFilter = ColorFilter.tint(chartreuse.copy(alpha = 1f))
                        )
                    }

                }
            }
        }



        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)) {
            //lextension du fichier
            Card(
                modifier = Modifier.padding(horizontal = 4.dp).weight(45f).align(Alignment.CenterVertically),
                colors = CardDefaults.cardColors(containerColor = chartreuse),
                shape = RoundedCornerShape(40.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Text(
                    text = fileExtension,
                    fontSize = 16.sp,
                    color = gunMetal,
                    fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(vertical = 2.dp, horizontal = 20.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp).weight(10f))

            //la taille du fichier
            Card(
                modifier = Modifier.padding(horizontal = 4.dp).weight(45f).align(Alignment.CenterVertically),
                colors = CardDefaults.cardColors(containerColor = chartreuse),
                shape = RoundedCornerShape(40.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Text(
                    text = formatFileSize(fileSize),
                    fontSize = 13.sp,
                    color = gunMetal,
                    fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(vertical = 2.dp, horizontal = 10.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        //le URI du fichier
        Text(
            text = "$fileUri",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray,
            fontFamily = FontFamily(
                Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
            ),
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 8.dp)
        )


    }
}

fun formatFileSize(size: Long): String {
    return when {
        size >= 1_000_000 -> String.format("%.2f MB", size / 1_000_000.0)
        size >= 1_000 -> String.format("%.2f KB", size / 1_000.0)
        else -> "$size bytes"
    }
}

@Composable
fun getTextPreview(uri: Uri): String {
    // Fonction pour obtenir un extrait du fichier texte
    val context = LocalContext.current
    return context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
        it.readText().take(100) // Retourne les 100 premiers caractères du fichier texte
    } ?: "Unable to preview text"
}

@Composable
fun getBinaryPreview(uri: Uri): String {
    val context = LocalContext.current
    return remember(uri) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(512)
                val bytesRead = inputStream.read(buffer)
                if (bytesRead > 0) buffer.copyOf(bytesRead) else ByteArray(0)
            } ?: return@remember "Unable to read binary"

            // Transforme les octets en hex, 16 octets par ligne
            bytes.toList()
                .chunked(8)
                .joinToString("\n> ") { line ->
                    line.joinToString(" ") { byte -> "%02X".format(byte) }
                }

        } catch (e: Exception) {
            "Error reading binary: ${e.message}"
        }
    }
}




// Fonction utilitaire
@Composable
fun formatIterations(value: Int): String {
    return when {
        value >= 1_000_000 -> "${value / 1_000_000}M"
        value >= 1_000 -> "${value / 1_000}k"
        else -> value.toString()
    }
}

fun Int.toPrettyFormat(): String {
    return when {
        this >= 1_000_000 -> "${this / 1_000_000}M"
        this >= 1_000 -> "${this / 1_000}k"
        else -> this.toString()
    }
}






