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
import com.apmobitech.ahorrodiario.database.AppDatabase
import com.apmobitech.ahorrodiario.database.GasolineraFavorita
import com.apmobitech.ahorrodiario.network.GasolineraJson
import com.apmobitech.ahorrodiario.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Objeto auxiliar que combina los datos del gobierno con el "estado" de si es tu favorita o no
data class GasolineraUI(val datos: GasolineraJson, var esFavorita: Boolean)

class CombustibleActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_LOCATION = 1001
    private var radioBusquedaKm = 100.0
    private var capacidadDeposito = 50.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combustible)

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
        }

        descargarDatosMinisterio(miUbicacion)
    }

    private fun descargarDatosMinisterio(ubicacionUsuario: Location) {
        val tvCosteLlenado = findViewById<TextView>(R.id.tvCosteLlenado)
        val tvGasolineraBarata = findViewById<TextView>(R.id.tvGasolineraBarata)
        val lvGasolineras = findViewById<ListView>(R.id.lvGasolineras)

        val textoTituloDeposito = "Llenar depósito (${capacidadDeposito.toInt()}L)"
        findViewById<TextView>(R.id.tvCosteLlenado).let {
            val layout = it.parent as android.widget.LinearLayout
            val titulo = layout.getChildAt(0) as TextView
            titulo.text = textoTituloDeposito
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Leemos las favoritas de nuestra base de datos local
                val dao = AppDatabase.getDatabase(this@CombustibleActivity).gasolineraDao()
                val listaFavoritasDB = dao.obtenerTodas()

                // 2. Descargamos las estaciones del Gobierno
                val respuesta = RetrofitClient.fuelApi.getGasolineras()
                val listaSegura = respuesta.listaGasolineras ?: emptyList()

                // 3. Filtramos y creamos la lista visual
                val listaVisual = mutableListOf<GasolineraUI>()

                for (gasolinera in listaSegura) {
                    val idGas = gasolinera.id ?: ""
                    val esFav = listaFavoritasDB.any { it.idEstacion == idGas }

                    val latLimpiada = gasolinera.latitud?.replace(",", ".")?.replace(" ", "")
                    val lonLimpiada = gasolinera.longitud?.replace(",", ".")?.replace(" ", "")
                    val latGas = latLimpiada?.toDoubleOrNull()
                    val lonGas = lonLimpiada?.toDoubleOrNull()

                    // TRUCO: Si es favorita, la incluimos SIEMPRE, sin importar la distancia
                    if (esFav) {
                        listaVisual.add(GasolineraUI(gasolinera, true))
                    } else if (latGas != null && lonGas != null) {
                        val distancia = calcularDistanciaHaversine(
                            ubicacionUsuario.latitude, ubicacionUsuario.longitude,
                            latGas, lonGas
                        )
                        if (distancia <= radioBusquedaKm) {
                            listaVisual.add(GasolineraUI(gasolinera, false))
                        }
                    }
                }

                // 4. Ordenamos: Primero las favoritas, luego las más baratas
                val listaOrdenada = listaVisual.sortedWith(
                    compareByDescending<GasolineraUI> { it.esFavorita }
                        .thenBy {
                            it.datos.precioDiesel?.replace(",", ".")?.replace(" ", "")?.toDoubleOrNull() ?: Double.MAX_VALUE
                        }
                )

                // 5. Calculamos la más barata PARA EL TITULO (solo entre las que NO son favoritas para ser objetivos con la zona)
                var precioDieselMasBarato = Double.MAX_VALUE
                var nombreBarata = ""
                val listaSoloCercanas = listaOrdenada.filter { !it.esFavorita } // Las favoritas de otra ciudad no deben estropear el récord de tu zona actual

                for (g in listaSoloCercanas) {
                    val precioNum = g.datos.precioDiesel?.replace(",", ".")?.replace(" ", "")?.toDoubleOrNull()
                    if (precioNum != null && precioNum < precioDieselMasBarato) {
                        precioDieselMasBarato = precioNum
                        nombreBarata = g.datos.rotulo ?: "Desconocida"
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

                    // 6. El Adaptador visual de la lista
                    val adapter = object : ArrayAdapter<GasolineraUI>(this@CombustibleActivity, R.layout.item_gasolinera, listaOrdenada) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: layoutInflater.inflate(R.layout.item_gasolinera, parent, false)
                            val item = getItem(position)!!

                            val tvRotulo = view.findViewById<TextView>(R.id.tvRotulo)
                            val tvDireccion = view.findViewById<TextView>(R.id.tvDireccion)
                            val tvPrecioDiesel = view.findViewById<TextView>(R.id.tvPrecioDiesel)
                            val tvPrecioGasolina = view.findViewById<TextView>(R.id.tvPrecioGasolina)
                            val btnFavorito = view.findViewById<ImageButton>(R.id.btnFavorito)

                            tvRotulo.text = item.datos.rotulo ?: "Sin Rótulo"
                            tvDireccion.text = "${item.datos.direccion ?: ""} (${item.datos.municipio ?: ""})"

                            val txtDiesel = if (item.datos.precioDiesel.isNullOrEmpty()) "N/A" else "${item.datos.precioDiesel} €"
                            val txtGasolina = if (item.datos.precioGasolina95.isNullOrEmpty()) "N/A" else "${item.datos.precioGasolina95} €"

                            tvPrecioDiesel.text = "Diésel: $txtDiesel"
                            tvPrecioGasolina.text = "Gasolina 95: $txtGasolina"

                            // Pintar estrella
                            if (item.esFavorita) {
                                btnFavorito.setImageResource(android.R.drawable.btn_star_big_on)
                            } else {
                                btnFavorito.setImageResource(android.R.drawable.btn_star_big_off)
                            }

                            // Acción al pulsar la estrella
                            btnFavorito.setOnClickListener {
                                item.esFavorita = !item.esFavorita
                                notifyDataSetChanged() // Refresca el icono al instante

                                CoroutineScope(Dispatchers.IO).launch {
                                    val daoAccion = AppDatabase.getDatabase(context).gasolineraDao()
                                    if (item.esFavorita) {
                                        daoAccion.insertar(GasolineraFavorita(
                                            idEstacion = item.datos.id ?: "",
                                            rotulo = item.datos.rotulo ?: "",
                                            latitud = item.datos.latitud ?: "",
                                            longitud = item.datos.longitud ?: ""
                                        ))
                                    } else {
                                        daoAccion.eliminar(item.datos.id ?: "")
                                    }
                                }
                            }
                            return view
                        }
                    }
                    lvGasolineras.adapter = adapter

                    // Acción al pulsar la fila (GPS)
                    lvGasolineras.setOnItemClickListener { _, _, position, _ ->
                        val gasolineraSeleccionada = listaOrdenada[position].datos
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