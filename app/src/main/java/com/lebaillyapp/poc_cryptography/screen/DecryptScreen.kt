package com.lebaillyapp.poc_cryptography.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lebaillyapp.poc_cryptography.R
import com.lebaillyapp.poc_cryptography.ui.theme.chartreuse
import com.lebaillyapp.poc_cryptography.ui.theme.gunMetal


@Composable
fun DecryptScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gunMetal),
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
            colorFilter = ColorFilter.tint(chartreuse.copy(alpha = 0.1f))
        )
    }
}