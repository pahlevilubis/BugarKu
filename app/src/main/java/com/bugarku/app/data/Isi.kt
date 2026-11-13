package com.bugarku.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Satu gerakan latihan beserta data animasinya. */
class Gerakan(
    val id: String,
    val nama: String,
    val kat: String,
    val otot: String,
    val level: Int,
    val tipe: String,
    val n: Int,
    val sisi: Boolean,
    val met: Double,
    val ringan: Boolean,
    val sorot: List<String>,
    val langkah: List<String>,
    val tips: List<String>,
    val salah: List<String>,
    val mudah: String,
    val depan: Boolean,
    val fps: Float,
    val nf: Int,
    val kotak: FloatArray,
    val props: List<FloatArray>,
    val ikon: Int,
    /** nf frame × 17 titik × (x, y), koordinat dunia (y ke atas, lantai di 0). */
    val frame: FloatArray,
) {
    val detik: Boolean get() = tipe == "detik"

    /** Gerakan di lantai (tiduran/merangkak/plank) digambar dengan matras. */
    val diLantai: Boolean by lazy {
        val leherY0 = frame[1 * 2 + 1]
        val leherYIkon = frame[ikon * TITIK * 2 + 1 * 2 + 1]
        minOf(leherY0, leherYIkon) < 32f
    }

    companion object {
        const val TITIK = 17
    }
}

class ItemSesi(val g: String, val n: Int)

class Sesi(
    val id: String,
    val judul: String,
    val ringkas: String,
    val kat: String,
    val level: Int,
    val putaran: Int,
    val istirahat: Int,
    val item: List<ItemSesi>,
)

class HariProgram(val sesi: String, val kali: Double, val istirahat: String) {
    val libur: Boolean get() = sesi.isEmpty() || istirahat.isNotEmpty()
}

class Program(
    val id: String,
    val judul: String,
    val ringkas: String,
    val kat: String,
    val catatan: String,
    val hari: List<HariProgram>,
)

class BagianArtikel(val t: String, val teks: String, val daftar: List<String>)

class Artikel(val id: String, val judul: String, val ringkas: String, val ikon: String, val isi: List<BagianArtikel>)

class Kategori(val id: String, val nama: String)

/** Isi app (gerakan, sesi, program, artikel) dari assets/bugarku.json. */
object Isi {
    var kategori: List<Kategori> = emptyList()
        private set
    var gerakan: List<Gerakan> = emptyList()
        private set
    var sesi: List<Sesi> = emptyList()
        private set
    var program: List<Program> = emptyList()
        private set
    var artikel: List<Artikel> = emptyList()
        private set
    var tips: List<String> = emptyList()
        private set

    private val petaGerakan = HashMap<String, Gerakan>()
    private val petaSesi = HashMap<String, Sesi>()
    private var siap = false

    @Synchronized
    fun muat(context: Context) {
        if (siap) return
        val teks = context.applicationContext.assets.open("bugarku.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val akar = JSONObject(teks)
        kategori = akar.getJSONArray("kategori").objek().map { Kategori(it.getString("id"), it.getString("nama")) }
        gerakan = akar.getJSONArray("gerakan").objek().map { bacaGerakan(it) }
        sesi = akar.getJSONArray("sesi").objek().map { s ->
            Sesi(
                id = s.getString("id"),
                judul = s.getString("judul"),
                ringkas = s.getString("ringkas"),
                kat = s.getString("kat"),
                level = s.getInt("level"),
                putaran = s.getInt("putaran"),
                istirahat = s.optInt("istirahat", 0),
                item = s.getJSONArray("item").objek().map { ItemSesi(it.getString("g"), it.getInt("n")) },
            )
        }
        program = akar.getJSONArray("program").objek().map { p ->
            Program(
                id = p.getString("id"),
                judul = p.getString("judul"),
                ringkas = p.getString("ringkas"),
                kat = p.getString("kat"),
                catatan = p.getString("catatan"),
                hari = p.getJSONArray("hari").objek().map {
                    HariProgram(it.optString("sesi", ""), it.optDouble("kali", 1.0), it.optString("istirahat", ""))
                },
            )
        }
        artikel = akar.getJSONArray("artikel").objek().map { a ->
            Artikel(
                id = a.getString("id"),
                judul = a.getString("judul"),
                ringkas = a.getString("ringkas"),
                ikon = a.getString("ikon"),
                isi = a.getJSONArray("isi").objek().map { b ->
                    val v = b.get("v")
                    if (v is JSONArray) BagianArtikel(b.getString("t"), "", v.teks())
                    else BagianArtikel(b.getString("t"), v.toString(), emptyList())
                },
            )
        }
        tips = akar.getJSONArray("tips").teks()
        gerakan.forEach { petaGerakan[it.id] = it }
        sesi.forEach { petaSesi[it.id] = it }
        siap = true
    }

    private fun bacaGerakan(g: JSONObject): Gerakan {
        val bagian = g.getString("f").split(' ')
        val frame = FloatArray(bagian.size)
        for (i in bagian.indices) frame[i] = bagian[i].toFloat()
        val kt = g.getJSONArray("kotak")
        val props = g.getJSONArray("props")
        return Gerakan(
            id = g.getString("id"),
            nama = g.getString("nama"),
            kat = g.getString("kat"),
            otot = g.getString("otot"),
            level = g.getInt("level"),
            tipe = g.getString("tipe"),
            n = g.getInt("n"),
            sisi = g.optBoolean("sisi", false),
            met = g.optDouble("met", 4.0),
            ringan = g.optBoolean("ringan", false),
            sorot = g.getJSONArray("sorot").teks(),
            langkah = g.getJSONArray("langkah").teks(),
            tips = g.getJSONArray("tips").teks(),
            salah = g.getJSONArray("salah").teks(),
            mudah = g.optString("mudah", ""),
            depan = g.optString("tampak") == "depan",
            fps = g.getDouble("fps").toFloat(),
            nf = g.getInt("nf"),
            kotak = FloatArray(4) { kt.getDouble(it).toFloat() },
            props = (0 until props.length()).map { i ->
                val p = props.getJSONArray(i)
                FloatArray(4) { p.getDouble(it).toFloat() }
            },
            ikon = g.optInt("ikon", 0),
            frame = frame,
        )
    }

    fun gerakan(id: String): Gerakan? = petaGerakan[id]
    fun sesi(id: String): Sesi? = petaSesi[id]
    fun program(id: String): Program? = program.firstOrNull { it.id == id }
    fun artikel(id: String): Artikel? = artikel.firstOrNull { it.id == id }
    fun namaKategori(id: String): String = kategori.firstOrNull { it.id == id }?.nama ?: id

    /** Sesi yang cocok untuk satu bagian tubuh, disesuaikan dengan level pengguna. */
    fun sesiUntuk(kat: String, level: Int): Sesi? {
        val calon = sesi.filter { it.kat == kat }
        if (calon.isEmpty()) return null
        return calon.minByOrNull { kotlin.math.abs(it.level - level) * 10 + it.level }
    }
}

private fun JSONArray.objek(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
private fun JSONArray.teks(): List<String> = (0 until length()).map { getString(it) }
