package com.example.rentapp.viewmodel

import androidx.lifecycle.*
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.data.repository.PaymentRepository
import com.example.rentapp.data.repository.ContractRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar
import kotlinx.coroutines.launch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

data class TenantDisplayModel(
    val tenant: Tenant,
    val paymentStatus: String? = null // "PAID", "PENDING", "DELAYED" or null if no payment info
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class TenantViewModel(
    private val repository: TenantRepository,
    private val paymentRepository: PaymentRepository,
    private val contractRepository: ContractRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow("ALL") // "ALL", "ACTIVE", "INACTIVE", "DELAYED"
    val statusFilter: StateFlow<String> = _statusFilter

    private val _countries = MutableStateFlow<List<com.example.rentapp.network.model.CountryResponse>>(emptyList())
    val countries: StateFlow<List<com.example.rentapp.network.model.CountryResponse>> = _countries

    init {
        viewModelScope.launch {
            try {
                val fetchedCountries = com.example.rentapp.network.RetrofitInstance.countriesApi.getAllCountries()
                _countries.value = fetchedCountries.sortedBy { it.name.common }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentTime = System.currentTimeMillis()
    private val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    // Current month payments for status calculation
    private val currentMonthPayments = paymentRepository.getPaymentsByYearMonth(currentYear, currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All delayed or pending payments to check for overdue across all months
    private val outstandingPayments = paymentRepository.getPendingAndDelayedPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All active contracts to link tenants with payments
    private val allContracts = contractRepository.getActiveContracts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val tenantDisplayList: StateFlow<List<TenantDisplayModel>> = combine(
        combine(_searchQuery.debounce(300L), _statusFilter) { query, filter ->
            Pair(query, filter)
        }.flatMapLatest { (query, filter) ->
            when {
                query.isNotBlank() -> repository.searchTenants(query)
                filter == "ACTIVE" -> repository.getTenantsByStatus("ACTIVE")
                filter == "INACTIVE" -> repository.getTenantsByStatus("INACTIVE")
                else -> repository.getAllTenants()
            }
        },
        currentMonthPayments,
        outstandingPayments,
        allContracts,
        _statusFilter
    ) { tenants: List<Tenant>, currentPayments: List<Payment>, allOutstanding: List<Payment>, contracts: List<Contract>, filter: String ->
        val list = tenants.map { tenant ->
            val tenantContract = contracts.find { it.tenantId == tenant.id }
            
            // Check for overdue payments
            val tenantOutstanding = allOutstanding.filter { it.contractId == tenantContract?.id }
            var hasOverdue = tenantOutstanding.any { 
                it.status == "DELAYED" || (it.status == "PENDING" && it.dueDate < currentTime)
            }

            // Proactive Check: If no payment record exists for months since contract start, it's delayed
            if (!hasOverdue && tenantContract != null) {
                val calendar = Calendar.getInstance()
                val nowYear = calendar.get(Calendar.YEAR)
                val nowMonth = calendar.get(Calendar.MONTH) + 1
                val nowDay = calendar.get(Calendar.DAY_OF_MONTH)

                val contractStart = Calendar.getInstance().apply { timeInMillis = tenantContract.startDate }
                val startYear = contractStart.get(Calendar.YEAR)
                val startMonth = contractStart.get(Calendar.MONTH) + 1

                // Iterate through months from start until now
                var checkYear = startYear
                var checkMonth = startMonth
                
                while (checkYear < nowYear || (checkYear == nowYear && checkMonth <= nowMonth)) {
                    // Check if a payment exists for this specific month/year
                    val paymentExists = allOutstanding.any { it.contractId == tenantContract.id && it.year == checkYear && it.month == checkMonth && it.status == "PAID" } ||
                                       currentPayments.any { it.contractId == tenantContract.id && it.year == checkYear && it.month == checkMonth && it.status == "PAID" }
                    
                    if (!paymentExists) {
                        // If it's the current month, only mark delayed if the due day has passed
                        if (checkYear == nowYear && checkMonth == nowMonth) {
                            if (nowDay > tenantContract.paymentDueDay) {
                                hasOverdue = true
                                break
                            }
                        } else {
                            // For any past month, if no PAID record, it's overdue
                            hasOverdue = true
                            break
                        }
                    }

                    checkMonth++
                    if (checkMonth > 12) {
                        checkMonth = 1
                        checkYear++
                    }
                }
            }

            val currentPayment = if (tenantContract != null) {
                currentPayments.find { it.contractId == tenantContract.id }
            } else null
            
            val status = when {
                hasOverdue -> "DELAYED"
                currentPayment?.status == "PAID" -> "PAID"
                currentPayment?.status == "DELAYED" -> "DELAYED"
                currentPayment?.status == "PENDING" && (tenantContract?.paymentDueDay ?: 5) < currentDay -> "DELAYED"
                currentPayment == null && tenantContract != null && tenantContract.paymentDueDay < currentDay -> "DELAYED"
                tenantContract != null -> "PENDING"
                else -> null
            }
            
            TenantDisplayModel(
                tenant = tenant,
                paymentStatus = status
            )
        }

        if (filter == "DELAYED") {
            list.filter { it.paymentStatus == "DELAYED" }
        } else if (filter == "PAID") {
            list.filter { it.paymentStatus == "PAID" }
        } else {
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount = repository.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedTenant = MutableLiveData<Tenant?>()
    val selectedTenant: LiveData<Tenant?> = _selectedTenant

    fun selectTenant(tenant: Tenant) { _selectedTenant.value = tenant }
    fun clearSelectedTenant() { _selectedTenant.value = null }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setStatusFilter(filter: String) { _statusFilter.value = filter }

    fun insertTenant(tenant: Tenant) = viewModelScope.launch {
        repository.insertTenant(tenant)
    }

    suspend fun insertTenantAndGetId(tenant: Tenant): Long {
        return repository.insertTenant(tenant)
    }

    fun updateTenant(tenant: Tenant) = viewModelScope.launch {
        repository.updateTenant(tenant)
    }

    fun deleteTenant(tenant: Tenant) = viewModelScope.launch {
        repository.deleteTenant(tenant)
    }

    suspend fun getTenantById(id: Long): Tenant? {
        return repository.getTenantById(id)
    }

    fun getTenantDisplayFlow(tenantId: Long): Flow<TenantDisplayModel?> {
        return combine(
            flow { emit(repository.getTenantById(tenantId)) },
            currentMonthPayments,
            outstandingPayments,
            allContracts
        ) { tenant, currentPayments, allOutstanding, contracts ->
            if (tenant == null) return@combine null

            val tenantContract = contracts.find { it.tenantId == tenant.id }
            val tenantOutstanding = allOutstanding.filter { it.contractId == tenantContract?.id }
            val hasOverdue = tenantOutstanding.any { 
                it.status == "DELAYED" || (it.status == "PENDING" && it.dueDate < currentTime)
            }
            val currentPayment = if (tenantContract != null) {
                currentPayments.find { it.contractId == tenantContract.id }
            } else null
            
            val status = when {
                hasOverdue -> "DELAYED"
                currentPayment?.status == "PAID" -> "PAID"
                currentPayment?.status == "DELAYED" -> "DELAYED"
                currentPayment?.status == "PENDING" && (tenantContract?.paymentDueDay ?: 5) < currentDay -> "DELAYED"
                currentPayment == null && tenantContract != null && tenantContract.paymentDueDay < currentDay -> "DELAYED"
                tenantContract != null -> "PENDING"
                else -> null
            }
            
            TenantDisplayModel(tenant, status)
        }
    }
}

class TenantViewModelFactory(
    private val repository: TenantRepository,
    private val paymentRepository: PaymentRepository,
    private val contractRepository: ContractRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TenantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TenantViewModel(repository, paymentRepository, contractRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
