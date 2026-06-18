package com.apmobitech.ahorrodiario.network

import retrofit2.http.GET

interface FuelApiService {
    // Endpoint público para bajarse todas las gasolineras de España en tiempo real
    @GET("ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/")
    suspend fun getGasolineras(): FuelResponse
}