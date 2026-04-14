package com.example.rentapp.viewmodel

import androidx.lifecycle.*
import android.util.Log
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.repository.PropertyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PropertyViewModel(private val repository: PropertyRepository) : ViewModel() {

    private val _exchangeRates = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Double>>(emptyMap())
    val exchangeRates: kotlinx.coroutines.flow.StateFlow<Map<String, Double>> = _exchangeRates

    init {
        viewModelScope.launch {
            Log.d("RentAppDebug", "PropertyViewModel: Fetching exchange rates...")
            try {
                val rates = com.example.rentapp.network.RetrofitInstance.exchangeRateApi.getLatestRates("USD")
                if (rates != null && rates.conversion_rates != null) {
                    _exchangeRates.value = rates.conversion_rates
                    Log.d("RentAppDebug", "PropertyViewModel: Rates updated: ${rates.conversion_rates.size} items")
                } else {
                    Log.w("RentAppDebug", "PropertyViewModel: API returned null rates")
                }
            } catch (e: Exception) {
                Log.e("RentAppDebug", "PropertyViewModel: Error fetching rates: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    val allProperties = repository.getAllProperties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableProperties = repository.getPropertiesByStatus("AVAILABLE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rentedProperties = repository.getPropertiesByStatus("RENTED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCount = repository.getAvailableCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rentedCount = repository.getRentedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMonthlyRevenue = repository.getTotalMonthlyRevenue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCount = repository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedProperty = MutableLiveData<Property?>()
    val selectedProperty: LiveData<Property?> = _selectedProperty

    fun selectProperty(property: Property) { _selectedProperty.value = property }
    fun clearSelectedProperty() { _selectedProperty.value = null }

    fun insertProperty(property: Property) = viewModelScope.launch {
        repository.insertProperty(property)
    }

    fun updateProperty(property: Property) = viewModelScope.launch {
        repository.updateProperty(property)
    }

    fun deleteProperty(property: Property) = viewModelScope.launch {
        repository.deleteProperty(property)
    }
}

class PropertyViewModelFactory(private val repository: PropertyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PropertyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
