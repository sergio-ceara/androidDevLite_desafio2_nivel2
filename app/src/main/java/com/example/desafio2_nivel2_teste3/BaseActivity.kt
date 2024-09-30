package com.example.desafio2_nivel2_teste3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.HeaderFooterLayoutBinding

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var binding: HeaderFooterLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HeaderFooterLayoutBinding.inflate(layoutInflater)
        val currentActivityName = this.javaClass.simpleName ?: "Unknown Activity"
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.content.id, getFragment())
                .commit()
        }

        binding.btnTelaPrincipal.visibility = View.VISIBLE
        binding.btnTelaPrincipal.setOnClickListener {
            Toast.makeText(this, "${currentActivityName}: Retornando para tela principal", Toast.LENGTH_LONG).show()
            this.finish()
        }
    }

    protected abstract fun getFragment(): Fragment

    override fun onDestroy() {
        super.onDestroy()
        // Libera referências para evitar vazamentos de memória (opcional)
    }
}