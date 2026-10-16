package com.example.appdispmov

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MembersActivity : AppCompatActivity() {

    private lateinit var tvNombreGrupo: TextView
    private lateinit var tvCantidadMiembros: TextView
    private lateinit var recyclerMiembros: RecyclerView
    private lateinit var btnAgregarMiembro: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var miembroAdapter: MiembroAdapter

    private val listaMiembros =
        mutableListOf<UsuarioGrupo>()

    private var grupoId: String = ""
    private var grupoNombre: String = ""

    override fun attachBaseContext(
        newBase: Context
    ) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_members
        )

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()
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
            findViewById(R.id.tvNombreGrupo)

        tvCantidadMiembros =
            findViewById(
                R.id.tvCantidadMiembros
            )

        recyclerMiembros =
            findViewById(
                R.id.recyclerMiembros
            )

        btnAgregarMiembro =
            findViewById(
                R.id.btnAgregarMiembro
            )

        tvNombreGrupo.text =
            grupoNombre

        recyclerMiembros.layoutManager =
            LinearLayoutManager(this)

        miembroAdapter =
            MiembroAdapter(
                listaMiembros,
                grupoId
            )

        recyclerMiembros.adapter =
            miembroAdapter

        btnAgregarMiembro.setOnClickListener {

            val intent =
                Intent(
                    this,
                    AddMemberActivity::class.java
                )

            intent.putExtra(
                "grupoId",
                grupoId
            )

            intent.putExtra(
                "grupoNombre",
                grupoNombre
            )

            startActivity(intent)
        }

        cargarMiembros()
    }

    override fun onResume() {
        super.onResume()

        if (::miembroAdapter.isInitialized) {
            cargarMiembros()
        }
    }

    private fun cargarMiembros() {

        db.collection("grupos")
            .document(grupoId)
            .get()
            .addOnSuccessListener { documento ->

                if (!documento.exists()) {
                    return@addOnSuccessListener
                }

                val miembros =
                    documento.get("miembros")
                            as? List<String>
                        ?: emptyList()

                val propietarioId =
                    documento.getString(
                        "propietarioId"
                    ) ?: ""

                tvCantidadMiembros.text =
                    getString(
                        R.string.members_count,
                        miembros.size
                    )

                listaMiembros.clear()

                for (uid in miembros) {

                    listaMiembros.add(
                        UsuarioGrupo(
                            uid = uid,
                            propietarioId =
                                propietarioId
                        )
                    )
                }

                miembroAdapter
                    .notifyDataSetChanged()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    getString(
                        R.string.members_load_error
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}