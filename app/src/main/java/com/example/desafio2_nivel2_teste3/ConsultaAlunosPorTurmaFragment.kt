package com.example.desafio2_nivel2_teste3

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.FragmentConsultaAlunosPorTurmaBinding

class ConsultaAlunosPorTurmaFragment : Fragment() {

    private lateinit var binding: FragmentConsultaAlunosPorTurmaBinding
    private lateinit var dbHelper: DatabaseHelper
    //private lateinit var turmaAdapter: ArrayAdapter<String>
    private lateinit var turmaAdapter: TurmaAdapter
    private lateinit var alunoAdapter: ArrayAdapter<String>
    private var alunoList = mutableListOf<AlunoComTurma>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConsultaAlunosPorTurmaBinding.inflate(inflater, container, false)
        return binding!!.root // Utilizando o operador `!!` já que `binding` foi inicializado
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Carregar turmas no ListView
        val turmas = getTurmasFromDB().toMutableList()
        turmas.add(0, "Geral") // Adiciona a opção "Geral" no início da lista

        //turmaAdapter = ArrayAdapter(requireContext(), R.layout.list_item_curto, android.R.id.text1, turmas)
        //binding.listViewTurmas.adapter = turmaAdapter // Ajuste para a nova ListView
        turmaAdapter = TurmaAdapter(requireContext(), turmas)
        binding.listViewTurmas.adapter = turmaAdapter

        binding.listViewTurmas.setOnItemClickListener { _, _, position, _ ->
            val turmaSelecionada = turmas[position]
            loadAlunosPorTurma(turmaSelecionada) // Carregar alunos da turma selecionada
            turmaAdapter.setSelectedPosition(position) // Atualiza a posição selecionada
        }
        /*
        // Carregar alunos quando uma turma for selecionada
        binding.listViewTurmas.setOnItemClickListener { _, _, position, _ ->
            val turmaSelecionada = turmas[position]
            loadAlunosPorTurma(turmaSelecionada) // Carregar alunos da turma selecionada
        }
        */
        // Configurar filtro de aluno
        binding.editTextFiltroAluno.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val filtro = s.toString()
                filtrarAlunos(filtro) // Filtrar a lista de alunos
            }
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

    // Função para carregar alunos por turma e exibir na ListView
    private fun loadAlunosPorTurma(turma: String) {
        alunoList = if (turma == "Geral") {
            dbHelper.alunosSelect(dbHelper.readableDatabase, "") // Carregar todos os alunos
        } else {
            dbHelper.alunosSelect(dbHelper.readableDatabase, " where turma.nome = '${turma}'")
        }
        adapterAluno(alunoList)
    }

    // Função para filtrar alunos com base no nome
    private fun filtrarAlunos(filtro: String) {
        alunoList = dbHelper.alunosSelect(dbHelper.readableDatabase, " where lower(aluno.nome) like '%${filtro.lowercase()}%'")
        adapterAluno(alunoList)
    }
    private fun adapterAluno(alunoList: MutableList<AlunoComTurma>) {
        alunoAdapter = object : ArrayAdapter<String>(requireContext(), R.layout.list_item_medio, android.R.id.text1, dbHelper.alunosLista(alunoList)) {
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
        binding.listViewAlunos.adapter = alunoAdapter
    }
    // Exemplo de funções para buscar dados do banco (substitua por sua lógica real)
    private fun getTurmasFromDB(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT nome FROM turma", null)
        val turmas = mutableListOf<String>()
        while (cursor.moveToNext()) {
            turmas.add(cursor.getString(0))
        }
        cursor.close()
        return turmas
    }
}
