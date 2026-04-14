package com.example.rentapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.data.repository.*
import com.example.rentapp.ui.screens.auth.LoginScreen
import com.example.rentapp.ui.screens.auth.RegisterScreen
import com.example.rentapp.ui.screens.dashboard.DashboardScreen
import com.example.rentapp.ui.screens.payment.*
import com.example.rentapp.ui.screens.property.*
import com.example.rentapp.ui.screens.reports.AnnualReportsScreen
import com.example.rentapp.ui.screens.tenant.*
import com.example.rentapp.ui.screens.profile.UserProfileScreen
import com.example.rentapp.viewmodel.*

@Composable
fun RentAppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val propertyRepo = remember { PropertyRepository(db.propertyDao()) }
    val tenantRepo = remember { TenantRepository(db.tenantDao()) }
    val paymentRepo = remember { PaymentRepository(db.paymentDao()) }
    val contractRepo = remember { ContractRepository(db.contractDao()) }

    val propertyViewModel: PropertyViewModel = viewModel(factory = PropertyViewModelFactory(propertyRepo))
    val tenantViewModel: TenantViewModel = viewModel(factory = TenantViewModelFactory(tenantRepo))
    val paymentViewModel: PaymentViewModel = viewModel(factory = PaymentViewModelFactory(paymentRepo))

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }},
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }},
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                propertyViewModel = propertyViewModel,
                paymentViewModel = paymentViewModel,
                tenantViewModel = tenantViewModel,
                navController = navController
            )
        }

        composable(Screen.PropertyList.route) {
            PropertyListScreen(
                viewModel = propertyViewModel,
                onPropertyClick = { id -> navController.navigate(Screen.PropertyDetail.createRoute(id)) },
                onAddClick = { navController.navigate(Screen.AddProperty.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.PropertyDetail.route,
            arguments = listOf(navArgument("propertyId") { type = NavType.LongType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId") ?: return@composable
            PropertyDetailScreen(
                propertyId = propertyId,
                viewModel = propertyViewModel,
                contractRepo = contractRepo,
                tenantRepo = tenantRepo,
                paymentRepo = paymentRepo,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.EditProperty.createRoute(propertyId)) },
                onPaymentClick = { pid -> navController.navigate(Screen.PaymentDetail.createRoute(pid)) }
            )
        }

        composable(Screen.AddProperty.route) {
            AddPropertyScreen(
                viewModel = propertyViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            Screen.EditProperty.route,
            arguments = listOf(navArgument("propertyId") { type = NavType.LongType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId") ?: return@composable
            AddPropertyScreen(
                viewModel = propertyViewModel,
                editPropertyId = propertyId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.TenantList.route) {
            TenantListScreen(
                viewModel = tenantViewModel,
                onTenantClick = {},
                onAddClick = { navController.navigate(Screen.AddTenant.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddTenant.route) {
            AddTenantScreen(
                viewModel = tenantViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentList.route) {
            PaymentListScreen(
                viewModel = paymentViewModel,
                onPaymentClick = { id -> navController.navigate(Screen.PaymentDetail.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.PaymentDetail.route,
            arguments = listOf(navArgument("paymentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val paymentId = backStackEntry.arguments?.getLong("paymentId") ?: return@composable
            PaymentDetailScreen(
                paymentId = paymentId,
                viewModel = paymentViewModel,
                contractRepo = contractRepo,
                propertyRepo = propertyRepo,
                tenantRepo = tenantRepo,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                viewModel = paymentViewModel,
                onBack = { navController.popBackStack() },
                onPaymentClick = { id -> navController.navigate(Screen.PaymentDetail.createRoute(id)) }
            )
        }

        composable(Screen.DelinquencyAlerts.route) {
            DelinquencyAlertsScreen(
                viewModel = paymentViewModel,
                propertyRepo = propertyRepo,
                contractRepo = contractRepo,
                tenantRepo = tenantRepo,
                onBack = { navController.popBackStack() },
                onPaymentClick = { id -> navController.navigate(Screen.PaymentDetail.createRoute(id)) }
            )
        }

        composable(Screen.AnnualReports.route) {
            AnnualReportsScreen(
                viewModel = paymentViewModel,
                propertyViewModel = propertyViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
