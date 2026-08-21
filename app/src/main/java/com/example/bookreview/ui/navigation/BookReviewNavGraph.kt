package com.example.bookreview.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookreview.ui.detail.DetalleScreen
import com.example.bookreview.ui.reviews.MisResenasScreen
import com.example.bookreview.ui.search.BusquedaScreen
import com.example.bookreview.ui.settings.AjustesScreen

private data class ItemBarraInferior(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val itemsBarraInferior = listOf(
    ItemBarraInferior(Screen.Busqueda, "Búsqueda", Icons.Default.Search),
    ItemBarraInferior(Screen.MisResenas, "Mis Reseñas", Icons.Default.Favorite),
    ItemBarraInferior(Screen.Ajustes, "Ajustes", Icons.Default.Settings)
)

@Composable
fun BookReviewNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Detalle no es un destino de la barra inferior: se llega a él
            // navegando desde Búsqueda o Mis Reseñas, así que se oculta ahí,
            // igual que en CineMatch.
            if (currentRoute != Screen.Detalle.route) {
                NavigationBar {
                    itemsBarraInferior.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = { navController.navigateSingleTopTo(item.screen.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Busqueda.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Busqueda.route) {
                BusquedaScreen(
                    onLibroClick = { libroId ->
                        navController.navigate(Screen.Detalle.createRoute(libroId))
                    }
                )
            }
            composable(Screen.MisResenas.route) {
                MisResenasScreen(
                    onResenaClick = { libroId ->
                        navController.navigate(Screen.Detalle.createRoute(libroId))
                    }
                )
            }
            composable(Screen.Ajustes.route) {
                AjustesScreen()
            }
            composable(
                route = Screen.Detalle.route,
                arguments = listOf(navArgument(ARG_LIBRO_ID) { type = NavType.StringType })
            ) {
                DetalleScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Evita apilar copias del mismo destino al tocar dos veces el mismo ítem
 *  de la barra inferior, y conserva el estado de scroll/formulario de cada
 *  pestaña al volver a ella (patrón estándar de Navigation Compose). */
private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
