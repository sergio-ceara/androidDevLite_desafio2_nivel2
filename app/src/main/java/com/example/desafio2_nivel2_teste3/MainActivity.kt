package com.example.desafio2_nivel2_teste3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.HeaderFooterLayoutBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: HeaderFooterLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate o layout com View Binding
        binding = HeaderFooterLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gerenciar os insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Substituir o FrameLayout pelo fragmento desejado
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.content.id, getFragment())
                .commit()
        }
    }

    // Método para retornar o fragmento desejado
    private fun getFragment(): Fragment {
        return MainFragment() // Altere se necessário
    }

}