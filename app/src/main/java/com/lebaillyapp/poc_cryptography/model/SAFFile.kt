package com.lebaillyapp.poc_cryptography.model

import android.net.Uri

data class SAFFile(
    val name: String,                    // COLUMN_DISPLAY_NAME
    val size: Long?,                    // COLUMN_SIZE (nullable si non fourni)
    val uri: Uri,                       // URI construit manuellement
    val mimeType: String?,             // COLUMN_MIME_TYPE (nullable si non fourni)
    val lastModified: Long?,           // COLUMN_LAST_MODIFIED (nullable si non fourni)
    val isVirtual: Boolean?,           // COLUMN_FLAGS & FLAG_VIRTUAL_DOCUMENT
    val isWritable: Boolean?,          // COLUMN_FLAGS & FLAG_SUPPORTS_WRITE
    val isDeletable: Boolean?,         // COLUMN_FLAGS & FLAG_SUPPORTS_DELETE
    val isDirectory: Boolean = false,  // booléen déduit du MIME type
    val documentId: String?,           // COLUMN_DOCUMENT_ID (utile pour build URI)
    val summary: String? = null        // COLUMN_SUMMARY (rarement fourni)
)
