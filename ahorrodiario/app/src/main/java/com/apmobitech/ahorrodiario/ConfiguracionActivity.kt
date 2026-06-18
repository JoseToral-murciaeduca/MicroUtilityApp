package com.apmobitech.ahorrodiario

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        val btnVolver = findViewById<ImageButton>(R.id.btnVolverConfig)
        val etRadioKm = findViewById<EditText>(R.id.etRadioKm)
        val etDepositoL = findViewById<EditText>(R.id.etDepositoL)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarConfig)
        val btnAbrirAjustes = findViewById<Button>(R.id.btnAbrirAjustesUbicacion)

        val rgTema = findViewById<RadioGroup>(R.id.rgTema)
        val rbTemaSistema = findViewById<RadioButton>(R.id.rbTemaSistema)
        val rbTemaClaro = findViewById<RadioButton>(R.id.rbTemaClaro)
        val rbTemaOscuro = findViewById<RadioButton>(R.id.rbTemaOscuro)

        // --- 1. LEER LOS DATOS ACTUALES ---
        val prefs = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        val radioActual = prefs.getFloat("radio_km", 100f)
        val depositoActual = prefs.getFloat("deposito_l", 50f)
        val temaActual = prefs.getInt("modo_oscuro", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Pintar Combustible
        etRadioKm.setText(radioActual.toString())
        etDepositoL.setText(depositoActual.toString())

        // Pintar Selección de Tema
        when (temaActual) {
            AppCompatDelegate.MODE_NIGHT_NO -> rbTemaClaro.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> rbTemaOscuro.isChecked = true
            else -> rbTemaSistema.isChecked = true
        }

        // --- 2. ACCIONES DE BOTONES ---
        btnVolver.setOnClickListener { finish() }

        // Botón de ayuda para el GPS (Abre la ficha técnica de tu app en los ajustes del móvil)
        btnAbrirAjustes.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        // Botón Guardar Maestro
        btnGuardar.setOnClickListener {
            val textoRadio = etRadioKm.text.toString()
            val textoDeposito = etDepositoL.text.toString()

            if (textoRadio.isNotEmpty() && textoDeposito.isNotEmpty()) {
                try {
                    val nuevoRadio = textoRadio.toFloat()
                    val nuevoDeposito = textoDeposito.toFloat()

                    // Averiguar qué tema ha elegido
                    val nuevoTema = when (rgTema.checkedRadioButtonId) {
                        R.id.rbTemaClaro -> AppCompatDelegate.MODE_NIGHT_NO
                        R.id.rbTemaOscuro -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }

                    // Guardar en memoria
                    prefs.edit()
                        .putFloat("radio_km", nuevoRadio)
                        .putFloat("deposito_l", nuevoDeposito)
                        .putInt("modo_oscuro", nuevoTema)
                        .apply()

                    // Aplicar el tema visual al instante en toda la app
                    AppCompatDelegate.setDefaultNightMode(nuevoTema)

                    Toast.makeText(this, "Ajustes aplicados correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error de formato. Usa punto para decimales.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Los campos de combustible no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}