package com.example.rentapp.network.model

data class CountryResponse(
    val name: CountryName,
    val cca2: String,
    val flags: CountryFlags,
    val capital: List<String>? = null
)

data class CountryName(
    val common: String,
    val official: String
)

data class CountryFlags(
    val png: String,
    val svg: String
)
