package com.apmobitech.ahorrodiario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.apmobitech.ahorrodiario.database.AppDatabase
import com.apmobitech.ahorrodiario.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsGlobal = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        val temaGuardado = prefsGlobal.getInt("modo_oscuro", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(temaGuardado)

        setContentView(R.layout.activity_menu)

        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.adViewMenu)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val cardLuz = findViewById<CardView>(R.id.cardLuz)
        val cardCombustible = findViewById<CardView>(R.id.cardCombustible)
        val cardGas = findViewById<CardView>(R.id.cardGas)
        val cardAjustes = findViewById<CardView>(R.id.cardAjustes)

        cardLuz.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
        cardCombustible.setOnClickListener { startActivity(Intent(this, CombustibleActivity::class.java)) }
        cardGas.setOnClickListener { startActivity(Intent(this, GasActivity::class.java)) }
        cardAjustes.setOnClickListener { startActivity(Intent(this, ConfiguracionActivity::class.java)) }

        programarNotificacionesDiarias()
        cargarResumenDashboard()
    }

    private fun programarNotificacionesDiarias() {
        val restriccionesCarga = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val tareaDiariaRequest = PeriodicWorkRequestBuilder<PreciosLuzWorker>(24, TimeUnit.HOURS)
            .setConstraints(restriccionesCarga)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AlertaLuzDiaria",
            ExistingPeriodicWorkPolicy.KEEP,
            tareaDiariaRequest
        )
    }

    private fun cargarResumenDashboard() {
        val tvLuz = findViewById<TextView>(R.id.tvResumenLuz)
        val tvGas = findViewById<TextView>(R.id.tvResumenGas)
        val tvCombustible = findViewById<TextView>(R.id.tvResumenGasolina)
        val layoutCombustible = tvCombustible.parent as LinearLayout
        val tvEtiquetaCombustible = layoutCombustible.getChildAt(0) as TextView

        val prefsGlobal = getSharedPreferences("AjustesGlobales", Context.MODE_PRIVATE)
        val combustiblePrincipal = prefsGlobal.getString("vehiculo_principal", "Diésel") ?: "Diésel"
        val combustibleSubtipo = prefsGlobal.getString("vehiculo_subtipo", "Normal") ?: "Normal"

        val luzSeleccionadas = prefsGlobal.getStringSet("luz_tarifas_seleccionadas", setOf("PVPC")) ?: setOf("PVPC")
        val gasSeleccionadas = prefsGlobal.getStringSet("gas_tarifas_seleccionadas", setOf("TUR")) ?: setOf("TUR")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // ==========================================
                // 1. CÁLCULO DE LUZ
                // ==========================================
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoyStr = sdf.format(Calendar.getInstance().time)
                val respuestaLuz = RetrofitClient.api.getPreciosLuz("${hoyStr}T00:00", "${hoyStr}T23:59")

                var textoFinalLuz = "No disponible"
                val listaPreciosLuz = respuestaLuz.included.firstOrNull()?.attributes?.values
                if (!listaPreciosLuz.isNullOrEmpty()) {
                    val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val precioActual = listaPreciosLuz.find { it.datetime.substring(11, 13).toInt() == horaActual }
                    if (precioActual != null) {
                        val precioKwh = precioActual.price / 1000
                        textoFinalLuz = String.format(Locale.getDefault(), "%.4f €/kWh", precioKwh)
                    }
                }

                // ==========================================
                // 2. CÁLCULO DE GAS
                // ==========================================
                val textoFinalGas = "TUR_1 (0.052 €/kWh)"

                // ==========================================
                // 3. CÁLCULO DE COMBUSTIBLE INTELIGENTE
                // ==========================================
                var textoFinalCombustible = "Buscando estaciones..."

                // IMPORTANTE: Obtenemos la BD usando el nombre correcto de tu clase abstracta "AppDatabase"
                val db = AppDatabase.getDatabase(this@MenuActivity)
                val favoritasGuardadas = db.gasolineraDao().obtenerTodas()

                if (favoritasGuardadas.isNotEmpty()) {
                    // CASO A: Hay favoritas. Mostramos el RÓTULO (nombre) de la primera que guardó, ya que la BD no guarda precios.
                    val primeraFavorita = favoritasGuardadas.first()
                    textoFinalCombustible = "⭐ ${primeraFavorita.rotulo}"
                } else {
                    // CASO B: No hay favoritas -> Mostramos mensaje para ir a buscar la más cercana
                    textoFinalCombustible = "📍 Buscar más cercana"
                }

                // Actualizar interfaz
                withContext(Dispatchers.Main) {
                    tvLuz.text = textoFinalLuz
                    tvGas.text = textoFinalGas

                    if (combustiblePrincipal == "Electricidad") {
                        tvEtiquetaCombustible.text = "⚡ Coche Eléctrico:"
                        tvCombustible.text = "Consultar panel de Luz"
                    } else {
                        tvEtiquetaCombustible.text = "⛽ $combustiblePrincipal ($combustibleSubtipo):"
                        tvCombustible.text = textoFinalCombustible
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvLuz.text = "Error de red"
                    tvGas.text = "Error de red"
                    tvCombustible.text = "Error de red"
                }
            }
        }
    }
}