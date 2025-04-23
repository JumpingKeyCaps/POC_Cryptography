package com.lebaillyapp.poc_cryptography.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    onEncryptDecryptClick: (password: String, extension: String) -> Unit
) {
    val backgroundColor = if (pagerState.currentPage == 0) gunMetal else chartreuse
    val buttonColor = if (pagerState.currentPage == 0) chartreuse else gunMetal
    val textColor = if (pagerState.currentPage == 0) gunMetal else chartreuse
    val textColorInv = if (pagerState.currentPage == 0) chartreuse else gunMetal

    var password by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("crypt") }
    var passwordVisible by remember { mutableStateOf(false) }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(),
            containerColor = backgroundColor,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
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
                    if(pagerState.currentPage == 0) "Setup the decryption key pass !" else "Enter the encryption key pass.",
                    color = textColorInv)

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
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


                if(pagerState.currentPage == 0){
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



                Spacer(Modifier.height(34.dp))

                Button(
                    onClick = {
                        onEncryptDecryptClick(password, extension)
                        onDismiss()
                    },
                    enabled = password.isNotBlank() && extension.isNotBlank(),
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