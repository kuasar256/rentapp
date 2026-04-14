package com.example.rentapp.viewmodel

import androidx.lifecycle.*
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.TenantRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TenantViewModel(private val repository: TenantRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

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

    val allTenants = repository.getAllTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTenants = repository.getTenantsByStatus("ACTIVE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<Tenant>> = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllTenants()
            else repository.searchTenants(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCount = repository.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedTenant = MutableLiveData<Tenant?>()
    val selectedTenant: LiveData<Tenant?> = _selectedTenant

    fun selectTenant(tenant: Tenant) { _selectedTenant.value = tenant }
    fun clearSelectedTenant() { _selectedTenant.value = null }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun insertTenant(tenant: Tenant) = viewModelScope.launch {
        repository.insertTenant(tenant)
    }

    fun updateTenant(tenant: Tenant) = viewModelScope.launch {
        repository.updateTenant(tenant)
    }

    fun deleteTenant(tenant: Tenant) = viewModelScope.launch {
        repository.deleteTenant(tenant)
    }
}

class TenantViewModelFactory(private val repository: TenantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TenantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TenantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
