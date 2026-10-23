package edu.udb.technostore_market.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import edu.udb.technostore_market.R
import edu.udb.technostore_market.viewmodel.AuthState
import edu.udb.technostore_market.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel, onLoginExitoso: () -> Unit) {


    // Gestion de estados ( controlan lo que se ve en pantalla)

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // "remember" guarda los valores dentro de cada campo

    var isLoginView by remember { mutableStateOf(true) } // true = Login, false = Registro
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }



    // Servicios externos  (Google Sign-In)

    val webClientId = stringResource(id = R.string.default_web_client_id)
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    //  Escucha la respuesta de la ventana de Google cuando se cierra
    val googleAuthLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                viewModel.loginConGoogle(idToken) // Enviamos el pase VIP a Firebase
            }
        } catch (e: ApiException) {
            viewModel.setError("Error Google: Código ${e.statusCode}")
        }
    }


    // Diseño de Interfaz de usuario


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp), // Un padding un poco más amplio se ve más limpio
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            //  Encabezado
            Text(
                text = if (isLoginView) "Iniciar Sesión" else "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Formulario de Entrada
            if (!isLoginView) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (!isLoginView) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            //  Cargas y Errores
            Spacer(modifier = Modifier.height(8.dp))
            when (authState) {
                is AuthState.Loading -> CircularProgressIndicator()
                is AuthState.Success -> {
                    Text("¡Operación exitosa!", color = MaterialTheme.colorScheme.primary)
                    // LaunchedEffect ejecuta acciones secundarias como navegar sin bloquear la UI
                    LaunchedEffect(Unit) {
                        onLoginExitoso()
                    }
                }
                is AuthState.Error -> Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                else -> {} // Estado inicial  no mostramos nada
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Acción
            Button(
                onClick = {
                    if (isLoginView) viewModel.login(email, password)
                    else viewModel.registrar(nombre, email, password, confirmPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading // Se desactiva si está cargando
            ) {
                Text(if (isLoginView) "Ingresar" else "Registrarme")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "— o —", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    // Al cerrar sesión antes de lanzar el intent, forzamos a Google
                    // a que siempre despliegue la ventana de selección de cuenta.
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleAuthLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                Text("Continuar con Google")
            }

            //  Enlaces de navegación interna
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    isLoginView = !isLoginView
                    viewModel.resetState() // Limpiamos errores al cambiar de vista
                }
            ) {
                Text(if (isLoginView) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Inicia sesión")
            }

            if (isLoginView) {
                TextButton(onClick = { viewModel.recuperarPassword(email) }) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }
        }
    }
}