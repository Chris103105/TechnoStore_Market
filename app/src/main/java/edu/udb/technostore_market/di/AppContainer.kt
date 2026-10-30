package edu.udb.technostore_market.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import edu.udb.technostore_market.data.repository.AuthRepository
import edu.udb.technostore_market.data.repository.CarritoRepository
import edu.udb.technostore_market.data.repository.PedidoRepository
import edu.udb.technostore_market.data.repository.PerfilRepository
import edu.udb.technostore_market.data.repository.ProductoRepository
import edu.udb.technostore_market.data.repository.SessionManager

/**
 * Contenedor de dependencias (inyección manual) creado una sola vez en la Application.
 * Única fuente de datos: Firebase (Authentication + Cloud Firestore).
 */
class AppContainer {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /** Firestore con caché persistente: catálogo disponible sin conexión y sincronización automática (RNF04). */
    private val firestore = FirebaseFirestore.getInstance().apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
    }

    val sesion = SessionManager(firebaseAuth)
    val authRepository = AuthRepository(firebaseAuth, firestore)
    val productoRepository = ProductoRepository(firestore)
    val carritoRepository = CarritoRepository(firestore, sesion)
    val pedidoRepository = PedidoRepository(firestore, sesion)
    val perfilRepository = PerfilRepository(firestore, sesion, firebaseAuth)
}
