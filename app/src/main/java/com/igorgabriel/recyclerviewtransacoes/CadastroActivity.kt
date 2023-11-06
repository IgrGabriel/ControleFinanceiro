package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityCadastroBinding

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
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

        binding.btnCadastro.setOnClickListener {
            cadastroUsuario()
        }

    }

    private fun cadastroUsuario() {

        val nome = binding.editCadastroNome.text.toString()
        val sobrenome = binding.editCadastroSobrenome.text.toString()
        val email =  binding.editCadastroEmail.text.toString()
        val senha = binding.editCadastroSenha.text.toString()

        autenticacao.createUserWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener { authResult ->
            val email = authResult.user?.email
            val id = authResult.user?.uid

            salvarDadosUsuario(nome, sobrenome)

            binding.textResultado.text = "Sucesso: $id - $email"
        }.addOnFailureListener { exception ->
            val msgErro = exception.message
            binding.textResultado.text = "Erro: $msgErro"
        }
    }

    private fun salvarDadosUsuario( nome: String, sobrenome: String ) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null){

            val dados = mapOf(
                "nome" to nome,
                "sobrenome" to sobrenome,
                "saldo" to 0.0
            )

            bancoDados
                .collection("usuarios")
                .document( idUsuarioLogado )
                .set( dados )
                .addOnSuccessListener { exibirMensagem("Dados salvos!") }
                .addOnFailureListener { exibirMensagem("Falha ao salvar dados!") }
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }

}