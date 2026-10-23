package edu.udb.technostore_market.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Recordando el color de fondo siempre
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "¡Bienvenido al Catálogo!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}