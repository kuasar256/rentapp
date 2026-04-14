package com.example.rentapp.network.api

import com.example.rentapp.network.model.CountryResponse
import retrofit2.http.GET

interface CountriesApi {
    @GET("v3.1/all")
    suspend fun getAllCountries(): List<CountryResponse>
}
