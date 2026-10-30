package edu.udb.technostore_market.data.firebase

import edu.udb.technostore_market.model.Producto

/**
 * Datos semilla del catálogo. Se suben a Firestore UNA sola vez, la primera vez que
 * la app encuentra la colección `productos` vacía (ver ProductoRepository.asegurarCatalogo).
 * Después de eso, el catálogo se administra desde la consola de Firebase.
 *
 * imagenUrl va vacío: pega la URL de cada imagen en la consola de Firestore.
 */
object CatalogoInicial {

    /** idCategoria -> nombre visible. */
    val categorias: Map<String, String> = linkedMapOf(
        "smartphones" to "Smartphones",
        "laptops" to "Laptops",
        "tablets" to "Tablets",
        "audio" to "Audio",
        "accesorios" to "Accesorios"
    )

    val productos: List<Producto> = listOf(
        // Smartphones
        producto(1, "Samsung Galaxy A55", "Pantalla Super AMOLED de 6.6\", 128 GB, cámara de 50 MP.", "smartphones", 449.99, 15, "Samsung", 4.6),
        producto(2, "iPhone 15", "Pantalla de 6.1\", chip A16 Bionic, 128 GB, USB-C.", "smartphones", 799.00, 8, "Apple", 4.8),
        producto(3, "Xiaomi Redmi Note 13", "Pantalla AMOLED de 6.67\", 256 GB, carga rápida de 33 W.", "smartphones", 249.99, 20, "Xiaomi", 4.4),
        producto(4, "Motorola Moto G84", "Pantalla pOLED de 6.5\", 256 GB, batería de 5000 mAh.", "smartphones", 279.00, 0, "Motorola", 4.3),
        // Laptops
        producto(5, "Lenovo IdeaPad Slim 3", "Ryzen 5, 8 GB RAM, SSD de 512 GB, pantalla de 15.6\".", "laptops", 549.00, 6, "Lenovo", 4.5),
        producto(6, "HP Pavilion 15", "Intel Core i5, 16 GB RAM, SSD de 512 GB, Windows 11.", "laptops", 699.99, 4, "HP", 4.4),
        producto(7, "MacBook Air M2", "Chip M2, 8 GB RAM, SSD de 256 GB, pantalla Liquid Retina de 13.6\".", "laptops", 999.00, 3, "Apple", 4.9),
        producto(8, "ASUS TUF Gaming F15", "Intel Core i7, RTX 4050, 16 GB RAM, SSD de 1 TB.", "laptops", 1099.00, 2, "ASUS", 4.6),
        // Tablets
        producto(9, "iPad 10.ª generación", "Pantalla de 10.9\", chip A14 Bionic, 64 GB.", "tablets", 349.00, 7, "Apple", 4.7),
        producto(10, "Samsung Galaxy Tab A9+", "Pantalla de 11\", 64 GB, cuatro bocinas.", "tablets", 219.99, 10, "Samsung", 4.4),
        producto(11, "Lenovo Tab M10", "Pantalla de 10.1\" HD, 32 GB, modo infantil.", "tablets", 149.00, 12, "Lenovo", 4.2),
        // Audio
        producto(12, "AirPods (3.ª generación)", "Audio espacial, estuche con carga MagSafe.", "audio", 169.00, 14, "Apple", 4.6),
        producto(13, "Sony WH-CH720N", "Audífonos inalámbricos con cancelación de ruido, 35 h de batería.", "audio", 129.99, 9, "Sony", 4.5),
        producto(14, "JBL Flip 6", "Bocina Bluetooth resistente al agua IP67, 12 h de batería.", "audio", 119.95, 11, "JBL", 4.7),
        producto(15, "Xiaomi Redmi Buds 5", "Audífonos TWS con cancelación activa de ruido.", "audio", 39.99, 25, "Xiaomi", 4.3),
        // Accesorios
        producto(16, "Cargador Anker 20 W USB-C", "Carga rápida para teléfonos y tablets.", "accesorios", 19.99, 40, "Anker", 4.8),
        producto(17, "Power bank Xiaomi 10000 mAh", "Batería externa con salida USB-C y USB-A.", "accesorios", 24.99, 30, "Xiaomi", 4.5),
        producto(18, "Mouse Logitech M185", "Mouse inalámbrico con receptor USB, 1 año de batería.", "accesorios", 14.99, 35, "Logitech", 4.6),
        producto(19, "Teclado Logitech K380", "Teclado Bluetooth multidispositivo compacto.", "accesorios", 39.99, 18, "Logitech", 4.7),
        producto(20, "Memoria USB SanDisk 64 GB", "USB 3.2 de alta velocidad.", "accesorios", 9.99, 50, "SanDisk", 4.5)
    )

    private fun producto(
        id: Int,
        nombre: String,
        descripcion: String,
        categoria: String,
        precio: Double,
        stock: Int,
        marca: String,
        calificacion: Double
    ) = Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        categoria = categoria,
        precio = precio,
        stock = stock,
        marca = marca,
        imagenUrl = "",
        calificacion = calificacion
    )
}
