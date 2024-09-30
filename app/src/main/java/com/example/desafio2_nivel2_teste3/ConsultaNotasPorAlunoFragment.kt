package com.example.desafio2_nivel2_teste3

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.FragmentConsultaNotasPorAlunoBinding

class ConsultaNotasPorAlunoFragment : Fragment() {

    private var binding: FragmentConsultaNotasPorAlunoBinding? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var notaAdapter: ArrayAdapter<String>
    private var notaList = mutableListOf<NotaComAluno>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConsultaNotasPorAlunoBinding.inflate(inflater, container, false)
        return binding!!.root // Utilizando o operador `!!` já que `binding` foi inicializado
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Carregar todas as notas ao abrir a Activity
        //notaAdapter = ArrayAdapter<String>(this, R.layout.list_item_medio, android.R.id.text1, notaList.map{""})
        notaAdapter = object : ArrayAdapter<String>(requireContext(), R.layout.list_item_medio, android.R.id.text1, notaList.map{""}) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.LTGRAY) // Linhas pares
                } else {
                    view.setBackgroundColor(Color.WHITE) // Linhas ímpares
                }
                return view
            }
        }
        binding?.listViewNotas?.adapter = notaAdapter
        getNotasFromDB("")

        // Configurar filtro de aluno
        binding?.editTextFiltroAluno?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filtro = s.toString()
                getNotasFromDB(filtro)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        (activity as? MainActivity)?.let { mainActivity ->
            mainActivity.binding.btnTelaPrincipal.setOnClickListener {
                val currentActivityName = activity?.javaClass?.simpleName ?: "Unknown Activity"
                Toast.makeText(context, "${currentActivityName}: Retornando para tela principal.", Toast.LENGTH_LONG).show()
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
    }
    // Exemplo de funções para buscar dados do banco (substitua por sua lógica real)
    private fun getNotasFromDB(alunoNome:String) {
        val db = dbHelper.readableDatabase
        var select = "SELECT nota.id, nota.aluno_id, IFNULL(aluno.nome,''), IFNULL(turma.nome,''), IFNULL(nota.nota,0) FROM notas as nota left JOIN aluno ON nota.aluno_id = aluno.id left join turma on aluno.turma_id = turma.id"
        if (alunoNome != "") {
            select+=" where lower(aluno.nome) like '%${alunoNome.lowercase()}%' "
        }
        val cursor = db.rawQuery(select, null)
        val notas = mutableListOf<NotaComAluno>()
        while (cursor.moveToNext()) {
            val nota = NotaComAluno(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4))
            notas.add(nota)
        }
        cursor.close()
        val notasInfo = notas.map { "${it.alunoNome} turma:${it.AlunoTurna} nota:${String.format("%.2f",it.nota)}" }
        notaAdapter.clear()
        notaAdapter.addAll(notasInfo)
        notaAdapter.notifyDataSetChanged()
    }
}
