package com.apmobitech.ahorrodiario.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // La dirección principal del servidor del Gobierno
    private const val BASE_URL = "https://apidatos.ree.es/"

    val api: ReeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReeApi::class.java)
    }
}