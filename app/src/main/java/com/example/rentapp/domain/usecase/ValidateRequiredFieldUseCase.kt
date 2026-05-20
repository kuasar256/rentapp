package com.example.rentapp.domain.usecase

import android.content.Context
import com.example.rentapp.R

/**
 * Use case to standardize the validation of required fields across the app.
 * Used during the presentation to demonstrate clean architecture and multi-language support.
 */
class ValidateRequiredFieldUseCase(private val context: Context) {
    
    operator fun invoke(value: String): String? {
        return if (value.isBlank()) {
            context.getString(R.string.error_field_required)
        } else {
            null
        }
    }
}
