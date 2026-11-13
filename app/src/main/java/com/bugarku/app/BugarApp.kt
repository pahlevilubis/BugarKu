package com.bugarku.app

import android.app.Application
import com.bugarku.app.data.Isi
import com.bugarku.app.data.Store
import com.bugarku.app.notif.Pengingat

class BugarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Store.init(this)
        Isi.muat(this)
        Pengingat.buatKanal(this)
        Pengingat.jadwalkanSemua(this)
    }
}
