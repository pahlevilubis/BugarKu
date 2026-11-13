package com.bugarku.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bugarku.app.notif.Pengingat
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Catatan satu sesi latihan yang sudah selesai. */
class Catatan(
    val waktu: Long,
    val tanggal: String,
    val judul: String,
    val sesiId: String,
    val detik: Int,
    val kalori: Int,
    val gerakan: Int,
)

/** Catatan ukuran tubuh. Nilai 0 = tidak diisi. */
class Ukur(val tanggal: String, val berat: Float, val perut: Float)

/** Semua pengaturan & catatan, tersimpan di HP saja. */
object Store {
    private lateinit var sp: SharedPreferences
    private lateinit var app: Context
    private var siap = false

    // ── Profil ──
    var nama by mutableStateOf("")
        private set
    var pria by mutableStateOf(true)
        private set
    var tinggi by mutableIntStateOf(165)
        private set
    var berat by mutableFloatStateOf(70f)
        private set
    var level by mutableIntStateOf(1)
        private set

    // ── Latihan ──
    var suara by mutableStateOf(true)
        private set
    var bip by mutableStateOf(true)
        private set
    var getar by mutableStateOf(true)
        private set
    var istirahat by mutableIntStateOf(20)
        private set
    var bersiap by mutableIntStateOf(10)
        private set

    // ── Tampilan ──
    var tema by mutableStateOf("terang")
        private set

    // ── Pengingat ──
    var olahragaAktif by mutableStateOf(true)
        private set
    var olahragaJam by mutableIntStateOf(6 * 60)
        private set
    var olahragaHari by mutableIntStateOf(0b0111111) // Senin–Sabtu
        private set
    var gerakAktif by mutableStateOf(false)
        private set
    var gerakInterval by mutableIntStateOf(60)
        private set
    var gerakMulai by mutableIntStateOf(9 * 60)
        private set
    var gerakSelesai by mutableIntStateOf(17 * 60)
        private set
    var gerakHari by mutableIntStateOf(0b0011111) // Senin–Jumat
        private set
    var minumAktif by mutableStateOf(false)
        private set
    var minumInterval by mutableIntStateOf(90)
        private set
    var minumMulai by mutableIntStateOf(7 * 60)
        private set
    var minumSelesai by mutableIntStateOf(21 * 60)
        private set

    // ── Program ──
    var programAktif by mutableStateOf("")
        private set
    var programSelesai by mutableStateOf(setOf<String>())
        private set

    // ── Catatan ──
    val riwayat = mutableStateListOf<Catatan>()
    val ukur = mutableStateListOf<Ukur>()

    fun init(context: Context) {
        if (siap) return
        app = context.applicationContext
        sp = app.getSharedPreferences("bugarku", Context.MODE_PRIVATE)
        nama = sp.getString("nama", "") ?: ""
        pria = sp.getBoolean("pria", true)
        tinggi = sp.getInt("tinggi", 165)
        berat = sp.getFloat("berat", 70f)
        level = sp.getInt("level", 1)
        suara = sp.getBoolean("suara", true)
        bip = sp.getBoolean("bip", true)
        getar = sp.getBoolean("getar", true)
        istirahat = sp.getInt("istirahat", 20)
        bersiap = sp.getInt("bersiap", 10)
        tema = sp.getString("tema", "terang") ?: "terang"
        olahragaAktif = sp.getBoolean("olahragaAktif", true)
        olahragaJam = sp.getInt("olahragaJam", 6 * 60)
        olahragaHari = sp.getInt("olahragaHari", 0b0111111)
        gerakAktif = sp.getBoolean("gerakAktif", false)
        gerakInterval = sp.getInt("gerakInterval", 60)
        gerakMulai = sp.getInt("gerakMulai", 9 * 60)
        gerakSelesai = sp.getInt("gerakSelesai", 17 * 60)
        gerakHari = sp.getInt("gerakHari", 0b0011111)
        minumAktif = sp.getBoolean("minumAktif", false)
        minumInterval = sp.getInt("minumInterval", 90)
        minumMulai = sp.getInt("minumMulai", 7 * 60)
        minumSelesai = sp.getInt("minumSelesai", 21 * 60)
        programAktif = sp.getString("programAktif", "") ?: ""
        programSelesai = sp.getStringSet("programSelesai", emptySet())?.toSet() ?: emptySet()
        bacaRiwayat()
        bacaUkur()
        siap = true
    }

    // ── Profil & latihan ──
    fun aturNama(v: String) { nama = v; sp.edit().putString("nama", v).apply() }
    fun aturPria(v: Boolean) { pria = v; sp.edit().putBoolean("pria", v).apply() }
    fun aturTinggi(v: Int) { tinggi = v; sp.edit().putInt("tinggi", v).apply() }
    fun aturBerat(v: Float) { berat = v; sp.edit().putFloat("berat", v).apply() }
    fun aturLevel(v: Int) { level = v.coerceIn(1, 3); sp.edit().putInt("level", level).apply() }
    fun aturSuara(v: Boolean) { suara = v; sp.edit().putBoolean("suara", v).apply() }
    fun aturBip(v: Boolean) { bip = v; sp.edit().putBoolean("bip", v).apply() }
    fun aturGetar(v: Boolean) { getar = v; sp.edit().putBoolean("getar", v).apply() }
    fun aturIstirahat(v: Int) { istirahat = v; sp.edit().putInt("istirahat", v).apply() }
    fun aturBersiap(v: Int) { bersiap = v; sp.edit().putInt("bersiap", v).apply() }
    fun aturTema(v: String) { tema = v; sp.edit().putString("tema", v).apply() }

    // ── Pengingat ──
    fun aturOlahraga(aktif: Boolean = olahragaAktif, jam: Int = olahragaJam, hari: Int = olahragaHari) {
        olahragaAktif = aktif; olahragaJam = jam; olahragaHari = hari
        sp.edit().putBoolean("olahragaAktif", aktif).putInt("olahragaJam", jam).putInt("olahragaHari", hari).apply()
        Pengingat.jadwalkanSemua(app)
    }

    fun aturGerak(
        aktif: Boolean = gerakAktif, interval: Int = gerakInterval, mulai: Int = gerakMulai,
        selesai: Int = gerakSelesai, hari: Int = gerakHari,
    ) {
        gerakAktif = aktif; gerakInterval = interval; gerakMulai = mulai; gerakSelesai = selesai; gerakHari = hari
        sp.edit().putBoolean("gerakAktif", aktif).putInt("gerakInterval", interval).putInt("gerakMulai", mulai)
            .putInt("gerakSelesai", selesai).putInt("gerakHari", hari).apply()
        Pengingat.jadwalkanSemua(app)
    }

    fun aturMinum(aktif: Boolean = minumAktif, interval: Int = minumInterval, mulai: Int = minumMulai, selesai: Int = minumSelesai) {
        minumAktif = aktif; minumInterval = interval; minumMulai = mulai; minumSelesai = selesai
        sp.edit().putBoolean("minumAktif", aktif).putInt("minumInterval", interval).putInt("minumMulai", mulai)
            .putInt("minumSelesai", selesai).apply()
        Pengingat.jadwalkanSemua(app)
    }

    fun hariAktif(mask: Int, hari: DayOfWeek): Boolean = mask and (1 shl (hari.value - 1)) != 0

    // ── Program ──
    fun mulaiProgram(id: String) { programAktif = id; sp.edit().putString("programAktif", id).apply() }
    fun hentikanProgram() { programAktif = ""; sp.edit().putString("programAktif", "").apply() }

    fun hariSelesai(programId: String, hari: Int): Boolean = "$programId:$hari" in programSelesai

    fun tandaiHari(programId: String, hari: Int, selesai: Boolean = true) {
        val kunci = "$programId:$hari"
        programSelesai = if (selesai) programSelesai + kunci else programSelesai - kunci
        sp.edit().putStringSet("programSelesai", programSelesai.toSet()).apply()
    }

    fun jumlahSelesai(programId: String): Int = programSelesai.count { it.startsWith("$programId:") }

    /** Hari berikutnya yang belum selesai (0-based), atau -1 kalau semua sudah. */
    fun hariBerikutnya(p: Program): Int = p.hari.indices.firstOrNull { !hariSelesai(p.id, it) } ?: -1

    fun ulangProgram(programId: String) {
        programSelesai = programSelesai.filterNot { it.startsWith("$programId:") }.toSet()
        sp.edit().putStringSet("programSelesai", programSelesai.toSet()).apply()
    }

    // ── Riwayat latihan ──
    fun catatLatihan(c: Catatan) {
        riwayat.add(0, c)
        while (riwayat.size > 500) riwayat.removeAt(riwayat.lastIndex)
        simpanRiwayat()
    }

    fun sudahLatihanHariIni(): Boolean {
        val hariIni = LocalDate.now().toString()
        return riwayat.any { it.tanggal == hariIni }
    }

    /** Jumlah hari berturut-turut berlatih, sampai hari ini (atau kemarin). */
    fun runtutan(): Int {
        val tanggal = riwayat.map { it.tanggal }.toHashSet()
        var hari = LocalDate.now()
        if (hari.toString() !in tanggal) hari = hari.minusDays(1)
        var n = 0
        while (hari.toString() in tanggal) {
            n++
            hari = hari.minusDays(1)
        }
        return n
    }

    fun awalMinggu(): LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Menit latihan per hari (Senin..Minggu) minggu ini. */
    fun menitPerHariMinggu(): IntArray {
        val awal = awalMinggu()
        val hasil = IntArray(7)
        for (c in riwayat) {
            val t = try { LocalDate.parse(c.tanggal) } catch (_: Exception) { continue }
            val i = (t.toEpochDay() - awal.toEpochDay()).toInt()
            if (i in 0..6) hasil[i] += c.detik
        }
        for (i in 0..6) hasil[i] = (hasil[i] + 30) / 60
        return hasil
    }

    // ── Ukuran tubuh ──
    fun simpanUkur(tanggal: String, beratBaru: Float, perut: Float) {
        val i = ukur.indexOfFirst { it.tanggal == tanggal }
        val u = Ukur(tanggal, beratBaru, perut)
        if (i >= 0) ukur[i] = u else ukur.add(u)
        urutkanUkur()
        if (beratBaru > 0f) aturBerat(beratBaru)
        simpanDaftarUkur()
    }

    fun hapusUkur(tanggal: String) {
        ukur.removeAll { it.tanggal == tanggal }
        simpanDaftarUkur()
    }

    private fun urutkanUkur() {
        val urut = ukur.sortedBy { it.tanggal }
        ukur.clear()
        ukur.addAll(urut)
    }

    // ── Simpan/baca JSON ──
    private fun simpanRiwayat() {
        val arr = JSONArray()
        riwayat.forEach {
            arr.put(
                JSONObject().put("w", it.waktu).put("t", it.tanggal).put("j", it.judul).put("s", it.sesiId)
                    .put("d", it.detik).put("k", it.kalori).put("g", it.gerakan)
            )
        }
        sp.edit().putString("riwayat", arr.toString()).apply()
    }

    private fun bacaRiwayat() {
        riwayat.clear()
        try {
            val arr = JSONArray(sp.getString("riwayat", "[]") ?: "[]")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                riwayat.add(
                    Catatan(
                        o.optLong("w"), o.optString("t"), o.optString("j"), o.optString("s"),
                        o.optInt("d"), o.optInt("k"), o.optInt("g"),
                    )
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun simpanDaftarUkur() {
        val arr = JSONArray()
        ukur.forEach { arr.put(JSONObject().put("t", it.tanggal).put("b", it.berat.toDouble()).put("p", it.perut.toDouble())) }
        sp.edit().putString("ukur", arr.toString()).apply()
    }

    private fun bacaUkur() {
        ukur.clear()
        try {
            val arr = JSONArray(sp.getString("ukur", "[]") ?: "[]")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                ukur.add(Ukur(o.optString("t"), o.optDouble("b", 0.0).toFloat(), o.optDouble("p", 0.0).toFloat()))
            }
            urutkanUkur()
        } catch (_: Exception) {
        }
    }
}
