package com.igorgabriel.recyclerviewtransacoes.model

import java.util.Locale

data class Data(
    val dia: Int,
    val mes: Int,
    val ano: Int
)

fun converterDataParaFormatoNumerico(data: String): String {
    val meses = listOf(
        "jan", "fev", "mar", "abr", "mai", "jun",
        "jul", "ago", "set", "out", "nov", "dez"
    )

    val partes = data.split(" ")
    val dia = partes[1].removeSuffix(".")
    val mes = (
            meses.indexOf( // retorna o indice da primeria ocorrencia
                partes[2]
                    .lowercase(Locale.ROOT)
                    .removeSuffix(".")) + 1
            ).toString()
        .padStart(2, '0') //  garante que essa string tenha pelo menos 2 caracteres, preenchendo com 0 à esquerda se necessário
    val ano = partes[3]

    return "$dia $mes $ano"
}

fun converterStingParaData(data: String): Data {
    val partes = data.split(" ")
    val dia = partes[0].toInt()
    val mes = partes[1].toInt()
    val ano = partes[2].toInt()

    return Data(dia, mes, ano)
}