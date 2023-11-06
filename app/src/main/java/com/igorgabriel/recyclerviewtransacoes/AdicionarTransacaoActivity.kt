package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityAdicionarTransacaoBinding
import com.igorgabriel.recyclerviewtransacoes.model.Data
import com.igorgabriel.recyclerviewtransacoes.model.converterDataParaFormatoNumerico
import com.igorgabriel.recyclerviewtransacoes.model.converterStingParaData
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

        spinnerExibicao()
        setupDatePicker()
        setupAddTransacao()
    }


    private fun spinnerExibicao() {

        val listCategoria = listOf(
            "Selecione uma categoria", "Alimentação", "Saúde", "Educação", "Compras", "Moradia", "Lazer", "Roupas", "Transporte", "Salário",
            "Investimentos", "Presentes", "Salário", "Outros"
        )

        binding.spinnerCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listCategoria
        )
    }

    private fun setupAddTransacao() {
        binding.btnAdicionarTransacao.setOnClickListener {
            val descricao = binding.editAddDescricao.text.toString().trim()
            val categoria = binding.spinnerCategoria.selectedItem.toString()
            val valor = formatarValor(binding.editAddValor.text.toString().trim())
            val checkedId = binding.radioTipo.checkedRadioButtonId

            if (checkedId == -1) {
                exibirMensagem("Selecione um tipo")
                return@setOnClickListener
            }

            val tipo = findViewById<RadioButton>(checkedId).text.toString()

            try {
                val dataString = converterDataParaFormatoNumerico(binding.textData.text.toString())
                val data = converterStingParaData(dataString)

                if (descricao.isNotEmpty() && !categoria.equals("Selecione uma categoria") && valor.isNotEmpty()) {
                    adicionarTransacao(descricao, categoria, valor, tipo, data)
                } else {
                    exibirMensagem("Preencha todos os campos")
                }
            } catch (e: Exception) {
                exibirMensagem("Ocorreu um erro ao converter a data")
            }
        }
    }

    private fun formatarValor(valor: String): String {
        // Converte a string para Double
        val valorNumerico = valor.toDoubleOrNull() ?: 0.0

        // Formata o Double para ter sempre duas casas decimais
        return String.format(Locale.US, "%.2f", valorNumerico)
    }


    private fun setupDatePicker() {
        val calendario = Calendar.getInstance()

        updateTextViewData(calendario)  // Atualiza a UI com a data atual

        binding.ivMostrarCalendario.setOnClickListener {
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                    val dataSelecionada = Calendar.getInstance()
                    dataSelecionada.set(anoSelecionado, mesSelecionado, diaSelecionado)

                    updateTextViewData(dataSelecionada)
                },
                ano,
                mes,
                dia
            )
            datePickerDialog.show()
        }
    }

    private fun updateTextViewData(calendar: Calendar) {
        // Formata a data e atualiza o texto no TextView
        val dataFormatada = SimpleDateFormat("EEE, dd MMM yyyy", Locale("pt", "BR")).format(calendar.time)
        binding.textData.text = dataFormatada
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