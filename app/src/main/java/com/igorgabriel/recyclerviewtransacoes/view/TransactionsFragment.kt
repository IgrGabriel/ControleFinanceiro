package com.igorgabriel.recyclerviewtransacoes.view

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.igorgabriel.recyclerviewtransacoes.AdicionarTransacaoActivity
import com.igorgabriel.recyclerviewtransacoes.EditarTransacoesActivity
import com.igorgabriel.recyclerviewtransacoes.TransacoesAdapter
import com.igorgabriel.recyclerviewtransacoes.databinding.FragmentTransactionsBinding
import com.igorgabriel.recyclerviewtransacoes.model.Transacao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transacaoAdapter: TransacoesAdapter


    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
        setupSpinerFiltros()
        excluirTransacao()
    }
    private fun setupListeners() {
        binding.btnAdicionar.setOnClickListener {
            startActivity(Intent(requireContext(), AdicionarTransacaoActivity::class.java))
        }


    }

    private fun setupUI() {
        transacaoAdapter = TransacoesAdapter { id, descricao, categoria, valor, tipo, data ->
            val intent = Intent(requireContext(), EditarTransacoesActivity::class.java).apply {
                putExtra("id", id)
                putExtra("descricao", descricao)
                putExtra("categoria", categoria)
                putExtra("valor", valor)
                putExtra("tipo", tipo)
                putExtra("data", data)
            }
            startActivity(intent)
        }

        binding.rvLista.adapter = transacaoAdapter
        binding.rvLista.layoutManager = LinearLayoutManager(context)
    }
    private fun listarTransacoes() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")

            referenciaUsuario.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents
                val lista_transacoes = mutableListOf<Transacao>()

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if (dados != null) {
                        val documentId = documentSnapshot.id
                        val descricao = dados["descricao"].toString()
                        val categoria = dados["categoria"].toString()
                        val valor = dados["valor"].toString()
                        val tipo = dados["tipo"].toString()

                        try {
                            val dataMap = dados["data"] as Map<String, Number>

                            val dia = dataMap["dia"]?.toInt()
                                ?: 1 // Use 1 como valor padrão se dia não estiver presente
                            val mes = dataMap["mes"]?.toInt() ?: 0 // Usa 0 como padrão (janeiro)
                            val ano = dataMap["ano"]?.toInt()
                                ?: 2023 // Use 2023 como ano padrão se ano não estiver presente

                            val calendario = Calendar.getInstance()
                            calendario.set(ano, mes - 1, dia) // O mês base é 0, então subtrai 1

                            val data =
                                SimpleDateFormat("EEE, dd MMM yyyy", Locale("pt", "BR")).format(
                                    calendario.time
                                )

                            lista_transacoes.add(
                                0,
                                Transacao(documentId, descricao, categoria, valor, tipo, data)
                            )
                            transacaoAdapter.atualizarListaDados(lista_transacoes)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Erro ao carregar transação: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            }
        }
    }
    private fun excluirTransacao() {
        // Remover transacoes arrastando para os lados
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            // Comportamentos de arratar na tela
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                alertDialogExclusao(transacaoAdapter, viewHolder.adapterPosition)
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvLista)
    }
    private fun alertDialogExclusao(transacoesAdapter: TransacoesAdapter, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remover transação")
            .setMessage("Tem certeza que deseja excluir essa transação?")
            .setNegativeButton("CANCELAR") { dialog, posicao ->
                transacoesAdapter.notifyItemChanged(position)
            }
            .setPositiveButton("REMOVER") { dialog, posicao ->
                transacaoAdapter.removeAt(position)
            }
            .setCancelable(false) // obriga o usuario a escolher uma opção
            .setIcon(com.igorgabriel.recyclerviewtransacoes.R.drawable.alert_icon)
            .create()
            .show()
    }


    private fun setupSpinerFiltros() {

        val listFiltros = listOf(
            "Todas as transações", "Feitas esse mês", "Despesas", "Receitas"
        )

        binding.spinnerFiltros.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            listFiltros
        )

        binding.spinnerFiltros.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val itemSelecionado = parent?.selectedItem.toString()

                if(itemSelecionado.equals("Todas as transações")) {
                    listarTransacoes()
                }else if(itemSelecionado.equals("Receitas")){
                    filtrarPorTipo("Receita")
                } else if(itemSelecionado.equals("Despesas")){
                    filtrarPorTipo("Despesa")
                } else if(itemSelecionado.equals("Feitas esse mês")) {
                    filtrarMesAtual()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //listarTransacoes()
            }

        }
    }
    private fun filtrarPorTipo(tipo: String) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", tipo)

            referenciaUsuario.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents
                val lista_transacoes = mutableListOf<Transacao>()

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if (dados != null) {
                        val documentId = documentSnapshot.id
                        val descricao = dados["descricao"].toString()
                        val categoria = dados["categoria"].toString()
                        val valor = dados["valor"].toString()
                        val tipo = dados["tipo"].toString()

                        try {
                            val dataMap = dados["data"] as Map<String, Number>

                            val dia = dataMap["dia"]?.toInt()
                                ?: 1 // Use 1 como valor padrão se dia não estiver presente
                            val mes = dataMap["mes"]?.toInt() ?: 0 // Usa 0 como padrão (janeiro)
                            val ano = dataMap["ano"]?.toInt()
                                ?: 2023 // Use 2023 como ano padrão se ano não estiver presente

                            val calendario = Calendar.getInstance()
                            calendario.set(ano, mes - 1, dia) // O mês base é 0, então subtrai 1

                            val data =
                                SimpleDateFormat("EEE, dd MMM yyyy", Locale("pt", "BR")).format(
                                    calendario.time
                                )

                            lista_transacoes.add(
                                0,
                                Transacao(documentId, descricao, categoria, valor, tipo, data)
                            )
                            transacaoAdapter.atualizarListaDados(lista_transacoes)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Erro ao carregar transação: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            }
        }
    }
    private fun filtrarMesAtual() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {

            val calendar = Calendar.getInstance()
            val ano = calendar.get(Calendar.YEAR)
            val mes = calendar.get(Calendar.MONTH) + 1

            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("data.mes", mes)
                .whereEqualTo("data.ano", ano)
                .orderBy("data.dia", Query.Direction.DESCENDING)

            referenciaUsuario.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents
                val lista_transacoes = mutableListOf<Transacao>()

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if (dados != null) {
                        val documentId = documentSnapshot.id
                        val descricao = dados["descricao"].toString()
                        val categoria = dados["categoria"].toString()
                        val valor = dados["valor"].toString()
                        val tipo = dados["tipo"].toString()

                        try {
                            val dataMap = dados["data"] as Map<String, Number>

                            val dia = dataMap["dia"]?.toInt()
                                ?: 1 // Use 1 como valor padrão se dia não estiver presente
                            val mes = dataMap["mes"]?.toInt() ?: 0 // Usa 0 como padrão (janeiro)
                            val ano = dataMap["ano"]?.toInt()
                                ?: 2023 // Use 2023 como ano padrão se ano não estiver presente

                            val calendario = Calendar.getInstance()
                            calendario.set(ano, mes - 1, dia) // O mês base é 0, então subtrai 1

                            val data =
                                SimpleDateFormat("EEE, dd MMM yyyy", Locale("pt", "BR")).format(
                                    calendario.time
                                )

                            lista_transacoes.add(
                                0,
                                Transacao(documentId, descricao, categoria, valor, tipo, data)
                            )
                            transacaoAdapter.atualizarListaDados(lista_transacoes)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Erro ao carregar transação: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
            }
        }
    }



}