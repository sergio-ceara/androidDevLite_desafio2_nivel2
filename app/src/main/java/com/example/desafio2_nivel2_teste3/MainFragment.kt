package com.example.desafio2_nivel2_teste3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnCadastroAluno?.setOnClickListener {
            val intent = Intent(activity, CadastroAlunoActivity::class.java)
            startActivity(intent)
        }

        binding?.btnCadastroTurma?.setOnClickListener {
            val intent = Intent(activity, CadastroTurmaActivity::class.java)
            startActivity(intent)
        }

        binding?.btnCadastroNota?.setOnClickListener {
            val intent = Intent(activity, CadastroNotaActivity::class.java)
            startActivity(intent)
        }

        binding?.btnConsultaAlunosPorTurma?.setOnClickListener {
            val intent = Intent(activity, ConsultaAlunosPorTurmaActivity::class.java)
            startActivity(intent)
        }

        binding?.btnConsultaNotasPorAluno?.setOnClickListener {
            val intent = Intent(activity, ConsultaNotasPorAlunoActivity::class.java)
            startActivity(intent)
        }
        binding?.btnTabelasLimpar?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Deseja reiniciar o sistema?")
                .setMessage("Essa opção irá apagar todos os dados. Deseja continuar?")
                .setPositiveButton("Sim") { dialog, _ ->
                    var dbHelper: DatabaseHelper
                    dbHelper = DatabaseHelper(requireContext())
                    val db = dbHelper.writableDatabase
                    try {
                        dbHelper.onUpgrade(db, db.version, db.version+1)
                        Toast.makeText(requireContext(), "Sistema reiniciado com limpeza das tabelas", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Não foi possível reiniciar o sistema.", Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Não") { dialog, _ ->
                    dialog.dismiss() // Fechar o diálogo sem fazer nada
                }
                .show() // Mostrar o diálogo
        }
        (activity as? MainActivity)?.let { mainActivity ->
            //mainActivity.binding.btnTelaPrincipal.visibility = View.GONE
            mainActivity.binding.btnTelaPrincipal.text = "Sair"
            mainActivity.binding.btnTelaPrincipal.setOnClickListener{
                mainActivity.finish()
            }
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // Limpa a referência para evitar vazamento de memória
    }
}