package com.apmobitech.ahorrodiario.network

import com.google.gson.annotations.SerializedName

// La respuesta principal de la API
data class ReeResponse(
    @SerializedName("included") val included: List<ReeIncluded>
)

// Filtramos la sección que contiene los datos
data class ReeIncluded(
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: ReeAttributes
)

// Accedemos a la lista de valores
data class ReeAttributes(
    @SerializedName("values") val values: List<ReeValue>
)

// El dato final que nos interesa: Precio y Hora
data class ReeValue(
    @SerializedName("value") val price: Double,
    @SerializedName("datetime") val datetime: String
)