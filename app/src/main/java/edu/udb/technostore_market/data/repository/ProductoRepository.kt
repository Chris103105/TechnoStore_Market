package edu.udb.technostore_market.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import edu.udb.technostore_market.data.firebase.CatalogoInicial
import edu.udb.technostore_market.data.firebase.FirestoreRutas
import edu.udb.technostore_market.data.firebase.toFirestore
import edu.udb.technostore_market.data.firebase.toProducto
import edu.udb.technostore_market.model.Producto
import edu.udb.technostore_market.util.llamadaSegura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Catálogo de productos leído directamente de las colecciones `productos` y `categorias`
 * de Cloud Firestore. Sin conexión, Firestore responde desde su caché local (RNF04).
 */
class ProductoRepository(private val db: FirebaseFirestore) {

    private val productos get() = db.collection(FirestoreRutas.PRODUCTOS)
    private val categorias get() = db.collection(FirestoreRutas.CATEGORIAS)

    private val candado = Mutex()
    private var catalogoVerificado = false

    suspend fun obtenerProductos(): Result<List<Producto>> = llamadaSegura {
        withContext(Dispatchers.IO) {
            asegurarCatalogo()
            leerTodos()
        }
    }

    /**
     * Firestore no tiene búsqueda por "contiene", así que se filtra en el dispositivo
     * por nombre, marca o descripción (el catálogo es pequeño).
     */
    suspend fun buscarProductos(consulta: String): Result<List<Producto>> = llamadaSegura {
        withContext(Dispatchers.IO) {
            asegurarCatalogo()
            val termino = consulta.trim()
            leerTodos().filter {
                it.nombre.contains(termino, ignoreCase = true) ||
                    it.marca.contains(termino, ignoreCase = true) ||
                    it.descripcion.contains(termino, ignoreCase = true)
            }
        }
    }

    suspend fun obtenerPorCategoria(categoria: String): Result<List<Producto>> = llamadaSegura {
        withContext(Dispatchers.IO) {
            asegurarCatalogo()
            productos.whereEqualTo(FirestoreRutas.ID_CATEGORIA, categoria)
                .get().await()
                .documents.map { it.toProducto() }
                .sortedBy { it.nombre }
        }
    }

    suspend fun obtenerCategorias(): Result<List<String>> = llamadaSegura {
        withContext(Dispatchers.IO) {
            asegurarCatalogo()
            categorias.get().await()
                .documents
                .sortedBy { it.getString(FirestoreRutas.NOMBRE) ?: it.id }
                .map { it.getString(FirestoreRutas.ID_CATEGORIA) ?: it.id }
        }
    }

    suspend fun obtenerProducto(id: Int): Result<Producto> = llamadaSegura {
        withContext(Dispatchers.IO) {
            val documento = productos.document(id.toString()).get().await()
            if (!documento.exists()) throw IllegalStateException("El producto ya no está disponible")
            documento.toProducto()
        }
    }

    private suspend fun leerTodos(): List<Producto> =
        productos.get().await().documents.map { it.toProducto() }.sortedBy { it.nombre }

    /**
     * La primera vez que se usa la app con un proyecto de Firebase nuevo, la colección
     * `productos` está vacía: se sube el catálogo inicial en un solo lote (batch).
     * Se verifica una vez por ejecución de la app.
     */
    private suspend fun asegurarCatalogo(): Unit = candado.withLock {
        if (catalogoVerificado) return@withLock

        val vacio = try {
            productos.limit(1).get(Source.SERVER).await().isEmpty
        } catch (e: FirebaseFirestoreException) {
            // Sin conexión: se trabaja con lo que haya en caché y se reintenta en la próxima carga.
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) return@withLock
            throw e
        }

        if (vacio) {
            Log.i(TAG, "Colección '${FirestoreRutas.PRODUCTOS}' vacía: subiendo catálogo inicial")
            val lote = db.batch()
            CatalogoInicial.categorias.forEach { (id, nombre) ->
                lote.set(
                    categorias.document(id),
                    mapOf(FirestoreRutas.ID_CATEGORIA to id, FirestoreRutas.NOMBRE to nombre)
                )
            }
            CatalogoInicial.productos.forEach { producto ->
                lote.set(productos.document(producto.id.toString()), producto.toFirestore())
            }
            lote.commit().await()
        }
        catalogoVerificado = true
    }

    private companion object {
        const val TAG = "ProductoRepository"
    }
}
