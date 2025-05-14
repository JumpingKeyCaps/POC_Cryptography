package com.lebaillyapp.poc_cryptography.v2

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lebaillyapp.poc_cryptography.R
import com.lebaillyapp.poc_cryptography.model.SAFFile

@Composable
fun SAFFileItem(
    file: SAFFile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEncryptDecryptClick: () -> Unit,
    progress: Float = 0f,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Aperçu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    file.mimeType?.startsWith("image/") == true -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(file.uri)
                                .size(48)
                                .build(),
                            contentDescription = "Aperçu image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Text(
                            text = file.name.substringAfterLast('.'),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Infos fichier
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(file.mimeType?.substringAfterLast('/') ?: "N/A", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = {
                            Icon(
                                painterResource(
                                    when (file.mimeType) {
                                        "application/zip","application/x-7z-compressed" -> R.drawable.folder_zip_24px
                                        "image/jpeg", "image/jpg", "image/webp" -> R.drawable.photo_24px
                                        "image/png" -> R.drawable.file_png_24px
                                        "image/gif" -> R.drawable.gif_24px
                                        else -> R.drawable.draft_24px
                                    }


                                ), contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp, end = 5.dp).height(28.dp),
                        shape = MaterialTheme.shapes.small,
                        border = AssistChipDefaults.assistChipBorder(false)
                    )
                    Text("${file.size} Bytes", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Action chiffrement/déchiffrement
            IconButton(onClick = onEncryptDecryptClick) {
                Icon(painterResource(R.drawable.no_encryption_24px), contentDescription = "Crypter", modifier = Modifier.size(18.dp))
            }

            // Expand
            IconButton(onClick = onExpandClick) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }


        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec     )

        // Progression
        if (progress > 0f && progress < 1f) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }

        // Infos supplémentaires
        if (isExpanded) {
            Text(
                text = "Type MIME : ${file.mimeType ?: "inconnu"}\n" +
                        "Date ajout/modification : ${file.lastModified ?: "unknown"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}