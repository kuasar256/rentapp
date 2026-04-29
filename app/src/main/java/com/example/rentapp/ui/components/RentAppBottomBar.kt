package com.example.rentapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.data.repository.UserRepository
import com.example.rentapp.sync.FirestoreSyncManager
import com.example.rentapp.ui.navigation.Screen
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.UserViewModel
import com.example.rentapp.viewmodel.UserViewModelFactory

@Composable
fun RentAppBottomBar(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val syncManager = remember { FirestoreSyncManager(context) }
    val userRepo = remember { UserRepository(db.userDao(), syncManager) }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepo))
    
    val userState by userViewModel.user.collectAsState()

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    
    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                // saveState = true // Comentado si causa problemas de acceso
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    NavigationBar(containerColor = SurfaceContainer) {
        NavigationBarItem(
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { onNavigate(Screen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
        NavigationBarItem(
                selected = currentRoute == Screen.PropertyList.route,
                onClick = { onNavigate(Screen.PropertyList.route) },
                icon = { Icon(Icons.Default.Domain, contentDescription = null) },
                label = { Text("Propiedades") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnPrimaryFixed,
                    indicatorColor = Primary,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                    selectedTextColor = Primary
                )
            )
            NavigationBarItem(
                selected = currentRoute == Screen.TenantList.route,
                onClick = { onNavigate(Screen.TenantList.route) },
                icon = { Icon(Icons.Default.People, contentDescription = null) },
                label = { Text("Inquilinos") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnPrimaryFixed,
                    indicatorColor = Primary,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                    selectedTextColor = Primary
                )
            )
        
        NavigationBarItem(
            selected = currentRoute == Screen.PaymentList.route,
            onClick = { onNavigate(Screen.PaymentList.route) },
            icon = { Icon(Icons.Default.Payments, contentDescription = null) },
            label = { Text("Pagos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
        
        NavigationBarItem(
                selected = currentRoute == Screen.AnnualReports.route,
                onClick = { onNavigate(Screen.AnnualReports.route) },
                icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                label = { Text("Reportes") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnPrimaryFixed,
                    indicatorColor = Primary,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                    selectedTextColor = Primary
                )
            )
    }
}
