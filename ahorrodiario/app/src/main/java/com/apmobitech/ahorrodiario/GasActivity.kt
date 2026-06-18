package com.apmobitech.ahorrodiario

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import java.util.Locale

class GasActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    // Lista dinámica que guardará solo las compañías de gas visibles
    private var listaCompaniasVisibles: List<CompaniaGas> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gas)

        sharedPreferences = getSharedPreferences("MisTarifasGas", Context.MODE_PRIVATE)

        val btnVolver = findViewById<ImageButton>(R.id.btnVolverGas)
        val spinnerGas = findViewById<Spinner>(R.id.spinnerGas)
        val btnOpcionesGas = findViewById<ImageButton>(R.id.btnOpcionesGas)
        val tvPrecioVariable = findViewById<TextView>(R.id.tvPrecioVariableGas)
        val tvPrecioFijo = findViewById<TextView>(R.id.tvPrecioFijoGas)
        val ivLogoGas = findViewById<ImageView>(R.id.ivLogoGas)
        val btnConfigurar = findViewById<Button>(R.id.btnConfigurarGas)

        btnVolver.setOnClickListener { finish() }

        // --- 1. CARGAMOS EL SPINNER POR PRIMERA VEZ ---
        actualizarListaSpinner(spinnerGas)

        // --- 2. ACCIÓN DEL ENGRANAJE (Buscador a prueba de fallos) ---
        btnOpcionesGas.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_buscar_companias, null)
            val etBuscar = dialogView.findViewById<EditText>(R.id.etBuscarCompania)
            val listView = dialogView.findViewById<ListView>(R.id.listViewCompanias)

            listView.choiceMode = ListView.CHOICE_MODE_NONE

            val estadoCompanias = DirectorioGas.lista.map { compania ->
                // Usamos la misma estructura CompaniaCheck que creamos en la luz
                CompaniaCheck(compania.nombre, sharedPreferences.getBoolean("mostrar_gas_${compania.nombre}", true))
            }

            val adapter = object : ArrayAdapter<CompaniaCheck>(this, android.R.layout.simple_list_item_multiple_choice, mutableListOf()) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as CheckedTextView
                    val item = getItem(position)
                    view.text = item?.nombre
                    view.isChecked = item?.isChecked ?: false
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

            AlertDialog.Builder(this)
                .setTitle("Filtrar tarifas de Gas")
                .setView(dialogView)
                .setPositiveButton("Guardar") { dialog, _ ->
                    val editor = sharedPreferences.edit()
                    for (compania in estadoCompanias) {
                        editor.putBoolean("mostrar_gas_${compania.nombre}", compania.isChecked)
                    }
                    editor.apply()

                    actualizarListaSpinner(spinnerGas)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // --- 3. LÓGICA DE SELECCIÓN DEL SPINNER ---
        spinnerGas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val compania = listaCompaniasVisibles[position]

                // Guardamos en memoria la compañía que el usuario está viendo
                sharedPreferences.edit().putString("compania_gas_seleccionada", compania.nombre).apply()

                ivLogoGas.load(compania.urlLogo) {
                    crossfade(true)
                    error(android.R.drawable.ic_dialog_info)
                }

                if (compania.tipo == TipoGas.REGULADO_TUR) {
                    tvPrecioVariable.text = String.format(Locale.getDefault(), "%.4f €/kWh", compania.terminoVariableKwh)
                    tvPrecioFijo.text = String.format(Locale.getDefault(), "%.2f €/mes", compania.terminoFijoMensual)
                    btnConfigurar.visibility = View.GONE
                } else {
                    val varGuardado = sharedPreferences.getFloat("${compania.nombre}_var", compania.terminoVariableKwh.toFloat())
                    val fijoGuardado = sharedPreferences.getFloat("${compania.nombre}_fijo", compania.terminoFijoMensual.toFloat())

                    tvPrecioVariable.text = String.format(Locale.getDefault(), "%.4f €/kWh", varGuardado)
                    tvPrecioFijo.text = String.format(Locale.getDefault(), "%.2f €/mes", fijoGuardado)
                    btnConfigurar.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- 4. BOTÓN DE CONFIGURAR TARIFA LIBRE ---
        btnConfigurar.setOnClickListener {
            val posicionActual = spinnerGas.selectedItemPosition
            val companiaActual = listaCompaniasVisibles[posicionActual]
            mostrarDialogoEdicionDoble(companiaActual, tvPrecioVariable, tvPrecioFijo)
        }
    }

    // --- FUNCIONES AUXILIARES ---
    private fun actualizarListaSpinner(spinner: Spinner) {
        val filtradas = DirectorioGas.lista.filter { compania ->
            sharedPreferences.getBoolean("mostrar_gas_${compania.nombre}", true)
        }

        listaCompaniasVisibles = filtradas.ifEmpty { listOf(DirectorioGas.lista[0]) }

        val nombres = listaCompaniasVisibles.map { it.nombre }
        val adapter = ArrayAdapter(this, R.layout.item_spinner, nombres)
        adapter.setDropDownViewResource(R.layout.item_spinner)
        spinner.adapter = adapter

        // Rescatamos la compañía guardada para que se seleccione sola
        val companiaGuardada = sharedPreferences.getString("compania_gas_seleccionada", "")
        val index = listaCompaniasVisibles.indexOfFirst { it.nombre == companiaGuardada }

        if (index != -1) {
            spinner.setSelection(index)
        }
    }

    private fun mostrarDialogoEdicionDoble(compania: CompaniaGas, tvVar: TextView, tvFijo: TextView) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Configurar ${compania.nombre}")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 20, 60, 0)

        // Campo 1: Término Variable
        val tvLabelVar = TextView(this)
        tvLabelVar.text = "Término Variable (€/kWh):"
        layout.addView(tvLabelVar)

        val inputVar = EditText(this)
        inputVar.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val varGuardado = sharedPreferences.getFloat("${compania.nombre}_var", compania.terminoVariableKwh.toFloat())
        inputVar.setText(varGuardado.toString())
        layout.addView(inputVar)

        // Campo 2: Término Fijo
        val tvLabelFijo = TextView(this)
        tvLabelFijo.text = "\nTérmino Fijo (€/mes):"
        layout.addView(tvLabelFijo)

        val inputFijo = EditText(this)
        inputFijo.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val fijoGuardado = sharedPreferences.getFloat("${compania.nombre}_fijo", compania.terminoFijoMensual.toFloat())
        inputFijo.setText(fijoGuardado.toString())
        layout.addView(inputFijo)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            try {
                val nuevoVar = inputVar.text.toString().toFloat()
                val nuevoFijo = inputFijo.text.toString().toFloat()

                sharedPreferences.edit()
                    .putFloat("${compania.nombre}_var", nuevoVar)
                    .putFloat("${compania.nombre}_fijo", nuevoFijo)
                    .apply()

                tvVar.text = String.format(Locale.getDefault(), "%.4f €/kWh", nuevoVar)
                tvFijo.text = String.format(Locale.getDefault(), "%.2f €/mes", nuevoFijo)

                Toast.makeText(this, "Tarifa de gas actualizada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error: Usa punto para los decimales", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}