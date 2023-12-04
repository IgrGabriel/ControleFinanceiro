package com.igorgabriel.recyclerviewtransacoes.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.R
import java.util.Calendar
import java.util.Locale


class HomeFragment : Fragment() {

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        calcularSaldo()
        calcularDespesasDoDia()
        calcularReceitasDoDia()
        calcularBalanco()

        return view
    }

    private fun calcularDespesasDoDia() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorTotal = 0.0

        if (idUsuarioLogado != null) {

            val refUserReceita = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Despesa")
                .whereEqualTo("data.dia", Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .whereEqualTo("data.mes", Calendar.getInstance().get(Calendar.MONTH) + 1)

            refUserReceita.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()
                        valorTotal += valor.toDouble()
                    }
                }
                view?.findViewById<TextView>(R.id.text_gastos_hoje)?.text = formatarValor(valorTotal.toString())

            }
        }
    }

    private fun calcularReceitasDoDia() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorTotal = 0.0

        if (idUsuarioLogado != null) {

            val refUserReceita = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Receita")
                .whereEqualTo("data.dia", Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .whereEqualTo("data.mes", Calendar.getInstance().get(Calendar.MONTH) + 1)

            refUserReceita.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()
                        valorTotal += valor.toDouble()
                    }
                }
                view?.findViewById<TextView>(R.id.text_receitas_hoje)?.text = formatarValor(valorTotal.toString())
            }
        }
    }

    private fun filtrarValorTotalTransacao(tipo: String, filtro: Boolean, callback: (String) -> Unit) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorTotal = 0.0

        if (idUsuarioLogado != null) {
            val refUserTransacao = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")

            val query = if (filtro) {
                refUserTransacao.whereEqualTo("tipo", tipo)
                    .whereEqualTo("data.mes", Calendar.getInstance().get(Calendar.MONTH) + 1)
            } else {
                refUserTransacao.whereEqualTo("tipo", tipo)
            }

            query.addSnapshotListener { querySnapshot, error ->
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

    private fun calcularSaldo() {
        var receitas = 0.0
        var despesas = 0.0

        filtrarValorTotalTransacao("Receita", false) { valorTotal ->
            receitas = valorTotal.toDouble()
            atualizarSaldo(receitas, despesas)
        }

        filtrarValorTotalTransacao("Despesa", false) { valorTotal ->
            despesas = valorTotal.toDouble()
            atualizarSaldo(receitas, despesas)
        }
    }

    private fun calcularBalanco() {
        var receitas = 0.0
        var despesas = 0.0

        filtrarValorTotalTransacao("Receita", true) { valorTotal ->
            receitas = valorTotal.toDouble()
            view?.findViewById<TextView>(R.id.text_bl_receita)?.text = formatarValor(valorTotal)
            atualizarBalanco(receitas, despesas)
        }

        filtrarValorTotalTransacao("Despesa", true) { valorTotal ->
            despesas = valorTotal.toDouble()
            view?.findViewById<TextView>(R.id.text_bl_despesa)?.text = formatarValor(valorTotal)
            atualizarBalanco(receitas, despesas)
        }
    }

    private fun atualizarSaldo(receitas: Double, despesas: Double){
        val saldo = receitas - despesas
        view?.findViewById<TextView>(R.id.text_saldo)?.text = formatarValor(saldo.toString())
    }

    private fun atualizarBalanco(receitas: Double, despesas: Double) {
        val balanco = receitas - despesas
        view?.findViewById<TextView>(R.id.text_balanco)?.text = formatarValor(balanco.toString())
    }

    private fun formatarValor(valor: String): String {
        // Formata o Double para ter sempre duas casas decimais
        return String.format(Locale.US, "%.2f", valor.toDoubleOrNull() ?: 0.0)
    }

}