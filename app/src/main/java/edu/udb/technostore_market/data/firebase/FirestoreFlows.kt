package edu.udb.technostore_market.data.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/*
 * Convierte los listeners en tiempo real de Firestore (addSnapshotListener) en Flow,
 * para que los ViewModels los observen igual que antes observaban los Flow de Room.
 * El listener se elimina automáticamente cuando el ViewModel deja de recolectar.
 */

fun Query.observar(incluirMetadatos: Boolean = false): Flow<QuerySnapshot> = callbackFlow {
    val cambios = if (incluirMetadatos) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
    val registro = addSnapshotListener(cambios) { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registro.remove() }
}

fun DocumentReference.observar(incluirMetadatos: Boolean = false): Flow<DocumentSnapshot> = callbackFlow {
    val cambios = if (incluirMetadatos) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
    val registro = addSnapshotListener(cambios) { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) trySend(snapshot)
    }
    awaitClose { registro.remove() }
}
