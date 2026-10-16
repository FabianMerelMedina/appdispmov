package com.example.appdispmov

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvCrearCuenta: TextView

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvCrearCuenta = findViewById(R.id.tvCrearCuenta)

        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        tvCrearCuenta.setOnClickListener {

            val intent = Intent(
                this,
                RegisterActivity::class.java
            )

            startActivity(intent)
        }
    }

    private fun iniciarSesion() {

        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString()

        if (correo.isEmpty()) {
            etCorreo.error = "Ingresa tu correo"
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contraseña"
            return
        }

        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser

                    if (user?.isEmailVerified == true) {

                        guardarUsuarioEnFirestore(user)

                    } else {

                        Toast.makeText(
                            this,
                            "Debes verificar tu correo antes de continuar",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(
                            this,
                            VerifyEmailActivity::class.java
                        )

                        startActivity(intent)
                    }

                } else {

                    Toast.makeText(
                        this,
                        "Correo o contraseña incorrectos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun guardarUsuarioEnFirestore(
        user: com.google.firebase.auth.FirebaseUser
    ) {

        val nombre = user.displayName?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "Usuario"

        val correo = user.email ?: ""

        val datosUsuario = hashMapOf(
            "nombre" to nombre,
            "email" to correo
        )

        db.collection("usuarios")
            .document(user.uid)
            .set(
                datosUsuario,
                SetOptions.merge()
            )
            .addOnSuccessListener {

                val intent = Intent(
                    this,
                    MainActivity::class.java
                )

                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Error Firestore: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}