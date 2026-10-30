package edu.udb.technostore_market.data.firebase

/**
 * Nombres de colecciones y campos de Cloud Firestore (un solo lugar para no repetir strings).
 *
 * Modelo de datos (ver documento de la Fase I):
 *
 * categorias/{idCategoria}                 -> idCategoria, nombre
 * productos/{idProducto}                   -> idProducto, nombre, descripcion, precio, stock,
 *                                             idCategoria, imagenUrl, marca, calificacion
 * usuarios/{uid}                           -> uid, nombre, email, telefono, direccion, fechaRegistro
 * usuarios/{uid}/carrito/{idProducto}      -> idItemCarrito, idProducto, nombreProducto,
 *                                             precioUnitario, cantidad, imagenUrl, stock
 * usuarios/{uid}/pedidos/{idPedido}        -> idPedido, idUsuario, fecha, estado, total,
 *                                             direccion, metodoPago, items[]
 */
object FirestoreRutas {

    // Colecciones
    const val CATEGORIAS = "categorias"
    const val PRODUCTOS = "productos"
    const val USUARIOS = "usuarios"
    const val CARRITO = "carrito"
    const val PEDIDOS = "pedidos"

    // categorias
    const val ID_CATEGORIA = "idCategoria"
    const val NOMBRE = "nombre"

    // productos
    const val ID_PRODUCTO = "idProducto"
    const val DESCRIPCION = "descripcion"
    const val PRECIO = "precio"
    const val STOCK = "stock"
    const val IMAGEN_URL = "imagenUrl"
    const val MARCA = "marca"
    const val CALIFICACION = "calificacion"

    // usuarios
    const val UID = "uid"
    const val EMAIL = "email"
    const val TELEFONO = "telefono"
    const val DIRECCION = "direccion"
    const val FECHA_REGISTRO = "fechaRegistro"

    // carrito
    const val ID_ITEM_CARRITO = "idItemCarrito"
    const val NOMBRE_PRODUCTO = "nombreProducto"
    const val PRECIO_UNITARIO = "precioUnitario"
    const val CANTIDAD = "cantidad"

    // pedidos
    const val ID_PEDIDO = "idPedido"
    const val ID_USUARIO = "idUsuario"
    const val FECHA = "fecha"
    const val ESTADO = "estado"
    const val TOTAL = "total"
    const val METODO_PAGO = "metodoPago"
    const val ITEMS = "items"
}
