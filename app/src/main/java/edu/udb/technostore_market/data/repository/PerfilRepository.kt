package edu.udb.technostore_market.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.udb.technostore_market.data.firebase.FirestoreRutas
import edu.udb.technostore_market.data.firebase.observar
import edu.udb.technostore_market.model.PerfilUsuario
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
 * Perfil del usuario guardado en el documento `usuarios/{uid}` de Firestore.
 * El nombre también se actualiza en Firebase Auth (displayName).
 */
class PerfilRepository(
    private val db: FirebaseFirestore,
    private val sesion: SessionManager,
    private val auth: FirebaseAuth
) {

    private fun usuario(uid: String) = db.collection(FirestoreRutas.USUARIOS).document(uid)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarPerfil(): Flow<PerfilUsuario?> =
        sesion.userIdFlow.flatMapLatest { uid ->
            if (uid == null) flowOf(null)
            else usuario(uid).observar()
                .map { documento ->
                    PerfilUsuario(
                        userId = uid,
                        nombre = documento.getString(FirestoreRutas.NOMBRE)
                            ?.takeIf { it.isNotBlank() } ?: sesion.nombreActual,
                        correo = documento.getString(FirestoreRutas.EMAIL)
                            ?.takeIf { it.isNotBlank() } ?: sesion.correoActual,
                        telefono = documento.getString(FirestoreRutas.TELEFONO).orEmpty(),
                        direccion = documento.getString(FirestoreRutas.DIRECCION).orEmpty()
                    )
                }
                .catch { e ->
                    Log.w(TAG, "No se pudo observar el perfil", e)
                    // Si Firestore falla, al menos se muestran los datos de Firebase Auth.
                    emit(PerfilUsuario(uid, sesion.nombreActual, sesion.correoActual, "", ""))
                }
        }

    suspend fun guardarPerfil(nombre: String, telefono: String, direccion: String): Result<Unit> =
        llamadaSegura {
            withContext<Unit>(Dispatchers.IO) {
                val uid = sesion.requerirUserId()
                Validadores.validarNombre(nombre)?.let { throw IllegalArgumentException(it) }
                Validadores.validarTelefono(telefono)?.let { throw IllegalArgumentException(it) }
                Validadores.validarDireccion(direccion)?.let { throw IllegalArgumentException(it) }

                val datos = mapOf(
                    FirestoreRutas.UID to uid,
                    FirestoreRutas.NOMBRE to nombre.trim(),
                    FirestoreRutas.EMAIL to sesion.correoActual,
                    FirestoreRutas.TELEFONO to telefono.replace(" ", "").replace("-", ""),
                    FirestoreRutas.DIRECCION to direccion.trim()
                )
                // merge: no borra fechaRegistro ni otros campos existentes.
                usuario(uid).set(datos, SetOptions.merge()).await()

                val cambios = UserProfileChangeRequest.Builder().setDisplayName(nombre.trim()).build()
                auth.currentUser?.updateProfile(cambios)?.await()
            }
        }

    private companion object {
        const val TAG = "PerfilRepository"
    }
}
