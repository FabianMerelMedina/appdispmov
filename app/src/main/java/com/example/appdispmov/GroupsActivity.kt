package com.example.appdispmov

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupsActivity : AppCompatActivity() {

    private lateinit var recyclerGrupos: RecyclerView
    private lateinit var btnCrearGrupo: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val listaGrupos = mutableListOf<Grupo>()

    private lateinit var grupoAdapter: GrupoAdapter

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_groups)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerGrupos =
            findViewById(R.id.recyclerGrupos)

        btnCrearGrupo =
            findViewById(R.id.btnCrearGrupo)

        recyclerGrupos.layoutManager =
            LinearLayoutManager(this)

        grupoAdapter = GrupoAdapter(
            listaGrupos
        ) { grupo ->

            val intent =
                Intent(this, ChatActivity::class.java)

            intent.putExtra(
                "grupoId",
                grupo.id
            )

            intent.putExtra(
                "grupoNombre",
                grupo.nombre
            )

            startActivity(intent)
        }

        recyclerGrupos.adapter =
            grupoAdapter

        escucharGrupos()

        btnCrearGrupo.setOnClickListener {

            val intent =
                Intent(
                    this,
                    CreateGroupActivity::class.java
                )

            startActivity(intent)
        }
    }

    private fun escucharGrupos() {

        val uidActual =
            auth.currentUser?.uid

        if (uidActual == null) {
            return
        }

        db.collection("grupos")
            .whereArrayContains(
                "miembros",
                uidActual
            )
            .addSnapshotListener { documentos, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (documentos == null) {
                    return@addSnapshotListener
                }

                listaGrupos.clear()

                for (documento in documentos.documents) {

                    val grupo =
                        documento.toObject(
                            Grupo::class.java
                        )

                    if (grupo != null) {

                        grupo.id =
                            documento.id

                        listaGrupos.add(grupo)
                    }
                }

                listaGrupos.sortBy {
                    it.nombre.lowercase()
                }

                grupoAdapter.notifyDataSetChanged()
            }
    }
}