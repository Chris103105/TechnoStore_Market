package edu.udb.technostore_market.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import edu.udb.technostore_market.data.firebase.FirestoreRutas as F
import edu.udb.technostore_market.model.EstadoPedido
import edu.udb.technostore_market.model.ItemCarrito
import edu.udb.technostore_market.model.Pedido
import edu.udb.technostore_market.model.PedidoItem
import edu.udb.technostore_market.model.Producto

/*
 * Conversión documento de Firestore <-> modelo de dominio.
 * Se hace a mano (en lugar de toObject) para tolerar campos faltantes y
 * números guardados como entero o decimal desde la consola de Firebase.
 */

// ---------- Producto ----------

fun DocumentSnapshot.toProducto(): Producto = Producto(
    id = getLong(F.ID_PRODUCTO)?.toInt() ?: id.toIntOrNull() ?: 0,
    nombre = getString(F.NOMBRE).orEmpty(),
    descripcion = getString(F.DESCRIPCION).orEmpty(),
    categoria = getString(F.ID_CATEGORIA).orEmpty(),
    precio = getDouble(F.PRECIO) ?: 0.0,
    stock = getLong(F.STOCK)?.toInt() ?: 0,
    marca = getString(F.MARCA).orEmpty(),
    imagenUrl = getString(F.IMAGEN_URL).orEmpty(),
    calificacion = getDouble(F.CALIFICACION) ?: 0.0
)

fun Producto.toFirestore(): Map<String, Any> = mapOf(
    F.ID_PRODUCTO to id,
    F.NOMBRE to nombre,
    F.DESCRIPCION to descripcion,
    F.ID_CATEGORIA to categoria,
    F.PRECIO to precio,
    F.STOCK to stock,
    F.MARCA to marca,
    F.IMAGEN_URL to imagenUrl,
    F.CALIFICACION to calificacion
)

// ---------- Carrito ----------

fun DocumentSnapshot.toItemCarrito(): ItemCarrito = ItemCarrito(
    productoId = getLong(F.ID_PRODUCTO)?.toInt() ?: id.toIntOrNull() ?: 0,
    nombre = getString(F.NOMBRE_PRODUCTO).orEmpty(),
    precio = getDouble(F.PRECIO_UNITARIO) ?: 0.0,
    imagenUrl = getString(F.IMAGEN_URL).orEmpty(),
    cantidad = getLong(F.CANTIDAD)?.toInt() ?: 0,
    stock = getLong(F.STOCK)?.toInt() ?: 0
)

fun ItemCarrito.toFirestore(): Map<String, Any> = mapOf(
    F.ID_ITEM_CARRITO to productoId.toString(),
    F.ID_PRODUCTO to productoId,
    F.NOMBRE_PRODUCTO to nombre,
    F.PRECIO_UNITARIO to precio,
    F.IMAGEN_URL to imagenUrl,
    F.CANTIDAD to cantidad,
    F.STOCK to stock
)

// ---------- Pedido ----------

fun PedidoItem.toFirestore(): Map<String, Any> = mapOf(
    F.ID_PRODUCTO to productoId,
    F.NOMBRE_PRODUCTO to nombre,
    F.PRECIO_UNITARIO to precio,
    F.CANTIDAD to cantidad,
    F.IMAGEN_URL to imagenUrl
)

private fun Map<*, *>.toPedidoItem(): PedidoItem = PedidoItem(
    productoId = (this[F.ID_PRODUCTO] as? Number)?.toInt() ?: 0,
    nombre = this[F.NOMBRE_PRODUCTO] as? String ?: "",
    precio = (this[F.PRECIO_UNITARIO] as? Number)?.toDouble() ?: 0.0,
    cantidad = (this[F.CANTIDAD] as? Number)?.toInt() ?: 0,
    imagenUrl = this[F.IMAGEN_URL] as? String ?: ""
)

fun DocumentSnapshot.toPedido(): Pedido = Pedido(
    id = getLong(F.ID_PEDIDO) ?: id.toLongOrNull() ?: 0L,
    fecha = getTimestamp(F.FECHA)?.toDate()?.time ?: 0L,
    total = getDouble(F.TOTAL) ?: 0.0,
    direccion = getString(F.DIRECCION).orEmpty(),
    metodoPago = getString(F.METODO_PAGO).orEmpty(),
    estado = runCatching { EstadoPedido.valueOf(getString(F.ESTADO).orEmpty()) }
        .getOrDefault(EstadoPedido.CONFIRMADO),
    // Firestore marca hasPendingWrites mientras el pedido solo existe en la caché local.
    sincronizado = !metadata.hasPendingWrites(),
    items = (get(F.ITEMS) as? List<*>).orEmpty()
        .filterIsInstance<Map<*, *>>()
        .map { it.toPedidoItem() }
)

fun Pedido.toFirestore(userId: String): Map<String, Any> = mapOf(
    F.ID_PEDIDO to id,
    F.ID_USUARIO to userId,
    F.FECHA to Timestamp(java.util.Date(fecha)),
    F.ESTADO to estado.name,
    F.TOTAL to total,
    F.DIRECCION to direccion,
    F.METODO_PAGO to metodoPago,
    F.ITEMS to items.map { it.toFirestore() }
)
