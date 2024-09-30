package com.example.desafio2_nivel2_teste3

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.DialogAlterarNotaBinding
import com.example.desafio2_nivel2_teste3.databinding.FragmentCadastroNotaBinding

class CadastroNotaFragment : Fragment() {

    private lateinit var binding: FragmentCadastroNotaBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var alunoAdapter: ArrayAdapter<String>
    private var alunoList = mutableListOf<String>()
    private var filteredAlunoList = mutableListOf<String>()
    private lateinit var notaAdapter: ArrayAdapter<String>
    private var notaList = mutableListOf<NotaComAluno>()
    private var alunoIdSelecionado: Int = 0 // Para armazenar o ID do aluno selecionado

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCadastroNotaBinding.inflate(inflater, container, false)
        return binding!!.root // Utilizando o operador `!!` já que `binding` foi inicializado
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Carregar alunos do banco de dados
        alunoList = getAlunosFromDB().toMutableList()
        filteredAlunoList.addAll(alunoList)

        // Configurar Adapter para ListView
        alunoAdapter = ArrayAdapter(requireContext(), R.layout.list_item_curto, android.R.id.text1, filteredAlunoList)
        binding.listViewAlunos.adapter = alunoAdapter

        // Configurar filtro
        binding.editTextFiltroAluno.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filtro = s.toString().lowercase()
                filteredAlunoList.clear()
                filteredAlunoList.addAll(alunoList.filter { it.lowercase().contains(filtro) })
                alunoAdapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Carregar notas na ListView
        loadNotas()
        // selecionar item da ListView dos alunos
        binding.listViewAlunos.setOnItemClickListener { _, _, position, _ ->
            var alunoSelecionado = binding.listViewAlunos.getItemAtPosition(position).toString()
            binding.textAlunoSelecionado.text = alunoSelecionado
            var alunoSelecionadoIdString = alunoSelecionado.split("-")[0].toString()
            try {
                alunoIdSelecionado = alunoSelecionadoIdString.toInt()
            } catch (e: NumberFormatException) {
                alunoIdSelecionado = 0
            }
        }
        binding.btnSalvarNota.setOnClickListener {
            var nota = 0f
            try {
                nota = binding.editTextNota.text.toString().toFloat()
            } catch (e: NumberFormatException) {
                nota = 0f
            }
            // Inserir nota no banco de dados
            if (alunoIdSelecionado > 0 && (nota > 0f && nota <= 10f)) {
                //Log.d("btnSalvarNota","alunoId:nota>=0 ${alunoIdSelecionado} nota: ${nota}")
                inserirNota(alunoIdSelecionado, nota)
                limparEdicao()
                loadNotas() // Atualizar a lista
            } else {
                if (alunoIdSelecionado == 0) {
                    Toast.makeText(requireContext(), "Selecione um aluno para salvar. Tente novamente.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Nota inválida. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.listViewNotas.setOnItemClickListener { _, _, position, _ ->
            val nota = notaList[position]
            // Exibir opções de alterar ou excluir
            showEditionDialog(requireContext(), "Opções de Edição", arrayOf("Alterar", "Excluir")) { selectedOption ->
                when (selectedOption) {
                    0 -> {exibirDialogoAlterarNota(nota)}
                    1 -> {excluirNota(nota)
                        loadNotas()
                    }
                }
            }
        }
    }
    // limpar edição após inclusão
    private fun limparEdicao() {
        binding.textAlunoSelecionado.text = ""
        binding.editTextNota.text.clear()

    }
    // Função para carregar as notas do banco e exibir na ListView
    private fun loadNotas() {
        notaList = getNotasFromDB() // Exemplo de função que busca as notas do banco
        val notasInfo = notaList.map { "${it.alunoNome} (${it.AlunoTurna}) Nota: ${String.format("%.2f",it.nota)}" }
        notaAdapter = object : ArrayAdapter<String>(requireContext(), R.layout.list_item_medio, android.R.id.text1, notasInfo) {
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
        binding.listViewNotas.adapter = notaAdapter
    }

    // Função para inserir nota no banco de dados
    private fun inserirNota(alunoId: Int, nota: Float) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("aluno_id", alunoId)
            put("nota", nota)
        }
        db.insert("notas", null, values)
    }

    // Função para exibir o diálogo de alteração de nota
    private fun exibirDialogoAlterarNota(nota: NotaComAluno) {
        // Inflate do layout usando ViewBinding
        val dialogBinding = DialogAlterarNotaBinding.inflate(layoutInflater)
        dialogBinding.editTextAlterarNota.setText(nota.nota.toString())
        AlertDialog.Builder(requireContext())
            .setTitle(" ${nota.alunoNome} turma: ${nota.AlunoTurna} ")
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar") { _, _ ->
                try {
                    val novaNota = dialogBinding.editTextAlterarNota.text.toString().toFloat()
                    if (novaNota > 0 && novaNota <= 10) {
                        alterarNota(nota.id, novaNota)
                        loadNotas() // Atualizar a lista
                    } else {
                        Toast.makeText(requireContext(), "Nota inválida. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Erro ao salvar a nota: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Função para alterar nota no banco de dados
    private fun alterarNota(id: Int, novaNota: Float) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nota", novaNota)
        }
        try {
            db.update("notas", values, "id=?", arrayOf(id.toString()))
            Toast.makeText(requireContext(),"Registro gravado..", Toast.LENGTH_LONG)
        } catch (e: Exception) {
            Toast.makeText(requireContext(),"Falha ao alterar registro.", Toast.LENGTH_LONG)
        }
    }

    // Função para excluir nota do banco de dados
    private fun excluirNota(nota: NotaComAluno) {
        if (nota.id <= 0) return // Retorna se o ID for inválido
        // Criar um diálogo de confirmação
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir nota?")
            .setMessage("${nota.alunoNome} turma: ${nota.AlunoTurna} nota: ${String.format("%.2f",nota.nota)}")
            .setPositiveButton("Sim") { dialog, _ ->
                val db = dbHelper.writableDatabase
                db.delete("notas", "id=?", arrayOf(nota.id.toString()))
                loadNotas() // Atualizar a lista
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss() // Fechar o diálogo sem fazer nada
            }
            .show() // Mostrar o diálogo
    }

    // Exemplo de funções para buscar dados do banco (substitua por sua lógica real)
    private fun getAlunosFromDB(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id,nome FROM aluno", null)
        val alunos = mutableListOf<String>()
        while (cursor.moveToNext()) {
            alunos.add(cursor.getString(0)+"-"+cursor.getString(1))
        }
        cursor.close()
        return alunos
    }

    private fun getNotasFromDB(): MutableList<NotaComAluno> {
        val db = dbHelper.readableDatabase
        var cursor = db.rawQuery("select * from notas;", null)
        cursor = db.rawQuery("SELECT nota.id, nota.aluno_id, aluno.nome AS alunoNome, turma.nome AS turmaNome, nota.nota FROM notas AS nota INNER JOIN aluno ON nota.aluno_id = aluno.id INNER JOIN turma ON aluno.turma_id = turma.id;", null)
        val notas = mutableListOf<NotaComAluno>()
        while (cursor.moveToNext()) {
            val nota = NotaComAluno(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getFloat(4))
            notas.add(nota)
        }
        cursor.close()
        return notas
    }
}
