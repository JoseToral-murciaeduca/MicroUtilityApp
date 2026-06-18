package com.apmobitech.ahorrodiario.network

import com.google.gson.annotations.SerializedName

data class FuelResponse(
    @SerializedName("Fecha") val fecha: String?,
    @SerializedName("ListaEESSPrecio") val listaGasolineras: List<GasolineraJson>?
)

data class GasolineraJson(
    @SerializedName("Rótulo") val rotulo: String?,
    @SerializedName("Dirección") val direccion: String?,
    @SerializedName("Municipio") val municipio: String?,
    @SerializedName("Localidad") val localidad: String?,
    @SerializedName("Precio Gasolina 95 E5") val precioGasolina95: String?,
    @SerializedName("Precio Gasoleo A") val precioDiesel: String?,
    @SerializedName("IDEESS") val id: String?,
    @SerializedName("Latitud") val latitud: String?,
    @SerializedName("Longitud (WGS84)") val longitud: String?
)