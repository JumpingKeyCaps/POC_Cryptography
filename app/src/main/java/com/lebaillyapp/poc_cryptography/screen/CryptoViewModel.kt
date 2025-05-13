package com.lebaillyapp.poc_cryptography.screen

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.poc_cryptography.data.repository.CryptoRepository
import com.lebaillyapp.poc_cryptography.model.CryptoConfig
import com.lebaillyapp.poc_cryptography.model.SAFFile
import com.lebaillyapp.poc_cryptography.model.SelectedFile
import com.lebaillyapp.poc_cryptography.v2.BottomDialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel pour la gestion des opérations de cryptage et de décryptage des fichiers.
 * Ce ViewModel interagit avec le [CryptoRepository] pour effectuer les opérations
 * de cryptage et de décryptage tout en exposant les états de progression et de résultat
 * à l'UI (composables Jetpack Compose).
 *
 * ### Architecture MVVM
 *
 * Ce ViewModel suit l'architecture Modèle-Vue-ViewModel (MVVM). Il contient la logique
 * d'interface utilisateur et expose des états sous forme de [StateFlow] que la Vue
 * observe pour mettre à jour l'interface. Il délègue les opérations de données au
 * [CryptoRepository], assurant ainsi une séparation claire des responsabilités.
 *
 * @param cryptoRepository Repository responsable des opérations de cryptage et de décryptage.
 */
@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    /**
     * ### `encryptState`
     *
     * Etat qui représente le résultat de l'opération d'encryptage.
     * Contient un [StateFlow] émettant un [Result]<[Boolean]?>.
     * - `Result.Success(true)` indique que l'encryption a réussi.
     * - `Result.Failure(Exception)` indique qu'une erreur s'est produite pendant l'encryption.
     * - `null` est l'état initial ou lorsque l'opération n'est pas en cours.
     */
    private val _encryptState = MutableStateFlow<Result<Boolean>?>(null)
    val encryptState: StateFlow<Result<Boolean>?> = _encryptState

    /**
     * ### `decryptState`
     *
     * Etat qui représente le résultat de l'opération de décryptage.
     * Contient un [StateFlow] émettant un [Result]<[ByteArray]?>.
     * - `Result.Success(ByteArray)` contient les données déchiffrées.
     * - `Result.Failure(Exception)` indique qu'une erreur s'est produite pendant le décryptage.
     * - `null` est l'état initial ou lorsque l'opération n'est pas en cours.
     */
    private val _decryptState = MutableStateFlow<Result<ByteArray>?>(null)
    val decryptState: StateFlow<Result<ByteArray>?> = _decryptState

    /**
     * ### `encryptProgress`
     *
     * Etat qui suit la progression de l'opération d'encryptage.
     * Contient un [StateFlow] émettant un [Float] entre 0 et 1,
     * représentant la progression de l'encryption (0.0 = début, 1.0 = terminé).
     */
    private val _encryptProgress = MutableStateFlow(0f)
    val encryptProgress: StateFlow<Float> = _encryptProgress

    /**
     * ### `decryptProgress`
     *
     * Etat qui suit la progression de l'opération de décryptage.
     * Contient un [StateFlow] émettant un [Float] entre 0 et 1,
     * représentant la progression du décryptage (0.0 = début, 1.0 = terminé).
     */
    private val _decryptProgress = MutableStateFlow(0f)
    val decryptProgress: StateFlow<Float> = _decryptProgress

    /**
     * ### `uiState`
     *
     * Etat qui représente l'état général de l'interface utilisateur liée aux opérations de cryptage.
     * Émet des valeurs de l'enum [CryptoUiState] pour indiquer différents états (Idle, FileSelectedToEncrypt, etc.).
     */
    private val _uiState = MutableStateFlow(CryptoUiState.Idle)
    val uiState: StateFlow<CryptoUiState> = _uiState

    /**
     * ### `defaultConfig`
     *
     * Etat qui contient la configuration par défaut pour les opérations de cryptage/décryptage,
     * telle que la taille de la clé, le nombre d'itérations pour PBKDF2 et le mode de cryptage.
     * Émet une instance de la classe [CryptoConfig].
     */
    private val _defaultConfig = MutableStateFlow(CryptoConfig())
    val defaultConfig: StateFlow<CryptoConfig> = _defaultConfig

    /**
     * ### `isEncryptionCancelled`
     *
     * Etat qui indique si l'opération d'encryption a été annulée par l'utilisateur.
     * Émet une valeur [Boolean] (true si annulée, false sinon).
     */
    private val _isEncryptionCancelled = MutableStateFlow(false)
    val isEncryptionCancelled: StateFlow<Boolean> = _isEncryptionCancelled

    /**
     * ### `selectedFile`
     *
     * Etat qui contient les informations sur le fichier sélectionné pour le cryptage.
     * Émet une instance de la classe [SelectedFile] ou `null` si aucun fichier n'est sélectionné.
     */
    private val _selectedFile = MutableStateFlow<SelectedFile?>(null)
    val selectedFile: StateFlow<SelectedFile?> = _selectedFile

    /**
     * ### `encryptedFilePath`
     *
     * Etat qui contient le chemin d'accès du fichier encrypté une fois l'opération réussie.
     * Émet une [String] représentant le chemin du fichier ou `null` si l'encryption n'est pas terminée ou a échoué.
     */
    private val _encryptedFilePath = MutableStateFlow<String?>(null)
    val encryptedFilePath: StateFlow<String?> = _encryptedFilePath

    /**
     * ### `generatedHash`
     *
     * Etat qui contient le hash généré à partir du mot de passe et du sel (pour affichage dans le POC).
     * Émet une [String] représentant le hash en format hexadécimal ou `null` si l'encryption n'a pas encore eu lieu.
     */
    private val _generatedHash = MutableStateFlow<String?>("Set the password to generate a hash !")
    val generatedHash: StateFlow<String?> = _generatedHash

    /**
     * ### `generatedSalt`
     *
     * Etat qui contient le sel généré lors de l'opération d'encryption (pour affichage dans le POC).
     * Émet une [String] représentant le sel en format hexadécimal ou `null` si l'encryption n'a pas encore eu lieu.
     */
    private val _generatedSalt = MutableStateFlow<String?>("Set the password to generate a salt !")
    val generatedSalt: StateFlow<String?> = _generatedSalt


    /**
     * Génère un nouveau sel et le hash correspondant pour le mot de passe donné.
     * Met à jour les StateFlow pour le sel et le hash.
     *
     * @param password Le mot de passe à partir duquel générer le hash.
     */
    fun generateSaltAndHash(password: String) {
        if (password.isNotBlank()) {
            val newSaltBytes = cryptoRepository.generateSalt()
            val hashBytes = cryptoRepository.generateHash(password)
            _generatedSalt.value = newSaltBytes.toHexString()
            _generatedHash.value = hashBytes?.toHexString()
        } else {
            _generatedSalt.value = null
            _generatedHash.value = null
        }
    }



    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: MutableStateFlow<Set<Uri>> = _selectedUris

    private val _expandedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val expandedUris: MutableStateFlow<Set<Uri>> = _expandedUris


    fun toggleFileSelection(uri: Uri) {
        _selectedUris.value = if (_selectedUris.value.contains(uri)) {
            _selectedUris.value - uri
        } else {
            _selectedUris.value + uri
        }
    }

    fun toggleExpand(uri: Uri) {
        _expandedUris.value = if (_expandedUris.value.contains(uri)) {
            _expandedUris.value - uri
        } else {
            _expandedUris.value + uri
        }
    }


    //dialogs
    private val _dialogState = MutableStateFlow<BottomDialogState>(BottomDialogState.None)
    val dialogState: StateFlow<BottomDialogState> = _dialogState

    fun showDialog(state: BottomDialogState) {
        _dialogState.value = state
    }

    fun dismissDialog() {
        _dialogState.value = BottomDialogState.None
    }


    /**
     * Met à jour l'état du fichier sélectionné lorsque l'utilisateur choisit un fichier via l'explorateur de fichiers.
     * Extrait le nom, l'extension et la taille du fichier à partir de l'[Uri].
     *
     * @param uri L'URI du fichier sélectionné.
     * @param context Le contexte de l'application nécessaire pour accéder aux informations du fichier.
     */
    fun onFileSelected(uri: Uri, context: Context) {
        Log.d("EncryptFile", "URI sélectionnée: $uri")
        val name = uri.getFileName(context) ?: return
        val extension = name.substringAfterLast('.', "")
        val size = uri.getFileSize(context)

        val selected = SelectedFile(
            name = name,
            extension = extension,
            uri = uri,
            size = size,
            file = null
        )
        _selectedFile.value = selected
        _uiState.value = CryptoUiState.FileSelectedToEncrypt
    }

    /**
     * Réinitialise l'état du fichier sélectionné à `null`.
     * Utilisé pour nettoyer l'état après une opération ou lorsque l'utilisateur annule la sélection.
     */
    fun resetFile() {
        _selectedFile.value = null
        _encryptState.value = null // Réinitialise également l'état d'encryption
    }

    /**
     * Fonction d'extension pour extraire le nom de fichier à partir d'une [Uri].
     *
     * @param context Le contexte de l'application.
     * @return Le nom du fichier ou `null` en cas d'erreur.
     */
    private fun Uri.getFileName(context: Context): String? {
        return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    /**
     * Fonction d'extension pour obtenir la taille du fichier à partir d'une [Uri].
     *
     * @param context Le contexte de l'application.
     * @return La taille du fichier en octets.
     */
    private fun Uri.getFileSize(context: Context): Long {
        return context.contentResolver.openFileDescriptor(this, "r")?.use { fileDescriptor ->
            fileDescriptor.statSize
        } ?: 0L
    }

    /**
     * Fonction d'extension pour extraire l'extension de fichier à partir d'une [Uri].
     *
     * @param context Le contexte de l'application.
     * @return L'extension du fichier ou `null` en cas d'erreur.
     */
    fun Uri.getFileExtension(context: Context): String? {
        return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex).substringAfterLast('.', "")
        }
    }

    /**
     * Fonction d'extension pour obtenir un objet [File] à partir d'une [Uri].
     * Gère les URI de type 'content' en copiant temporairement le fichier dans le cache de l'application.
     *
     * @param context Le contexte de l'application.
     * @return L'objet [File] correspondant à l'URI ou `null` en cas d'erreur.
     */
    fun Uri.getFile(context: Context): File? {
        return when (this.scheme) {
            "content" -> {
                getContentFile(context)
            }
            "file" -> {
                File(this.path)
            }
            else -> null
        }
    }

    /**
     * Fonction d'extension privée pour récupérer un fichier à partir d'une [Uri] de type 'content'.
     * Copie le contenu du flux d'entrée dans un fichier temporaire dans le cache de l'application.
     * Le fichier temporaire est marqué pour être supprimé à la sortie de la JVM.
     *
     * @param context Le contexte de l'application.
     * @return Le fichier temporaire contenant le contenu de l'URI ou `null` en cas d'erreur.
     */
    private fun Uri.getContentFile(context: Context): File? {
        return try {
            context.contentResolver.openInputStream(this)?.use { inputStream ->
                val tempFile = File.createTempFile("tmp_", null, context.cacheDir)
                tempFile.deleteOnExit()
                inputStream.copyTo(tempFile.outputStream())
                tempFile
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Annule l'opération d'encryption en cours en mettant à jour l'état `isEncryptionCancelled`.
     * Le collecteur du Flow d'encryption dans la coroutine doit vérifier cet état pour arrêter l'opération.
     */
    fun cancelEncryption() {
        _isEncryptionCancelled.value = true
    }

    /**
     * Permet de modifier la configuration du cryptage/décryptage en mettant à jour l'état `defaultConfig`.
     *
     * @param newConfig La nouvelle configuration à appliquer (instance de [CryptoConfig]).
     */
    fun updateConfig(newConfig: CryptoConfig) {
        _defaultConfig.value = newConfig
    }

    /**
     * Fonction principale pour crypter un fichier.
     * Récupère la configuration, ouvre les flux nécessaires et appelle le [CryptoRepository]
     * pour effectuer l'opération de cryptage. Suit la progression et met à jour l'état `encryptProgress`
     * et `encryptState`.
     *
     * @param password Le mot de passe utilisé pour le cryptage.
     * @param extension L'extension souhaitée pour le fichier encrypté.
     * @param context Le contexte de l'application.
     */
    fun encryptFile(password: String, extension: String, context: Context,obfuscateFileName: Boolean = false) {
        _isEncryptionCancelled.value = false
        val selectedFile = _selectedFile.value ?: return

        viewModelScope.launch {
            try {
                val config = _defaultConfig.value
                Log.d("EncryptFile", "Starting file encryption with config: $config")

                context.contentResolver.openInputStream(selectedFile.uri)?.use { inputStream ->
                    val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    val encryptedDir = File(publicDir, "Encrypted")
                    if (!encryptedDir.exists() && !encryptedDir.mkdirs()) {
                        Log.e("EncryptFile", "Erreur lors de la création du dossier 'Encrypted'.")
                        _encryptState.value = Result.failure(Exception("Erreur lors de la création du dossier de sauvegarde."))
                        return@launch
                    }

                    val randomId = Random.nextInt(1000000)
                    val outputFileName = if (obfuscateFileName) {
                        "XX${randomId}XX"
                    } else {
                        selectedFile.name.substringBeforeLast('.')
                    }


                    val encryptedFile = File(encryptedDir, "${outputFileName}.$extension")
                    FileOutputStream(encryptedFile).use { outputStream ->
                        cryptoRepository.encryptFile(
                            inputStream = inputStream,
                            outputStream = outputStream,
                            password = password,
                            keySize = config.keySize,
                            iterations = config.iterations,
                            mode = config.mode,
                            extension = selectedFile.extension
                        ).catch { e ->
                            Log.e("EncryptFile", "Encryption failed: ${e.message}", e)
                            _encryptState.value = Result.failure(e)
                        }.collectLatest { progress ->
                            if (_isEncryptionCancelled.value) {
                                Log.d("EncryptFile", "Encryption cancelled.")
                                return@collectLatest
                            }
                            _encryptProgress.value = progress
                            if (progress >= 1f) {
                                Log.d("EncryptFile", "Encryption completed successfully, saved to: ${encryptedFile.absolutePath}")
                                _encryptState.value = Result.success(true)
                                _encryptedFilePath.value = encryptedFile.absolutePath
                                _generatedSalt.value = cryptoRepository.currentSalt?.toHexString() // Récupérer le sel utilisé
                                _generatedHash.value = cryptoRepository.currentHash?.toHexString() // Récupérer le hash utilisé
                            }
                        }
                    }
                } ?: run {
                    Log.e("EncryptFile", "Failed to open InputStream for URI: ${selectedFile.uri}")
                    _encryptState.value = Result.failure(Exception("Impossible d'ouvrir le fichier."))
                }

            } catch (e: Exception) {
                Log.e("EncryptFile", "Encryption process failed: ${e.message}", e)
                _encryptState.value = Result.failure(e)
            }
        }
    }

    /**
     * Fonction pour décrypter un fichier.
     * Cette méthode démarre une coroutine qui appelle le repository pour effectuer le décryptage du fichier
     * en utilisant des flux.
     * La progression du décryptage est suivie et exposée en temps réel à l'UI.
     *
     * @param uri L'URI du fichier à décrypter.
     * @param password Le mot de passe utilisé pour le décryptage.
     * @param context Le contexte de l'application.
     */
    fun decryptFile(uri: Uri, password: String, context: Context) {
        viewModelScope.launch {
            try {
                val config = _defaultConfig.value
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val originalName = uri.getFileName(context)?.substringBeforeLast('.') ?: "decrypted_file"
                    val extension = "decrypted" // Define a default extension for decrypted files
                    val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val decryptedFile = File(publicDir, "${originalName}.$extension")
                    FileOutputStream(decryptedFile).use { outputStream ->
                        cryptoRepository.decryptFile(
                            inputStream = inputStream,
                            outputStream = outputStream,
                            password = password,
                            keySize = config.keySize,
                            iterations = config.iterations,
                            mode = config.mode
                        ).catch { e ->
                            Log.e("DecryptFile", "Decryption failed: ${e.message}", e)
                            _decryptState.value = Result.failure(e)
                        }.collect { progress ->
                            _decryptProgress.value = progress
                            if (progress >= 1f) {
                                Log.d("DecryptFile", "Decryption completed successfully, saved to: ${decryptedFile.absolutePath}")
                                _decryptState.value = Result.success(decryptedFile.readBytes()) // Or just success indication
                            }
                        }
                    }
                } ?: run {
                    Log.e("DecryptFile", "Failed to open InputStream for URI: $uri")
                    _decryptState.value = Result.failure(Exception("Impossible d'ouvrir le fichier à décrypter."))
                }
            } catch (e: Exception) {
                Log.e("DecryptFile", "Decryption process failed: ${e.message}", e)
                _decryptState.value = Result.failure(e)
            }
        }
    }





    private val _files = MutableStateFlow<List<SAFFile>>(emptyList())
    val files: StateFlow<List<SAFFile>> = _files

    /**
     * Method to load files from a given base URI directory.
     * @param baseUri The base URI of the directory to load files from.
     * @param context The application context.
     */
    fun loadDirectoryFiles(baseUri: Uri?, context: Context) {
        if (baseUri == null) {
            Log.w("CryptoViewModel", "URI null : impossible de charger les fichiers.")
            return
        }

        val contentResolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            baseUri,
            DocumentsContract.getTreeDocumentId(baseUri)
        )

        try {
            val resultList = mutableListOf<SAFFile>()

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_FLAGS,
                DocumentsContract.Document.COLUMN_SUMMARY
            )

            contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val size = cursor.getLongOrNull(DocumentsContract.Document.COLUMN_SIZE)
                    val documentId = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeType = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val lastModified = cursor.getLongOrNull(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val flags = cursor.getIntOrNull(DocumentsContract.Document.COLUMN_FLAGS)
                    val summary = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_SUMMARY)

                    // Vérification minimale
                    if (documentId == null || name == null) continue

                    // On ignore les dossiers
                    if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) continue

                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, documentId)

                    val isVirtual = flags?.let {
                        it and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
                    }

                    val isWritable = flags?.let {
                        it and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0
                    }

                    val isDeletable = flags?.let {
                        it and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0
                    }

                    val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR

                    resultList.add(
                        SAFFile(
                            name = name,
                            size = size,
                            uri = fileUri,
                            mimeType = mimeType,
                            lastModified = lastModified,
                            isVirtual = isVirtual,
                            isWritable = isWritable,
                            isDeletable = isDeletable,
                            isDirectory = isDirectory,
                            documentId = documentId,
                            summary = summary
                        )
                    )
                }
            }

            _files.value = resultList

        } catch (e: Exception) {
            Log.e("CryptoViewModel", "Erreur SAF : ${e.message}", e)
        }
    }





}

fun Cursor.getStringOrNull(column: String): String? {
    val index = getColumnIndex(column)
    return if (index != -1 && !isNull(index)) getString(index) else null
}

fun Cursor.getLongOrNull(column: String): Long? {
    val index = getColumnIndex(column)
    return if (index != -1 && !isNull(index)) getLong(index) else null
}

fun Cursor.getIntOrNull(column: String): Int? {
    val index = getColumnIndex(column)
    return if (index != -1 && !isNull(index)) getInt(index) else null
}



// Extension pour convertir ByteArray en String Hex
fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
// Extension pour convertir une String Hex en ByteArray
fun String.toByteArrayFromHexString(): ByteArray {
    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
