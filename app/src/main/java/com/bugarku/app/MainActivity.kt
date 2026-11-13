package com.bugarku.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import com.bugarku.app.data.Store
import com.bugarku.app.notif.Pengingat
import com.bugarku.app.ui.BugarRoot
import com.bugarku.app.ui.BugarTheme
import com.bugarku.app.ui.Nav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bacaIntent(intent)
        setContent {
            val gelap = when (Store.tema) {
                "gelap" -> true
                "sistem" -> isSystemInDarkTheme()
                else -> false
            }
            LaunchedEffect(gelap) {
                val gaya = if (gelap) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = gaya, navigationBarStyle = gaya)
            }
            BugarTheme(gelap) {
                BugarRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        bacaIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        Pengingat.jadwalkanSemua(this)
    }

    private fun bacaIntent(intent: Intent?) {
        val buka = intent?.getStringExtra("buka")
        if (!buka.isNullOrBlank()) {
            Nav.minta(buka)
            intent?.removeExtra("buka")
        }
    }
}
