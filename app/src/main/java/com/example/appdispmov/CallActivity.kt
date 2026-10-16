package com.example.appdispmov

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import com.google.firebase.auth.FirebaseAuth

class CallActivity : AppCompatActivity() {

    companion object {
        private const val ZEGO_APP_ID: Long = 272135506
        private const val ZEGO_APP_SIGN: String = "fd8d70aa33a3a0a82e3f7bb38f4a12eb4319153fb7334258830164e5e0e4d962"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_llamada_zego)

        SystemBarsHelper.applyInsets(
            findViewById(android.R.id.content)
        )

        val callID = intent.getStringExtra("callID") ?: return

        val auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser ?: return

        val userID = usuarioActual.uid

        val userName =
            usuarioActual.displayName
                ?: getString(R.string.default_user)

        val config =
            ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall().apply {
                turnOnCameraWhenJoining = true
                turnOnMicrophoneWhenJoining = true
                useSpeakerWhenJoining = true
            }

        val fragment =
            ZegoUIKitPrebuiltCallFragment.newInstance(
                ZEGO_APP_ID,
                ZEGO_APP_SIGN,
                userID,
                userName,
                callID,
                config
            )

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_container,
                fragment
            )
            .commitNow()
    }
}