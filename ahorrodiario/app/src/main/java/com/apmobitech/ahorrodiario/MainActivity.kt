package com.apmobitech.ahorrodiario

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.apmobitech.ahorrodiario.network.ReeValue
import com.apmobitech.ahorrodiario.network.RetrofitClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

        val btnVolver = findViewById<ImageButton>(R.id.btnVolverElectricidad)
        val tvPrecioActual = findViewById<TextView>(R.id.tvPrecioActual)
        val tvNombreFuente = findViewById<TextView>(R.id.tvNombreFuente)
        val ivLogoFuente = findViewById<ImageView>(R.id.ivLogoFuente)
        val btnVer24h = findViewById<Button>(R.id.btnVer24h)
        val cardGrafica = findViewById<View>(R.id.cardGrafica)
        val chartPrecios = findViewById<LineChart>(R.id.chartPrecios)
        val spinnerCompania = findViewById<Spinner>(R.id.spinnerCompania)
        val btnOpcionesEnergia = findViewById<ImageButton>(R.id.btnOpcionesEnergia)

        btnVolver.setOnClickListener { finish() }

        actualizarListaSpinner(spinnerCompania)

        // LÓGICA DEL BUSCADOR (Intacta)
        btnOpcionesEnergia.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_buscar_companias, null)
            val etBuscar = dialogView.findViewById<EditText>(R.id.etBuscarCompania)
            val listView = dialogView.findViewById<ListView>(R.id.listViewCompanias)
            val btnSeleccionarTodo = dialogView.findViewById<Button>(R.id.btnSeleccionarTodo)
            val btnDeseleccionarTodo = dialogView.findViewById<Button>(R.id.btnDeseleccionarTodo)

            listView.choiceMode = ListView.CHOICE_MODE_NONE

            val estadoCompanias = DirectorioCompanias.lista.map { compania ->
                CompaniaCheck(compania.nombre, sharedPreferences.getBoolean("mostrar_${compania.nombre}", true))
            }

            val adapter = object : ArrayAdapter<CompaniaCheck>(this, android.R.layout.simple_list_item_multiple_choice, mutableListOf()) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as CheckedTextView
                    val item = getItem(position)
                    view.text = item?.nombre
                    view.isChecked = item?.isChecked ?: false
                    view.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.texto_principal))
                    return view
                }
            }
            listView.adapter = adapter
            adapter.addAll(estadoCompanias)

            listView.setOnItemClickListener { _, _, position, _ ->
                val item = adapter.getItem(position)
                if (item != null) {
                    item.isChecked = !item.isChecked
                    adapter.notifyDataSetChanged()
                }
            }

            btnSeleccionarTodo.setOnClickListener {
                for (check in estadoCompanias) check.isChecked = true
                adapter.notifyDataSetChanged()
            }

            btnDeseleccionarTodo.setOnClickListener {
                for (check in estadoCompanias) check.isChecked = false
                adapter.notifyDataSetChanged()
            }

            etBuscar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val texto = s.toString().lowercase()
                    val filtrada = if (texto.isEmpty()) estadoCompanias else estadoCompanias.filter { it.nombre.lowercase().contains(texto) }
                    adapter.clear()
                    adapter.addAll(filtrada)
                }
            })

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

        // LÓGICA DEL SPINNER
        spinnerCompania.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val compania = listaCompaniasVisibles[position]

                sharedPreferences.edit().putString("compania_seleccionada_actual", compania.nombre).apply()

                tvNombreFuente.text = compania.nombre
                ivLogoFuente.load(compania.urlLogo) {
                    crossfade(true)
                    error(android.R.drawable.ic_dialog_info)
                }

                cardGrafica.visibility = View.GONE

                if (compania.tipo == TipoMercado.REGULADO_PVPC) {
                    esMercadoReguladoActivo = true
                    btnVer24h.text = "Ver gráfica 24 horas"
                    precioMostradoActualmente = ""
                    comprobarVariacionPrecio(tvPrecioActual)
                } else {
                    esMercadoReguladoActivo = false
                    btnVer24h.text = "Configurar mi tarifa"
                    val precioEjemplo = compania.tarifaFijaKwh?.toFloat() ?: 0f
                    val precioGuardado = sharedPreferences.getFloat(compania.nombre, precioEjemplo)
                    tvPrecioActual.text = String.format(Locale.getDefault(), "%.4f €", precioGuardado)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // BOTÓN VER GRÁFICA / CONFIGURAR
        btnVer24h.setOnClickListener {
            if (esMercadoReguladoActivo) {
                if (cardGrafica.visibility == View.GONE) {
                    cardGrafica.visibility = View.VISIBLE
                    btnVer24h.text = "Ocultar gráfica 24 horas"
                    chartPrecios.animateX(1000) // Animación suave al desplegar
                } else {
                    cardGrafica.visibility = View.GONE
                    btnVer24h.text = "Ver gráfica 24 horas"
                }
            } else {
                val posicionActual = spinnerCompania.selectedItemPosition
                val companiaActual = listaCompaniasVisibles[posicionActual]
                mostrarDialogoConfiguracion(companiaActual, tvPrecioActual)
            }
        }

        // DESCARGA DE LA API Y PREPARACIÓN DE DATOS PARA LA GRÁFICA
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val hoyStr = sdf.format(Date())
                val respuesta = RetrofitClient.api.getPreciosLuz(startDate = "${hoyStr}T00:00", endDate = "${hoyStr}T23:59")
                listaPreciosGlobal = respuesta.included.firstOrNull()?.attributes?.values

                if (!listaPreciosGlobal.isNullOrEmpty()) {
                    val entradasGrafica = ArrayList<Entry>()
                    val etiquetasHoras = ArrayList<String>()

                    for ((indice, dato) in listaPreciosGlobal!!.withIndex()) {
                        val hora = dato.datetime.substring(11, 13).toInt()
                        val precioKwh = (dato.price / 1000).toFloat()

                        // Añadimos el dato matemático (X, Y)
                        entradasGrafica.add(Entry(indice.toFloat(), precioKwh))
                        etiquetasHoras.add("${hora}h") // Etiqueta visual para el eje X
                    }

                    withContext(Dispatchers.Main) {
                        configurarGrafica(chartPrecios, entradasGrafica, etiquetasHoras)
                    }
                }
            } catch (e: Exception) {
                Log.e("APP_AHORRO", "Error al descargar: ${e.message}")
            }
        }

        // RELOJ ACTUALIZADOR
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (esMercadoReguladoActivo) comprobarVariacionPrecio(tvPrecioActual)
                delay(1000)
            }
        }
    }

    // --- DISEÑO Y CONFIGURACIÓN DE LA GRÁFICA MPANDROIDCHART ---
    private fun configurarGrafica(chart: LineChart, entradas: List<Entry>, etiquetas: List<String>) {
        val colorTexto = ContextCompat.getColor(this, R.color.texto_secundario)
        val colorAcento = Color.parseColor("#F59E0B") // Naranja eléctrico

        val dataSet = LineDataSet(entradas, "Precio (€/kWh)").apply {
            color = colorAcento
            valueTextColor = colorTexto
            valueTextSize = 10f
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(colorAcento)
            circleRadius = 4f

            // Efecto relleno degradado
            setDrawFilled(true)
            fillColor = colorAcento
            fillAlpha = 40

            mode = LineDataSet.Mode.CUBIC_BEZIER // Hace que la línea sea curva en lugar de recta
        }

        chart.data = LineData(dataSet)

        chart.apply {
            description.isEnabled = false // Quita el texto por defecto de la esquina
            legend.textColor = colorTexto

            // Eje de abajo (Horas)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = colorTexto
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(etiquetas)
                granularity = 1f
            }

            // Eje de la izquierda (Precios)
            axisLeft.apply {
                textColor = colorTexto
                gridColor = Color.parseColor("#33888888") // Líneas de fondo semitransparentes
            }

            // Ocultamos el eje derecho para que se vea más limpio
            axisRight.isEnabled = false

            invalidate() // Pinta la gráfica en la pantalla
        }
    }

    private fun actualizarListaSpinner(spinner: Spinner) {
        val filtradas = DirectorioCompanias.lista.filter { sharedPreferences.getBoolean("mostrar_${it.nombre}", true) }
        listaCompaniasVisibles = filtradas.ifEmpty { listOf(DirectorioCompanias.lista[0]) }

        val adapter = ArrayAdapter(this, R.layout.item_spinner, listaCompaniasVisibles.map { it.nombre })
        adapter.setDropDownViewResource(R.layout.item_spinner)
        spinner.adapter = adapter

        val companiaGuardada = sharedPreferences.getString("compania_seleccionada_actual", "")
        val index = listaCompaniasVisibles.indexOfFirst { it.nombre == companiaGuardada }
        if (index != -1) spinner.setSelection(index)
    }

    private fun comprobarVariacionPrecio(tvPrecioActual: TextView) {
        val lista = listaPreciosGlobal ?: return
        val horaActual = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val precioHoraActual = lista.find {
            try { it.datetime.substring(11, 13).toInt() == horaActual } catch (e: Exception) { false }
        }

        if (precioHoraActual != null) {
            val precioKwhReal = String.format(Locale.getDefault(), "%.4f €", precioHoraActual.price / 1000)
            if (precioMostradoActualmente != precioKwhReal) {
                precioMostradoActualmente = precioKwhReal
                tvPrecioActual.text = precioMostradoActualmente
            }
        }
    }

    private fun mostrarDialogoConfiguracion(compania: CompaniaElectrica, tvPrecioActual: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Configurar ${compania.nombre}")

        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(sharedPreferences.getFloat(compania.nombre, compania.tarifaFijaKwh?.toFloat() ?: 0f).toString())
        }

        val container = FrameLayout(this).apply {
            addView(input, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(60, 0, 60, 0) })
        }

        builder.setView(container)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            if (input.text.isNotEmpty()) {
                try {
                    val nuevoPrecio = input.text.toString().toFloat()
                    sharedPreferences.edit().putFloat(compania.nombre, nuevoPrecio).apply()
                    tvPrecioActual.text = String.format(Locale.getDefault(), "%.4f €", nuevoPrecio)
                    Toast.makeText(this, "Tarifa guardada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Usa punto para los decimales.", Toast.LENGTH_LONG).show()
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}