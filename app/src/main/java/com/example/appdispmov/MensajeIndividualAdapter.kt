package com.example.appdispmov

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MensajeIndividualAdapter(
    private val listaMensajes:
    List<MensajeIndividual>
) : RecyclerView.Adapter<
        MensajeIndividualAdapter.MensajeViewHolder>() {

    class MensajeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNombre:
                TextView =
            itemView.findViewById(
                R.id.tvNombreMensajeIndividual
            )

        val tvTexto:
                TextView =
            itemView.findViewById(
                R.id.tvTextoMensajeIndividual
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MensajeViewHolder {

        val vista =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_mensaje_individual,
                    parent,
                    false
                )

        return MensajeViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: MensajeViewHolder,
        position: Int
    ) {

        val mensaje =
            listaMensajes[position]

        holder.tvNombre.text =
            mensaje.nombre

        holder.tvTexto.text =
            mensaje.texto
    }

    override fun getItemCount(): Int =
        listaMensajes.size
}