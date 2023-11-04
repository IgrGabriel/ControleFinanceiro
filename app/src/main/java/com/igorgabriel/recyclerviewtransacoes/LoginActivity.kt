package com.igorgabriel.recyclerviewtransacoes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( binding.root)

        binding.btnLogin.setOnClickListener {
            loginUsuario()
        }

        binding.textCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        val usuario = autenticacao.currentUser

        if(usuario != null){
            exibirMensagem("Bem-vindo!")
            startActivity(Intent(this, HistoricoTransacoesActivity::class.java))
        }
    }

    private fun loginUsuario() {

        val email =  binding.editEmail.text.toString()
        val senha = binding.editSenha.text.toString()

        if (email != "" && senha != ""){
            autenticacao.signInWithEmailAndPassword(
                email, senha
            ).addOnSuccessListener { authResult ->
                startActivity(Intent(this, HistoricoTransacoesActivity::class.java))
            }.addOnFailureListener { exception ->
                val msgErro = exception.message
                binding.textResultado.text = "Não foi possível fazer login: $msgErro"
            }
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}