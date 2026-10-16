package com.example.appdispmov

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class GrupoAdapter(
    private val listaGrupos: List<Grupo>,
    private val onGrupoClick: (Grupo) -> Unit
) : RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder>() {

    class GrupoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvNombreGrupo: TextView =
            itemView.findViewById(R.id.tvNombreGrupo)

        val tvRolGrupo: TextView =
            itemView.findViewById(R.id.tvRolGrupo)

        val tvMensajesNoLeidos: TextView =
            itemView.findViewById(R.id.tvMensajesNoLeidos)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GrupoViewHolder {

        val vista =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_group,
                    parent,
                    false
                )

        return GrupoViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: GrupoViewHolder,
        position: Int
    ) {

        val grupo = listaGrupos[position]

        holder.tvNombreGrupo.text =
            grupo.nombre

        val uidActual =
            FirebaseAuth.getInstance()
                .currentUser?.uid

        // Mostrar el rol
        if (grupo.propietarioId == uidActual) {

            holder.tvRolGrupo.text =
                holder.itemView.context
                    .getString(R.string.owner)

        } else {

            holder.tvRolGrupo.text =
                holder.itemView.context
                    .getString(R.string.user_role)
        }

        val mensajesNoLeidos =
            if (uidActual != null) {
                grupo.noLeidos[uidActual] ?: 0L
            } else {
                0L
            }

        if (mensajesNoLeidos > 0) {

            holder.tvMensajesNoLeidos.visibility =
                View.VISIBLE

            holder.tvMensajesNoLeidos.text =
                if (mensajesNoLeidos > 99) {
                    "99+"
                } else {
                    mensajesNoLeidos.toString()
                }

        } else {

            holder.tvMensajesNoLeidos.visibility =
                View.GONE
        }

        holder.itemView.setOnClickListener {
            onGrupoClick(grupo)
        }
    }

    override fun getItemCount(): Int =
        listaGrupos.size
}