package com.apmobitech.ahorrodiario

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
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

// Estructura auxiliar para manejar los ticks sin bugs al buscar
data class CompaniaCheck(val nombre: String, var isChecked: Boolean)

class MainActivity : AppCompatActivity() {

    private var listaPreciosGlobal: List<ReeValue>? = null
    private var precioMostradoActualmente: String = ""
    private var esMercadoReguladoActivo: Boolean = true

    private lateinit var sharedPreferences: SharedPreferences
    private var listaCompaniasVisibles: List<CompaniaElectrica> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("MisTarifasElectricas", Context.MODE_PRIVATE)

        val tvPrecioActual = findViewById<TextView>(R.id.tvPrecioActual)
        val tvNombreFuente = findViewById<TextView>(R.id.tvNombreFuente)
        val ivLogoFuente = findViewById<ImageView>(R.id.ivLogoFuente)
        val btnVer24h = findViewById<Button>(R.id.btnVer24h)
        val scroll24h = findViewById<ScrollView>(R.id.scroll24h)
        val tvListaPrecios = findViewById<TextView>(R.id.tvListaPrecios)
        val spinnerCompania = findViewById<Spinner>(R.id.spinnerCompania)
        val btnOpcionesEnergia = findViewById<ImageButton>(R.id.btnOpcionesEnergia)

        // --- BOTÓN VOLVER
        // Al inicio de onCreate
        val btnVolver = findViewById<ImageButton>(R.id.btnVolverElectricidad)
        btnVolver.setOnClickListener {
            finish() // Esto cierra la actividad actual y te devuelve al menú anterior
        }

        // --- 1. CARGAMOS EL SPINNER POR PRIMERA VEZ ---
        actualizarListaSpinner(spinnerCompania)

        // --- 2. ACCIÓN DEL ENGRANAJE (Buscador a prueba de fallos) ---
        btnOpcionesEnergia.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_buscar_companias, null)
            val etBuscar = dialogView.findViewById<EditText>(R.id.etBuscarCompania)
            val listView = dialogView.findViewById<ListView>(R.id.listViewCompanias)

            // Apagamos el gestor automático de Android para evitar el bug de las posiciones
            listView.choiceMode = ListView.CHOICE_MODE_NONE

            // Obtenemos el estado de la memoria
            val estadoCompanias = DirectorioCompanias.lista.map { compania ->
                CompaniaCheck(compania.nombre, sharedPreferences.getBoolean("mostrar_${compania.nombre}", true))
            }

            // Adaptador personalizado
            val adapter = object : ArrayAdapter<CompaniaCheck>(this, android.R.layout.simple_list_item_multiple_choice, mutableListOf()) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as CheckedTextView
                    val item = getItem(position)
                    view.text = item?.nombre
                    // Nosotros forzamos el tick visual leyendo nuestra variable
                    view.isChecked = item?.isChecked ?: false
                    return view
                }
            }
            listView.adapter = adapter
            adapter.addAll(estadoCompanias)

            // Clic en un elemento de la lista (incluso si está filtrada)
            listView.setOnItemClickListener { _, _, position, _ ->
                val item = adapter.getItem(position)
                if (item != null) {
                    item.isChecked = !item.isChecked // Invertimos la variable
                    adapter.notifyDataSetChanged()   // Repintamos
                }
            }

            // Buscador en tiempo real
            etBuscar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val texto = s.toString().lowercase()
                    val filtrada = if (texto.isEmpty()) {
                        estadoCompanias
                    } else {
                        estadoCompanias.filter { it.nombre.lowercase().contains(texto) }
                    }
                    adapter.clear()
                    adapter.addAll(filtrada)
                }
            })

            // Guardar cambios
            AlertDialog.Builder(this)
                .setTitle("Filtrar compañías")
                .setView(dialogView)
                .setPositiveButton("Guardar") { dialog, _ ->
                    val editor = sharedPreferences.edit()
                    for (compania in estadoCompanias) {
                        editor.putBoolean("mostrar_${compania.nombre}", compania.isChecked)
                    }
                    editor.apply()

                    actualizarListaSpinner(spinnerCompania)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // --- 3. LÓGICA DE SELECCIÓN DEL SPINNER ---
        spinnerCompania.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val compania = listaCompaniasVisibles[position]

                // Guardamos en memoria la compañía que el usuario está viendo AHORA MISMO
                sharedPreferences.edit().putString("compania_seleccionada_actual", compania.nombre).apply()

                tvNombreFuente.text = compania.nombre
                ivLogoFuente.load(compania.urlLogo) {
                    crossfade(true)
                    error(android.R.drawable.ic_dialog_info)
                }

                scroll24h.visibility = View.GONE

                if (compania.tipo == TipoMercado.REGULADO_PVPC) {
                    esMercadoReguladoActivo = true
                    btnVer24h.text = "Ver últimas 24 horas"

                    precioMostradoActualmente = ""
                    comprobarVariacionPrecio(tvPrecioActual)
                } else {
                    esMercadoReguladoActivo = false
                    btnVer24h.text = "Configurar mi tarifa"

                    val precioEjemplo = compania.tarifaFijaKwh?.toFloat() ?: 0f
                    val precioGuardado = sharedPreferences.getFloat(compania.nombre, precioEjemplo)

                    val precioFijo = String.format(Locale.getDefault(), "%.4f", precioGuardado) + " €"
                    tvPrecioActual.text = precioFijo
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- 4. LÓGICA DEL BOTÓN MULTIUSOS ---
        btnVer24h.setOnClickListener {
            if (esMercadoReguladoActivo) {
                if (scroll24h.visibility == View.GONE) {
                    scroll24h.visibility = View.VISIBLE
                    btnVer24h.text = "Ocultar 24 horas"
                } else {
                    scroll24h.visibility = View.GONE
                    btnVer24h.text = "Ver últimas 24 horas"
                }
            } else {
                val posicionActual = spinnerCompania.selectedItemPosition
                val companiaActual = listaCompaniasVisibles[posicionActual]
                mostrarDialogoConfiguracion(companiaActual, tvPrecioActual)
            }
        }

        // --- 5. DESCARGA INICIAL DE LA API ---
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
                    withContext(Dispatchers.Main) { tvListaPrecios.text = sb.toString() }
                }
            } catch (e: Exception) {
                Log.e("APP_AHORRO", "Error al descargar: ${e.message}")
            }
        }

        // --- 6. RELOJ ACTUALIZADOR EN TIEMPO REAL ---
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (esMercadoReguladoActivo) {
                    comprobarVariacionPrecio(tvPrecioActual)
                }
                delay(1000)
            }
        }
    }

    // --- FUNCIONES AUXILIARES ---
    private fun actualizarListaSpinner(spinner: Spinner) {
        val filtradas = DirectorioCompanias.lista.filter { compania ->
            sharedPreferences.getBoolean("mostrar_${compania.nombre}", true)
        }

        listaCompaniasVisibles = filtradas.ifEmpty { listOf(DirectorioCompanias.lista[0]) }

        val nombres = listaCompaniasVisibles.map { it.nombre }
        val adapter = ArrayAdapter(this, R.layout.item_spinner, nombres)
        adapter.setDropDownViewResource(R.layout.item_spinner)
        spinner.adapter = adapter

        // NUEVO: Rescatamos la compañía que teníamos seleccionada antes de cerrar la app
        val companiaGuardada = sharedPreferences.getString("compania_seleccionada_actual", "")
        val index = listaCompaniasVisibles.indexOfFirst { it.nombre == companiaGuardada }

        if (index != -1) {
            // Si la encontramos en la lista, la seleccionamos automáticamente
            spinner.setSelection(index)
        }
    }

    private fun comprobarVariacionPrecio(tvPrecioActual: TextView) {
        val lista = listaPreciosGlobal ?: return
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val precioHoraActual = lista.find { dato ->
            try {
                val horaApi = dato.datetime.substring(11, 13).toInt()
                horaApi == horaActual
            } catch (e: Exception) { false }
        }

        if (precioHoraActual != null) {
            val precioKwhReal = String.format(Locale.getDefault(), "%.4f", precioHoraActual.price / 1000) + " €"
            if (precioMostradoActualmente != precioKwhReal) {
                precioMostradoActualmente = precioKwhReal
                tvPrecioActual.text = precioMostradoActualmente
            }
        }
    }

    private fun mostrarDialogoConfiguracion(compania: CompaniaElectrica, tvPrecioActual: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Configurar ${compania.nombre}")
        builder.setMessage("Introduce tu precio fijo en €/kWh:")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        val precioEjemplo = compania.tarifaFijaKwh?.toFloat() ?: 0f
        val precioActualGuardado = sharedPreferences.getFloat(compania.nombre, precioEjemplo)
        input.setText(precioActualGuardado.toString())

        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(60, 0, 60, 0)
        input.layoutParams = params
        container.addView(input)

        builder.setView(container)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val textoIntroducido = input.text.toString()
            if (textoIntroducido.isNotEmpty()) {
                try {
                    val nuevoPrecio = textoIntroducido.toFloat()
                    sharedPreferences.edit().putFloat(compania.nombre, nuevoPrecio).apply()

                    val precioFijoPantalla = String.format(Locale.getDefault(), "%.4f", nuevoPrecio) + " €"
                    tvPrecioActual.text = precioFijoPantalla

                    Toast.makeText(this, "Tarifa guardada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Formato incorrecto. Usa punto para decimales.", Toast.LENGTH_LONG).show()
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}