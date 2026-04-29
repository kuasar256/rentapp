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
}
