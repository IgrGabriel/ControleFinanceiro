package com.igorgabriel.recyclerviewtransacoes.model

data class Transacao(
    val id: String,
    val descricao: String,
    val categoria: String,
    val valor: String,
    val tipo: String,
    val data: String
)
