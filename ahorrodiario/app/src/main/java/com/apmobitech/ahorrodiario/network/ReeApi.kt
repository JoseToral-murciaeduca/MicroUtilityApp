package com.apmobitech.ahorrodiario.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ReeApi {
    // Esta es la ruta oficial de Red Eléctrica para el PVPC
    @GET("es/datos/mercados/precios-mercados-tiempo-real")
    suspend fun getPreciosLuz(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("time_trunc") timeTrunc: String = "hour"
    ): ReeResponse
}