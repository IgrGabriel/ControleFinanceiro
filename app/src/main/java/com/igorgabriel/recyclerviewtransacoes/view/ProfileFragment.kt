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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment: Fragment() {

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    private var coroutineJob: Job? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        carregarInformacoes()

        return view
    }

    private fun carregarInformacoes() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        val emailUsuarioLogado = autenticacao.currentUser?.email

        if (idUsuarioLogado != null && emailUsuarioLogado != null) {
            coroutineJob = CoroutineScope(Dispatchers.IO).launch {
                val refUser = bancoDados.collection("usuarios").document(idUsuarioLogado)
                var nomeCompleto = ""
                refUser.addSnapshotListener { documentSnapshot, error ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val dados = documentSnapshot.data
                        if (dados != null) {

                            val nome = dados["nome"].toString()
                            val sobrenome = dados["sobrenome"].toString()
                            nomeCompleto = nome + " " + sobrenome
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    view?.findViewById<TextView>(R.id.txt_nome_sobrenome)?.text = nomeCompleto
                    view?.findViewById<TextView>(R.id.text_email)?.text = emailUsuarioLogado.toString()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineJob?.cancel()
    }
}