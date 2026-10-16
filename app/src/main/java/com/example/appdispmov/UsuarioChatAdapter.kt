package com.example.appdispmov

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UsuarioChatAdapter(
    private val listaUsuarios: List<UsuarioGrupo>,
    private val onUsuarioClick: (UsuarioGrupo) -> Unit
) : RecyclerView.Adapter<UsuarioChatAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvIniciales: TextView =
            itemView.findViewById(
                R.id.tvInicialesChat
            )

        val tvNombre: TextView =
            itemView.findViewById(
                R.id.tvNombreChat
            )

        val tvCorreo: TextView =
            itemView.findViewById(
                R.id.tvCorreoChat
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsuarioViewHolder {

        val vista =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_usuario_chat,
                    parent,
                    false
                )

        return UsuarioViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: UsuarioViewHolder,
        position: Int
    ) {

        val usuario =
            listaUsuarios[position]

        holder.tvNombre.text =
            usuario.nombre.ifEmpty {
                holder.itemView.context.getString(
                    R.string.default_user
                )
            }

        holder.tvCorreo.text =
            usuario.correo

        holder.tvIniciales.text =
            obtenerIniciales(usuario.nombre)

        holder.itemView.setOnClickListener {
            onUsuarioClick(usuario)
        }
    }

    private fun obtenerIniciales(
        nombre: String
    ): String {

        val partes =
            nombre.trim()
                .split("\\s+".toRegex())

        return when {

            partes.isEmpty() ->
                "U"

            partes.size == 1 ->
                partes[0]
                    .take(2)
                    .uppercase()

            else ->
                "${partes[0].first()}${partes[1].first()}"
                    .uppercase()
        }
    }

    override fun getItemCount(): Int =
        listaUsuarios.size
}