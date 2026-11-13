package com.bugarku.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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

private class WarnaKarakter(
    val kulit: Color,
    val kulitG: Color,
    val rambut: Color,
    val baju: Color,
    val bajuG: Color,
    val celana: Color,
    val celanaG: Color,
    val sepatu: Color,
    val sol: Color,
)

private val PRIA = WarnaKarakter(
    kulit = Color(0xFFC68B59), kulitG = Color(0xFFA87146), rambut = Color(0xFF231E1E),
    baju = Color(0xFF2C3240), bajuG = Color(0xFF1E222D), celana = Color(0xFF1C1F26), celanaG = Color(0xFF121419),
    sepatu = Color(0xFFF5F5F5), sol = Color(0xFFFF6A3D),
)

private val WANITA = WarnaKarakter(
    kulit = Color(0xFFD6A174), kulitG = Color(0xFFB5825A), rambut = Color(0xFF2A1F1C),
    baju = Color(0xFF2E7F8F), bajuG = Color(0xFF21606C), celana = Color(0xFF26222D), celanaG = Color(0xFF17151C),
    sepatu = Color(0xFFF5F5F5), sol = Color(0xFFFF5E7E),
)

private val SOROT = Color(0x99FF6014)
private val KAYU = Color(0xFF9A7A5A)

/**
 * Karakter latihan yang bergerak (atau diam di pose ikon kalau animasi = false).
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
    val warna = if (Store.pria) PRIA else WANITA
    val pria = Store.pria
    val path = remember { Path() }
    val bentuk = RoundedCornerShape(sudut)
    val dasar = if (latar) modifier.clip(bentuk).background(warnaLatar) else modifier
    Canvas(dasar) {
        val posisi = if (animasi) (waktu * g.fps) % g.nf else g.ikon.toFloat()
        gambarFigur(g, posisi, warna, pria, sorot, gelap, path)
    }
}

private fun antara(a: Offset, b: Offset, t: Float): Offset = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

private fun arahDerajat(d: Float): Offset {
    val r = Math.toRadians(d.toDouble())
    return Offset(cos(r).toFloat(), sin(r).toFloat())
}

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

private fun DrawScope.gambarFigur(
    g: Gerakan,
    posisi: Float,
    w: WarnaKarakter,
    pria: Boolean,
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

    fun lengan(n: Int, gelapSisi: Boolean) {
        val bahu = p(if (n == 1) 3 else 6)
        val siku = p(if (n == 1) 4 else 7)
        val tangan = p(if (n == 1) 5 else 8)
        val kul = if (gelapSisi) w.kulitG else w.kulit
        tabung(path, siku, tangan, 1.9f * u, 1.5f * u, kul)
        tabung(path, bahu, siku, 2.3f * u, 1.9f * u, kul)
        tabung(path, bahu, antara(bahu, siku, 0.45f), 2.8f * u, 2.5f * u, if (gelapSisi) w.bajuG else w.baju)
        drawCircle(kul, 1.8f * u, tangan)
    }

    fun kaki(n: Int, gelapSisi: Boolean) {
        val pg = p(if (n == 1) 9 else 13)
        val lutut = p(if (n == 1) 10 else 14)
        val mata = p(if (n == 1) 11 else 15)
        val jari = p(if (n == 1) 12 else 16)
        val c = if (gelapSisi) w.celanaG else w.celana
        tabung(path, lutut, mata, 2.9f * u, 2.1f * u, c)
        tabung(path, pg, lutut, 3.9f * u, 2.9f * u, c)
        tabung(path, mata, jari, 1.9f * u, 1.6f * u, w.sepatu)
        tabung(path, antara(mata, jari, 0.1f), jari, 0.9f * u, 0.8f * u, w.sol)
    }

    val bayang = !depan
    kaki(2, bayang)
    lengan(2, bayang)

    // Badan
    val pg = p(2)
    val lh = p(1)
    if (depan) {
        val b1 = p(3)
        val b2 = p(6)
        val g1 = p(9)
        val g2 = p(13)
        tabung(path, g1, g2, 3.6f * u, 3.6f * u, w.celana)
        path.reset()
        path.moveTo(b1.x, b1.y)
        path.lineTo(b2.x, b2.y)
        val c2 = antara(g2, b2, 0.12f)
        val c1 = antara(g1, b1, 0.12f)
        path.lineTo(c2.x, c2.y)
        path.lineTo(c1.x, c1.y)
        path.close()
        drawPath(path, w.baju)
        tabung(path, b1, b2, 2.6f * u, 2.6f * u, w.baju)
        tabung(path, antara(g1, b1, 0.1f), antara(g2, b2, 0.1f), 3.2f * u, 3.2f * u, w.baju)
    } else {
        tabung(path, pg, antara(pg, lh, 0.15f), 4.6f * u, 4.4f * u, w.celana)
        tabung(path, antara(pg, lh, 0.12f), antara(pg, lh, 0.85f), 4.3f * u, 4.9f * u, w.baju)
        tabung(path, antara(pg, lh, 0.8f), lh, 4.6f * u, 3.4f * u, w.baju)
    }
    kaki(1, false)

    // Leher & kepala
    val kp = p(0)
    tabung(path, lh, antara(lh, kp, 0.6f), 1.9f * u, 1.9f * u, w.kulitG)
    val rk = 5.4f * u
    val atas = Math.toDegrees(atan2((kp.y - lh.y).toDouble(), (kp.x - lh.x).toDouble())).toFloat()
    val muka = arahDerajat(atas + 90f)
    if (!pria) {
        // sanggul rambut
        val pusat = if (depan) kp + arahDerajat(atas) * (rk * 0.95f) else kp + arahDerajat(atas - 45f) * (rk * 0.95f)
        drawCircle(w.rambut, rk * 0.5f, pusat)
    }
    drawCircle(w.kulit, rk, kp)
    val rr = rk * 1.07f
    val kotakRambut = Size(rr * 2, rr * 2)
    val kiriAtas = Offset(kp.x - rr, kp.y - rr)
    if (depan) {
        drawArc(w.rambut, atas - 95f, 190f, true, kiriAtas, kotakRambut)
    } else {
        val awal = if (pria) atas - 140f else atas - 165f
        drawArc(w.rambut, awal, atas + 45f - awal, true, kiriAtas, kotakRambut)
        // telinga & hidung
        drawCircle(w.kulitG, 1.1f * u, kp - muka * (0.4f * u))
        drawCircle(w.kulit, 0.95f * u, kp + muka * (rk * 0.92f))
    }
    lengan(1, false)
    if (depan) lengan(2, false)

    // Otot yang dilatih
    if (sorot) {
        val lebarSorot = 2f
        for (s in g.sorot) {
            when (s) {
                "lengan" -> {
                    drawLine(SOROT, antara(p(3), p(4), 0.2f), p(4), 3.2f * u, StrokeCap.Round)
                    if (depan) drawLine(SOROT, antara(p(6), p(7), 0.2f), p(7), 3.2f * u, StrokeCap.Round)
                }
                "dada", "bahu", "punggung" ->
                    drawLine(SOROT, antara(pg, lh, 0.62f), antara(pg, lh, 0.9f), 5.4f * u, StrokeCap.Round)
                "perut" ->
                    drawLine(SOROT, antara(pg, lh, 0.2f), antara(pg, lh, 0.55f), 5.4f * u, StrokeCap.Round)
                "kaki" -> {
                    drawLine(SOROT, antara(p(9), p(10), 0.15f), antara(p(9), p(10), 0.85f), 4.6f * u, StrokeCap.Round)
                    if (depan) drawLine(SOROT, antara(p(13), p(14), 0.15f), antara(p(13), p(14), 0.85f), 4.6f * u, StrokeCap.Round)
                }
                "bokong" ->
                    drawLine(SOROT, pg, antara(pg, p(10), 0.25f), 5.8f * u * lebarSorot / 2f, StrokeCap.Round)
            }
        }
    }
}
