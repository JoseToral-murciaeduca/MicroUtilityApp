package com.apmobitech.ahorrodiario

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.apmobitech.ahorrodiario.network.GasolineraJson
import com.apmobitech.ahorrodiario.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CombustibleActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_LOCATION = 1001

    // Variables dinámicas que leeremos de la Configuración
    private var radioBusquedaKm = 100.0
    private var capacidadDeposito = 50.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combustible)

        // LEEMOS LOS AJUSTES GLOBALES DEL USUARIO
        val prefs = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        radioBusquedaKm = prefs.getFloat("radio_km", 100f).toDouble()
        capacidadDeposito = prefs.getFloat("deposito_l", 50f).toDouble()

        val btnVolver = findViewById<ImageButton>(R.id.btnVolver)
        btnVolver.setOnClickListener { finish() }

        verificarPermisosYComenzar()
    }

    private fun verificarPermisosYComenzar() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_LOCATION
            )
        } else {
            obtenerUbicacionYPrecios()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionYPrecios()
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación", Toast.LENGTH_LONG).show()
                findViewById<TextView>(R.id.tvCosteLlenado).text = "-- €"
            }
        }
    }

    private fun obtenerUbicacionYPrecios() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var miUbicacion: Location? = null

        try {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            miUbicacion = gpsLocation ?: networkLocation
        } catch (e: SecurityException) {
            Log.e("Combustible", "Error de seguridad de ubicación", e)
        }

        if (miUbicacion == null || miUbicacion.longitude < -100.0) {
            miUbicacion = Location("Simulado").apply {
                latitude = 38.1824
                longitude = -1.1246
            }
            Toast.makeText(this, "Emulador detectado. Usando ubicación de Murcia.", Toast.LENGTH_SHORT).show()
        }

        descargarDatosMinisterio(miUbicacion)
    }

    private fun descargarDatosMinisterio(ubicacionUsuario: Location) {
        val tvCosteLlenado = findViewById<TextView>(R.id.tvCosteLlenado)
        val tvGasolineraBarata = findViewById<TextView>(R.id.tvGasolineraBarata)
        val lvGasolineras = findViewById<ListView>(R.id.lvGasolineras)

        // Actualizamos dinámicamente el texto estático de la pantalla
        val textoTituloDeposito = "Llenar depósito (${capacidadDeposito.toInt()}L)"
        findViewById<TextView>(R.id.tvCosteLlenado).let { // Trampa para acceder al textview del titulo
            val layout = it.parent as android.widget.LinearLayout
            val titulo = layout.getChildAt(0) as TextView
            titulo.text = textoTituloDeposito
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = RetrofitClient.fuelApi.getGasolineras()
                val listaSegura = respuesta.listaGasolineras ?: emptyList()

                val gasolinerasFiltradas = listaSegura.filter { gasolinera ->
                    val latLimpiada = gasolinera.latitud?.replace(",", ".")?.replace(" ", "")
                    val lonLimpiada = gasolinera.longitud?.replace(",", ".")?.replace(" ", "")

                    val latGas = latLimpiada?.toDoubleOrNull()
                    val lonGas = lonLimpiada?.toDoubleOrNull()

                    if (latGas != null && lonGas != null) {
                        val distancia = calcularDistanciaHaversine(
                            ubicacionUsuario.latitude, ubicacionUsuario.longitude,
                            latGas, lonGas
                        )
                        distancia <= radioBusquedaKm
                    } else {
                        false
                    }
                }

                var precioDieselMasBarato = Double.MAX_VALUE
                var nombreBarata = ""

                for (g in gasolinerasFiltradas) {
                    val precioLimpio = g.precioDiesel?.replace(",", ".")?.replace(" ", "")
                    if (!precioLimpio.isNullOrEmpty()) {
                        val precioNum = precioLimpio.toDoubleOrNull()
                        if (precioNum != null && precioNum < precioDieselMasBarato) {
                            precioDieselMasBarato = precioNum
                            nombreBarata = g.rotulo ?: "Desconocida"
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    val latStr = String.format(Locale.getDefault(), "%.2f", ubicacionUsuario.latitude)
                    val lonStr = String.format(Locale.getDefault(), "%.2f", ubicacionUsuario.longitude)

                    if (precioDieselMasBarato != Double.MAX_VALUE) {
                        val costeTotal = precioDieselMasBarato * capacidadDeposito
                        tvCosteLlenado.text = String.format(Locale.getDefault(), "%.2f €", costeTotal)
                        tvGasolineraBarata.text = "🏆 $nombreBarata (${String.format(Locale.getDefault(), "%.3f", precioDieselMasBarato)} €/L)\nBuscando desde GPS: [$latStr, $lonStr]"
                    } else {
                        tvCosteLlenado.text = "-- €"
                        tvGasolineraBarata.text = "Ninguna estación a ${radioBusquedaKm.toInt()}km de [$latStr, $lonStr]."
                    }

                    val adapter = object : ArrayAdapter<GasolineraJson>(this@CombustibleActivity, R.layout.item_gasolinera, gasolinerasFiltradas) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: layoutInflater.inflate(R.layout.item_gasolinera, parent, false)
                            val item = getItem(position)

                            val tvRotulo = view.findViewById<TextView>(R.id.tvRotulo)
                            val tvDireccion = view.findViewById<TextView>(R.id.tvDireccion)
                            val tvPrecioDiesel = view.findViewById<TextView>(R.id.tvPrecioDiesel)
                            val tvPrecioGasolina = view.findViewById<TextView>(R.id.tvPrecioGasolina)

                            tvRotulo.text = item?.rotulo ?: "Sin Rótulo"
                            tvDireccion.text = "${item?.direccion ?: ""} (${item?.municipio ?: ""})"

                            val txtDiesel = if (item?.precioDiesel.isNullOrEmpty()) "N/A" else "${item?.precioDiesel} €"
                            val txtGasolina = if (item?.precioGasolina95.isNullOrEmpty()) "N/A" else "${item?.precioGasolina95} €"

                            tvPrecioDiesel.text = "Diésel: $txtDiesel"
                            tvPrecioGasolina.text = "Gasolina 95: $txtGasolina"

                            return view
                        }
                    }
                    lvGasolineras.adapter = adapter

                    lvGasolineras.setOnItemClickListener { _, _, position, _ ->
                        val gasolineraSeleccionada = gasolinerasFiltradas[position]
                        val latStrLimpia = gasolineraSeleccionada.latitud?.replace(",", ".")?.replace(" ", "")
                        val lonStrLimpia = gasolineraSeleccionada.longitud?.replace(",", ".")?.replace(" ", "")

                        if (!latStrLimpia.isNullOrEmpty() && !lonStrLimpia.isNullOrEmpty()) {
                            val nombreEtiqueta = gasolineraSeleccionada.rotulo ?: "Gasolinera"
                            val geoUriStr = "geo:$latStrLimpia,$lonStrLimpia?q=$latStrLimpia,$lonStrLimpia($nombreEtiqueta)"
                            val intentMapas = Intent(Intent.ACTION_VIEW, Uri.parse(geoUriStr))
                            try {
                                startActivity(intentMapas)
                            } catch (e: Exception) {
                                Toast.makeText(this@CombustibleActivity, "No se encontró app de mapas", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@CombustibleActivity, "Coordenadas no disponibles", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CombustibleActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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