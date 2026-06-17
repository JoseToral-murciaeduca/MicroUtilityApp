package com.apmobitech.ahorrodiario

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.apmobitech.ahorrodiario.network.ReeValue
import com.apmobitech.ahorrodiario.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import coil.load
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private var listaPreciosGlobal: List<ReeValue>? = null
    // Variable para guardar exactamente lo que hay escrito en la pantalla en este momento
    private var precioMostradoActualmente: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvPrecioActual = findViewById<TextView>(R.id.tvPrecioActual)
        val btnVer24h = findViewById<Button>(R.id.btnVer24h)
        val scroll24h = findViewById<ScrollView>(R.id.scroll24h)
        val tvListaPrecios = findViewById<TextView>(R.id.tvListaPrecios)
        val ivLogoFuente = findViewById<ImageView>(R.id.ivLogoFuente)

        // URL pública del logotipo de Red Eléctrica (versión PNG transparente alojada en Wikipedia)
        val urlLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Logotipo_de_Red_El%C3%A9ctrica_de_Espa%C3%B1a.svg/512px-Logotipo_de_Red_El%C3%A9ctrica_de_Espa%C3%B1a.svg.png"

        // Usamos Coil para descargar y poner la imagen automáticamente
        ivLogoFuente.load(urlLogo) {
            crossfade(true) // Hace que la imagen aparezca de forma suave
            placeholder(android.R.drawable.ic_menu_gallery) // Muestra un icono genérico de Android mientras carga
            error(android.R.drawable.ic_dialog_alert) // Muestra una alerta si falla el internet o el enlace
        }

        btnVer24h.setOnClickListener {
            if (scroll24h.visibility == View.GONE) {
                scroll24h.visibility = View.VISIBLE
                btnVer24h.text = "Ocultar 24 horas"
            } else {
                scroll24h.visibility = View.GONE
                btnVer24h.text = "Ver últimas 24 horas"
            }
        }

        // Descarga inicial de datos (Una sola vez para no ser baneados)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoyStr = sdf.format(Date())
                val start = "${hoyStr}T00:00"
                val end = "${hoyStr}T23:59"

                val respuesta = RetrofitClient.api.getPreciosLuz(startDate = start, endDate = end)
                listaPreciosGlobal = respuesta.included.firstOrNull()?.attributes?.values

                if (!listaPreciosGlobal.isNullOrEmpty()) {
                    val sb = StringBuilder()
                    for (dato in listaPreciosGlobal!!) {
                        val horaTexto = dato.datetime.substring(11, 16)
                        val precioKwh = String.format(Locale.getDefault(), "%.4f", dato.price / 1000)
                        sb.append("Hora $horaTexto -> $precioKwh €/kWh\n")
                    }

                    withContext(Dispatchers.Main) {
                        tvListaPrecios.text = sb.toString()
                    }
                }
            } catch (e: Exception) {
                Log.e("APP_AHORRO", "Error al descargar: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvPrecioActual.text = "Error"
                    tvListaPrecios.text = "Comprueba tu conexión."
                }
            }
        }

        // Comprobador en tiempo real (Tick cada 1 segundo)
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                comprobarVariacionPrecio(tvPrecioActual)
                delay(1000)
            }
        }
    }

    // Compara el precio de la pantalla con el que DEBERÍA haber ahora mismo
    private fun comprobarVariacionPrecio(tvPrecioActual: TextView) {
        val lista = listaPreciosGlobal ?: return
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val precioHoraActual = lista.find { dato ->
            try {
                val horaApi = dato.datetime.substring(11, 13).toInt()
                horaApi == horaActual
            } catch (e: Exception) {
                false
            }
        }

        if (precioHoraActual != null) {
            // Calculamos cuál es el precio real válido en este segundo
            val precioKwhReal = String.format(Locale.getDefault(), "%.4f", precioHoraActual.price / 1000) + " €"

            // LÓGICA NUEVA: Si el precio de la pantalla es distinto al real, lo cambiamos
            if (precioMostradoActualmente != precioKwhReal) {
                precioMostradoActualmente = precioKwhReal
                tvPrecioActual.text = precioMostradoActualmente
                Log.d("APP_AHORRO", "Variación detectada: Pantalla actualizada a $precioMostradoActualmente")
            }
        } else {
            tvPrecioActual.text = "-- €"
        }
    }
}