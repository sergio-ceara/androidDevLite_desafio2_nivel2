package com.example.desafio2_nivel2_teste3

import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.desafio2_nivel2_teste3.databinding.ActivityCadastroTurmaBinding

class CadastroTurmaActivity : BaseActivity() {
    override fun getFragment(): Fragment {
        return CadastroTurmaFragment() // Substitua pelo fragmento que você deseja usar
    }
}
