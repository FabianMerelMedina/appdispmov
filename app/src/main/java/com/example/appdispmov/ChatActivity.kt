package com.example.appdispmov

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.content.Intent
import com.google.firebase.firestore.FieldValue

class ChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvNombreGrupoChat: TextView
    private lateinit var recyclerMensajes: RecyclerView
    private lateinit var etMensaje: EditText
    private lateinit var btnEnviarMensaje: MaterialButton

    private lateinit var mensajeAdapter: MensajeAdapter

    private lateinit var tvInicialesGrupoChat: TextView

    private lateinit var tvMiembrosChat: TextView

    private lateinit var btnMiembros: MaterialButton

    private val listaMensajes = mutableListOf<Mensaje>()

    private var grupoId: String = ""
    private var grupoNombre: String = ""


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase)
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        grupoId =
            intent.getStringExtra("grupoId") ?: ""

        grupoNombre =
            intent.getStringExtra("grupoNombre") ?: ""


        if (grupoId.isEmpty()) {

            Toast.makeText(
                this,
                "No se encontró el grupo",
                Toast.LENGTH_SHORT
            ).show()

            finish()

            return
        }

        marcarGrupoComoLeido()


        tvNombreGrupoChat =
            findViewById(R.id.tvNombreGrupoChat)

        btnMiembros =
            findViewById(R.id.btnMiembros)

        recyclerMensajes =
            findViewById(R.id.recyclerMensajes)

        etMensaje =
            findViewById(R.id.etMensaje)

        btnEnviarMensaje =
            findViewById(R.id.btnEnviarMensaje)


        tvNombreGrupoChat.text =
            grupoNombre

        tvInicialesGrupoChat =
            findViewById(R.id.tvInicialesGrupoChat)

        tvMiembrosChat =
            findViewById(R.id.tvMiembrosChat)

        tvInicialesGrupoChat.text =
            obtenerIniciales(grupoNombre)

        cargarCantidadMiembros()

        btnMiembros.setOnClickListener {

            val intent = Intent(
                this,
                MembersActivity::class.java
            )

            intent.putExtra("grupoId", grupoId)
            intent.putExtra("grupoNombre", grupoNombre)

            startActivity(intent)
        }


        recyclerMensajes.layoutManager =
            LinearLayoutManager(this)

        mensajeAdapter =
            MensajeAdapter(listaMensajes)

        recyclerMensajes.adapter =
            mensajeAdapter


        escucharMensajes()



        btnEnviarMensaje.setOnClickListener {

            enviarMensaje()
        }
    }


    private fun enviarMensaje() {

        val texto =
            etMensaje.text
                .toString()
                .trim()

        if (texto.isEmpty()) {
            return
        }

        val usuario =
            auth.currentUser ?: return

        val nombre =
            usuario.displayName
                ?: getString(R.string.default_user)

        val mensaje = hashMapOf(
            "texto" to texto,
            "usuarioId" to usuario.uid,
            "nombre" to nombre,
            "fecha" to System.currentTimeMillis()
        )

        db.collection("grupos")
            .document(grupoId)
            .collection("mensajes")
            .add(mensaje)
            .addOnSuccessListener {

                actualizarEstadoGrupo(
                    texto,
                    usuario.uid
                )

                etMensaje.text.clear()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    getString(R.string.message_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun escucharMensajes() {

        db.collection("grupos")
            .document(grupoId)
            .collection("mensajes")
            .orderBy(
                "fecha",
                Query.Direction.ASCENDING
            )
            .addSnapshotListener { snapshots, error ->

                if (error != null) {

                    Toast.makeText(
                        this,
                        "Error al cargar mensajes",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }


                if (snapshots == null) {
                    return@addSnapshotListener
                }


                listaMensajes.clear()


                for (documento in snapshots.documents) {

                    val mensaje =
                        documento.toObject(
                            Mensaje::class.java
                        )

                    if (mensaje != null) {

                        mensaje.id =
                            documento.id

                        listaMensajes.add(mensaje)
                    }
                }


                mensajeAdapter.notifyDataSetChanged()


                if (listaMensajes.isNotEmpty()) {

                    recyclerMensajes.scrollToPosition(
                        listaMensajes.size - 1
                    )
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
                "G"

            partes.size == 1 ->
                partes[0]
                    .take(2)
                    .uppercase()

            else ->
                "${partes[0].first()}${partes[1].first()}"
                    .uppercase()
        }
    }

    private fun cargarCantidadMiembros() {

        db.collection("grupos")
            .document(grupoId)
            .get()
            .addOnSuccessListener { documento ->

                val miembros =
                    documento.get("miembros")
                            as? List<*>
                        ?: emptyList<Any>()

                tvMiembrosChat.text =
                    getString(
                        R.string.members_count,
                        miembros.size
                    )
            }
    }

    private fun actualizarEstadoGrupo(
        texto: String,
        uidEmisor: String
    ) {

        db.collection("grupos")
            .document(grupoId)
            .get()
            .addOnSuccessListener { documento ->

                val miembros =
                    documento.get("miembros")
                            as? List<String>
                        ?: emptyList()

                val actualizaciones =
                    mutableMapOf<String, Any>()

                actualizaciones["ultimoMensaje"] =
                    texto

                actualizaciones["ultimoMensajePor"] =
                    uidEmisor

                actualizaciones["fechaUltimoMensaje"] =
                    System.currentTimeMillis()

                for (uid in miembros) {

                    if (uid == uidEmisor) {

                        actualizaciones[
                            "noLeidos.$uid"
                        ] = 0

                    } else {

                        actualizaciones[
                            "noLeidos.$uid"
                        ] = FieldValue.increment(1)
                    }
                }

                db.collection("grupos")
                    .document(grupoId)
                    .update(actualizaciones)
            }
    }

    private fun marcarGrupoComoLeido() {

        val usuario =
            auth.currentUser ?: return

        db.collection("grupos")
            .document(grupoId)
            .update(
                "noLeidos.${usuario.uid}",
                0
            )
    }
}