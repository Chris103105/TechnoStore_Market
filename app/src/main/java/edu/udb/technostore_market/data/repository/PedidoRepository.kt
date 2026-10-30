package edu.udb.technostore_market.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.udb.technostore_market.data.firebase.FirestoreRutas
import edu.udb.technostore_market.data.firebase.observar
import edu.udb.technostore_market.data.firebase.toFirestore
import edu.udb.technostore_market.data.firebase.toPedido
import edu.udb.technostore_market.model.EstadoPedido
import edu.udb.technostore_market.model.ItemCarrito
import edu.udb.technostore_market.model.MetodoPago
import edu.udb.technostore_market.model.Pedido
import edu.udb.technostore_market.model.PedidoItem
import edu.udb.technostore_market.util.Validadores
import edu.udb.technostore_market.util.llamadaSegura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Pedidos e historial de compras en la subcolección `usuarios/{uid}/pedidos` de Firestore.
 * Al confirmar, el pedido se guarda y el carrito se vacía en un mismo lote (batch) atómico.
 */
class PedidoRepository(
    private val db: FirebaseFirestore,
    private val sesion: SessionManager
) {

    private fun usuario(uid: String) = db.collection(FirestoreRutas.USUARIOS).document(uid)
    private fun pedidos(uid: String) = usuario(uid).collection(FirestoreRutas.PEDIDOS)
    private fun carrito(uid: String) = usuario(uid).collection(FirestoreRutas.CARRITO)

    /** Historial en orden cronológico (más reciente primero), en tiempo real. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarPedidos(): Flow<List<Pedido>> =
        sesion.userIdFlow.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else pedidos(uid)
                .orderBy(FirestoreRutas.FECHA, Query.Direction.DESCENDING)
                .observar(incluirMetadatos = true)
                .map { snapshot -> snapshot.documents.map { it.toPedido() } }
                .catch { e ->
                    Log.w(TAG, "No se pudo observar el historial", e)
                    emit(emptyList())
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarPedido(id: Long): Flow<Pedido?> =
        sesion.userIdFlow.flatMapLatest { uid ->
            if (uid == null) flowOf(null)
            else pedidos(uid).document(id.toString())
                .observar(incluirMetadatos = true)
                .map { documento -> if (documento.exists()) documento.toPedido() else null }
                .catch { e ->
                    Log.w(TAG, "No se pudo observar el pedido $id", e)
                    emit(null)
                }
        }

    /** Devuelve el número del pedido creado (1, 2, 3... por usuario). */
    suspend fun registrarPedido(
        items: List<ItemCarrito>,
        direccion: String,
        metodoPago: MetodoPago
    ): Result<Long> = llamadaSegura {
        withContext(Dispatchers.IO) {
            val uid = sesion.requerirUserId()
            if (items.isEmpty()) throw IllegalStateException("Tu carrito está vacío")
            Validadores.validarDireccion(direccion)?.let { throw IllegalArgumentException(it) }

            val id = siguienteNumeroPedido(uid)
            val pedido = Pedido(
                id = id,
                fecha = System.currentTimeMillis(),
                total = items.sumOf { it.subtotal },
                direccion = direccion.trim(),
                metodoPago = metodoPago.etiqueta,
                estado = EstadoPedido.CONFIRMADO,
                sincronizado = false,
                items = items.map {
                    PedidoItem(
                        productoId = it.productoId,
                        nombre = it.nombre,
                        precio = it.precio,
                        cantidad = it.cantidad,
                        imagenUrl = it.imagenUrl
                    )
                }
            )

            // Guarda el pedido y vacía el carrito en una sola operación atómica.
            val lote = db.batch()
            lote.set(pedidos(uid).document(id.toString()), pedido.toFirestore(uid))
            items.forEach { lote.delete(carrito(uid).document(it.productoId.toString())) }
            lote.commit().await()
            id
        }
    }

    /** Número consecutivo por usuario: el último idPedido + 1. */
    private suspend fun siguienteNumeroPedido(uid: String): Long {
        val ultimo = pedidos(uid)
            .orderBy(FirestoreRutas.ID_PEDIDO, Query.Direction.DESCENDING)
            .limit(1)
            .get().await()
            .documents.firstOrNull()
            ?.getLong(FirestoreRutas.ID_PEDIDO) ?: 0L
        return ultimo + 1
    }

    suspend fun cancelarPedido(id: Long): Result<Unit> = llamadaSegura {
        withContext<Unit>(Dispatchers.IO) {
            pedidos(sesion.requerirUserId()).document(id.toString())
                .update(FirestoreRutas.ESTADO, EstadoPedido.CANCELADO.name)
                .await()
        }
    }

    suspend fun eliminarPedido(id: Long): Result<Unit> = llamadaSegura {
        withContext<Unit>(Dispatchers.IO) {
            pedidos(sesion.requerirUserId()).document(id.toString()).delete().await()
        }
    }

    private companion object {
        const val TAG = "PedidoRepository"
    }
}
