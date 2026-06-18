package com.apmobitech.ahorrodiario

object DirectorioCompanias {
    // Esta es la lista maestra que alimentará tu desplegable
    val lista = listOf(
        // ==========================================
        // COMERCIALIZADORAS DE MERCADO REGULADO (COR)
        // ==========================================
        CompaniaElectrica(
            nombre = "Energía XXI (Endesa Regulada)",
            tipo = TipoMercado.REGULADO_PVPC,
            urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_energiaxxi.png",
            tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "Curenergía (Iberdrola Regulada)",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_curenergia.png",
        tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "Comercializadora Regulada, Gas & Power (Naturgy Regulada)",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_gasandpower.png",
        tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "Baser COR (TotalEnergies Regulada)",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_baser.png",
        tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "Régsiti (Grupo Repsol)",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_regsiti.png",
        tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "CHC COR (CHC Energía Regulada)",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_chccor.png",
        tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "Teramelcor (Melilla Regulada)",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_teramelcor.png",
        tarifaFijaKwh = null
        ),
        CompaniaElectrica(
        nombre = "Energía Ceuta XXI",
        tipo = TipoMercado.REGULADO_PVPC,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_ceutaxxi.png",
        tarifaFijaKwh = null
        ),

        // ==========================================
        // TOP 50 COMERCIALIZADORAS DE MERCADO LIBRE
        // ==========================================
        CompaniaElectrica(
        nombre = "Octopus Energy",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_octopus.png",
        tarifaFijaKwh = 0.1180
        ),
        CompaniaElectrica(
        nombre = "Iberdrola Clientes",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_iberdrola.png",
        tarifaFijaKwh = 0.1250
        ),
        CompaniaElectrica(
        nombre = "Endesa Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_endesa.png",
        tarifaFijaKwh = 0.1150
        ),
        CompaniaElectrica(
        nombre = "Naturgy Clientes",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_naturgy.png",
        tarifaFijaKwh = 0.1300
        ),
        CompaniaElectrica(
        nombre = "Repsol",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_repsol.png",
        tarifaFijaKwh = 0.1290
        ),
        CompaniaElectrica(
        nombre = "TotalEnergies",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_totalenergies.png",
        tarifaFijaKwh = 0.1190
        ),
        CompaniaElectrica(
        nombre = "Gana Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_ganaenergia.png",
        tarifaFijaKwh = 0.1100
        ),
        CompaniaElectrica(
        nombre = "HolaLuz",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_holaluz.png",
        tarifaFijaKwh = 0.1400
        ),
        CompaniaElectrica(
        nombre = "Pepeenergy",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_pepeenergy.png",
        tarifaFijaKwh = 0.1210
        ),
        CompaniaElectrica(
        nombre = "Lucera",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_lucera.png",
        tarifaFijaKwh = 0.1240
        ),
        CompaniaElectrica(
        nombre = "Fenie Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_fenieenergia.png",
        tarifaFijaKwh = 0.1350
        ),
        CompaniaElectrica(
        nombre = "Som Energia",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_somenergia.png",
        tarifaFijaKwh = 0.1320
        ),
        CompaniaElectrica(
        nombre = "Podo",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_podo.png",
        tarifaFijaKwh = 0.1280
        ),
        CompaniaElectrica(
        nombre = "Factor Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_factorenergia.png",
        tarifaFijaKwh = 0.1260
        ),
        CompaniaElectrica(
        nombre = "Plenitude",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_plenitude.png",
        tarifaFijaKwh = 0.1220
        ),
        CompaniaElectrica(
        nombre = "Audax Renovables",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_audax.png",
        tarifaFijaKwh = 0.1340
        ),
        CompaniaElectrica(
        nombre = "CHC Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_chcenergia.png",
        tarifaFijaKwh = 0.1270
        ),
        CompaniaElectrica(
        nombre = "Imagina Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_imaginaenergia.png",
        tarifaFijaKwh = 0.1190
        ),
        CompaniaElectrica(
        nombre = "Gesternova",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_gesternova.png",
        tarifaFijaKwh = 0.1310
        ),
        CompaniaElectrica(
        nombre = "Próxima Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_proximaenergia.png",
        tarifaFijaKwh = 0.1200
        ),
        CompaniaElectrica(
        nombre = "Unieéctrica",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_unielectrica.png",
        tarifaFijaKwh = 0.1295
        ),
        CompaniaElectrica(
        nombre = "Alterna",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_alterna.png",
        tarifaFijaKwh = 0.1330
        ),
        CompaniaElectrica(
        nombre = "Sweno",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_sweno.png",
        tarifaFijaKwh = 0.1245
        ),
        CompaniaElectrica(
        nombre = "Novaluz",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_novaluz.png",
        tarifaFijaKwh = 0.1360
        ),
        CompaniaElectrica(
        nombre = "Wekiwi",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_wekiwi.png",
        tarifaFijaKwh = 0.1170
        ),
        CompaniaElectrica(
        nombre = "Goiener",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_goiener.png",
        tarifaFijaKwh = 0.1325
        ),
        CompaniaElectrica(
        nombre = "Escandinava de Electricidad",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_escandinava.png",
        tarifaFijaKwh = 0.1230
        ),
        CompaniaElectrica(
        nombre = "Nexus Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_nexus.png",
        tarifaFijaKwh = 0.1290
        ),
        CompaniaElectrica(
        nombre = "Axpo Ibérica",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_axpo.png",
        tarifaFijaKwh = 0.1355
        ),
        CompaniaElectrica(
        nombre = "Alpiq",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_alpiq.png",
        tarifaFijaKwh = 0.1315
        ),
        CompaniaElectrica(
        nombre = "Engie España",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_engie.png",
        tarifaFijaKwh = 0.1265
        ),
        CompaniaElectrica(
        nombre = "Fortuluz",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_fortuluz.png",
        tarifaFijaKwh = 0.1370
        ),
        CompaniaElectrica(
        nombre = "Integra Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_integraenergia.png",
        tarifaFijaKwh = 0.1285
        ),
        CompaniaElectrica(
        nombre = "Énercoop",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_enercoop.png",
        tarifaFijaKwh = 0.1335
        ),
        CompaniaElectrica(
        nombre = "Electra Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_electraenergia.png",
        tarifaFijaKwh = 0.1345
        ),
        CompaniaElectrica(
        nombre = "SunAir One",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_sunairone.png",
        tarifaFijaKwh = 0.1380
        ),
        CompaniaElectrica(
        nombre = "Oppidum Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_oppidum.png",
        tarifaFijaKwh = 0.1310
        ),
        CompaniaElectrica(
        nombre = "Alcanzia",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_alcanzia.png",
        tarifaFijaKwh = 0.1290
        ),
        CompaniaElectrica(
        nombre = "Helios Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_heliosenergia.png",
        tarifaFijaKwh = 0.1240
        ),
        CompaniaElectrica(
        nombre = "Nufri Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_nufri.png",
        tarifaFijaKwh = 0.1275
        ),
        CompaniaElectrica(
        nombre = "Candela Energía",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_candelaenergia.png",
        tarifaFijaKwh = 0.1299
        ),
        CompaniaElectrica(
        nombre = "Bonpreu Esclat Energia",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_bonpreu.png",
        tarifaFijaKwh = 0.1260
        ),
        CompaniaElectrica(
        nombre = "Catgas",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_catgas.png",
        tarifaFijaKwh = 0.1310
        ),
        CompaniaElectrica(
        nombre = "MIWenergia",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_miwenergia.png",
        tarifaFijaKwh = 0.1280
        ),
        CompaniaElectrica(
        nombre = "Next Energia",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_nextenergia.png",
        tarifaFijaKwh = 0.1255
        ),
        CompaniaElectrica(
        nombre = "Enercoluz",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_enercoluz.png",
        tarifaFijaKwh = 0.1235
        ),
        CompaniaElectrica(
        nombre = "Bassols Energia",
        tipo = TipoMercado.LIBRE,
        urlLogo = "https://raw.githubusercontent.com/JoseToral-murciaeduca/MicroUtilityApp/main/Assets/logo_bassols.png",
        tarifaFijaKwh = 0.1305
        )
    )
}