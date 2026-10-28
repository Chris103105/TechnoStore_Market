package edu.udb.technostore_market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.udb.technostore_market.model.productosDePrueba
import edu.udb.technostore_market.ui.HomeScreen
import edu.udb.technostore_market.ui.LoginScreen
import edu.udb.technostore_market.ui.theme.TechnoStore_MarketTheme
import edu.udb.technostore_market.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechnoStore_MarketTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

                        val authViewModel: AuthViewModel = viewModel()
                        val navController = rememberNavController() // Controlador de rutas

                        // Definimos el mapa de navegación
                        NavHost(navController = navController, startDestination = "login") {

                            composable("login") {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onLoginExitoso = {
                                        // Navega al home y destruye la pantalla de login del historial
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // AQUÍ ESTÁ EL CAMBIO: Ahora HomeScreen recibe la función para navegar
                            composable("home") {
                                HomeScreen(
                                    onProductoClick = { producto ->
                                        // Navegamos a la ruta "detalle" y le pasamos el ID del producto
                                        navController.navigate("detalle/${producto.id}")
                                    }
                                )
                            }

                            // NUEVA RUTA TEMPORAL PARA EL DETALLE
                            composable("detalle/{productoId}") { backStackEntry ->
                                val productoId = backStackEntry.arguments?.getString("productoId")?.toIntOrNull()
                                val producto = productosDePrueba.find { it.id == productoId }

                                if (producto != null) {
                                    // Esto es solo un texto temporal para que no falle la app
                                    // cuando hagas clic en una tarjeta.
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Detalle del producto: ${producto.nombre}")
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}