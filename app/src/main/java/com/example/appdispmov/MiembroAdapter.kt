package com.example.appdispmov

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MiembroAdapter(
    private val listaMiembros: MutableList<UsuarioGrupo>,
    private val grupoId: String
) : RecyclerView.Adapter<MiembroAdapter.MiembroViewHolder>() {

    class MiembroViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val tvIniciales: TextView =
            itemView.findViewById(
                R.id.tvInicialesMiembro
            )

        val tvNombre: TextView =
            itemView.findViewById(
                R.id.tvNombreMiembro
            )

        val tvCorreo: TextView =
            itemView.findViewById(
                R.id.tvCorreoMiembro
            )

        val tvRol: TextView =
            itemView.findViewById(
                R.id.tvRolMiembro
            )

        val btnEliminar: TextView =
            itemView.findViewById(
                R.id.btnEliminarMiembro
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MiembroViewHolder {

        val vista =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_miembro,
                    parent,
                    false
                )

        return MiembroViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: MiembroViewHolder,
        position: Int
    ) {

        val miembro =
            listaMiembros[position]

        cargarDatosUsuario(
            miembro.uid,
            holder
        )

        val esPropietario =
            miembro.uid ==
                    miembro.propietarioId

        if (esPropietario) {

            holder.tvRol.text =
                holder.itemView.context.getString(
                    R.string.owner
                )

            holder.btnEliminar.visibility =
                View.GONE

        } else {

            holder.tvRol.text =
                holder.itemView.context.getString(
                    R.string.user_role
                )

            val uidActual =
                FirebaseAuth
                    .getInstance()
                    .currentUser
                    ?.uid

            if (
                uidActual ==
                miembro.propietarioId
            ) {

                holder.btnEliminar.visibility =
                    View.VISIBLE

                holder.btnEliminar.setOnClickListener {

                    mostrarConfirmacion(
                        holder,
                        miembro
                    )
                }

            } else {

                holder.btnEliminar.visibility =
                    View.GONE
            }
        }
    }

    private fun cargarDatosUsuario(
        uid: String,
        holder: MiembroViewHolder
    ) {

        FirebaseFirestore
            .getInstance()
            .collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {

                    val nombre =
                        documento.getString(
                            "nombre"
                        ) ?: holder.itemView.context
                            .getString(
                                R.string.default_user
                            )

                    val email =
                        documento.getString(
                            "email"
                        ) ?: ""

                    holder.tvNombre.text =
                        nombre

                    holder.tvCorreo.text =
                        email

                    holder.tvIniciales.text =
                        obtenerIniciales(nombre)
                }
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

    private fun mostrarConfirmacion(
        holder: MiembroViewHolder,
        miembro: UsuarioGrupo
    ) {

        val contexto =
            holder.itemView.context

        AlertDialog.Builder(contexto)
            .setTitle(
                R.string.remove_member_title
            )
            .setMessage(
                R.string.remove_member_confirmation
            )
            .setNegativeButton(
                R.string.cancel,
                null
            )
            .setPositiveButton(
                R.string.remove_member
            ) { _, _ ->

                eliminarMiembro(
                    holder,
                    miembro
                )
            }
            .show()
    }

    private fun eliminarMiembro(
        holder: MiembroViewHolder,
        miembro: UsuarioGrupo
    ) {

        FirebaseFirestore
            .getInstance()
            .collection("grupos")
            .document(grupoId)
            .update(
                "miembros",
                FieldValue.arrayRemove(
                    miembro.uid
                )
            )
            .addOnSuccessListener {

                val posicion =
                    holder.adapterPosition

                if (
                    posicion !=
                    RecyclerView.NO_POSITION
                ) {

                    listaMiembros.removeAt(
                        posicion
                    )

                    notifyItemRemoved(
                        posicion
                    )
                }

                Toast.makeText(
                    holder.itemView.context,
                    holder.itemView.context
                        .getString(
                            R.string.member_removed
                        ),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {

                Toast.makeText(
                    holder.itemView.context,
                    holder.itemView.context
                        .getString(
                            R.string.remove_member_error
                        ),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun getItemCount(): Int =
        listaMiembros.size
}