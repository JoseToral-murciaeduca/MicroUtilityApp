package com.apmobitech.ahorrodiario

// Diferenciamos el motor que usará la app para calcular el precio
enum class TipoMercado {
    REGULADO_PVPC, // Usará la API de Red Eléctrica
    LIBRE          // Usará el precio fijo que le pongamos
}

// El molde exacto de cada compañía
data class CompaniaElectrica(
    val nombre: String,
    val tipo: TipoMercado,
    val urlLogo: String,
    val tarifaFijaKwh: Double? = null // Solo se rellena en el mercado libre
)