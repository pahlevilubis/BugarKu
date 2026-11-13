package com.bugarku.app.data

import kotlin.math.max
import kotlin.math.roundToInt

/** Satu langkah dalam latihan: gerakan + jumlah (repetisi atau detik, per sisi). */
class Langkah(val g: Gerakan, val n: Int, val putaran: Int, val totalPutaran: Int) {
    /** Lama kerja kira-kira (detik), termasuk kedua sisi. */
    val perkiraanDetik: Int
        get() {
            val sisi = if (g.sisi) 2 else 1
            return if (g.detik) n * sisi else (n * detikPerRep(g) * sisi).roundToInt() + 3
        }

    fun teksJumlah(): String {
        val dasar = if (g.detik) jamMenit(n) else "x$n"
        return if (g.sisi) "$dasar tiap sisi" else dasar
    }
}

/** Rencana latihan yang siap dijalankan di layar latihan. */
class Rencana(
    val judul: String,
    val sesiId: String,
    val langkah: List<Langkah>,
    val istirahat: Int,
    val istirahatPutaran: Int,
    val programId: String? = null,
    val hariKe: Int? = null,
) {
    val perkiraanDetik: Int
        get() = langkah.sumOf { it.perkiraanDetik } + max(0, langkah.size - 1) * istirahat

    val perkiraanMenit: Int get() = max(1, (perkiraanDetik / 60.0).roundToInt())
}

val NAMA_LEVEL = listOf("Pemula", "Menengah", "Lanjut")

fun pengaliLevel(level: Int): Double = when (level) {
    1 -> 0.7
    3 -> 1.3
    else -> 1.0
}

fun detikPerRep(g: Gerakan): Double = when (g.id) {
    "burpee" -> 4.5
    "squat_jump", "mountain_climber" -> 2.5
    "superman", "bird_dog", "dead_bug", "glute_bridge", "donkey_kick" -> 3.5
    else -> 3.0
}

fun jumlahUntuk(g: Gerakan, dasar: Int, level: Int, kali: Double): Int {
    val mentah = dasar * pengaliLevel(level) * kali
    return if (g.detik) {
        max(10, ((mentah / 5.0).roundToInt() * 5))
    } else {
        max(3, mentah.roundToInt())
    }
}

fun buatRencana(
    s: Sesi,
    level: Int,
    kali: Double = 1.0,
    istirahatBawaan: Int = Store.istirahat,
    programId: String? = null,
    hariKe: Int? = null,
): Rencana {
    val putaran = when {
        s.kat == "regang" -> s.putaran
        level == 1 -> max(1, s.putaran - 1)
        level == 3 -> minOf(4, s.putaran + 1)
        else -> s.putaran
    }
    val daftar = ArrayList<Langkah>()
    for (p in 1..putaran) {
        for (item in s.item) {
            val g = Isi.gerakan(item.g) ?: continue
            daftar.add(Langkah(g, jumlahUntuk(g, item.n, level, kali), p, putaran))
        }
    }
    val istirahat = if (s.istirahat > 0) s.istirahat else istirahatBawaan
    return Rencana(
        judul = s.judul,
        sesiId = s.id,
        langkah = daftar,
        istirahat = istirahat,
        istirahatPutaran = if (s.istirahat > 0) s.istirahat else max(istirahat, 40),
        programId = programId,
        hariKe = hariKe,
    )
}

/** Latihan satu gerakan saja (dari halaman detail gerakan). */
fun rencanaSatu(g: Gerakan, level: Int): Rencana =
    Rencana(g.nama, "", listOf(Langkah(g, jumlahUntuk(g, g.n, level, 1.0), 1, 1)), 0, 0)

fun jamMenit(detik: Int): String {
    val d = max(0, detik)
    return "%02d:%02d".format(d / 60, d % 60)
}

/** Perkiraan kalori: MET × berat (kg) × jam. */
fun kalori(met: Double, beratKg: Float, detik: Int): Double = met * beratKg * detik / 3600.0
