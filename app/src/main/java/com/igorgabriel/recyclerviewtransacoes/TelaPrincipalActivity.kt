package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityTelaPrincipalBinding
import java.util.Calendar
import java.util.Locale

class TelaPrincipalActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaPrincipalBinding.inflate(layoutInflater)
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

        getSaldo()
        calcularBalanco()
        calcularDespesasDoDia()
    }

    private fun calcularDespesasDoDia() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorTotal = 0.0

        if (idUsuarioLogado != null) {

            val calendario = Calendar.getInstance()
            val mes = calendario.get(Calendar.MONTH) + 1
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val refUserReceita = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Despesa")
                .whereEqualTo("data.dia", dia)
                .whereEqualTo("data.mes", mes)

            refUserReceita.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()
                        valorTotal += valor.toDouble()
                    }
                }
                binding.textGastosHoje.text = formatarValor(valorTotal.toString())
            }
        }
    }

    private fun getSaldo() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val refUserSaldo = bancoDados.collection("usuarios").document(idUsuarioLogado)

            refUserSaldo.addSnapshotListener { documentSnapshot, error ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val saldo = dados["saldo"].toString()
                        binding.textSaldo.text = formatarValor(saldo)
                    }
                }
            }
        }
    }

    private fun filtrarValorTotalTransacao(tipo: String, callback: (String) -> Unit) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorTotal = 0.0

        if (idUsuarioLogado != null) {
            val refUserReceita = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", tipo)

            refUserReceita.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()
                        valorTotal += valor.toDouble()
                    }
                }
                // Chame o callback com o valorTotal após a consulta ser completada
                callback(valorTotal.toString())
            }
        } else {
            callback(valorTotal.toString())
        }
    }

    private fun calcularBalanco() {
        var receitas = 0.0
        var despesas = 0.0

        filtrarValorTotalTransacao("Receita") { valorTotal ->
            receitas = valorTotal.toDouble()
            binding.textBlReceita.text = formatarValor(valorTotal)
            atualizarBalanco(receitas, despesas)
        }

        filtrarValorTotalTransacao("Despesa") { valorTotal ->
            despesas = valorTotal.toDouble()
            binding.textBlDespesa.text = formatarValor(valorTotal)
            atualizarBalanco(receitas, despesas)
        }
    }

    private fun formatarValor(valor: String): String {
        // Converte a string para Double
        val valorNumerico = valor.toDoubleOrNull() ?: 0.0

        // Formata o Double para ter sempre duas casas decimais
        return String.format(Locale.US, "%.2f", valorNumerico)
    }

    private fun atualizarBalanco(receitas: Double, despesas: Double) {
        val balanco = receitas - despesas
        binding.textBalanco.text = formatarValor(balanco.toString())
    }
}