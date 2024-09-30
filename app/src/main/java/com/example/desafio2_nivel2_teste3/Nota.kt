package com.example.desafio2_nivel2_teste3

data class Nota(
    val id: Int,
    val alunoId: Int,
    val nota: Float
)

data class NotaComAluno(
    val id: Int,
    val alunoId: Int,
    val alunoNome: String,
    val AlunoTurna: String,
    val nota: Float
)
