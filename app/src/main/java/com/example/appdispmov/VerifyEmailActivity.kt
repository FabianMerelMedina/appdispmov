package com.example.appdispmov

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Context

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var btnVerificar: Button
    private lateinit var btnReenviar: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()

        btnVerificar = findViewById(R.id.btnVerificar)
        btnReenviar = findViewById(R.id.tvReenviarCorreo)

        btnVerificar.setOnClickListener {
            comprobarVerificacion()
        }

        btnReenviar.setOnClickListener {
            reenviarCorreo()
        }

    }

    private fun comprobarVerificacion() {

        val user = auth.currentUser

        user?.reload()?.addOnCompleteListener {

            val usuarioActualizado = auth.currentUser

            if (usuarioActualizado?.isEmailVerified == true) {

                Toast.makeText(
                    this,
                    "Correo verificado correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this, MainActivity::class.java)

                startActivity(intent)
                finish()

            } else {

                Toast.makeText(
                    this,
                    "Todavía no has verificado tu correo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun reenviarCorreo() {

        val user = auth.currentUser

        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Correo reenviado",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        this,
                        "No se pudo enviar el correo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }
}