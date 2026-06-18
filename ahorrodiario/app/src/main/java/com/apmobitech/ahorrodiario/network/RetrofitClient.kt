package com.apmobitech.ahorrodiario.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // --- MOTOR 1: RED ELÉCTRICA (Para la Luz) ---
    private val retrofitLuz = Retrofit.Builder()
        .baseUrl("https://apidatos.ree.es/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Corregido: Ahora usa ReeApi, que es el nombre real de tu archivo
    val api: ReeApi = retrofitLuz.create(ReeApi::class.java)


    // --- MOTOR 2: MINISTERIO (Para el Combustible) ---
    private val retrofitCombustible = Retrofit.Builder()
        .baseUrl("https://sedeaplicaciones.minetur.gob.es/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Este es el objeto que llamaremos desde la pantalla de la gasolina
    val fuelApi: FuelApiService = retrofitCombustible.create(FuelApiService::class.java)
}