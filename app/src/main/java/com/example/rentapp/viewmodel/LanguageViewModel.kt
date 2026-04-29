package com.example.rentapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentapp.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LanguageViewModel(private val context: Context) : ViewModel() {
    
    val currentLanguage: StateFlow<String> = PreferencesManager.getLanguageFlow(context)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "es")
    
    val currentCurrency: StateFlow<String> = PreferencesManager.getCurrencyFlow(context)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")
    
    fun setLanguage(language: String) = viewModelScope.launch {
        PreferencesManager.setLanguage(context, language)
    }
    
    fun setCurrency(currency: String) = viewModelScope.launch {
        PreferencesManager.setCurrency(context, currency)
    }
}
