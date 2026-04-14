package com.example.rentapp.network

import com.example.rentapp.network.api.CountriesApi
import com.example.rentapp.network.api.ExchangeRateApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val COUNTRIES_BASE_URL = "https://restcountries.com/"
    private const val EXCHANGE_RATE_BASE_URL = "https://api.exchangerate-api.com/"

    val countriesApi: CountriesApi by lazy {
        Retrofit.Builder()
            .baseUrl(COUNTRIES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountriesApi::class.java)
    }

    val exchangeRateApi: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl(EXCHANGE_RATE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}
