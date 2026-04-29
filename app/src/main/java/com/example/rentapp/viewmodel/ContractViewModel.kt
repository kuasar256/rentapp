package com.example.rentapp.viewmodel

import androidx.lifecycle.*
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.repository.ContractRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContractViewModel(private val repository: ContractRepository) : ViewModel() {

    val allContracts = repository.getAllContracts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeContracts = repository.getActiveContracts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertContract(contract: Contract) = viewModelScope.launch {
        repository.insertContract(contract)
    }

    fun updateContract(contract: Contract) = viewModelScope.launch {
        repository.updateContract(contract)
    }

    fun deleteContract(contract: Contract) = viewModelScope.launch {
        repository.deleteContract(contract)
    }

    fun getContractsByTenant(tenantId: Long) = repository.getContractsByTenant(tenantId)
}

class ContractViewModelFactory(private val repository: ContractRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContractViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContractViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
