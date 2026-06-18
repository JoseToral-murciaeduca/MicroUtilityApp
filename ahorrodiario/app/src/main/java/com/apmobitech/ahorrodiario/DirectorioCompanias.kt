package com.apmobitech.ahorrodiario

object DirectorioCompanias {
    // Esta es la lista maestra que alimentará tu desplegable
    val lista = listOf(
        // --- MERCADO REGULADO (Usan la API del Gobierno) ---
        CompaniaElectrica(
            nombre = "Red Eléctrica (PVPC)",
            tipo = TipoMercado.REGULADO_PVPC,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_ree.png"
        ),
        CompaniaElectrica(
            nombre = "Energía XXI",
            tipo = TipoMercado.REGULADO_PVPC,
            // Nota: Tendrás que subir este logo a tu carpeta de GitHub igual que hiciste con el de REE
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_energiaxxi.png"
        ),

        // --- MERCADO LIBRE (Tarifas fijas de ejemplo) ---
        CompaniaElectrica(
            nombre = "Octopus Energy",
            tipo = TipoMercado.LIBRE,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_octopus.png",
            tarifaFijaKwh = 0.118 // Precio de ejemplo en euros
        ),
        CompaniaElectrica(
            nombre = "Repsol",
            tipo = TipoMercado.LIBRE,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_repsol.png",
            tarifaFijaKwh = 0.135
        )
    )
}