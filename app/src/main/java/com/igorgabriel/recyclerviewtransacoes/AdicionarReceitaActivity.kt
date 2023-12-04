package com.igorgabriel.recyclerviewtransacoes

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityAdicionarReceitaBinding
import com.igorgabriel.recyclerviewtransacoes.model.Data
import com.igorgabriel.recyclerviewtransacoes.model.converterDataParaFormatoNumerico
import com.igorgabriel.recyclerviewtransacoes.model.converterStingParaData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdicionarReceitaActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityAdicionarReceitaBinding.inflate(layoutInflater)
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
        val idUsuarioLogado = autenticacao.currentUser?.uid

        if (idUsuarioLogado != null) {
            val listCategoria = mutableListOf<String>()

            listCategoria.add("Selecione uma categoria")

            val adapter = ArrayAdapter(
                this,
                R.layout.simple_spinner_dropdown_item,
                listCategoria
            )

            binding.spinnerCategoria.adapter = adapter

            bancoDados
                .collection("usuarios/${idUsuarioLogado}/categorias")
                .addSnapshotListener { value, error ->
                    if (error != null){
                        Log.e("Firebase", "Erro ao escutar alterações: $error")
                        return@addSnapshotListener
                    }

                    listCategoria.clear()
                    listCategoria.add("Selecione uma categoria")

                    for (document in value!!) {
                        val nome = document.getString("nome")
                        val tipo = document.getString("tipo")

                        if (tipo.equals("Receita")) {
                            listCategoria.add(nome.toString())
                        }
                    }

                    listCategoria.add("+ Adicionar nova categoria")

                    adapter.notifyDataSetChanged()

                    binding.spinnerCategoria.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val selectedItem = listCategoria[position]

                                if (selectedItem == "+ Adicionar nova categoria") {
                                    // Inicie a nova atividade para adicionar uma nova categoria
                                    val intent = Intent(
                                        this@AdicionarReceitaActivity,
                                        AdicionarCategoriaActivity::class.java
                                    )
                                    intent.putExtra("tipo", "Receita")
                                    startActivity(intent)

                                    // Reverte a seleção do Spinner para o item anterior
                                    binding.spinnerCategoria.setSelection(0)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                //nenhum item selecionado
                            }
                        }
                }
        }
    }


    private fun setupAddTransacao() {
        binding.btnAdicionarTransacao.setOnClickListener {
            val descricao = binding.editAddDescricao.text.toString().trim()
            val categoria = binding.spinnerCategoria.selectedItem.toString()
            val valor = formatarValor(binding.editAddValor.text.toString().trim())
            val tipo = binding.radioTipo.text.toString().trim()

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
        // Formata o Double para ter sempre duas casas decimais
        return String.format(Locale.US, "%.2f", valor.toDoubleOrNull() ?: 0.0)
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
        val dataFormatada =
            SimpleDateFormat("EEE, dd MMM yyyy", Locale("pt", "BR")).format(calendar.time)
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