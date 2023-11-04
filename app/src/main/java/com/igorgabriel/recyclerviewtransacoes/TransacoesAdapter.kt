package com.igorgabriel.recyclerviewtransacoes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.model.Transacao

private val autenticacao by lazy {
    FirebaseAuth.getInstance()
}

private val bancoDados by lazy {
    FirebaseFirestore.getInstance()
}

class TransacoesAdapter(
    private val clique: (String, String, String, String, String, String) -> Unit
) : Adapter<TransacoesAdapter.TransacoesViewHolder>() {

    private var listaTransacoes = mutableListOf<Transacao>()

    fun atualizarListaDados(lista: MutableList<Transacao> ) {
        listaTransacoes = lista
        notifyDataSetChanged()
    }

    inner class TransacoesViewHolder(
        val itemView : View
    ): ViewHolder( itemView ){

        val textDescricao: TextView = itemView.findViewById( R.id.text_cardview_descricao )
        val textCategoria: TextView = itemView.findViewById( R.id.text_cardview_categoria )
        val textValor: TextView = itemView.findViewById( R.id.text_cardview_valor )
        val textData: TextView = itemView.findViewById( R.id.text_cardview_data )

        val cardView: CardView = itemView.findViewById( R.id.card_view_layout )

        fun bind( transacao: Transacao){
            textDescricao.text = transacao.descricao
            textCategoria.text = transacao.categoria
            textValor.text = transacao.valor
            textData.text = transacao.data

            if(transacao.tipo.equals("Receita")) {
                textValor.setTextColor( Color.GREEN )
            } else if(transacao.tipo.equals("Despesa")){
                textValor.setTextColor( Color.RED )
            }

            cardView.setOnClickListener{
                clique(
                    transacao.id,
                    transacao.descricao,
                    transacao.categoria,
                    transacao.valor,
                    transacao.tipo,
                    transacao.data
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransacoesViewHolder {
        val layoutInflater = LayoutInflater.from(
            parent.context
        )

        val itemView = layoutInflater.inflate(
            R.layout.item_cardview_transacoes, parent, false
        )

        return TransacoesViewHolder( itemView )
    }

    override fun getItemCount(): Int {
        return listaTransacoes.size
    }

    override fun onBindViewHolder(holder: TransacoesViewHolder, position: Int) {
        val transacoes = listaTransacoes[position]

        holder.bind( transacoes )
    }

    fun removeAt(position: Int) {

        removerItemBD(position)

        listaTransacoes.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun removerItemBD(position: Int) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null) {
            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .document(listaTransacoes[position].id)

            referenciaUsuario
                .delete()
                .addOnSuccessListener {}
                .addOnFailureListener {}
        }
    }
}