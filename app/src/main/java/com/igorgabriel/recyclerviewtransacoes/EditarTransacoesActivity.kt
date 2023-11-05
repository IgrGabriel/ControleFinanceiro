package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityEditarTransacoesBinding
import com.igorgabriel.recyclerviewtransacoes.model.Data
import com.igorgabriel.recyclerviewtransacoes.model.converterDataParaFormatoNumerico
import com.igorgabriel.recyclerviewtransacoes.model.converterStingParaData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditarTransacoesActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEditarTransacoesBinding.inflate(layoutInflater)
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

        val bundle = intent.extras // Todos os parametros passados da outra tela

        if (bundle != null) {
            val id = bundle.getString("id").toString()
            val descricao = bundle.getString("descricao")
            val categoria = bundle.getString("categoria")
            val valor = bundle.getString("valor")
            val tipo = bundle.getString("tipo")
            val data = bundle.getString("data")

            binding.editDescricao.setText(descricao)
            binding.editCategoria.setText(categoria)
            binding.editValor.setText(valor)
            binding.editData.setText(data)
            binding.radioTipo.setText(tipo)

            binding.btnEditar.setOnClickListener {
                val descricao = binding.editDescricao.text.toString().trim()
                val categoria = binding.editCategoria.text.toString().trim()
                val valor = binding.editValor.text.toString().trim()
                val tipo = binding.radioTipo.text.toString().trim()

                try {
                    val dataString = converterDataParaFormatoNumerico(binding.editData.text.toString())
                    val data = converterStingParaData(dataString)

                    if (descricao.isNotEmpty() && categoria.isNotEmpty() && valor.isNotEmpty()) {
                        editarTransacao(id, descricao, categoria, valor, tipo, data)
                    } else {
                        exibirMensagem("Preencha todos os campos")
                    }
                } catch (e: Exception) {
                    exibirMensagem("Ocorreu um erro ao converter a data")
                }
            }
        }

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

                    binding.editData.text = SimpleDateFormat("EEE, dd MMM yyyy").format(dataSelecionada.time)
                },
                ano,
                mes,
                dia
            )
            datePickerDialog.show()
        }

    }

    private fun editarTransacao(id: String, descricao: String, categoria: String, valor: String, tipo: String, data: Data) {
        val idUsuarioLogado = autenticacao.currentUser?.uid

        val valorFormatado = formatarValor(valor)

        if(idUsuarioLogado != null) {
            val dados = mapOf(
                "descricao" to descricao,
                "categoria" to categoria,
                "valor" to valorFormatado,
                "tipo" to tipo,
                "data" to data
            )

            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .document( id )

            referenciaUsuario
                .set(dados)
                .addOnSuccessListener {
                    exibirMensagem("Transacao atualizada com sucesso")
                    finish()
                }.addOnFailureListener { exception ->
                    exibirMensagem("Falha ao atualizar transacao")
                }
        }
    }

    private fun formatarValor(valor: String): String {
        // Converte a string para Double
        val valorNumerico = valor.toDoubleOrNull() ?: 0.0

        // Formata o Double para ter sempre duas casas decimais
        return String.format(Locale.US, "%.2f", valorNumerico)
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}