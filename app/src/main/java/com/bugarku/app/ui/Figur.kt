package com.bugarku.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Gerakan
import com.bugarku.app.data.Store
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Penampilan peraga (warna & gaya pakaian). Sama dengan tools/karakter.py. */
private class Peraga(
    val kulit: Color,
    val kulitG: Color,
    val rambut: Color,
    val atasan: Color,
    val atasanG: Color,
    val bawahan: Color,
    val bawahanG: Color,
    val sepatu: Color,
    val sepatuG: Color,
    val sol: Color,
    /** Panjang lengan baju (0 = tanpa lengan / tank top). */
    val lenganBaju: Float,
    /** Panjang celana di paha (1 = celana panjang). */
    val celana: Float,
    val kuncir: Boolean,
    val pipi: Boolean,
    val ramping: Float,
)

/** Peraga wanita: kuncir kuda, tank top & celana pendek navy, sepatu toska. */
private val WANITA = Peraga(
    kulit = Color(0xFFEDBE99), kulitG = Color(0xFFD09E7A), rambut = Color(0xFF2A1D19),
    atasan = Color(0xFF283047), atasanG = Color(0xFF1C2233), bawahan = Color(0xFF283047), bawahanG = Color(0xFF1C2233),
    sepatu = Color(0xFF2BC0C4), sepatuG = Color(0xFF21999C), sol = Color(0xFFFF7A45),
    lenganBaju = 0f, celana = 0.42f, kuncir = true, pipi = true, ramping = 0.92f,
)

/** Peraga pria: kaos & celana training, sepatu putih. */
private val PRIA = Peraga(
    kulit = Color(0xFFC68B59), kulitG = Color(0xFFA87146), rambut = Color(0xFF231E1E),
    atasan = Color(0xFF2C3240), atasanG = Color(0xFF1E222D), bawahan = Color(0xFF1C1F26), bawahanG = Color(0xFF121419),
    sepatu = Color(0xFFF5F5F5), sepatuG = Color(0xFFD9D9D9), sol = Color(0xFFFF6A3D),
    lenganBaju = 0.45f, celana = 1f, kuncir = false, pipi = false, ramping = 1f,
)

private val MATA = Color(0xFF2B2020)
private val BIBIR = Color(0xFFB4584A)
private val PIPI = Color(0x46FF7878)
private val JAM = Color(0xFF22252C)
private val SOROT = Color(0x7DFF460A)
private val KAYU = Color(0xFF9A7A5A)

/**
 * Peraga latihan yang bergerak (atau diam di pose ikon kalau animasi = false).
 * Data pose berasal dari assets/bugarku.json.
 */
@Composable
fun Figur(
    g: Gerakan,
    modifier: Modifier = Modifier,
    animasi: Boolean = true,
    jalan: Boolean = true,
    sorot: Boolean = true,
    latar: Boolean = true,
    sudut: Dp = 20.dp,
    /** Paksa peraga tertentu (untuk pratinjau di pengaturan); null = ikut pengaturan. */
    wanita: Boolean? = null,
) {
    val gelap = LocalGelap.current
    val warnaLatar = if (gelap) Color(0xFF252A31) else Color(0xFFECEFF3)
    var waktu by remember(g.id) { mutableFloatStateOf(0f) }
    if (animasi) {
        LaunchedEffect(g.id, jalan) {
            if (!jalan) return@LaunchedEffect
            var lalu = 0L
            while (true) {
                withFrameNanos { sekarang ->
                    if (lalu != 0L) waktu += (sekarang - lalu) / 1_000_000_000f
                    lalu = sekarang
                }
            }
        }
    }
    val peraga = if (wanita ?: Store.peragaWanita) WANITA else PRIA
    val path = remember { Path() }
    val bentuk = RoundedCornerShape(sudut)
    val dasar = if (latar) modifier.clip(bentuk).background(warnaLatar) else modifier
    Canvas(dasar) {
        val posisi = if (animasi) (waktu * g.fps) % g.nf else g.ikon.toFloat()
        gambarFigur(g, posisi, waktu, peraga, sorot, gelap, path)
    }
}

/** Contoh kecil peraga untuk pilihan di menu Atur. */
@Composable
fun PratinjauPeraga(g: Gerakan, wanita: Boolean) {
    Figur(g, Modifier.fillMaxWidth().aspectRatio(0.9f), animasi = true, sorot = false, sudut = 14.dp, wanita = wanita)
}

private fun antara(a: Offset, b: Offset, t: Float): Offset = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

private fun arahDerajat(d: Float): Offset {
    val r = Math.toRadians(d.toDouble())
    return Offset(cos(r).toFloat(), sin(r).toFloat())
}

private fun sudutDari(v: Offset): Float = Math.toDegrees(atan2(v.y.toDouble(), v.x.toDouble())).toFloat()

private fun DrawScope.tabung(path: Path, a: Offset, b: Offset, ra: Float, rb: Float, c: Color) {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val l = max(sqrt(dx * dx + dy * dy), 0.001f)
    val nx = -dy / l
    val ny = dx / l
    path.reset()
    path.moveTo(a.x + nx * ra, a.y + ny * ra)
    path.lineTo(b.x + nx * rb, b.y + ny * rb)
    path.lineTo(b.x - nx * rb, b.y - ny * rb)
    path.lineTo(a.x - nx * ra, a.y - ny * ra)
    path.close()
    drawPath(path, c)
    drawCircle(c, ra, a)
    drawCircle(c, rb, b)
}

private fun DrawScope.garis(a: Offset, b: Offset, lebar: Float, c: Color) {
    drawLine(c, a, b, lebar, StrokeCap.Round)
}

private fun DrawScope.gambarFigur(
    g: Gerakan,
    posisi: Float,
    waktu: Float,
    w: Peraga,
    sorot: Boolean,
    gelap: Boolean,
    path: Path,
) {
    val nf = g.nf
    val i0 = (floor(posisi).toInt() % nf + nf) % nf
    val i1 = (i0 + 1) % nf
    val a = posisi - floor(posisi)
    val f = g.frame
    val n2 = Gerakan.TITIK * 2
    val b0 = i0 * n2
    val b1 = i1 * n2
    val pts = FloatArray(n2)
    for (k in 0 until n2) pts[k] = f[b0 + k] + (f[b1 + k] - f[b0 + k]) * a

    val kt = g.kotak
    val lebar = kt[2] - kt[0]
    val tinggi = kt[3] - kt[1]
    val pad = size.minDimension * 0.1f
    val sk = min((size.width - 2 * pad) / lebar, (size.height - 2 * pad) / tinggi)
    val ox = (size.width - lebar * sk) / 2 - kt[0] * sk
    val bawah = size.height - pad * 0.8f
    fun ke(x: Float, y: Float) = Offset(ox + x * sk, bawah - (y - kt[1]) * sk)
    fun p(i: Int) = ke(pts[i * 2], pts[i * 2 + 1])
    val u = sk
    val lantai = ke(0f, 0f).y

    // Matras & bayangan
    if (g.diLantai) {
        val kiri = ke(kt[0] - 4f, 0f).x
        val kanan = ke(kt[2] + 4f, 0f).x
        drawRoundRect(
            color = if (gelap) Color(0xFF3A4049) else Color(0xFF4A505C),
            topLeft = Offset(kiri, lantai - 1.2f * u),
            size = Size(kanan - kiri, 2.8f * u),
            cornerRadius = CornerRadius(1.2f * u, 1.2f * u),
        )
    }
    var minX = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    for (i in 0 until Gerakan.TITIK) {
        val x = p(i).x
        if (x < minX) minX = x
        if (x > maxX) maxX = x
    }
    drawOval(
        color = Color.Black.copy(alpha = if (gelap) 0.25f else 0.10f),
        topLeft = Offset(minX - 2f * u, lantai - 1.2f * u),
        size = Size(maxX - minX + 4f * u, 3f * u),
    )
    for (pr in g.props) {
        tabung(path, ke(pr[0], pr[1]), ke(pr[2], pr[3]), 1.3f * u, 1.3f * u, KAYU)
    }

    val depan = g.depan
    val rp = w.ramping

    fun lengan(n: Int, gelapSisi: Boolean, jam: Boolean) {
        val bahu = p(if (n == 1) 3 else 6)
        val siku = p(if (n == 1) 4 else 7)
        val tangan = p(if (n == 1) 5 else 8)
        val kul = if (gelapSisi) w.kulitG else w.kulit
        tabung(path, siku, tangan, 1.75f * u * rp, 1.35f * u * rp, kul)
        tabung(path, bahu, siku, 2.2f * u * rp, 1.75f * u * rp, kul)
        if (w.lenganBaju > 0f) {
            tabung(path, bahu, antara(bahu, siku, w.lenganBaju), 2.75f * u, 2.45f * u, if (gelapSisi) w.atasanG else w.atasan)
        }
        drawCircle(kul, 1.65f * u * rp, tangan)
        if (jam) drawCircle(JAM, 1.05f * u, antara(siku, tangan, 0.8f))
    }

    fun kaki(n: Int, gelapSisi: Boolean) {
        val pg = p(if (n == 1) 9 else 13)
        val lutut = p(if (n == 1) 10 else 14)
        val mata = p(if (n == 1) 11 else 15)
        val jari = p(if (n == 1) 12 else 16)
        val kul = if (gelapSisi) w.kulitG else w.kulit
        val cel = if (gelapSisi) w.bawahanG else w.bawahan
        val pendek = w.celana < 1f
        tabung(path, lutut, mata, 2.6f * u * rp, 1.75f * u * rp, if (pendek) kul else cel)
        tabung(path, pg, lutut, 3.7f * u * rp, 2.6f * u * rp, if (pendek) kul else cel)
        if (pendek) tabung(path, pg, antara(pg, lutut, w.celana), 4.1f * u * rp, 3.5f * u * rp, cel)
        tabung(path, mata, jari, 1.95f * u, 1.65f * u, if (gelapSisi) w.sepatuG else w.sepatu)
        tabung(path, antara(mata, jari, 0.05f), jari, 0.85f * u, 0.75f * u, w.sol)
    }

    val bayang = !depan
    kaki(2, bayang)
    lengan(2, bayang, false)

    val pg = p(2)
    val lh = p(1)
    val kp = p(0)
    val rk = 5.4f * u
    val atas = sudutDari(kp - lh)
    val keAtas = arahDerajat(atas)
    val muka = arahDerajat(atas + 90f)
    val belakang = arahDerajat(atas - 90f)

    // Kuncir kuda (di belakang kepala), ikut berayun saat bergerak
    if (w.kuncir) {
        val akar = if (depan) kp + keAtas * (rk * 0.75f) else kp + arahDerajat(atas - 40f) * (rk * 0.8f)
        val condong = if (depan) Offset(muka.x * 0.5f, muka.y * 0.5f + 0.9f)
        else Offset(belakang.x * 0.6f, belakang.y * 0.6f + 0.8f)
        val a1 = sudutDari(condong) + sin(waktu * 7f) * 9f
        val tengah = akar + arahDerajat(a1) * (rk * 1.05f)
        val tekuk = if (cos(Math.toRadians(a1.toDouble())) < 0) 15f else -15f
        val a2 = a1 + tekuk + sin(waktu * 7f + 0.8f) * 12f
        val ujung = tengah + arahDerajat(a2) * rk
        tabung(path, akar, tengah, rk * 0.36f, rk * 0.27f, w.rambut)
        tabung(path, tengah, ujung, rk * 0.27f, rk * 0.07f, w.rambut)
    }

    // Badan
    if (depan) {
        val b1 = p(3)
        val b2 = p(6)
        val g1 = p(9)
        val g2 = p(13)
        val pusat = antara(pg, lh, 0.5f)
        val masuk = if (w.kuncir) 0.14f else 0.05f
        val pinggang1 = antara(antara(g1, b1, 0.42f), pusat, masuk)
        val pinggang2 = antara(antara(g2, b2, 0.42f), pusat, masuk)
        tabung(path, g1, g2, 3.7f * u, 3.7f * u, w.bawahan)
        path.reset()
        path.moveTo(b1.x, b1.y)
        path.lineTo(b2.x, b2.y)
        path.lineTo(pinggang2.x, pinggang2.y)
        val c2 = antara(g2, b2, 0.12f)
        val c1 = antara(g1, b1, 0.12f)
        path.lineTo(c2.x, c2.y)
        path.lineTo(c1.x, c1.y)
        path.lineTo(pinggang1.x, pinggang1.y)
        path.close()
        drawPath(path, w.atasan)
        tabung(path, b1, b2, 2.3f * u, 2.3f * u, w.atasan)
        tabung(path, antara(g1, b1, 0.1f), antara(g2, b2, 0.1f), 3.3f * u, 3.3f * u, w.atasan)
    } else {
        tabung(path, pg, antara(pg, lh, 0.16f), 4.5f * u * rp, 4.2f * u * rp, w.bawahan)
        tabung(path, antara(pg, lh, 0.12f), antara(pg, lh, 0.5f), 4.2f * u * rp, 3.9f * u * rp, w.atasan)
        tabung(path, antara(pg, lh, 0.48f), antara(pg, lh, 0.84f), 3.9f * u * rp, 4.8f * u * rp, w.atasan)
        tabung(path, antara(pg, lh, 0.8f), lh, 4.5f * u * rp, 3.1f * u * rp, w.atasan)
    }
    kaki(1, false)

    // Leher & kepala
    tabung(path, lh, antara(lh, kp, 0.62f), 1.75f * u, 1.75f * u, w.kulitG)
    drawCircle(w.kulit, rk, kp)
    val rr = rk * 1.07f
    val kotakRambut = Size(rr * 2, rr * 2)
    val kiriAtas = Offset(kp.x - rr, kp.y - rr)
    if (depan) {
        drawArc(w.rambut, atas - 98f, 196f, true, kiriAtas, kotakRambut)
        if (w.kuncir) {
            for (s in listOf(1f, -1f)) {
                garis(
                    kp + muka * (s * rk * 0.88f) + keAtas * (rk * 0.35f),
                    kp + muka * (s * rk * 0.93f) - keAtas * (rk * 0.75f),
                    rk * 0.42f, w.rambut,
                )
            }
        }
        for (s in listOf(1f, -1f)) {
            val mata = kp + muka * (s * rk * 0.36f) - keAtas * (rk * 0.05f)
            drawCircle(MATA, rk * 0.13f, mata)
            drawCircle(Color.White, rk * 0.045f, mata + muka * (rk * 0.04f) + keAtas * (rk * 0.05f))
            garis(
                kp + muka * (s * rk * 0.22f) + keAtas * (rk * 0.24f),
                kp + muka * (s * rk * 0.5f) + keAtas * (rk * 0.22f),
                rk * 0.08f, w.rambut,
            )
            if (w.pipi) drawCircle(PIPI, rk * 0.16f, kp + muka * (s * rk * 0.55f) - keAtas * (rk * 0.3f))
        }
        garis(
            kp - muka * (rk * 0.14f) - keAtas * (rk * 0.5f),
            kp + muka * (rk * 0.14f) - keAtas * (rk * 0.5f),
            rk * 0.08f, BIBIR,
        )
    } else {
        val awal = if (w.kuncir) atas - 150f else atas - 140f
        drawArc(w.rambut, awal, atas + 48f - awal, true, kiriAtas, kotakRambut)
        // telinga, hidung, mata, alis, mulut
        drawCircle(w.kulitG, rk * 0.2f, kp - muka * (rk * 0.12f))
        drawCircle(w.kulit, rk * 0.15f, kp + muka * (rk * 0.95f) - keAtas * (rk * 0.08f))
        val mata = kp + muka * (rk * 0.55f) - keAtas * (rk * 0.02f)
        drawCircle(MATA, rk * 0.13f, mata)
        drawCircle(Color.White, rk * 0.045f, mata + muka * (rk * 0.04f) + keAtas * (rk * 0.05f))
        garis(
            kp + muka * (rk * 0.42f) + keAtas * (rk * 0.27f),
            kp + muka * (rk * 0.72f) + keAtas * (rk * 0.25f),
            rk * 0.08f, w.rambut,
        )
        garis(
            kp + muka * (rk * 0.6f) - keAtas * (rk * 0.5f),
            kp + muka * (rk * 0.8f) - keAtas * (rk * 0.45f),
            rk * 0.07f, BIBIR,
        )
        if (w.pipi) drawCircle(PIPI, rk * 0.17f, kp + muka * (rk * 0.4f) - keAtas * (rk * 0.3f))
    }
    lengan(1, false, true)
    if (depan) lengan(2, false, false)

    // Otot yang dilatih
    if (sorot) {
        for (s in g.sorot) {
            when (s) {
                "lengan" -> {
                    garis(antara(p(3), p(4), 0.2f), p(4), 3.2f * u, SOROT)
                    if (depan) garis(antara(p(6), p(7), 0.2f), p(7), 3.2f * u, SOROT)
                }
                "dada", "bahu", "punggung" -> garis(antara(pg, lh, 0.62f), antara(pg, lh, 0.9f), 5.4f * u, SOROT)
                "perut" -> garis(antara(pg, lh, 0.2f), antara(pg, lh, 0.55f), 5.4f * u, SOROT)
                "kaki" -> {
                    garis(antara(p(9), p(10), 0.15f), antara(p(9), p(10), 0.85f), 4.6f * u, SOROT)
                    if (depan) garis(antara(p(13), p(14), 0.15f), antara(p(13), p(14), 0.85f), 4.6f * u, SOROT)
                }
                "bokong" -> garis(pg, antara(pg, p(10), 0.25f), 5.8f * u, SOROT)
            }
        }
    }
}
