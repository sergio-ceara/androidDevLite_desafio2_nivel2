package com.example.desafio2_nivel2_teste3

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

fun showEditionDialog(
    context: Context,
    title: String,
    options: Array<String>,
    onOptionSelected: (Int) -> Unit
) {
    // Inflate o layout usando ViewBinding
    val binding = LayoutInflater.from(context).inflate(R.layout.function_show_edition_dialog, null)

    // Definir o título
    val titleView = binding.findViewById<TextView>(R.id.textViewTitle)
    titleView.text = title

    // Obter o LinearLayout para adicionar as opções
    val optionsLayout = binding.findViewById<LinearLayout>(R.id.linearLayoutOptions)

    // Criar o AlertDialog
    val dialog = AlertDialog.Builder(context)
        .setView(binding)
        .create()

    // Adicionar opções horizontalmente
    for ((index, option) in options.withIndex()) {
        val textView = TextView(context).apply {
            text = option
            textSize = 20f
            setTextColor(if (index % 2 == 0) Color.MAGENTA else Color.BLUE)
            setPadding(16, 16, 16, 16)
            setOnClickListener {
                onOptionSelected(index)
                dialog.dismiss() // Fechar o diálogo após a seleção
            }
        }
        optionsLayout.addView(textView)
    }

    dialog.show()
}
