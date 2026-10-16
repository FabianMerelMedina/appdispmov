package com.example.appdispmov

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvBienvenida: TextView
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnIdioma: MaterialButton
    private lateinit var btnGrupos: MaterialButton

    private lateinit var recyclerUsuariosChat: RecyclerView
    private lateinit var tvSinUsuariosChat: TextView

    private lateinit var usuarioChatAdapter: UsuarioChatAdapter

    private val listaUsuarios = mutableListOf<UsuarioGrupo>()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvBienvenida = findViewById(R.id.tvBienvenida)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnIdioma = findViewById(R.id.btnIdioma)
        btnGrupos = findViewById(R.id.btnGrupos)

        recyclerUsuariosChat =
            findViewById(R.id.recyclerUsuariosChat)

        tvSinUsuariosChat =
            findViewById(R.id.tvSinUsuariosChat)

        val user = auth.currentUser

        val nombre =
            user?.displayName ?: getString(R.string.user)

        tvBienvenida.text =
            getString(R.string.welcome_user, nombre)

        recyclerUsuariosChat.layoutManager =
            LinearLayoutManager(this)

        usuarioChatAdapter = UsuarioChatAdapter(
            listaUsuarios
        ) { usuario ->

            abrirChatIndividual(usuario)
        }

        recyclerUsuariosChat.adapter =
            usuarioChatAdapter

        cargarUsuarios()

        btnIdioma.setOnClickListener {
            mostrarDialogoIdioma()
        }

        btnCerrarSesion.setOnClickListener {

            auth.signOut()

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        btnGrupos.setOnClickListener {

            val intent = Intent(
                this,
                GroupsActivity::class.java
            )

            startActivity(intent)
        }
    }

    private fun cargarUsuarios() {

        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            return
        }

        db.collection("usuarios")
            .get()
            .addOnSuccessListener { documentos ->

                listaUsuarios.clear()

                for (documento in documentos) {

                    val uid = documento.id

                    // No mostrar al usuario que inició sesión
                    if (uid == usuarioActual.uid) {
                        continue
                    }

                    val nombre =
                        documento.getString("nombre")
                            ?.trim()
                            ?: ""

                    val correo =
                        documento.getString("email")
                            ?.trim()
                            ?: ""

                    listaUsuarios.add(
                        UsuarioGrupo(
                            uid = uid,
                            nombre = nombre,
                            correo = correo
                        )
                    )
                }

                listaUsuarios.sortBy {
                    it.nombre.lowercase()
                }

                usuarioChatAdapter.notifyDataSetChanged()

                if (listaUsuarios.isEmpty()) {

                    recyclerUsuariosChat.visibility =
                        RecyclerView.GONE

                    tvSinUsuariosChat.visibility =
                        TextView.VISIBLE

                } else {

                    recyclerUsuariosChat.visibility =
                        RecyclerView.VISIBLE

                    tvSinUsuariosChat.visibility =
                        TextView.GONE
                }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    getString(R.string.users_load_error),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun abrirChatIndividual(
        usuario: UsuarioGrupo
    ) {

        val intent = Intent(
            this,
            ChatIndividualActivity::class.java
        )

        intent.putExtra(
            "usuarioId",
            usuario.uid
        )

        intent.putExtra(
            "usuarioNombre",
            usuario.nombre
        )

        intent.putExtra(
            "usuarioCorreo",
            usuario.correo
        )

        startActivity(intent)
    }

    private fun mostrarDialogoIdioma() {

        val idiomas = arrayOf(
            getString(R.string.spanish),
            getString(R.string.english)
        )

        AlertDialog.Builder(this)
            .setTitle(
                getString(R.string.select_language)
            )
            .setItems(idiomas) { _, which ->

                when (which) {

                    0 -> cambiarIdioma("es")

                    1 -> cambiarIdioma("en")
                }
            }
            .show()
    }

    private fun cambiarIdioma(idioma: String) {

        val preferences =
            getSharedPreferences(
                "MyChatPreferences",
                Context.MODE_PRIVATE
            )

        preferences.edit()
            .putString("language", idioma)
            .apply()

        val locale = Locale(idioma)

        Locale.setDefault(locale)

        recreate()
    }


}