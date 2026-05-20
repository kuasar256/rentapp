package com.example.rentapp.viewmodel

import androidx.lifecycle.*
import com.example.rentapp.data.local.entity.PropertyCondition
import com.example.rentapp.data.repository.PropertyConditionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyConditionViewModel(private val repository: PropertyConditionRepository) : ViewModel() {

    private val _propertyId = MutableStateFlow<Long?>(null)
    val propertyConditions: StateFlow<List<PropertyCondition>> = _propertyId
        .filterNotNull()
        .flatMapLatest { id -> repository.getConditionsByProperty(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _contractId = MutableStateFlow<Long?>(null)
    val contractConditions: StateFlow<List<PropertyCondition>> = _contractId
        .filterNotNull()
        .flatMapLatest { id -> repository.getConditionsByContract(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadConditionsForProperty(propertyId: Long) {
        _propertyId.value = propertyId
    }

    fun loadConditionsForContract(contractId: Long) {
        _contractId.value = contractId
    }

    suspend fun getConditionById(id: Long): PropertyCondition? =
        repository.getConditionById(id)

    fun insertCondition(condition: PropertyCondition) = viewModelScope.launch {
        repository.insertCondition(condition)
    }

    fun updateCondition(condition: PropertyCondition) = viewModelScope.launch {
        repository.updateCondition(condition)
    }

    fun deleteCondition(condition: PropertyCondition) = viewModelScope.launch {
        repository.deleteCondition(condition)
    }
}

class PropertyConditionViewModelFactory(private val repository: PropertyConditionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyConditionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PropertyConditionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
