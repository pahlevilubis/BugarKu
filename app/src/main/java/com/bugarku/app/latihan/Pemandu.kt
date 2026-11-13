package com.bugarku.app.latihan

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import com.bugarku.app.data.Store
import java.util.Locale

/** Suara pemandu (Text-to-Speech bahasa Indonesia), bunyi bip, dan getar selama latihan. */
class Pemandu(context: Context) : TextToSpeech.OnInitListener {
    private val app = context.applicationContext
    private var tts: TextToSpeech? = null
    private var siap = false
    private var nada: ToneGenerator? = null

    init {
        try {
            tts = TextToSpeech(app, this)
        } catch (_: Exception) {
            tts = null
        }
        nada = try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Exception) {
            null
        }
    }

    override fun onInit(status: Int) {
        val t = tts ?: return
        if (status != TextToSpeech.SUCCESS) return
        val hasil = try {
            t.setLanguage(Locale.forLanguageTag("id-ID"))
        } catch (_: Exception) {
            TextToSpeech.LANG_NOT_SUPPORTED
        }
        siap = hasil != TextToSpeech.LANG_MISSING_DATA && hasil != TextToSpeech.LANG_NOT_SUPPORTED
        if (siap) t.setSpeechRate(1.0f)
    }

    val bisaBicara: Boolean get() = siap

    fun bicara(teks: String, antre: Boolean = false) {
        if (!Store.suara || !siap) return
        try {
            tts?.speak(teks, if (antre) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH, null, "bugarku")
        } catch (_: Exception) {
        }
    }

    fun bip(panjang: Boolean = false) {
        if (Store.bip) {
            try {
                if (panjang) nada?.startTone(ToneGenerator.TONE_PROP_BEEP2, 450)
                else nada?.startTone(ToneGenerator.TONE_PROP_BEEP, 160)
            } catch (_: Exception) {
            }
        }
        if (Store.getar) getar(if (panjang) 220L else 60L)
    }

    private fun getar(ms: Long) {
        try {
            @Suppress("DEPRECATION")
            val v = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(ms)
            }
        } catch (_: Exception) {
        }
    }

    fun diam() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun tutup() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
        try {
            nada?.release()
        } catch (_: Exception) {
        }
        tts = null
        nada = null
    }
}
