package edu.udb.technostore_market.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.GoogleAuthProvider


// Estados de la pantalla

// Una 'sealed class' es como un menú cerrado de opciones.
// Le dice a la UI: "Solo puedes estar en uno de estos 4 estados, nada más".
// Esto evita errores donde la pantalla está cargando y mostrando un error a la vez.
sealed class AuthState {
    object Idle : AuthState()                       // Reposo: No pasa nada.
    object Loading : AuthState()                    // Cargando: Muestra la ruedita.
    object Success : AuthState()                    // Éxito
    data class Error(val message: String) : AuthState() // Error: Muestra un mensaje en texto.
}

class AuthViewModel : ViewModel() {

    // Instancia principal para hablar con los servidores de Firebase
    private val auth = FirebaseAuth.getInstance()


    // Comunicación con la UI

    // _authState Privada: El ViewModel la usa para modificar el estado internamente.
    // authState Pública: La Pantalla (LoginScreen) la recibe (collectAsState), pero no puede modificarla.

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState



    // Funciones de Correo y Contraseña


    //  Iniciar Sesión
    fun login(correo: String, pass: String) {
        // Validación rápida local
        if (correo.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Ingresa tu correo y contraseña")
            return
        }

        //  Avisamos a la UI que muestre la ruedita de carga
        _authState.value = AuthState.Loading

        // Petición a Firebase
        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    // Si falla, extraemos el mensaje de error de Firebase o ponemos uno genérico
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Error al iniciar sesión")
                }
            }
    }

    //Crear Cuenta Nueva
    fun registrar(nombre: String, correo: String, pass: String, confirmPass: String) {
        //  Validaciones locales
        if (nombre.isBlank() || correo.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Completa todos los campos")
            return
        }
        if (pass != confirmPass) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden")
            return
        }

        _authState.value = AuthState.Loading

        //  Petición a Firebase para crear la cuenta
        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Firebase crea la cuenta solo con correo y contraseña.
                    // Para guardar el "Nombre", debemos actualizar el perfil del usuario recién creado.
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    user?.updateProfile(profileUpdates)

                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Error al registrar")
                }
            }
    }

    //  Recuperar Contraseña
    fun recuperarPassword(correo: String) {
        if (correo.isBlank()) {
            _authState.value = AuthState.Error("Ingresa tu correo para recuperar la contraseña")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //  Aquí usamos el estado 'Error' temporalmente
                    // para mostrar el mensaje rojo en pantalla sin tener que crear un estado nuevo
                    // de "Notificación".
                    _authState.value = AuthState.Error("Correo de recuperación enviado")
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Error al enviar correo")
                }
            }
    }



    //  Iniciar sesion con Google


    // Firebase valida el Id que la UI extrajo.
    fun loginConGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        //  Empaquetamos el token de Google en una credencial que Firebase entienda
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        //  Le damos la credencial a Firebase. Si el sello coincide , nos deja pasar.
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Error con Google")
                }
            }
    }



    // Funciones de ayuda para manejo de errores

    // Atrapa los errores que suceden en la ventana emergente de Google antes de llegar a Firebase
    fun setError(mensaje: String) {
        _authState.value = AuthState.Error(mensaje)
    }

    // Devuelve la pantalla a su estado normal (borra mensajes de error, por ejemplo,
    // cuando el usuario cambia entre la vista de Login y Registro)
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}