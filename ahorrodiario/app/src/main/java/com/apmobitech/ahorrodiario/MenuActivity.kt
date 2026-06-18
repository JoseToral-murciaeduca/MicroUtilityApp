package com.apmobitech.ahorrodiario

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.apmobitech.ahorrodiario.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Leemos el tema guardado y lo aplicamos antes de dibujar la pantalla
        val prefsGlobal = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        val temaGuardado = prefsGlobal.getInt("modo_oscuro", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(temaGuardado)

        setContentView(R.layout.activity_menu)

        val cardLuz = findViewById<CardView>(R.id.cardLuz)
        val cardCombustible = findViewById<CardView>(R.id.cardCombustible)
        val cardGas = findViewById<CardView>(R.id.cardGas)
        val cardAjustes = findViewById<CardView>(R.id.cardAjustes)

        // Asignamos las navegaciones
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

    // El método onResume se dispara cada vez que el menú vuelve a estar visible en pantalla
    override fun onResume() {
        super.onResume()
        actualizarDashboard()
    }

    private fun actualizarDashboard() {
        val tvLuz = findViewById<TextView>(R.id.tvResumenLuz)
        val tvGas = findViewById<TextView>(R.id.tvResumenGas)
        val tvGasolina = findViewById<TextView>(R.id.tvResumenGasolina)

        // ==========================================
        // 1. RESUMEN DE LUZ
        // ==========================================
        val prefsLuz = getSharedPreferences("MisTarifasElectricas", Context.MODE_PRIVATE)
        // Buscamos la compañía que guardaste en MainActivity, por defecto cogemos la primera de la lista
        val nombreLuz = prefsLuz.getString("compania_seleccionada_actual", DirectorioCompanias.lista[0].nombre)
        val companiaLuz = DirectorioCompanias.lista.find { it.nombre == nombreLuz }

        if (companiaLuz?.tipo == TipoMercado.REGULADO_PVPC) {
            tvLuz.text = "Cargando PVPC actual..."
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val hoyStr = sdf.format(Date())
                    val start = "${hoyStr}T00:00"
                    val end = "${hoyStr}T23:59"

                    val respuesta = RetrofitClient.api.getPreciosLuz(startDate = start, endDate = end)
                    val precios = respuesta.included.firstOrNull()?.attributes?.values

                    val horaActualStr = SimpleDateFormat("HH", Locale.getDefault()).format(Date())

                    val precioActual = precios?.find { dato ->
                        try {
                            dato.datetime.substring(11, 13) == horaActualStr
                        } catch (e: Exception) { false }
                    }?.price

                    withContext(Dispatchers.Main) {
                        if (precioActual != null) {
                            val precioKwh = precioActual / 1000.0
                            tvLuz.text = "${companiaLuz.nombre}: ${String.format(Locale.getDefault(), "%.4f", precioKwh)} €/kWh"
                        } else {
                            tvLuz.text = "Mercado Regulado (Sin datos para esta hora)"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { tvLuz.text = "Mercado Regulado (Error de red)" }
                }
            }
        } else if (companiaLuz != null) {
            val precioEjemplo = companiaLuz.tarifaFijaKwh?.toFloat() ?: 0f
            val precioGuardado = prefsLuz.getFloat(companiaLuz.nombre, precioEjemplo)
            tvLuz.text = "${companiaLuz.nombre}: ${String.format(Locale.getDefault(), "%.4f", precioGuardado)} €/kWh"
        }

        // ==========================================
        // 2. RESUMEN DE GAS
        // ==========================================
        val prefsGas = getSharedPreferences("MisTarifasGas", Context.MODE_PRIVATE)
        val nombreGas = prefsGas.getString("compania_gas_seleccionada", DirectorioGas.lista[0].nombre)
        val companiaGas = DirectorioGas.lista.find { it.nombre == nombreGas }

        if (companiaGas != null) {
            val varGuardado = prefsGas.getFloat("${companiaGas.nombre}_var", companiaGas.terminoVariableKwh.toFloat())
            tvGas.text = "${companiaGas.nombre}: ${String.format(Locale.getDefault(), "%.4f", varGuardado)} €/kWh"
        }

        // ==========================================
        // 3. RESUMEN DE GASOLINA
        // ==========================================
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            tvGasolina.text = "Buscando la estación más barata..."

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var miUbicacion: Location? = null
            try {
                val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                miUbicacion = gpsLoc ?: netLoc
            } catch (e: SecurityException) {
                Log.e("Dashboard", "Error de GPS", e)
            }

            val prefsGlobales = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
            val radioKm = prefsGlobales.getFloat("radio_km", 100f).toDouble()

            // Trampa del emulador
            if (miUbicacion == null || miUbicacion.longitude < -100.0) {
                miUbicacion = Location("Simulado").apply { latitude = 38.1824; longitude = -1.1246 }
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val respuesta = RetrofitClient.fuelApi.getGasolineras()
                    val listaSegura = respuesta.listaGasolineras ?: emptyList()
                    var precioMasBarato = Double.MAX_VALUE
                    var nombreGasolinera = ""

                    for (g in listaSegura) {
                        val latGas = g.latitud?.replace(",", ".")?.replace(" ", "")?.toDoubleOrNull()
                        val lonGas = g.longitud?.replace(",", ".")?.replace(" ", "")?.toDoubleOrNull()
                        val precio = g.precioDiesel?.replace(",", ".")?.replace(" ", "")?.toDoubleOrNull()

                        if (latGas != null && lonGas != null && precio != null) {
                            val distancia = calcularDistanciaHaversine(miUbicacion.latitude, miUbicacion.longitude, latGas, lonGas)
                            if (distancia <= radioKm && precio < precioMasBarato) {
                                precioMasBarato = precio
                                nombreGasolinera = g.rotulo ?: "Desconocida"
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (precioMasBarato != Double.MAX_VALUE) {
                            tvGasolina.text = "🏆 $nombreGasolinera (${String.format(Locale.getDefault(), "%.3f", precioMasBarato)} €/L)"
                        } else {
                            tvGasolina.text = "No se encontraron datos en tu zona."
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { tvGasolina.text = "Pulsa en Combustible para actualizar" }
                }
            }
        } else {
            tvGasolina.text = "Permiso de ubicación requerido. Entra en Combustible."
        }
    }

    // Fórmula matemática para distancias en el mapa
    private fun calcularDistanciaHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierraKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return radioTierraKm * c
    }
}