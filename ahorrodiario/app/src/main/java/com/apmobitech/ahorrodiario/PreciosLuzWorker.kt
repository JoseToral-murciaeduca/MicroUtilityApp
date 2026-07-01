package com.apmobitech.ahorrodiario

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result // <-- ESTE ES EL IMPORT CLAVE
import com.apmobitech.ahorrodiario.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PreciosLuzWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            // 1. Calculamos la fecha del día de mañana
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val mananaStr = sdf.format(calendar.time)

            // 2. Hacemos la llamada a la API en segundo plano
            val respuesta = RetrofitClient.api.getPreciosLuz(
                startDate = "${mananaStr}T00:00",
                endDate = "${mananaStr}T23:59"
            )
            val listaPrecios = respuesta.included.firstOrNull()?.attributes?.values

            if (!listaPrecios.isNullOrEmpty()) {
                // 3. Buscamos el objeto matemático que tenga el precio mínimo
                val horaMasBarata = listaPrecios.minByOrNull { it.price }

                if (horaMasBarata != null) {
                    val hora = horaMasBarata.datetime.substring(11, 13).toInt()
                    val precioKwh = horaMasBarata.price / 1000

                    // 4. Lanzamos la alerta local al dispositivo
                    enviarNotificacion(hora, precioKwh)
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("WORKER_AHORRO", "Error consultando tarifas en segundo plano: ${e.message}")
            // Si falla (por ejemplo, por falta de cobertura), le pedimos a Android que lo reintente más tarde
            return Result.retry()
        }
    }

    private fun enviarNotificacion(hora: Int, precio: Double) {
        val channelId = "alertas_ahorro_diario"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificaciones obligatorio para Android 8.0 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Ahorro",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones con los mínimos históricos de la tarifa de la luz"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val textoAlerta = String.format(
            Locale.getDefault(),
            "Mañana el precio de la luz tendrá su mínimo a las %02d:00h con %.4f €/kWh.",
            hora, precio
        )

        // Construcción visual de la tarjeta de la notificación
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💡 ¡Hora más barata detectada para mañana!")
            .setContentText(textoAlerta)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$textoAlerta ¡Buen momento para programar tus electrodomésticos de mayor consumo!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}