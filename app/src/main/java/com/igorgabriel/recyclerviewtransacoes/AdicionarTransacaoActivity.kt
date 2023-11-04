package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityAdicionarTransacaoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdicionarTransacaoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdicionarTransacaoBinding.inflate(layoutInflater)
    }

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupDatePicker()
        setupAddTransacao()
    }

    private fun setupAddTransacao() {
        binding.btnAdicionarTransacao.setOnClickListener {
            val descricao = binding.editAddDescricao.text.toString().trim()
            val categoria = binding.editAddCategoria.text.toString().trim()
            val valor = binding.editAddValor.text.toString().trim()
            val checkedId = binding.radioTipo.checkedRadioButtonId
            if (checkedId == -1) {
                exibirMensagem("Selecione um tipo")
                return@setOnClickListener
            }
            val tipo = findViewById<RadioButton>(checkedId).text.toString()

            try {
                val dataString = converterDataParaFormatoNumerico(binding.textData.text.toString())
                val data = converterStingParaData(dataString)

                if (descricao.isNotEmpty() && categoria.isNotEmpty() && valor.isNotEmpty()) {
                    adicionarTransacao(descricao, categoria, valor, tipo, data)
                } else {
                    exibirMensagem("Preencha todos os campos")
                }
            } catch (e: Exception) {
                exibirMensagem("Ocorreu um erro ao converter a data")
            }
        }
    }

    private fun setupDatePicker() {
        binding.ivMostrarCalendario.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                    val dataSelecionada = Calendar.getInstance()
                    dataSelecionada.set(anoSelecionado, mesSelecionado, diaSelecionado)

                    val formattedDate = SimpleDateFormat("EEE, dd MMM yyyy", Locale("pt", "BR")).format(dataSelecionada.time)

                    binding.textData.text = formattedDate
                },
                ano,
                mes,
                dia
            )
            datePickerDialog.show()
        }
    }

    private fun converterStingParaData(data: String): Data {
        val partes = data.split(" ")
        val dia = partes[0].toInt()
        val mes = partes[1].toInt()
        val ano = partes[2].toInt()

        return Data(dia, mes, ano)
    }

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


    private fun adicionarTransacao(
        descricao: String,
        categoria: String,
        valor: String,
        tipo: String,
        data: Data
    ) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val dados = mapOf(
                "descricao" to descricao,
                "categoria" to categoria,
                "valor" to valor,
                "tipo" to tipo,
                "data" to data
            )

            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")

            referenciaUsuario
                .add(dados)
                .addOnSuccessListener {
                    exibirMensagem("Transacao adicionada com sucesso")
                    finish()
                }.addOnFailureListener { exception ->
                    exibirMensagem("Falha ao adicionar transacao")
                }
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}