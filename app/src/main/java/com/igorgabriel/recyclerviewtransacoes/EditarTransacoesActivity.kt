package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityEditarTransacoesBinding
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

            binding.btnEditar.setOnClickListener {
                val descricao = binding.editDescricao.text.toString().trim()
                val categoria = binding.editCategoria.text.toString().trim()
                val valor = binding.editValor.text.toString().trim()
                //val data = binding.editData.text.toString()

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

                //editarTransacao(id, descricao, categoria, valor, tipo, data)
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

    private fun editarTransacao(id: String, descricao: String, categoria: String, valor: String, tipo: String?, data: Data) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null) {
            val dados = mapOf(
                "descricao" to descricao,
                "categoria" to categoria,
                "valor" to valor,
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
    private fun converterStingParaData(data: String): Data {
        val partes = data.split(" ")
        val dia = partes[0].toInt()
        val mes = partes[1].toInt()
        val ano = partes[2].toInt()

        return Data(dia, mes, ano)
    }


    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}