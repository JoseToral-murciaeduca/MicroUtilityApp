package com.apmobitech.ahorrodiario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configuración del tema (Debe ir antes de pintar la interfaz)
        val prefsGlobal = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        val temaGuardado = prefsGlobal.getInt("modo_oscuro", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(temaGuardado)

        // 2. Vinculación del diseño XML
        setContentView(R.layout.activity_menu)

        // 3. Inicialización de anuncios (Ejecutado tras cargar el contenedor)
        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.adViewMenu)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // 4. Declaración de las vistas y flujos de navegación
        val cardLuz = findViewById<CardView>(R.id.cardLuz)
        val cardCombustible = findViewById<CardView>(R.id.cardCombustible)
        val cardGas = findViewById<CardView>(R.id.cardGas)
        val cardAjustes = findViewById<CardView>(R.id.cardAjustes)

        cardLuz.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        cardCombustible.setOnClickListener {
            startActivity(Intent(this, CombustibleActivity::class.java))
        }

        cardGas.setOnClickListener {
            startActivity(Intent(this, GasActivity::class.java))
        }

        cardAjustes.setOnClickListener {
            startActivity(Intent(this, ConfiguracionActivity::class.java))
        }
    }
}