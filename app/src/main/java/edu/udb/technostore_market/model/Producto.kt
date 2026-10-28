package edu.udb.technostore_market.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart

// Modelo de datos
data class Producto(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val categoria: String,
    val stock: Int = 10,
    val imagen: ImageVector? = null // Usaremos íconos por ahora como placeholder
)

// Datos de prueba (Mock data)
val productosDePrueba = listOf(
    Producto(1, "Audífonos", 15.00, "Audífonos inalámbricos con cancelación de ruido.", "Electrónica"),
    Producto(2, "Camiseta", 30.00, "Camiseta de algodón premium.", "Ropa"),
    Producto(3, "Teclado", 45.00, "Teclado mecánico RGB.", "Electrónica"),
    Producto(4, "Zapatos", 60.00, "Zapatos deportivos para correr.", "Deportes"),
    Producto(5, "Mouse", 20.00, "Mouse ergonómico inalámbrico.", "Electrónica"),
    Producto(6, "Pantalón", 35.00, "Pantalón de mezclilla clásico.", "Ropa")
)

val categorias = listOf("Todos", "Electrónica", "Ropa", "Hogar", "Deportes")