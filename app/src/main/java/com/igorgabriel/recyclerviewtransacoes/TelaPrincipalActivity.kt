package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityTelaPrincipalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        filtrarReceitas()
        filtrarDespesas()
        calcularBalanco()
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
                        binding.textSaldo.text = saldo
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

                binding.textBlReceita.text = valorReceita.toString()
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

                binding.textBlDespesa.text = valorDespesa.toString()
            }
        }
    }

    private fun calcularBalanco() {
        val receitas = binding.textBlReceita.text.toString().toDoubleOrNull() ?: 0.0
        val despesas = binding.textBlDespesa.text.toString().toDoubleOrNull() ?: 0.0
        val balanco = receitas - despesas

        binding.textBalanco.text = balanco.toString()
    }
}