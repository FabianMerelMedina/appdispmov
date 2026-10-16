package com.example.appdispmov

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MensajeAdapter(
    private val listaMensajes: List<Mensaje>
) : RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder>() {

    class MensajeViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNombreMensaje: TextView =
            itemView.findViewById(R.id.tvNombreMensaje)

        val tvTextoMensaje: TextView =
            itemView.findViewById(R.id.tvTextoMensaje)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MensajeViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_mensaje,
                parent,
                false
            )

        return MensajeViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: MensajeViewHolder,
        position: Int
    ) {

        val mensaje = listaMensajes[position]

        holder.tvNombreMensaje.text = mensaje.nombre
        holder.tvTextoMensaje.text = mensaje.texto
    }

    override fun getItemCount(): Int {
        return listaMensajes.size
    }
}