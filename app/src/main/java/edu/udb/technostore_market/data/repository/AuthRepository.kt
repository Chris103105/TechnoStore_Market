package edu.udb.technostore_market.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.udb.technostore_market.data.firebase.FirestoreRutas
import edu.udb.technostore_market.util.llamadaSegura
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Autenticación con Firebase Authentication (correo/contraseña y Google).
 * Al registrarse se crea también el documento `usuarios/{uid}` en Firestore.
 */
class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    fun haySesionActiva(): Boolean = auth.currentUser != null

    suspend fun iniciarSesion(correo: String, password: String): Result<Unit> = llamadaSegura {
        withContext(Dispatchers.IO) {
            auth.signInWithEmailAndPassword(correo.trim(), password).await()
        }
        Unit
    }

    suspend fun registrar(nombre: String, correo: String, password: String): Result<Unit> = llamadaSegura {
        withContext(Dispatchers.IO) {
            val resultado = auth.createUserWithEmailAndPassword(correo.trim(), password).await()
            val cambios = UserProfileChangeRequest.Builder().setDisplayName(nombre.trim()).build()
            resultado.user?.updateProfile(cambios)?.await()
            resultado.user?.let { crearDocumentoUsuario(it, nombre.trim()) }
        }
        Unit
    }

    suspend fun recuperarPassword(correo: String): Result<Unit> = llamadaSegura {
        withContext(Dispatchers.IO) { auth.sendPasswordResetEmail(correo.trim()).await() }
        Unit
    }

    suspend fun iniciarSesionConGoogle(idToken: String): Result<Unit> = llamadaSegura {
        withContext(Dispatchers.IO) {
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            val resultado = auth.signInWithCredential(credencial).await()
            if (resultado.additionalUserInfo?.isNewUser == true) {
                resultado.user?.let { crearDocumentoUsuario(it, it.displayName.orEmpty()) }
            }
        }
        Unit
    }

    fun cerrarSesion() = auth.signOut()

    /**
     * Crea `usuarios/{uid}`. Si falla (p. ej. reglas de Firestore), la cuenta ya existe en Auth,
     * así que no se bloquea el ingreso: el documento se completa al guardar el perfil.
     */
    private suspend fun crearDocumentoUsuario(usuario: FirebaseUser, nombre: String) {
        try {
            val datos = mapOf(
                FirestoreRutas.UID to usuario.uid,
                FirestoreRutas.NOMBRE to nombre,
                FirestoreRutas.EMAIL to usuario.email.orEmpty(),
                FirestoreRutas.TELEFONO to "",
                FirestoreRutas.DIRECCION to "",
                FirestoreRutas.FECHA_REGISTRO to FieldValue.serverTimestamp()
            )
            db.collection(FirestoreRutas.USUARIOS).document(usuario.uid)
                .set(datos, SetOptions.merge())
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("AuthRepository", "No se pudo crear el documento del usuario", e)
        }
    }
}
