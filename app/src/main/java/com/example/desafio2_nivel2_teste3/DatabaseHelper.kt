package com.example.desafio2_nivel2_teste3

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "escola.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAlunoTable = """
            CREATE TABLE aluno (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                turma_id INTEGER,
                FOREIGN KEY(turma_id) REFERENCES turma(id)
            );
        """.trimIndent()

        val createTurmaTable = """
            CREATE TABLE turma (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL
            );
        """.trimIndent()

        val createNotasTable = """
            CREATE TABLE notas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                aluno_id INTEGER,
                nota REAL NOT NULL,
                FOREIGN KEY(aluno_id) REFERENCES aluno(id)
            );
        """.trimIndent()

        db.execSQL(createAlunoTable)
        db.execSQL(createTurmaTable)
        db.execSQL(createNotasTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS aluno")
        db.execSQL("DROP TABLE IF EXISTS turma")
        db.execSQL("DROP TABLE IF EXISTS notas")
        onCreate(db)
    }

    fun updateStatus(average: Float): Status {
        return if (average >= Status.APROVADO.limit) {
            Status.APROVADO
        } else {
            Status.REPROVADO
        }
    }
    fun turmaQuantAlunos(db: SQLiteDatabase, turmaId: Int): Int {
        var select = "select count(*) from aluno where turma_id = ${turmaId}"
        Log.d("turmaQuantAlunos"," select: ${select}")
        val cursor = db.rawQuery(select,null)
        var quant = 0
        while (cursor.moveToNext()) {
           quant = cursor.getInt(0)
        }
        return quant
    }
    fun alunosSelect(db: SQLiteDatabase, whereString: String): MutableList<AlunoComTurma> {
        var select = "SELECT \n" +
                "    aluno.id, \n" +
                "    aluno.nome, \n" +
                "    aluno.turma_id, \n" +
                "    turma.nome, \n" +
                "    COUNT(notas.id) AS quantidade_notas, \n" +
                "    IFNULL(SUM(notas.nota),0) AS soma_notas, \n" +
                "    IFNULL(SUM(notas.nota)/COUNT(notas.id),0) AS media\n" +
                "FROM \n" +
                "    aluno AS aluno \n" +
                "LEFT JOIN \n" +
                "    turma ON aluno.turma_id = turma.id\n" +
                "LEFT JOIN \n" +
                "    notas ON aluno.id = notas.aluno_id\n"
        if (whereString != "") {
            select+= " $whereString"
        }
        select+=" GROUP BY \n" +
                "    aluno.id, \n" +
                "    aluno.nome, \n" +
                "    aluno.turma_id, \n" +
                "    turma.nome;\n"
        Log.d("alunosSelect"," select: ${select}")
        val cursor = db.rawQuery(select,null)
        val alunos = mutableListOf<AlunoComTurma>()
        while (cursor.moveToNext()) {
            val aluno = AlunoComTurma(cursor.getInt(0), cursor.getString(1), cursor.getInt(2),
                cursor.getString(3), cursor.getInt(4), cursor.getFloat(6),
                updateStatus(cursor.getFloat(6)).toString())
            alunos.add(aluno)
        }
        cursor.close()
        return alunos
    }
    fun alunosLista(alunoList: MutableList<AlunoComTurma>): List<String> {
        val alunosNomes = alunoList.map { " ${it.nome} (${it.turmaNome}) " +
                " ${it.notasQuant} nota(s) média:${String.format("%.2f",it.media)}" +
                " ${if (it.notasQuant > 0) it.status else ""} "}
        return alunosNomes
    }
}
