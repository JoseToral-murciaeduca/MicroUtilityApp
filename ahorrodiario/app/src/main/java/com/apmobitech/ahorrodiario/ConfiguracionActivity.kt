package com.apmobitech.ahorrodiario

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        // Habilitar la flecha de volver atrás nativa en la barra superior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val spinnerPrincipal = findViewById<Spinner>(R.id.spinnerPrincipal)
        val spinnerSubtipo = findViewById<Spinner>(R.id.spinnerSubtipo)
        val prefsGlobal = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)

        // 1. Definimos las opciones
        val tiposPrincipales = arrayOf("Diésel", "Gasolina", "Gas", "Electricidad")
        val opcionesDiesel = arrayOf("Normal", "Plus")
        val opcionesGasolina = arrayOf("95", "98")
        val opcionesGas = arrayOf("GNC", "GLP")
        val opcionesElectrico = arrayOf("Estándar")

        // 2. Configuramos el adaptador del menú principal
        val adapterPrincipal = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposPrincipales)
        spinnerPrincipal.adapter = adapterPrincipal

        val tipoGuardado = prefsGlobal.getString("vehiculo_principal", "Diésel")
        val indicePrincipal = tiposPrincipales.indexOf(tipoGuardado).takeIf { it >= 0 } ?: 0
        spinnerPrincipal.setSelection(indicePrincipal)

        // 3. Lógica dinámica de selección
        spinnerPrincipal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccionPrincipal = tiposPrincipales[position]
                prefsGlobal.edit().putString("vehiculo_principal", seleccionPrincipal).apply()

                val listaSubtipo = when (seleccionPrincipal) {
                    "Diésel" -> opcionesDiesel
                    "Gasolina" -> opcionesGasolina
                    "Gas" -> opcionesGas
                    else -> opcionesElectrico
                }

                val adapterSubtipo = ArrayAdapter(this@ConfiguracionActivity, android.R.layout.simple_spinner_dropdown_item, listaSubtipo)
                spinnerSubtipo.adapter = adapterSubtipo

                val subtipoGuardado = prefsGlobal.getString("vehiculo_subtipo", listaSubtipo[0])
                val indexSubtipo = listaSubtipo.indexOf(subtipoGuardado).takeIf { it >= 0 } ?: 0
                spinnerSubtipo.setSelection(indexSubtipo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerSubtipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val subtipoSeleccionado = spinnerSubtipo.selectedItem.toString()
                prefsGlobal.edit().putString("vehiculo_subtipo", subtipoSeleccionado).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Al pulsar la flecha atrás nativa de la barra superior, cerramos la pantalla y volvemos al menú
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}