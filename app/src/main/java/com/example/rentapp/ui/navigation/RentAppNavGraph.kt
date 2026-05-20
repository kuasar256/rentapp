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
import com.example.rentapp.ui.screens.auth.BiometricLockScreen
import com.example.rentapp.ui.screens.dashboard.DashboardScreen
import com.example.rentapp.ui.screens.payment.*
import com.example.rentapp.ui.screens.property.*
import com.example.rentapp.ui.screens.reports.AnnualReportsScreen
import com.example.rentapp.ui.screens.tenant.*
import com.example.rentapp.ui.screens.contract.*
import com.example.rentapp.ui.screens.profile.UserProfileScreen
import com.example.rentapp.ui.screens.budget.RepairBudgetListScreen
import com.example.rentapp.ui.screens.budget.AddRepairBudgetScreen
import com.example.rentapp.ui.screens.common.PhotoViewerScreen
import com.example.rentapp.viewmodel.*
import com.google.firebase.auth.FirebaseAuth

import com.example.rentapp.ui.screens.splash.SplashScreen

@Composable
fun RentAppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val syncManager = remember { com.example.rentapp.sync.FirestoreSyncManager(context) }
    
    val propertyRepo = remember { PropertyRepository(db.propertyDao(), syncManager) }
    val tenantRepo = remember { TenantRepository(db.tenantDao(), syncManager) }
    val paymentRepo = remember { PaymentRepository(db.paymentDao(), syncManager) }
    val contractRepo = remember { ContractRepository(db.contractDao(), syncManager) }
    val userRepo = remember { UserRepository(db.userDao(), syncManager) }
    val repairBudgetRepo = remember { RepairBudgetRepository(db.repairBudgetDao()) }
    val expenseRepo = remember { ExpenseRepository(db.expenseDao(), syncManager) }
    val propertyConditionRepo = remember { PropertyConditionRepository(db.propertyConditionDao()) }

    val propertyViewModel: PropertyViewModel = viewModel(factory = PropertyViewModelFactory(propertyRepo))
    val tenantViewModel: TenantViewModel = viewModel(factory = TenantViewModelFactory(tenantRepo, paymentRepo, contractRepo))
    val paymentViewModel: PaymentViewModel = viewModel(factory = PaymentViewModelFactory(paymentRepo))
    val contractViewModel: ContractViewModel = viewModel(factory = ContractViewModelFactory(contractRepo))
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepo))
    val repairBudgetViewModel: RepairBudgetViewModel = viewModel(factory = RepairBudgetViewModelFactory(repairBudgetRepo))
    val expenseViewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(expenseRepo))
    val propertyConditionViewModel: PropertyConditionViewModel = viewModel(factory = PropertyConditionViewModelFactory(propertyConditionRepo))
    val languageViewModel = remember { LanguageViewModel(context) }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToBiometric = {
                    navController.navigate(Screen.BiometricLock.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            com.example.rentapp.ui.screens.onboarding.OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.BiometricLock.route) {
            BiometricLockScreen(
                onAuthSuccess = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

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
                userViewModel = userViewModel,
                languageViewModel = languageViewModel,
                navController = navController
            )
        }

        composable(Screen.PropertyList.route) {
            PropertyListScreen(
                viewModel = propertyViewModel,
                navController = navController,
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
                onRentProperty = { pid -> navController.navigate(Screen.AddContract.createRoute(pid)) },
                onAddContractClick = { navController.navigate(Screen.AddContract.createRoute(propertyId)) },
                onContractClick = { cid -> navController.navigate(Screen.ContractDetail.createRoute(cid)) },
                onAddPaymentClick = { cid -> navController.navigate(Screen.AddPayment.createRoute(cid)) },
                onPaymentClick = { pid -> navController.navigate(Screen.PaymentDetail.createRoute(pid)) },
                onBudgetClick = { pid -> navController.navigate(Screen.RepairBudgetList.route) },
                onAddExpenseClick = { pid -> navController.navigate(Screen.AddExpense.createRoute(pid)) },
                onAddConditionClick = { pid, cid, type -> navController.navigate(Screen.AddPropertyCondition.createRoute(pid, cid, type)) },
                onViewPhotos = { uris, index -> navController.navigate(Screen.PhotoViewer.createRoute(uris, index)) }
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
            Screen.AddContract.route,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.LongType },
                navArgument("tenantId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId") ?: return@composable
            val tenantId = backStackEntry.arguments?.getLong("tenantId").takeIf { it != -1L }
            com.example.rentapp.ui.screens.contract.AddContractScreen(
                propertyId = propertyId,
                tenantId = tenantId,
                tenantViewModel = tenantViewModel,
                propertyViewModel = propertyViewModel,
                contractRepo = contractRepo,
                onBack = { navController.popBackStack() },
                onSuccess = { 
                    navController.popBackStack(Screen.PropertyDetail.createRoute(propertyId), inclusive = false)
                }
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
                navController = navController,
                onTenantClick = { id -> navController.navigate(Screen.TenantDetail.createRoute(id)) },
                onAddClick = { navController.navigate(Screen.AddTenant.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.TenantDetail.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: return@composable
            TenantDetailScreen(
                tenantId = tenantId,
                viewModel = tenantViewModel,
                userViewModel = userViewModel,
                contractRepo = contractRepo,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.EditTenant.createRoute(tenantId)) },
                onContractClick = { cid -> navController.navigate(Screen.ContractDetail.createRoute(cid)) },
                onViewPhotos = { uris, index -> navController.navigate(Screen.PhotoViewer.createRoute(uris, index)) }
            )
        }

        composable(
            Screen.AddTenant.route,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId").takeIf { it != -1L }
            AddTenantScreen(
                viewModel = tenantViewModel,
                propertyId = propertyId,
                onBack = { navController.popBackStack() },
                onSuccess = { tenantId ->
                    if (propertyId != null && tenantId != null) {
                        navController.navigate(Screen.AddContract.createRoute(propertyId, tenantId))
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            Screen.EditTenant.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: return@composable
            AddTenantScreen(
                viewModel = tenantViewModel,
                editTenantId = tenantId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentList.route) {
            PaymentListScreen(
                viewModel = paymentViewModel,
                navController = navController,
                onPaymentClick = { id -> navController.navigate(Screen.PaymentDetail.createRoute(id)) },
                onAddClick = { navController.navigate(Screen.PropertyList.route) },
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
                userViewModel = userViewModel,
                contractRepo = contractRepo,
                propertyRepo = propertyRepo,
                tenantRepo = tenantRepo,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.AddPayment.route,
            arguments = listOf(navArgument("contractId") { type = NavType.LongType })
        ) { backStackEntry ->
            val contractId = backStackEntry.arguments?.getLong("contractId") ?: return@composable
            AddPaymentScreen(
                contractId = contractId,
                paymentViewModel = paymentViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen(
                viewModel = paymentViewModel,
                navController = navController,
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
                navController = navController,
                onBack = { navController.popBackStack() },
                onPaymentClick = { id -> navController.navigate(Screen.PaymentDetail.createRoute(id)) }
            )
        }

        composable(Screen.AnnualReports.route) {
            AnnualReportsScreen(
                viewModel = paymentViewModel,
                propertyViewModel = propertyViewModel,
                contractViewModel = contractViewModel,
                repairBudgetViewModel = repairBudgetViewModel,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Screen.ContractDetail.route,
            arguments = listOf(navArgument("contractId") { type = NavType.LongType })
        ) { backStackEntry ->
            val contractId = backStackEntry.arguments?.getLong("contractId") ?: return@composable
            ContractDetailScreen(
                contractId = contractId,
                contractRepo = contractRepo,
                propertyRepo = propertyRepo,
                tenantRepo = tenantRepo,
                paymentRepo = paymentRepo,
                conditionRepo = propertyConditionRepo,
                onBack = { navController.popBackStack() },
                onAddPayment = { cid -> navController.navigate(Screen.AddPayment.createRoute(cid)) },
                onViewPayments = { },
                onAddCondition = { pid, cid, type -> navController.navigate(Screen.AddPropertyCondition.createRoute(pid, cid, type)) },
                onViewPhotos = { uris, index -> navController.navigate(Screen.PhotoViewer.createRoute(uris, index)) }
            )
        }

        composable(Screen.UserProfile.route) {
            UserProfileScreen(
                navController = navController,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RepairBudgetList.route) {
            RepairBudgetListScreen(
                viewModel = repairBudgetViewModel,
                paymentViewModel = paymentViewModel,
                onBack = { navController.popBackStack() },
                onAddBudget = { navController.navigate(Screen.AddRepairBudget.createRoute(-1L)) },
                onEditBudget = { propertyId, budgetId -> 
                    navController.navigate(Screen.AddRepairBudget.createRoute(propertyId) + "?budgetId=$budgetId")
                }
            )
        }

        composable(
            Screen.AddRepairBudget.route + "?budgetId={budgetId}",
            arguments = listOf(
                navArgument("propertyId") { type = NavType.LongType },
                navArgument("budgetId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId")?.takeIf { it != -1L }
            val budgetId = backStackEntry.arguments?.getLong("budgetId")?.takeIf { it != -1L }
            AddRepairBudgetScreen(
                viewModel = repairBudgetViewModel,
                propertyViewModel = propertyViewModel,
                propertyId = propertyId,
                editBudgetId = budgetId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            Screen.AddExpense.route,
            arguments = listOf(navArgument("propertyId") { type = NavType.LongType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId") ?: return@composable
            AddExpenseScreen(
                propertyId = propertyId,
                viewModel = expenseViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                viewModel = expenseViewModel,
                navController = navController,
                onBack = { navController.popBackStack() },
                onAddExpense = { 
                    // Si queremos agregar un gasto general, podemos necesitar una propiedad por defecto o dejar que el usuario elija.
                    // Por ahora, asumimos que se navega desde una propiedad. Si se navega desde la lista general,
                    // podríamos redirigir a la lista de propiedades para seleccionar una.
                    navController.navigate(Screen.PropertyList.route)
                }
            )
        }

        composable(
            Screen.AddPropertyCondition.route,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.LongType },
                navArgument("contractId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("type") { type = NavType.StringType; defaultValue = "CHECK_IN" }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId") ?: return@composable
            val contractId = backStackEntry.arguments?.getLong("contractId").takeIf { it != -1L }
            val type = backStackEntry.arguments?.getString("type") ?: "CHECK_IN"
            AddPropertyConditionScreen(
                propertyId = propertyId,
                contractId = contractId,
                initialType = type,
                viewModel = propertyConditionViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
                onViewPhotos = { uris, index ->
                    navController.navigate(Screen.PhotoViewer.createRoute(uris, index))
                }
            )
        }

        composable(
            Screen.PhotoViewer.route,
            arguments = listOf(
                navArgument("uris") { type = NavType.StringType },
                navArgument("initialIndex") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val urisEncoded = backStackEntry.arguments?.getString("uris") ?: ""
            val uris = java.net.URLDecoder.decode(urisEncoded, "UTF-8").split(",").filter { it.isNotBlank() }
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
            
            PhotoViewerScreen(
                uris = uris,
                initialIndex = initialIndex,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
