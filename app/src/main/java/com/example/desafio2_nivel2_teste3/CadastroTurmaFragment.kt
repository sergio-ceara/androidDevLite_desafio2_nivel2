package com.example.desafio2_nivel2_teste3

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.FragmentCadastroTurmaBinding

class CadastroTurmaFragment : Fragment() {

    private var binding: FragmentCadastroTurmaBinding? = null
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var turmaAdapter: ArrayAdapter<String>
    private var turmaList = mutableListOf<Turma>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCadastroTurmaBinding.inflate(inflater, container, false)
        return binding!!.root // Utilizando o operador `!!` já que `binding` foi inicializado
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Carregar turmas na ListView
        loadTurmas()

        binding?.btnSalvarTurma?.setOnClickListener {
            val nome = binding?.editTextNomeTurma?.text.toString()
            // Inserir turma no banco de dados
            if (nome.isNotEmpty()) {
                inserirTurma(nome)
                loadTurmas() // Atualizar a lista
            } else {
                Toast.makeText(context, "Complete a digitação para salvar.", Toast.LENGTH_LONG).show()
            }
        }
        // Exibir opções de alterar ou excluir
        binding?.listViewTurmas?.setOnItemClickListener { _, _, position, _ ->
            val turma = turmaList[position]
            val quantAlunos = dbHelper.turmaQuantAlunos(dbHelper.writableDatabase, turma.id)
            val tituloExcluir = if (quantAlunos == 0) "Excluir" else "Excluir (bloqueado)"
            showEditionDialog(requireContext(), "Opções de Edição", arrayOf("Alterar", tituloExcluir)) { selectedOption ->
                when (selectedOption) {
                    0 -> {exibirDialogoAlterarTurma(turma)}
                    1 -> {if (quantAlunos == 0) excluirTurma(turma) else
                              Toast.makeText(context, "Exclusão bloqueada. Turma com ${quantAlunos} aluno(s) cadastrados(s).", Toast.LENGTH_LONG).show()}
                }
            }
        }
    }
    // Função para carregar as turmas do banco e exibir na ListView
    private fun loadTurmas() {
        turmaList = getTurmasFromDB() // Exemplo de função que busca as turmas do banco
        val turmaNomes = turmaList.map { "${it.id}-${it.nome}" }
        //turmaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, turmaNomes)
        turmaAdapter = object : ArrayAdapter<String>(requireContext(), R.layout.list_item_medio, android.R.id.text1, turmaNomes) {
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
        binding?.listViewTurmas?.adapter = turmaAdapter
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

    // Função para inserir turma no banco de dados
    private fun inserirTurma(nome: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", nome)
        }
        try {
            db.insert("turma", null, values)
            binding?.editTextNomeTurma?.text?.clear()
            Toast.makeText(requireContext(), "Turma gravada.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Não foi possível gravar a turma.", Toast.LENGTH_LONG).show()
        }
    }

    // Função para exibir o diálogo de alteração de turma
    private fun exibirDialogoAlterarTurma(turma: Turma) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alterar_turma, null)
        val editTextNome = dialogView.findViewById<EditText>(R.id.editTextAlterarNomeTurma)
        editTextNome.setText(turma.nome)

        AlertDialog.Builder(requireContext())
            .setTitle("Alterar Turma")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = editTextNome.text.toString()
                alterarTurma(turma.id, novoNome)
                loadTurmas() // Atualizar a lista
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Função para alterar turma no banco de dados
    private fun alterarTurma(id: Int, novoNome: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nome", novoNome)
        }
        db.update("turma", values, "id=?", arrayOf(id.toString()))
    }

    // Função para excluir turma do banco de dados
    private fun excluirTurma(turma: Turma) {
        //val db = dbHelper.writableDatabase
        //db.delete("turma", "id=?", arrayOf(id.toString()))
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir turma?")
            .setMessage("${turma.id}-${turma.nome}")
            .setPositiveButton("Sim") { dialog, _ ->
                val db = dbHelper.writableDatabase
                db.delete("turma", "id=?", arrayOf(turma.id.toString()))
                loadTurmas() // Atualizar a lista
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss() // Fechar o diálogo sem fazer nada
            }
            .show() // Mostrar o diálogo
    }

    // Exemplo de função para buscar turmas do banco (substitua por sua lógica real)
    private fun getTurmasFromDB(): MutableList<Turma> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM turma", null)
        val turmas = mutableListOf<Turma>()
        while (cursor.moveToNext()) {
            val turma = Turma(cursor.getInt(0), cursor.getString(1))
            turmas.add(turma)
        }
        cursor.close()
        return turmas
    }
}
