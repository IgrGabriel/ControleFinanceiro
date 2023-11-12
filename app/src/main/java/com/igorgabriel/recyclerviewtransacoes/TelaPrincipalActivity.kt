package com.igorgabriel.recyclerviewtransacoes

//import com.igorgabriel.recyclerviewtransacoes.view.MapsFragment
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityTelaPrincipalBinding
import com.igorgabriel.recyclerviewtransacoes.view.HomeFragment
import com.igorgabriel.recyclerviewtransacoes.view.ProfileFragment
import com.igorgabriel.recyclerviewtransacoes.view.TransactionsFragment

class TelaPrincipalActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaPrincipalBinding.inflate(layoutInflater)
    }

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        replaceFragment(HomeFragment())
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.item_home -> replaceFragment(HomeFragment())
                R.id.item_transactions -> replaceFragment(TransactionsFragment())
                //R.id.item_map -> replaceFragment(MapsFragment())
                R.id.item_map -> replaceFragment(ProfileFragment())
                R.id.item_logout -> confirmLogout()
                else -> {
                    replaceFragment(HomeFragment())
                }
            }
            true


        }


    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Sair")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setNegativeButton("CANCELAR") { dialog, which ->
            }
            .setPositiveButton("SAIR") { dialog, which ->
                logoutUser()
            }
            .setCancelable(false)
            .setIcon(R.drawable.alert_icon)
            .create()
            .show()
    }

    private fun logoutUser() {
        autenticacao.signOut()
        val intent = Intent(this, LoginActivity::class.java) //
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
