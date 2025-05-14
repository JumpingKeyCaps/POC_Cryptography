package com.lebaillyapp.poc_cryptography.v2

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lebaillyapp.poc_cryptography.data.repository.CryptoRepository
import com.lebaillyapp.poc_cryptography.model.CryptoConfig
import com.lebaillyapp.poc_cryptography.model.SAFFile
import com.lebaillyapp.poc_cryptography.screen.getIntOrNull
import com.lebaillyapp.poc_cryptography.screen.getLongOrNull
import com.lebaillyapp.poc_cryptography.screen.getStringOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoViewModelV2 @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    // [STATES] #############################################################################

    /**
     * ### `files`
     */
    private val _files = MutableStateFlow<List<SAFFile>>(emptyList())
    val files: MutableStateFlow<List<SAFFile>> = _files

    /**
     * ### `fileProgressMap`
     */
    private  val _fileProgressMap = MutableStateFlow<Map<Uri, Float>>(emptyMap())
    val fileProgressMap: StateFlow<Map<Uri, Float>> = _fileProgressMap


    /**
     * ### `selectedUris`
     */
    private val _selectedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val selectedUris: MutableStateFlow<Set<Uri>> = _selectedUris

    /**
     * ### `expandedUris`
     */
    private val _expandedUris = MutableStateFlow<Set<Uri>>(emptySet())
    val expandedUris: MutableStateFlow<Set<Uri>> = _expandedUris

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
     * ### `dialogState`
     */
    private val _dialogState = MutableStateFlow<BottomDialogState>(BottomDialogState.None)
    val dialogState: StateFlow<BottomDialogState> = _dialogState


    // [FUNCTIONS] #############################################################################

    /**
     * ### `updateConfig`
     *
     * Met à jour la configuration de cryptage. Cette méthode permet de modifier
     * la configuration de cryptage, incluant le mode, l'IV, le nombre de bits,
     * et le mot de passe. Utilisée chaque fois que l'utilisateur modifie les options.
     */
    fun updateConfig(newConfig: CryptoConfig) {
        _defaultConfig.value = newConfig
    }

    /**
     * ### `loadDirectoryFiles`
     *
     * Charge les fichiers contenus dans un dossier SAF. Cette fonction lit les fichiers
     * présents dans un dossier spécifique sélectionné par l'utilisateur via le
     * Storage Access Framework (SAF) et met à jour la liste des fichiers dans l'UI.
     *
     * @param baseUri URI du dossier contenant les fichiers.
     * @param context Contexte de l'application utilisé pour accéder au système de fichiers.
     */
    fun loadDirectoryFiles(baseUri: Uri?, context: Context) {
        if (baseUri == null) {
            Log.w("CryptoViewModel", "URI null : impossible de charger les fichiers.")
            return
        }

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

            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val size = cursor.getLongOrNull(DocumentsContract.Document.COLUMN_SIZE)
                    val documentId = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeType = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val lastModified = cursor.getLongOrNull(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val flags = cursor.getIntOrNull(DocumentsContract.Document.COLUMN_FLAGS)
                    val summary = cursor.getStringOrNull(DocumentsContract.Document.COLUMN_SUMMARY)

                    if (documentId == null || name == null || mimeType == DocumentsContract.Document.MIME_TYPE_DIR) continue

                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(baseUri, documentId)

                    val isVirtual = flags?.let { it and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0 }
                    val isWritable = flags?.let { it and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0 }
                    val isDeletable = flags?.let { it and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0 }
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

    /**
     * ### `toggleFileSelection`
     *
     * Sélectionne ou désélectionne un fichier donné par son URI. Cette méthode
     * permet à l'utilisateur de choisir quels fichiers seront chiffrés ou déchiffrés.
     *
     * @param uri URI du fichier à sélectionner ou désélectionner.
     */
    fun toggleFileSelection(uri: Uri) {
        selectedUris.value = if (selectedUris.value.contains(uri)) {
            selectedUris.value - uri
        } else {
            selectedUris.value + uri
        }
    }

    /**
     * ### `toggleExpand`
     *
     * Active ou désactive l'état développé d'une ligne fichier dans l'interface.
     * Cette méthode permet de gérer l'affichage détaillé de chaque fichier.
     *
     * @param uri URI du fichier à développer ou réduire.
     */
    fun toggleExpand(uri: Uri) {
        expandedUris.value = if (expandedUris.value.contains(uri)) {
            expandedUris.value - uri
        } else {
            expandedUris.value + uri
        }
    }


    /**
     * ###  `encryptFile`
     * Crypte un fichier à partir de son URI, le sauvegarde dans le dossier SAF sélectionné,
     * et tente de supprimer automatiquement le fichier source **si les permissions le permettent**.
     *
     * @param context Contexte de l'application.
     * @param uri URI du fichier à crypter.
     * @param directoryUri URI du dossier SAF sélectionné pour la sauvegarde du fichier crypté.
     */
    fun encryptFile(context: Context, uri: Uri, directoryUri: Uri) {
        Log.d("CryptoViewModel", "Encryption démarrée !")

        viewModelScope.launch {
            val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                Log.e("CryptoViewModel", "Impossible d'ouvrir le fichier source.")
                return@launch
            }

            val originalFileName = getFileNameFromUri(uri, context)
            val encryptedFileName = "$originalFileName${defaultConfig.value.cryptExtension}"

            val encryptedFileUri = createFileInSameDirectory(
                treeUri = directoryUri,
                fileName = encryptedFileName,
                context = context
            )

            if (encryptedFileUri == null) {
                Log.e("CryptoViewModel", "Impossible de créer le fichier de sortie.")
                return@launch
            }

            val outputStream = context.contentResolver.openOutputStream(encryptedFileUri) ?: run {
                Log.e("CryptoViewModel", "Impossible d'ouvrir le fichier de sortie.")
                return@launch
            }

            //  Lancement du chiffrement avec suivi de progression
            cryptoRepository.encryptFile(
                inputStream = inputStream,
                outputStream = outputStream,
                password = defaultConfig.value.password,
                keySize = defaultConfig.value.keySize,
                iterations = defaultConfig.value.iterations,
                mode = defaultConfig.value.mode,
                extension = defaultConfig.value.cryptExtension
            ).collect { progress ->
                updateFileProgress(uri, progress)
            }

            updateFileProgress(uri, 1f)
            Log.d("CryptoViewModel", "Chiffrement terminé : $encryptedFileName")

            //  Tentative de suppression du fichier source
            try {
                if (DocumentsContract.isDocumentUri(context, uri) &&
                    uri.toString().startsWith(directoryUri.toString())
                ) {
                    val deleted = DocumentsContract.deleteDocument(context.contentResolver, uri)
                    if (deleted) {
                        Log.d("CryptoViewModel", "Fichier source supprimé automatiquement.")
                    } else {
                        Log.w("CryptoViewModel", "Échec suppression automatique.")
                    }

                    //refresh!
                    loadDirectoryFiles(directoryUri, context)


                } else {
                    Log.w("CryptoViewModel", "Fichier source hors du dossier SAF autorisé, suppression ignorée.")
                }
            } catch (e: Exception) {
                Log.e("CryptoViewModel", "Erreur suppression fichier source : ${e.message}", e)
            }
        }
    }


    private fun getParentUriFromUri(uri: Uri): Uri {
        val documentId = DocumentsContract.getDocumentId(uri)
        val treeUri = DocumentsContract.buildDocumentUriUsingTree(uri, documentId)
        return treeUri
    }

    private fun createFileInSameDirectory(
        treeUri: Uri,
        fileName: String,
        context: Context
    ): Uri? {
        return try {
            val contentResolver = context.contentResolver

            // On extrait le documentId du treeUri
            val docId = DocumentsContract.getTreeDocumentId(treeUri)

            // On recrée une Document URI à partir de ce docId
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)

            // Maintenant, on peut créer le fichier dans ce dossier
            DocumentsContract.createDocument(
                contentResolver,
                documentUri,
                "application/octet-stream", // ou autre mime type adapté
                fileName
            )
        } catch (e: Exception) {
            Log.e("CryptoViewModel", "Erreur lors de la création du fichier dans le répertoire parent : ${e.message}", e)
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    return it.getString(displayNameIndex)
                }
            }
        }
        return null
    }


    /**
     * ### `encryptSelectedFiles`
     *
     * Chiffre tous les fichiers sélectionnés par l'utilisateur.
     *
     * Cette fonction parcourt tous les fichiers actuellement sélectionnés et appelle la
     * méthode `encryptFile` sur chacun d'eux.
     *
     * @param context Le contexte Android.
     */
    fun encryptSelectedFiles(context: Context,directoryUri: Uri) {
        selectedUris.value.forEach { uri ->
            encryptFile(context, uri,directoryUri)
        }
    }

    /**
     * ### `encryptSingleFile`
     *
     * Chiffre un fichier precis.
     * @param context Le contexte Android.
     * @param uri le fichier a crypter
     */
    fun encryptSingleFile(context: Context, uri: Uri, directoryUri: Uri) {
        encryptFile(context, uri, directoryUri)
    }

    /**
     * ### `updateFileProgress`
     *
     * Met à jour la progression de chiffrement/déchiffrement pour un fichier spécifique.
     *
     * Cette méthode met à jour la carte de progression de chaque fichier dans l'interface.
     *
     * @param uri L'URI du fichier concerné.
     * @param progress La progression du fichier (valeur entre 0f et 1f).
     */
    private fun updateFileProgress(uri: Uri, progress: Float) {
        _fileProgressMap.update { currentMap ->
            currentMap.toMutableMap().apply {
                put(uri, progress)
            }
        }
    }


    /**
     * ### `showDialog`
     *
     * @param state L'état du BottomDialog à afficher.
     */
    fun showDialog(state: BottomDialogState) {
        _dialogState.value = state
    }

    /**
     * ### `dismissDialog`
     */
    fun dismissDialog() {
        _dialogState.value = BottomDialogState.None
    }



}