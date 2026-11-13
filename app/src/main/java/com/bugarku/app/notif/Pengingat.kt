package com.bugarku.app.notif

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bugarku.app.MainActivity
import com.bugarku.app.R
import com.bugarku.app.data.Isi
import com.bugarku.app.data.Store
import com.bugarku.app.data.buatRencana
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Pengingat olahraga, gerak saat kerja, dan minum air. */
object Pengingat {
    const val OLAHRAGA = 1
    const val GERAK = 2
    const val MINUM = 3

    private const val KANAL_OLAHRAGA = "olahraga"
    private const val KANAL_GERAK = "gerak"
    private const val KANAL_MINUM = "minum"

    fun buatKanal(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(KANAL_OLAHRAGA, "Pengingat olahraga", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Mengingatkan jadwal olahraga pada hari dan jam yang dipilih" }
        )
        nm.createNotificationChannel(
            NotificationChannel(KANAL_GERAK, "Gerak saat kerja", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Ajakan berdiri dan peregangan setelah duduk lama" }
        )
        nm.createNotificationChannel(
            NotificationChannel(KANAL_MINUM, "Minum air", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Pengingat minum air putih" }
        )
    }

    /** Waktu pengingat berikutnya, atau null kalau dimatikan. */
    fun berikutnya(jenis: Int, dari: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val batas = dari.plusSeconds(20)
        for (tambah in 0L..7L) {
            val tgl: LocalDate = dari.toLocalDate().plusDays(tambah)
            when (jenis) {
                OLAHRAGA -> {
                    if (!Store.olahragaAktif || Store.olahragaHari == 0) return null
                    if (!Store.hariAktif(Store.olahragaHari, tgl.dayOfWeek)) continue
                    val k = tgl.atStartOfDay().plusMinutes(Store.olahragaJam.toLong())
                    if (k.isAfter(batas)) return k
                }
                GERAK -> {
                    if (!Store.gerakAktif || Store.gerakHari == 0) return null
                    if (!Store.hariAktif(Store.gerakHari, tgl.dayOfWeek)) continue
                    val k = slotBerikutnya(tgl, Store.gerakMulai + Store.gerakInterval, Store.gerakSelesai, Store.gerakInterval, batas)
                    if (k != null) return k
                }
                MINUM -> {
                    if (!Store.minumAktif) return null
                    val k = slotBerikutnya(tgl, Store.minumMulai, Store.minumSelesai, Store.minumInterval, batas)
                    if (k != null) return k
                }
                else -> return null
            }
        }
        return null
    }

    private fun slotBerikutnya(tgl: LocalDate, mulai: Int, selesai: Int, interval: Int, batas: LocalDateTime): LocalDateTime? {
        if (interval <= 0) return null
        var m = mulai
        while (m <= selesai) {
            val k = tgl.atStartOfDay().plusMinutes(m.toLong())
            if (k.isAfter(batas)) return k
            m += interval
        }
        return null
    }

    fun jadwalkanSemua(context: Context) {
        val am = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return
        listOf(OLAHRAGA, GERAK, MINUM).forEach { atur(context, am, it, berikutnya(it)) }
    }

    fun bolehTepatWaktu(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return false
        return am.canScheduleExactAlarms()
    }

    private fun atur(context: Context, am: AlarmManager, jenis: Int, kapan: LocalDateTime?) {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("jenis", jenis)
        val ms = kapan?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        if (ms != null) intent.putExtra("waktu", ms)
        val pi = PendingIntent.getBroadcast(context, jenis, intent, flagPending())
        am.cancel(pi)
        if (ms == null) return
        try {
            if (bolehTepatWaktu(context)) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, ms, pi)
            } else {
                AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, ms, pi)
            }
        } catch (_: SecurityException) {
            AlarmManagerCompat.setAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, ms, pi)
        }
    }

    fun flagPending(): Int {
        val dasar = PendingIntent.FLAG_UPDATE_CURRENT
        return if (Build.VERSION.SDK_INT >= 23) dasar or PendingIntent.FLAG_IMMUTABLE else dasar
    }

    fun bolehNotif(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Teks notifikasi olahraga: hari program berikutnya kalau ada program aktif. */
    private fun teksOlahraga(): String {
        val p = Isi.program(Store.programAktif)
        if (p != null) {
            val h = Store.hariBerikutnya(p)
            if (h >= 0) {
                val hari = p.hari[h]
                val s = Isi.sesi(hari.sesi)
                return if (!hari.libur && s != null) {
                    val r = buatRencana(s, Store.level, hari.kali)
                    "${p.judul} — Hari ${h + 1}: ${s.judul} (±${r.perkiraanMenit} menit). Ketuk untuk mulai."
                } else {
                    "${p.judul} — Hari ${h + 1}: istirahat aktif. Jalan kaki santai 20–30 menit, yuk."
                }
            }
        }
        return "Latihan singkat 10–15 menit hari ini? Ketuk untuk memilih latihan."
    }

    fun tampilkan(context: Context, jenis: Int, uji: Boolean = false) {
        if (!bolehNotif(context)) return
        // Sudah latihan hari ini? Tidak perlu diingatkan lagi.
        if (jenis == OLAHRAGA && !uji && Store.sudahLatihanHariIni()) return
        val judul: String
        val isi: String
        val buka: String
        val kanal: String
        when (jenis) {
            OLAHRAGA -> {
                judul = if (Store.nama.isNotBlank()) "Waktunya olahraga, ${Store.nama}!" else "Waktunya olahraga!"
                isi = teksOlahraga()
                buka = "olahraga"
                kanal = KANAL_OLAHRAGA
            }
            GERAK -> {
                judul = "Sudah duduk cukup lama"
                isi = "Berdiri sebentar dan lakukan Peregangan Kantor 2–3 menit. Punggung dan leher akan berterima kasih."
                buka = "regang_kantor"
                kanal = KANAL_GERAK
            }
            else -> {
                judul = "Minum air putih"
                isi = "Segelas air putih dulu, yuk — supaya tetap segar dan tidak salah mengira haus sebagai lapar."
                buka = ""
                kanal = KANAL_MINUM
            }
        }
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("buka", buka)
        val pi = PendingIntent.getActivity(context, 100 + jenis, intent, flagPending())
        val n = NotificationCompat.Builder(context, kanal)
            .setSmallIcon(R.drawable.ic_stat_bugar)
            .setContentTitle(judul)
            .setContentText(isi)
            .setStyle(NotificationCompat.BigTextStyle().bigText(isi))
            .setColor(0xFFFF6A3D.toInt())
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(if (jenis == OLAHRAGA) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        ContextCompat.getSystemService(context, NotificationManager::class.java)?.notify(jenis, n)
    }
}

/** Dipanggil AlarmManager saat waktunya tiba. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Store.init(context)
        Isi.muat(context)
        val jenis = intent.getIntExtra("jenis", 0)
        val waktu = intent.getLongExtra("waktu", 0L)
        val telat = if (waktu > 0) System.currentTimeMillis() - waktu else 0L
        // Kalau HP mati lama lalu alarm baru jalan, jangan tampilkan pengingat yang sudah basi
        if (telat < 30 * 60 * 1000L) Pengingat.tampilkan(context, jenis)
        Pengingat.jadwalkanSemua(context)
    }
}

/** Setelah HP dinyalakan ulang / jam diubah / app diperbarui: jadwalkan ulang. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Store.init(context)
        Pengingat.jadwalkanSemua(context)
    }
}
