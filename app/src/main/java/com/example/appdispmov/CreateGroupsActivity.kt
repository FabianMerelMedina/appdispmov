package com.example.appdispmov

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var etNombreGrupo: TextInputEditText
    private lateinit var btnCrearGrupo: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun attachBaseContext(
        newBase: Context
    ) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase)
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_create_groups
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombreGrupo =
            findViewById(
                R.id.etNombreGrupo
            )

        btnCrearGrupo =
            findViewById(
                R.id.btnCrearGrupo
            )

        btnCrearGrupo.setOnClickListener {
            crearGrupo()
        }
    }

    private fun crearGrupo() {

        val nombre =
            etNombreGrupo.text
                ?.toString()
                ?.trim()
                ?: ""

        if (nombre.isEmpty()) {

            etNombreGrupo.error =
                getString(
                    R.string.enter_group_name
                )

            return
        }

        if (nombre.length < 3) {

            etNombreGrupo.error =
                getString(
                    R.string.group_name_too_short
                )

            return
        }

        val usuario =
            auth.currentUser

        if (usuario == null) {

            Toast.makeText(
                this,
                getString(
                    R.string.no_authenticated_user
                ),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        btnCrearGrupo.isEnabled = false

        val grupo = hashMapOf(
            "nombre" to nombre,
            "propietarioId" to usuario.uid,
            "miembros" to listOf(
                usuario.uid
            )
        )

        db.collection("grupos")
            .add(grupo)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    getString(
                        R.string.group_created
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener {

                btnCrearGrupo.isEnabled = true

                Toast.makeText(
                    this,
                    getString(
                        R.string.group_create_error
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}