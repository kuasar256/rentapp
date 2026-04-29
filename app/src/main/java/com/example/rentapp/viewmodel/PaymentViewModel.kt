package com.example.rentapp.viewmodel

import androidx.lifecycle.*
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.repository.PaymentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class PaymentViewModel(private val repository: PaymentRepository) : ViewModel() {

    val allPayments = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingPayments = repository.getPaymentsByStatus("PENDING")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val delayedPayments = repository.getDelayedPayments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val paidPayments = repository.getPaymentsByStatus("PAID")
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingAndDelayed = repository.getPendingAndDelayedPayments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val delayedCount = repository.getDelayedCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val pendingCount = repository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear

    val paymentsByYear: StateFlow<List<Payment>> = _selectedYear
        .flatMapLatest { year -> repository.getPaymentsByYear(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCollectedByYear: StateFlow<Double> = _selectedYear
        .flatMapLatest { year -> repository.getTotalCollectedByYear(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyEarningsByYear: StateFlow<List<com.example.rentapp.data.local.dao.MonthlyEarning>> = _selectedYear
        .flatMapLatest { year -> repository.getMonthlyEarningsByYear(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paidCountByYear: StateFlow<Int> = _selectedYear
        .flatMapLatest { year -> repository.getPaidCountByYear(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingCountByYear: StateFlow<Int> = _selectedYear
        .flatMapLatest { year -> repository.getPendingCountByYear(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val delayedCountByYear: StateFlow<Int> = _selectedYear
        .flatMapLatest { year -> repository.getDelayedCountByYear(year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedPayment = MutableLiveData<Payment?>()
    val selectedPayment: LiveData<Payment?> = _selectedPayment

    fun selectPayment(payment: Payment) { _selectedPayment.value = payment }
    fun clearSelectedPayment() { _selectedPayment.value = null }
    fun setSelectedYear(year: Int) { _selectedYear.value = year }

    fun insertPayment(payment: Payment) = viewModelScope.launch {
        repository.insertPayment(payment)
    }

    fun updatePayment(payment: Payment) = viewModelScope.launch {
        repository.updatePayment(payment)
    }

    fun markAsPaid(payment: Payment) = viewModelScope.launch {
        val updated = payment.copy(
            status = "PAID",
            paidDate = System.currentTimeMillis()
        )
        repository.updatePayment(updated)
    }

    fun markAsDelayed(payment: Payment) = viewModelScope.launch {
        val updated = payment.copy(status = "DELAYED")
        repository.updatePayment(updated)
    }

    fun deletePayment(payment: Payment) = viewModelScope.launch {
        repository.deletePayment(payment)
    }

    fun clearPaidHistory() = viewModelScope.launch {
        paidPayments.value.forEach {
            repository.deletePayment(it)
        }
    }

    fun getPaymentsByContract(contractId: Long): Flow<List<Payment>> =
        repository.getPaymentsByContract(contractId)
}

class PaymentViewModelFactory(private val repository: PaymentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
