package com.apmobitech.ahorrodiario

enum class TipoGas { REGULADO_TUR, LIBRE }

data class CompaniaGas(
    val nombre: String,
    val tipo: TipoGas,
    val urlLogo: String,
    val terminoFijoMensual: Double, // Precio base al mes
    val terminoVariableKwh: Double  // Precio por consumo
)

object DirectorioGas {
    // Precios orientativos. Los TUR se actualizan cada 3 meses.
    val lista = listOf(
        CompaniaGas(
            nombre = "Curenergía (TUR 1 - Cocina/Agua)",
            tipo = TipoGas.REGULADO_TUR,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_curenergia.png",
            terminoFijoMensual = 4.26,
            terminoVariableKwh = 0.043
        ),
        CompaniaGas(
            nombre = "Curenergía (TUR 2 - Calefacción)",
            tipo = TipoGas.REGULADO_TUR,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_curenergia.png",
            terminoFijoMensual = 8.02,
            terminoVariableKwh = 0.040
        ),
        CompaniaGas(
            nombre = "Naturgy (Tarifa Por Uso)",
            tipo = TipoGas.LIBRE,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_naturgy.png",
            terminoFijoMensual = 5.50,
            terminoVariableKwh = 0.060
        ),
        CompaniaGas(
            nombre = "Endesa (Única)",
            tipo = TipoGas.LIBRE,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_endesa.png",
            terminoFijoMensual = 6.00,
            terminoVariableKwh = 0.055
        )
    )
}