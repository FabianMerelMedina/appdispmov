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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var btnCrearCuenta: Button
    private lateinit var tvIrLogin: TextView

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.setLocale(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnCrearCuenta = findViewById(R.id.btnRegister)
        tvIrLogin = findViewById(R.id.tvIniciarSesion)

        btnCrearCuenta.setOnClickListener {
            registrarUsuario()
        }

        tvIrLogin.setOnClickListener {

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
    }

    private fun registrarUsuario() {

        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword =
            etConfirmPassword.text.toString()

        if (
            nombre.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty() ||
            confirmPassword.isEmpty()
        ) {

            Toast.makeText(
                this,
                getString(R.string.complete_all_fields),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (password != confirmPassword) {

            Toast.makeText(
                this,
                getString(R.string.passwords_dont_match),
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        auth.createUserWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener { task ->

            if (!task.isSuccessful) {

                Toast.makeText(
                    this,
                    getString(
                        R.string.register_error,
                        task.exception?.message ?: ""
                    ),
                    Toast.LENGTH_LONG
                ).show()

                return@addOnCompleteListener
            }

            val user = auth.currentUser

            if (user == null) {
                return@addOnCompleteListener
            }

            val profileUpdates =
                UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener {

                    val usuario = hashMapOf(
                        "nombre" to nombre,
                        "email" to email
                    )

                    db.collection("usuarios")
                        .document(user.uid)
                        .set(usuario)
                        .addOnCompleteListener {

                            user.sendEmailVerification()
                                .addOnCompleteListener { verificationTask ->

                                    if (verificationTask.isSuccessful) {

                                        Toast.makeText(
                                            this,
                                            getString(
                                                R.string.account_created_check_email
                                            ),
                                            Toast.LENGTH_LONG
                                        ).show()

                                    } else {

                                        Toast.makeText(
                                            this,
                                            getString(
                                                R.string.account_created_email_error
                                            ),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    val intent = Intent(
                                        this,
                                        VerifyEmailActivity::class.java
                                    )

                                    startActivity(intent)
                                    finish()
                                }
                        }
                }
        }
    }
}