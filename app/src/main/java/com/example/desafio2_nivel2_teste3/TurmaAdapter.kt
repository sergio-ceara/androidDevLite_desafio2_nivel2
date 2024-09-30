package com.example.desafio2_nivel2_teste3

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class TurmaAdapter(context: Context, private val turmas: List<String>) : ArrayAdapter<String>(context, 0, turmas) {
    private var selectedPosition = -1
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_selecionado, parent, false)
        val textView = view.findViewById<TextView>(R.id.item_selecionado)
        textView.text = turmas[position]
        // Altera a cor de fundo dependendo se o item está selecionado
        if (position == selectedPosition) {
            val corFundoSelecionado = ContextCompat.getColor(context, R.color.blue3)
            val corLetraSelecionado = ContextCompat.getColor(context, R.color.white)
            view.setBackgroundColor(corFundoSelecionado) // ou qualquer outra cor
            textView.setTextColor(Color.WHITE)
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
            textView.setTextColor(Color.BLACK)
        }
        return view
    }
    fun setSelectedPosition(position: Int) {
        //Log.d("TurmaAdapter","TurmaAdapter: position: ${position}")
        selectedPosition = position
        notifyDataSetChanged()
    }
}
