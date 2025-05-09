package com.lebaillyapp.poc_cryptography.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lebaillyapp.poc_cryptography.R
import com.lebaillyapp.poc_cryptography.model.SelectedFile
import com.lebaillyapp.poc_cryptography.ui.theme.chartreuse
import com.lebaillyapp.poc_cryptography.ui.theme.gunMetal

@Composable
fun EncryptScreen(viewModel: CryptoViewModel, selectedFile: SelectedFile?) {
    // Déclare le launcher pour la sélection de fichier
    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        Log.d("EncryptScreen", "EncryptScreen: URI SELECTED : ${uri?.path}")
        uri?.let { viewModel.onFileSelected(it, context) }
    }

    val selectedStuff by viewModel.selectedFile.collectAsState()
    LaunchedEffect(selectedStuff) {
        Log.d("EncryptScreen", "Selected file: ${selectedStuff}")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(chartreuse),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bunnydeco_b),
            contentDescription = "Top Bar Decoration",
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(gunMetal.copy(alpha = 0.1f))
        )


        // Si un fichier a été sélectionné, affiche une carte avec les infos du fichier
        selectedFile?.let {
            Row(modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 54.dp)) {
                //file infos
                FileInfoCard(selectedFile = it,modifier = Modifier.weight(0.65f))
                Spacer(modifier = Modifier.width(2.dp))
                // Aperçu binaire
                Card(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxSize()
                        .padding(end = 8.dp, top = 10.dp, bottom = 20.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = gunMetal.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,   // Coin supérieur gauche
                        topEnd = 16.dp,     // Coin supérieur droit
                        bottomStart = 0.dp, // Coin inférieur gauche
                        bottomEnd = 16.dp  // Coin inférieur droit
                    )
                ) {
                    Text(
                        text = "> ${getBinaryPreview(it.uri)}",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                            .align(Alignment.Start)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall,
                        color = chartreuse,
                        textAlign = TextAlign.Start,
                        fontFamily = FontFamily(
                            Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                        ),
                        fontSize = 11.sp
                    )
                }

            }


            // Bouton Reset
            Button(
                onClick = { viewModel.resetFile() },
                modifier = Modifier
                    .height(56.dp)
                    .padding(top = 0.dp, start = 30.dp, end = 30.dp, bottom = 20.dp)
                    .align(Alignment.BottomStart),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = gunMetal)
            ) {
                Text(
                    "Reset File",
                    style = MaterialTheme.typography.bodySmall,
                    color = chartreuse,
                    fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    )
                )
            }




        } ?: run {
            // Sinon, affiche le bouton pour ajouter un fichier
            Button(
                onClick = { filePickerLauncher.launch("application/*") },
                modifier = Modifier
                    .height(86.dp)
                    .padding(top = 10.dp, start = 30.dp, end = 30.dp, bottom = 10.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = gunMetal)
            ) {
                Text(
                    "Add a file to encrypt !",
                    style = MaterialTheme.typography.bodyLarge,
                    color = chartreuse,
                    fontFamily = FontFamily(
                        Font(R.font.sairacondensed_black, weight = FontWeight.Bold)
                    )
                )
            }
        }


    }
}