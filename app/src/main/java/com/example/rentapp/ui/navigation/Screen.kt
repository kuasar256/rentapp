package com.example.rentapp.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object BiometricLock : Screen("biometric_lock")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object PropertyList : Screen("property_list")
    object PropertyDetail : Screen("property_detail/{propertyId}") {
        fun createRoute(propertyId: Long) = "property_detail/$propertyId"
    }
    object AddProperty : Screen("add_property")
    object EditProperty : Screen("edit_property/{propertyId}") {
        fun createRoute(propertyId: Long) = "edit_property/$propertyId"
    }
    object TenantList : Screen("tenant_list")
    object TenantDetail : Screen("tenant_detail/{tenantId}") {
        fun createRoute(tenantId: Long) = "tenant_detail/$tenantId"
    }
    object AddTenant : Screen("add_tenant?propertyId={propertyId}") {
        fun createRoute(propertyId: Long? = null) = if (propertyId != null) "add_tenant?propertyId=$propertyId" else "add_tenant"
    }
    object EditTenant : Screen("edit_tenant/{tenantId}") {
        fun createRoute(tenantId: Long) = "edit_tenant/$tenantId"
    }
    object AddContract : Screen("add_contract/{propertyId}?tenantId={tenantId}") {
        fun createRoute(propertyId: Long, tenantId: Long? = null) = 
            if (tenantId != null) "add_contract/$propertyId?tenantId=$tenantId" 
            else "add_contract/$propertyId"
    }
    object ContractDetail : Screen("contract_detail/{contractId}") {
        fun createRoute(contractId: Long) = "contract_detail/$contractId"
    }
    object PaymentList : Screen("payment_list")
    object PaymentDetail : Screen("payment_detail/{paymentId}") {
        fun createRoute(paymentId: Long) = "payment_detail/$paymentId"
    }
    object AddPayment : Screen("add_payment/{contractId}") {
        fun createRoute(contractId: Long) = "add_payment/$contractId"
    }
    object PaymentHistory : Screen("payment_history")
    object DelinquencyAlerts : Screen("delinquency_alerts")
    object AnnualReports : Screen("annual_reports")
    object UserProfile : Screen("user_profile")
    object Onboarding : Screen("onboarding")
    object RepairBudgetList : Screen("repair_budget_list")
    object AddRepairBudget : Screen("add_repair_budget/{propertyId}") {
        fun createRoute(propertyId: Long) = "add_repair_budget/$propertyId"
    }
    object ExpenseList : Screen("expense_list")
    object AddExpense : Screen("add_expense/{propertyId}") {
        fun createRoute(propertyId: Long) = "add_expense/$propertyId"
    }
    object AddPropertyCondition : Screen("add_property_condition/{propertyId}?contractId={contractId}&type={type}") {
        fun createRoute(propertyId: Long, contractId: Long? = null, type: String = "CHECK_IN") =
            "add_property_condition/$propertyId?contractId=${contractId ?: -1L}&type=$type"
    }
    object PhotoViewer : Screen("photo_viewer?uris={uris}&initialIndex={initialIndex}") {
        fun createRoute(uris: List<String>, initialIndex: Int = 0): String {
            val urisEncoded = java.net.URLEncoder.encode(uris.joinToString(","), "UTF-8")
            return "photo_viewer?uris=$urisEncoded&initialIndex=$initialIndex"
        }
    }
}
