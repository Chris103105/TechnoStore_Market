package edu.udb.technostore_market.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import edu.udb.technostore_market.data.firebase.FirestoreRutas
import edu.udb.technostore_market.data.firebase.observar
import edu.udb.technostore_market.data.firebase.toFirestore
import edu.udb.technostore_market.data.firebase.toItemCarrito
import edu.udb.technostore_market.model.ItemCarrito
import edu.udb.technostore_market.model.Producto
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
 * CRUD del carrito sobre la subcolección `usuarios/{uid}/carrito` de Firestore.
 * Cada documento usa el id del producto como id, así no hay líneas duplicadas.
 */
class CarritoRepository(
    private val db: FirebaseFirestore,
    private val sesion: SessionManager
) {

    private fun carrito(uid: String) =
        db.collection(FirestoreRutas.USUARIOS).document(uid).collection(FirestoreRutas.CARRITO)

    /** Se actualiza en tiempo real (badge del carrito, pantalla de carrito). */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarCarrito(): Flow<List<ItemCarrito>> =
        sesion.userIdFlow.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else carrito(uid).observar()
                .map { snapshot -> snapshot.documents.map { it.toItemCarrito() }.sortedBy { it.nombre } }
                .catch { e ->
                    Log.w(TAG, "No se pudo observar el carrito", e)
                    emit(emptyList())
                }
        }

    suspend fun agregar(producto: Producto, cantidad: Int = 1): Result<Unit> = llamadaSegura {
        withContext<Unit>(Dispatchers.IO) {
            val uid = sesion.requerirUserId()
            if (producto.agotado) throw IllegalStateException("${producto.nombre} está agotado")

            val documento = carrito(uid).document(producto.id.toString())
            val cantidadActual = documento.get().await().toItemCarritoOrNull()?.cantidad ?: 0
            val nuevaCantidad = cantidadActual + cantidad
            if (nuevaCantidad > producto.stock) {
                throw IllegalStateException("Solo hay ${producto.stock} unidades disponibles")
            }
            val item = ItemCarrito(
                productoId = producto.id,
                nombre = producto.nombre,
                precio = producto.precio,
                imagenUrl = producto.imagenUrl,
                cantidad = nuevaCantidad,
                stock = producto.stock
            )
            documento.set(item.toFirestore()).await()
        }
    }

    suspend fun actualizarCantidad(productoId: Int, cantidad: Int): Result<Unit> = llamadaSegura {
        withContext<Unit>(Dispatchers.IO) {
            val uid = sesion.requerirUserId()
            val documento = carrito(uid).document(productoId.toString())
            val actual = documento.get().await().toItemCarritoOrNull() ?: return@withContext
            when {
                cantidad <= 0 -> documento.delete().await()
                cantidad > actual.stock -> throw IllegalStateException("Solo hay ${actual.stock} unidades disponibles")
                else -> documento.update(FirestoreRutas.CANTIDAD, cantidad).await()
            }
        }
    }

    suspend fun eliminar(productoId: Int): Result<Unit> = llamadaSegura {
        withContext<Unit>(Dispatchers.IO) {
            carrito(sesion.requerirUserId()).document(productoId.toString()).delete().await()
        }
    }

    suspend fun vaciar(): Result<Unit> = llamadaSegura {
        withContext<Unit>(Dispatchers.IO) {
            val documentos = carrito(sesion.requerirUserId()).get().await().documents
            val lote = db.batch()
            documentos.forEach { lote.delete(it.reference) }
            lote.commit().await()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toItemCarritoOrNull(): ItemCarrito? =
        if (exists()) toItemCarrito() else null

    private companion object {
        const val TAG = "CarritoRepository"
    }
}
