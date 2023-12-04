package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityAdicionarCategoriaBinding

class AdicionarCategoriaActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdicionarCategoriaBinding.inflate(layoutInflater)
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

        val bundle = intent.extras

        if (bundle != null) {
            val tipo = bundle.getString("tipo")

            binding.radioTipo.setText(tipo)

            adicionarCategoria(tipo.toString())
        }
    }

    private fun adicionarCategoria(tipo: String) {

        binding.btnAdicionarCategoria.setOnClickListener {
            val idUsuarioLogado = autenticacao.currentUser?.uid
            val nome = binding.editAddCategoria.text.toString().trim()

            if (idUsuarioLogado != null){
                if(nome.isNotEmpty()) {
                    val categoria = mapOf(
                        "nome" to nome,
                        "tipo" to tipo
                    )

                    val refUsuario = bancoDados
                        .collection("usuarios/${idUsuarioLogado}/categorias")

                    refUsuario
                        .add(categoria)
                        .addOnSuccessListener {
                            exibirMensagem("Categoria adicionada com sucesso")
                            finish()
                        }.addOnFailureListener { exception ->
                            exibirMensagem("Falha ao adicionar categoria")
                        }
                }else{
                    exibirMensagem("Preencha todos os campos")
                }
            }
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}