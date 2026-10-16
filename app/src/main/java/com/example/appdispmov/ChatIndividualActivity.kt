package com.example.appdispmov

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import android.content.Intent

class ChatIndividualActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvNombreUsuarioChat: TextView
    private lateinit var tvCorreoUsuarioChat: TextView
    private lateinit var recyclerMensajesIndividual: RecyclerView
    private lateinit var etMensajeIndividual: EditText
    private lateinit var btnEnviarMensajeIndividual: MaterialButton
    private lateinit var btnVideollamada: MaterialButton

    private lateinit var mensajeIndividualAdapter: MensajeIndividualAdapter
    private val listaMensajes = mutableListOf<MensajeIndividual>()

    private var usuarioId: String = ""
    private var usuarioNombre: String = ""
    private var usuarioCorreo: String = ""
    private var chatId: String = ""

    private var llamadaMostrada = false

    companion object {
       private const val ZEGO_APP_ID: Long = 272135506
       private const val ZEGO_APP_SIGN: String = "fd8d70aa33a3a0a82e3f7bb38f4a12eb4319153fb7334258830164e5e0e4d962"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_individual)

        SystemBarsHelper.applyInsets(findViewById(android.R.id.content))

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        usuarioId = intent.getStringExtra("usuarioId") ?: ""
        usuarioNombre = intent.getStringExtra("usuarioNombre") ?: ""
        usuarioCorreo = intent.getStringExtra("usuarioCorreo") ?: ""

        val usuarioActual = auth.currentUser
        if (usuarioActual == null || usuarioId.isEmpty()) {
            finish()
            return
        }

        chatId = obtenerChatId(usuarioActual.uid, usuarioId)

        tvNombreUsuarioChat = findViewById(R.id.tvNombreUsuarioChat)
        tvCorreoUsuarioChat = findViewById(R.id.tvCorreoUsuarioChat)
        recyclerMensajesIndividual = findViewById(R.id.recyclerMensajesIndividual)
        etMensajeIndividual = findViewById(R.id.etMensaje)
        btnEnviarMensajeIndividual = findViewById(R.id.btnEnviarMensajeIndividual)
        btnVideollamada = findViewById(R.id.btnVideollamada)

        tvNombreUsuarioChat.text = usuarioNombre.ifEmpty { getString(R.string.default_user) }
        tvCorreoUsuarioChat.text = usuarioCorreo

        recyclerMensajesIndividual.layoutManager = LinearLayoutManager(this)
        mensajeIndividualAdapter = MensajeIndividualAdapter(listaMensajes)
        recyclerMensajesIndividual.adapter = mensajeIndividualAdapter

        escucharMensajes()
        marcarComoLeidos()
        escucharLlamadas()

        btnEnviarMensajeIndividual.setOnClickListener { enviarMensaje() }
        btnVideollamada.setOnClickListener {

            Toast.makeText(
                this,
                "BOTÓN DE VIDEOLLAMADA FUNCIONA",
                Toast.LENGTH_LONG
            ).show()

            comprobarPermisosYLLamar()
        }

    }

    private fun obtenerChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    private fun enviarMensaje() {
        val texto = etMensajeIndividual.text.toString().trim()
        if (texto.isEmpty()) return

        val usuarioActual = auth.currentUser ?: return
        val nombreActual = usuarioActual.displayName ?: getString(R.string.default_user)

        val referenciaChat = db.collection("chats").document(chatId)

        val mensaje = hashMapOf(
            "texto" to texto,
            "usuarioId" to usuarioActual.uid,
            "nombre" to nombreActual,
            "fecha" to System.currentTimeMillis()
        )

        referenciaChat.collection("mensajes").add(mensaje).addOnSuccessListener {
            val actualizacion = hashMapOf(
                "participantes" to listOf(usuarioActual.uid, usuarioId),
                "ultimoMensaje" to texto,
                "ultimoMensajePor" to usuarioActual.uid,
                "fechaUltimoMensaje" to System.currentTimeMillis(),
                "noLeidos.${usuarioId}" to FieldValue.increment(1),
                "noLeidos.${usuarioActual.uid}" to 0
            )
            referenciaChat.set(actualizacion, SetOptions.merge())
            etMensajeIndividual.text.clear()
        }
    }

    private fun escucharMensajes() {
        db.collection("chats")
            .document(chatId)
            .collection("mensajes")
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                listaMensajes.clear()
                for (documento in snapshots.documents) {
                    val mensaje = documento.toObject(MensajeIndividual::class.java)
                    if (mensaje != null) {
                        mensaje.id = documento.id
                        listaMensajes.add(mensaje)
                    }
                }
                mensajeIndividualAdapter.notifyDataSetChanged()
                if (listaMensajes.isNotEmpty()) {
                    recyclerMensajesIndividual.scrollToPosition(listaMensajes.size - 1)
                }
            }
    }

    private fun marcarComoLeidos() {
        val usuarioActual = auth.currentUser ?: return
        db.collection("chats").document(chatId).set(
            mapOf("noLeidos.${usuarioActual.uid}" to 0),
            SetOptions.merge()
        )
    }

    private fun iniciarVideollamada() {
        val usuarioActual = auth.currentUser ?: return
        val callID = "call_${java.util.UUID.randomUUID().toString().replace("-", "_")}"

        val llamada = hashMapOf(
            "estado" to "llamando",
            "emisorId" to usuarioActual.uid,
            "emisorNombre" to (usuarioActual.displayName ?: getString(R.string.default_user)),
            "receptorId" to usuarioId,
            "callID" to callID,
            "fecha" to System.currentTimeMillis()
        )

        db.collection("chats")
            .document(chatId)
            .update("llamada", llamada)
            .addOnSuccessListener {
                // En lugar de cargar Zego aquí, abrimos la nueva Actividad
                val intent = Intent(this, CallActivity::class.java).apply {
                    putExtra("callID", callID)
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show()
            }
    }

    private fun escucharLlamadas() {
        val usuarioActual = auth.currentUser ?: return

        db.collection("chats").document(chatId).addSnapshotListener { documento, error ->
            if (error != null || documento == null || !documento.exists()) return@addSnapshotListener

            val llamada = documento.get("llamada") as? Map<*, *> ?: return@addSnapshotListener

            val estado = llamada["estado"] as? String ?: ""
            val receptorId = llamada["receptorId"] as? String ?: ""
            val emisorId = llamada["emisorId"] as? String ?: ""
            val emisorNombre = llamada["emisorNombre"] as? String ?: getString(R.string.default_user)
            val callID = llamada["callID"] as? String ?: ""

            if (estado == "llamando" && receptorId == usuarioActual.uid && emisorId != usuarioActual.uid && !llamadaMostrada) {
                llamadaMostrada = true
                mostrarLlamadaEntrante(emisorNombre, callID)
            }

            if (estado == "rechazada" || estado == "finalizada") {
                llamadaMostrada = false
            }
        }
    }

    private fun mostrarLlamadaEntrante(nombreEmisor: String, callID: String) {
        AlertDialog.Builder(this)
            .setTitle("📹 Llamada entrante")
            .setMessage("$nombreEmisor te está llamando")
            .setNegativeButton("Rechazar") { _, _ -> rechazarLlamada() }
            .setPositiveButton("Aceptar") { _, _ -> aceptarLlamada(callID) }
            .setCancelable(false)
            .show()
    }

    private fun aceptarLlamada(callID: String) {
        db.collection("chats").document(chatId).update("llamada.estado", "aceptada")
            .addOnSuccessListener {
                // Abrimos la Actividad de llamada al aceptar
                val intent = Intent(this, CallActivity::class.java).apply {
                    putExtra("callID", callID)
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                llamadaMostrada = false
                Toast.makeText(this, "No se pudo aceptar la llamada", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rechazarLlamada() {
        db.collection("chats").document(chatId).update("llamada.estado", "rechazada")
        llamadaMostrada = false
    }

    private fun entrarALlamadaZego(callID: String) {

        Toast.makeText(
            this,
            "1. Entrando a ZEGOCLOUD",
            Toast.LENGTH_LONG
        ).show()

        val usuarioActual = auth.currentUser ?: return

        val userID = usuarioActual.uid

        val userName =
            usuarioActual.displayName
                ?: getString(R.string.default_user)

        val config =
            ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()

        config.turnOnCameraWhenJoining = true
        config.turnOnMicrophoneWhenJoining = true

        // Primero cargamos la pantalla de llamada
        setContentView(
            R.layout.activity_llamada_zego
        )

        Toast.makeText(
            this,
            "2. Pantalla de llamada cargada",
            Toast.LENGTH_LONG
        ).show()

        // Después creamos el Fragment de ZEGOCLOUD
        val fragment =
            ZegoUIKitPrebuiltCallFragment.newInstance(
                ZEGO_APP_ID,
                ZEGO_APP_SIGN,
                userID,
                userName,
                callID,
                config
            )

        Toast.makeText(
            this,
            "3. Fragment creado",
            Toast.LENGTH_LONG
        ).show()

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container,
                fragment
            )
            .commitNow()

        Toast.makeText(
            this,
            "4. Fragment insertado",
            Toast.LENGTH_LONG
        ).show()

        android.os.Handler(mainLooper).postDelayed({

            val camaraEncendida =
                ZegoUIKitPrebuiltCallService.isCameraOn()

            Toast.makeText(
                this,
                "Cámara Zego: $camaraEncendida",
                Toast.LENGTH_LONG
            ).show()

        }, 3000)
    }

    private val permisosLlamada = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        comprobarPermisosYContinuar()
    }

    private fun comprobarPermisosYContinuar() {
        val camaraConcedida =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

        val microfonoConcedido =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (camaraConcedida && microfonoConcedido) {
            iniciarVideollamada()
        } else {
            Toast.makeText(
                this,
                "Debes conceder los permisos de cámara y micrófono",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun comprobarPermisosYLLamar() {
        val permisos = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val faltantes = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) !=
                    PackageManager.PERMISSION_GRANTED
        }

        if (faltantes.isEmpty()) {
            iniciarVideollamada()
        } else {
            permisosLlamada.launch(faltantes.toTypedArray())
        }
    }

}