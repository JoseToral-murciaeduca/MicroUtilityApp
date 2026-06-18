package com.apmobitech.ahorrodiario

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Leemos el tema guardado y lo aplicamos antes de dibujar la pantalla
        val prefs = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        val temaGuardado = prefs.getInt("modo_oscuro", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(temaGuardado)
        setContentView(R.layout.activity_menu)

        val cardLuz = findViewById<CardView>(R.id.cardLuz)
        val cardCombustible = findViewById<CardView>(R.id.cardCombustible)
        val cardGas = findViewById<CardView>(R.id.cardGas)
        val cardAjustes = findViewById<CardView>(R.id.cardAjustes)

        // Al pulsar Electricidad, abrimos tu pantalla actual (MainActivity)
        cardLuz.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        cardCombustible.setOnClickListener {
            val intent = Intent(this, CombustibleActivity::class.java)
            startActivity(intent)
        }

        cardGas.setOnClickListener {
            val intent = Intent(this, GasActivity::class.java)
            startActivity(intent)
        }

        cardAjustes.setOnClickListener {
            val intent = Intent(this, ConfiguracionActivity::class.java)
            startActivity(intent)
        }
    }
}