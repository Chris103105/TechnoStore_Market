package edu.udb.technostore_market

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

                            composable("home") {
                                HomeScreen()
                            }

                        }
                    }
                }
            }
        }
    }
}