package com.example.rentapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentapp.data.local.entity.RepairBudget
import com.example.rentapp.data.repository.RepairBudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RepairBudgetViewModel(private val repository: RepairBudgetRepository) : ViewModel() {

    val allBudgets: StateFlow<List<RepairBudget>> = repository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getBudgetsByProperty(propertyId: Long) = repository.getBudgetsByProperty(propertyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBudget(budget: RepairBudget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun updateBudget(budget: RepairBudget) = viewModelScope.launch {
        repository.updateBudget(budget)
    }

    fun deleteBudget(budget: RepairBudget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }
}

class RepairBudgetViewModelFactory(private val repository: RepairBudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepairBudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepairBudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
