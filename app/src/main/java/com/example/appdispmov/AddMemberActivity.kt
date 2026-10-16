package com.example.appdispmov

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddMemberActivity : AppCompatActivity() {

    private lateinit var tvNombreGrupo: TextView
    private lateinit var tvCantidadUsuarios: TextView
    private lateinit var tvSinUsuarios: TextView
    private lateinit var etBuscarUsuario: EditText
    private lateinit var recyclerUsuarios: RecyclerView

    private lateinit var db: FirebaseFirestore
    private lateinit var usuarioAdapter: UsuarioAdapter

    private val todosLosUsuarios =
        mutableListOf<UsuarioGrupo>()

    private val usuariosFiltrados =
        mutableListOf<UsuarioGrupo>()

    private var grupoId: String = ""
    private var grupoNombre: String = ""

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase)
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_add_member
        )

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        db = FirebaseFirestore.getInstance()

        grupoId =
            intent.getStringExtra("grupoId")
                ?: ""

        grupoNombre =
            intent.getStringExtra("grupoNombre")
                ?: ""

        if (grupoId.isEmpty()) {
            finish()
            return
        }

        tvNombreGrupo =
            findViewById(
                R.id.tvNombreGrupoAgregar
            )

        tvCantidadUsuarios =
            findViewById(
                R.id.tvCantidadUsuarios
            )

        tvSinUsuarios =
            findViewById(
                R.id.tvSinUsuarios
            )

        etBuscarUsuario =
            findViewById(
                R.id.etBuscarUsuario
            )

        recyclerUsuarios =
            findViewById(
                R.id.recyclerUsuarios
            )

        tvNombreGrupo.text =
            grupoNombre

        recyclerUsuarios.layoutManager =
            LinearLayoutManager(this)

        usuarioAdapter =
            UsuarioAdapter(
                usuariosFiltrados
            ) { usuario ->

                agregarUsuario(usuario)
            }

        recyclerUsuarios.adapter =
            usuarioAdapter

        configurarBusqueda()

        cargarUsuariosDisponibles()
    }

    private fun configurarBusqueda() {

        etBuscarUsuario.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    filtrarUsuarios(
                        s?.toString() ?: ""
                    )
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            }
        )
    }

    private fun cargarUsuariosDisponibles() {

        tvSinUsuarios.visibility =
            TextView.GONE

        db.collection("grupos")
            .document(grupoId)
            .get()
            .addOnSuccessListener { grupoDocumento ->

                if (!grupoDocumento.exists()) {

                    mostrarSinUsuarios(
                        getString(
                            R.string.no_group_found
                        )
                    )

                    return@addOnSuccessListener
                }

                val miembrosActuales =
                    grupoDocumento
                        .get("miembros")
                            as? List<String>
                        ?: emptyList()

                cargarUsuarios(
                    miembrosActuales
                )
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    getString(
                        R.string.group_load_error
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                mostrarSinUsuarios(
                    error.message
                        ?: getString(
                            R.string.users_load_error
                        )
                )
            }
    }

    private fun cargarUsuarios(
        miembrosActuales: List<String>
    ) {

        db.collection("usuarios")
            .get()
            .addOnSuccessListener { documentos ->

                todosLosUsuarios.clear()

                for (documento in documentos) {

                    val uid =
                        documento.id

                    if (
                        miembrosActuales
                            .contains(uid)
                    ) {
                        continue
                    }

                    val nombre =
                        documento
                            .getString("nombre")
                            ?.trim()
                            ?: ""

                    val email =
                        documento
                            .getString("email")
                            ?.trim()
                            ?: ""

                    todosLosUsuarios.add(
                        UsuarioGrupo(
                            uid = uid,
                            nombre = nombre,
                            correo = email
                        )
                    )
                }

                todosLosUsuarios.sortBy {
                    it.nombre.lowercase()
                }

                filtrarUsuarios("")

            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    getString(
                        R.string.users_load_error
                    ),
                    Toast.LENGTH_LONG
                ).show()

                mostrarSinUsuarios(
                    error.message
                        ?: getString(
                            R.string.users_load_error
                        )
                )
            }
    }

    private fun filtrarUsuarios(
        texto: String
    ) {

        val busqueda =
            texto.trim().lowercase()

        usuariosFiltrados.clear()

        if (busqueda.isEmpty()) {

            usuariosFiltrados.addAll(
                todosLosUsuarios
            )

        } else {

            usuariosFiltrados.addAll(
                todosLosUsuarios.filter { usuario ->

                    usuario.nombre
                        .lowercase()
                        .contains(busqueda) ||

                            usuario.correo
                                .lowercase()
                                .contains(busqueda)
                }
            )
        }

        usuarioAdapter.notifyDataSetChanged()

        actualizarEstadoLista()
    }

    private fun actualizarEstadoLista() {

        if (usuariosFiltrados.isEmpty()) {

            recyclerUsuarios.visibility =
                RecyclerView.GONE

            tvSinUsuarios.visibility =
                TextView.VISIBLE

            if (
                todosLosUsuarios.isNotEmpty()
            ) {

                tvSinUsuarios.text =
                    getString(
                        R.string.no_users_match
                    )

            } else {

                tvSinUsuarios.text =
                    getString(
                        R.string.no_users_available
                    )
            }

        } else {

            recyclerUsuarios.visibility =
                RecyclerView.VISIBLE

            tvSinUsuarios.visibility =
                TextView.GONE
        }

        tvCantidadUsuarios.text =
            getString(
                R.string.available_users_count,
                usuariosFiltrados.size
            )
    }

    private fun mostrarSinUsuarios(
        mensaje: String
    ) {

        recyclerUsuarios.visibility =
            RecyclerView.GONE

        tvSinUsuarios.visibility =
            TextView.VISIBLE

        tvSinUsuarios.text =
            mensaje

        tvCantidadUsuarios.text =
            getString(
                R.string.available_users_count,
                0
            )
    }

    private fun agregarUsuario(
        usuario: UsuarioGrupo
    ) {

        db.collection("grupos")
            .document(grupoId)
            .update(
                "miembros",
                FieldValue.arrayUnion(
                    usuario.uid
                )
            )
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    getString(
                        R.string.member_added
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                todosLosUsuarios.removeAll {
                    it.uid == usuario.uid
                }

                usuariosFiltrados.removeAll {
                    it.uid == usuario.uid
                }

                usuarioAdapter
                    .notifyDataSetChanged()

                actualizarEstadoLista()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    getString(
                        R.string.add_member_error
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}