package com.example.desafio2_nivel2_teste3

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.FragmentCadastroAlunoBinding

class CadastroAlunoFragment : Fragment() {

    private var binding: FragmentCadastroAlunoBinding? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var alunoAdapter: ArrayAdapter<String>
    private var alunoList = mutableListOf<AlunoComTurma>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCadastroAlunoBinding.inflate(inflater, container, false)
        return binding!!.root // Utilizando o operador `!!` já que `binding` foi inicializado
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Carregar turmas no Spinner (substituir com lógica real)
        val turmas = getTurmasFromDB() // Exemplo de função que busca as turmas
        val turmaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, turmas)
        turmaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinnerTurma?.adapter = turmaAdapter

        // Carregar alunos na ListView
        loadAlunos()

        binding?.btnSalvar?.setOnClickListener {
            val nome = binding?.editTextNome?.text.toString()
            val turmaIdStr = binding?.spinnerTurma?.selectedItem.toString()
            var turmaId = 0
            if (turmaIdStr.contains("-")) {
                turmaId = turmaIdStr.split("-")[0].toInt()
            }
            // Inserir aluno no banco de dados
            if (nome.isNotEmpty() && turmaId > 0) {
                inserirAluno(nome, turmaId)
                loadAlunos() // Atualizar a lista
            } else {
                Toast.makeText(requireContext(), "Complemente os campos para gravar.", Toast.LENGTH_LONG).show()
            }
        }

        binding?.listViewAlunos?.setOnItemClickListener { _, _, position, _ ->
            val aluno = alunoList[position]
            val notas = aluno.notasQuant
            val tituloExcluir = if (notas==0) "Excluir" else "Excluir (bloqueado)"
            showEditionDialog(requireContext(), "Opções de Edição", arrayOf("Alterar", tituloExcluir)) { selectedOption ->
                when (selectedOption) {
                    0 -> {exibirDialogoAlterarAluno(aluno)}
                    1 -> {if (notas==0) excluirAluno(aluno) else
                        Toast.makeText(requireContext(), "Exclusão bloqueada. Aluno com ${notas} nota(s) gravada(s).", Toast.LENGTH_LONG).show()}
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // Limpa a referência para evitar vazamento de memória
    }

    private fun loadAlunos() {
        alunoList = dbHelper.alunosSelect(dbHelper.readableDatabase, "")
        alunoAdapter = object : ArrayAdapter<String>(requireContext(), R.layout.list_item_medio, android.R.id.text1, dbHelper.alunosLista(alunoList)) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                // Altera a cor de fundo com base na posição
                if (position % 2 == 0) {
                    view.setBackgroundColor(Color.LTGRAY) // Linhas pares
                } else {
                    view.setBackgroundColor(Color.WHITE) // Linhas ímpares
                }
                return view
            }
        }
        binding?.listViewAlunos?.adapter = alunoAdapter
    }

    private fun inserirAluno(nome: String, turmaId: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", nome)
            put("turma_id", turmaId)
        }
        try {
            db.insert("aluno", null, values)
            binding?.editTextNome?.text?.clear()
            Toast.makeText(requireContext(), "Informação gravada.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Não foi possível gravar registro.", Toast.LENGTH_LONG).show()
        }

    }

    private fun exibirDialogoAlterarAluno(aluno: AlunoComTurma) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alterar_aluno, null)

        val editTextNome = dialogView.findViewById<EditText>(R.id.editTextAlterarNome)
        val spinnerTurma = dialogView.findViewById<Spinner>(R.id.spinnerAlterarTurma)
        val textTurma    = dialogView.findViewById<TextView>(R.id.textSpinnerTurmaAlteracao)

        editTextNome.setText(aluno.nome)
        if (aluno.notasQuant > 0) {
            // Cria a TextView dinamicamente
            val textViewInformacoes = TextView(requireContext()).apply {
                text = "Modificar turma bloqueado. Aluno com ${aluno.notasQuant} nota(s) registrada(s)."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(5, 5, 5, 5) // Adiciona algum padding
                setTextColor(ContextCompat.getColor(requireContext(), R.color.red1))
            }
            // Adiciona a TextView após o spinnerTurma
            val textTurmaIndex = (dialogView as LinearLayout).indexOfChild(textTurma)
            dialogView.addView(textViewInformacoes, textTurmaIndex + 1) // +1 para adicionar após
        }

        // Carrega as turmas e configura o spinner
        val turmas = getTurmasFromDB() // Função que busca as turmas
        val turmaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, turmas)
        turmaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTurma.adapter = turmaAdapter

        // Define a seleção do spinner de acordo com a turma atual do aluno
        spinnerTurma.setSelection(turmas.indexOfFirst { it.startsWith("${aluno.turmaId}-") })
        spinnerTurma.isEnabled = aluno.notasQuant == 0
        AlertDialog.Builder(requireContext())
            .setTitle("Alterar Aluno")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = editTextNome.text.toString()
                val novaTurmaId = spinnerTurma.selectedItem.toString().split("-")[0].toInt()
                alterarAluno(aluno.id, novoNome, novaTurmaId)
                loadAlunos()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun alterarAluno(id: Int, novoNome: String, novaTurma: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", novoNome)
            put("turma_id", novaTurma)
        }
        try {
            db.update("aluno", values, "id=?", arrayOf(id.toString()))
            Toast.makeText(requireContext(),"Registro gravado..", Toast.LENGTH_LONG)
        } catch (e: Exception) {
            Toast.makeText(requireContext(),"Falha ao alterar registro.", Toast.LENGTH_LONG)
        }

    }

    private fun excluirAluno(aluno: AlunoComTurma) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir aluno?")
            .setMessage("${aluno.nome} turma: ${aluno.turmaNome} ")
            .setPositiveButton("Sim") { dialog, _ ->
                val db = dbHelper.writableDatabase
                db.delete("aluno", "id=?", arrayOf(aluno.id.toString()))
                loadAlunos()
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss() // Fechar o diálogo sem fazer nada
            }
            .show() // Mostrar o diálogo

    }

    private fun getTurmasFromDB(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, nome FROM turma", null)
        val turmas = mutableListOf<String>()
        while (cursor.moveToNext()) {
            turmas.add("${cursor.getInt(0)}-${cursor.getString(1)}")
        }
        cursor.close()
        return turmas
    }
}
