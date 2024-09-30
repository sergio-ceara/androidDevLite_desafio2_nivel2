package com.example.desafio2_nivel2_teste3

data class Aluno(
    val id: Int,
    val nome: String,
    val turmaId: Int
)

data class AlunoComTurma(
    val id: Int,
    val nome: String,
    val turmaId: Int,
    val turmaNome: String,
    val notasQuant: Int,
    val media: Float,
    val status: String
)
