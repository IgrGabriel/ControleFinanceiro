package com.igorgabriel.recyclerviewtransacoes.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.R
import java.util.Calendar


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

        getSaldo()
        filtrarDespesas()
        filtrarReceitas()
        calcularDespesasDoDia()
        calcularBalanco()

        return view
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
                        view?.findViewById<TextView>(R.id.text_saldo)?.text = saldo
                    }
                }
            }
        }
    }



    private fun filtrarReceitas() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val refUserReceita = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Receita")

            refUserReceita.addSnapshotListener { querySnapshot, error ->
                var valorReceita = 0.0
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()
                        valorReceita += valor.toDouble()
                    }
                }
                view?.findViewById<TextView>(R.id.text_bl_receita)?.text= valorReceita.toString()
            }
        }
    }

    private fun filtrarDespesas() {
        val idUsuarioLogado = autenticacao.currentUser?.uid

        if (idUsuarioLogado != null) {
            val refUserDespesa = bancoDados.collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Despesa")

            refUserDespesa.addSnapshotListener { querySnapshot, error ->
                var valorDespesa = 0.0
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()
                        valorDespesa += valor.toDouble()
                    }
                }

                view?.findViewById<TextView>(R.id.text_bl_despesa)?.text = valorDespesa.toString()
            }
        }
    }

    private fun calcularBalanco() {
        val receitasText = view?.findViewById<TextView>(R.id.text_bl_receita)?.text.toString()
        val receitas = receitasText.toDoubleOrNull() ?: 0.0

        val despesasText = view?.findViewById<TextView>(R.id.text_bl_despesa)?.text.toString()
        val despesas = despesasText.toDoubleOrNull() ?: 0.0

        val balanco = receitas - despesas

        view?.findViewById<TextView>(R.id.text_balanco)?.text = balanco.toString()
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
                view?.findViewById<TextView>(R.id.text_gastos_hoje)?.text = valorTotal.toString()

            }
        }
    }





}