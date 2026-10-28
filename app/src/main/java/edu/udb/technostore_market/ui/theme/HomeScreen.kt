package edu.udb.technostore_market.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.udb.technostore_market.model.Producto
import edu.udb.technostore_market.model.categorias
import edu.udb.technostore_market.model.productosDePrueba

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onProductoClick: (Producto) -> Unit) {
    // Estados para la búsqueda y el filtro
    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    // Lógica para filtrar los productos
    val productosFiltrados = productosDePrueba.filter { producto ->
        (categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada) &&
                producto.nombre.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1. Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar productos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Filtros de Categorías
        Text("Categorías", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categorias) { categoria ->
                FilterChip(
                    selected = categoriaSeleccionada == categoria,
                    onClick = { categoriaSeleccionada = categoria },
                    label = { Text(categoria) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Título y Grid de Productos (2 columnas)
        Text("Productos", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Fuerza a que sean 2 columnas
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(productosFiltrados) { producto ->
                ProductoCard(
                    producto = producto,
                    onCardClick = { onProductoClick(producto) },
                    onAgregarClick = { /* Aquí irá la lógica del carrito luego */ }
                )
            }
        }
    }
}