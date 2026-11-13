package com.bugarku.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Store
import com.bugarku.app.data.Ukur
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val FORMAT_TGL = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("id-ID"))
private val FORMAT_TGL_PENDEK = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id-ID"))

fun tanggalIndah(iso: String, pendek: Boolean = false): String = try {
    LocalDate.parse(iso).format(if (pendek) FORMAT_TGL_PENDEK else FORMAT_TGL)
} catch (_: Exception) {
    iso
}

fun angka1(v: Float): String = String.format(Locale.forLanguageTag("id-ID"), "%.1f", v)

fun kategoriImt(imt: Float): String = when {
    imt < 18.5f -> "Kurus"
    imt <= 25.0f -> "Normal"
    imt <= 27.0f -> "Gemuk"
    else -> "Obesitas"
}

@Composable
fun ProgresScreen(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    var catat by remember { mutableStateOf(false) }
    var grafik by rememberSaveable { mutableIntStateOf(0) }
    val riwayat = Store.riwayat
    Tengah {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Progres", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Angka("Latihan", "${riwayat.size}", Modifier.weight(1f))
                Angka("Total menit", "${riwayat.sumOf { it.detik } / 60}", Modifier.weight(1f))
                Angka("Hari beruntun", "${Store.runtutan()}", Modifier.weight(1f))
            }

            // 14 hari terakhir
            Kartu {
                Text("14 hari terakhir (menit)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                val hariIni = LocalDate.now()
                val menit = IntArray(14)
                for (c in riwayat) {
                    val t = try { LocalDate.parse(c.tanggal) } catch (_: Exception) { null } ?: continue
                    val i = 13 - (hariIni.toEpochDay() - t.toEpochDay()).toInt()
                    if (i in 0..13) menit[i] += c.detik
                }
                for (i in 0..13) menit[i] = (menit[i] + 30) / 60
                val maks = maxOf(20, menit.maxOrNull() ?: 0)
                Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 0..13) {
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                            val f = (menit[i].toFloat() / maks).coerceIn(0f, 1f)
                            Box(
                                Modifier.fillMaxWidth().fillMaxHeight(maxOf(0.04f, f)).clip(RoundedCornerShape(5.dp))
                                    .background(if (menit[i] > 0) cs.primary else cs.surfaceContainerHighest)
                            )
                        }
                    }
                }
                Row {
                    Text(tanggalIndah(hariIni.minusDays(13).toString(), true), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text("Hari ini", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
            }

            // Ukuran tubuh
            JudulBagian("Ukuran tubuh") {
                TextButton(onClick = { catat = true }) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Catat")
                }
            }
            KartuTubuh()
            val ukur = Store.ukur
            if (ukur.size >= 2) {
                Kartu {
                    Segmen(listOf("Berat badan", "Lingkar perut"), grafik, { grafik = it })
                    Spacer(Modifier.height(12.dp))
                    val data = ukur.mapNotNull { u ->
                        val v = if (grafik == 0) u.berat else u.perut
                        if (v > 0f) u.tanggal to v else null
                    }
                    if (data.size >= 2) {
                        GrafikGaris(data.map { it.second }, Modifier.fillMaxWidth().height(160.dp), cs.primary)
                        Row {
                            Text(tanggalIndah(data.first().first, true), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Text(tanggalIndah(data.last().first, true), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                    } else {
                        Text("Butuh minimal 2 catatan untuk grafik.", color = cs.onSurfaceVariant)
                    }
                }
            }
            if (ukur.isNotEmpty()) {
                Kartu(padding = 8.dp) {
                    ukur.reversed().take(30).forEach { u -> BarisUkur(u) }
                }
            }

            JudulBagian("Riwayat latihan")
            if (riwayat.isEmpty()) {
                Kartu {
                    Text("Belum ada latihan. Mulai dari Beranda — 7 menit pertama paling penting!", color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    TombolBesar("Pilih latihan", onClick = { aksi.keTab(1) }, ikon = Icons.Rounded.FitnessCenter)
                }
            } else {
                Kartu(padding = 12.dp) {
                    riwayat.take(40).forEach { c ->
                        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(c.judul, style = MaterialTheme.typography.titleMedium)
                                Text(tanggalIndah(c.tanggal), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${(c.detik + 30) / 60} mnt", style = MaterialTheme.typography.titleMedium)
                                Text("±${c.kalori} kkal", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
    if (catat) DialogCatat { catat = false }
}

@Composable
private fun KartuTubuh() {
    val cs = MaterialTheme.colorScheme
    val ukur = Store.ukur
    val beratAkhir = ukur.lastOrNull { it.berat > 0f }?.berat ?: Store.berat
    val beratAwal = ukur.firstOrNull { it.berat > 0f }
    val perutAkhir = ukur.lastOrNull { it.perut > 0f }
    val perutAwal = ukur.firstOrNull { it.perut > 0f }
    val tinggiM = Store.tinggi / 100f
    val imt = if (tinggiM > 0.5f) beratAkhir / (tinggiM * tinggiM) else 0f
    Kartu {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Berat", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                Text("${angka1(beratAkhir)} kg", style = MaterialTheme.typography.headlineSmall)
                if (beratAwal != null && ukur.count { it.berat > 0f } >= 2) {
                    val beda = beratAkhir - beratAwal.berat
                    Text(
                        (if (beda > 0) "+" else "") + "${angka1(beda)} kg sejak ${tanggalIndah(beratAwal.tanggal, true)}",
                        style = MaterialTheme.typography.bodySmall, color = if (beda <= 0) cs.secondary else cs.error,
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Lingkar perut", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                Text(if (perutAkhir != null) "${angka1(perutAkhir.perut)} cm" else "—", style = MaterialTheme.typography.headlineSmall)
                if (perutAkhir != null && perutAwal != null && ukur.count { it.perut > 0f } >= 2) {
                    val beda = perutAkhir.perut - perutAwal.perut
                    Text(
                        (if (beda > 0) "+" else "") + "${angka1(beda)} cm sejak ${tanggalIndah(perutAwal.tanggal, true)}",
                        style = MaterialTheme.typography.bodySmall, color = if (beda <= 0) cs.secondary else cs.error,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (imt > 0f) {
            Text(
                "IMT ${angka1(imt)} · ${kategoriImt(imt)} (tinggi ${Store.tinggi} cm)",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (perutAkhir != null) {
            val batas = if (Store.pria) 90f else 80f
            Text(
                if (perutAkhir.perut >= batas) "Lingkar perut ≥ ${batas.roundToInt()} cm: termasuk obesitas sentral (Kemenkes). Target: di bawah ${batas.roundToInt()} cm."
                else "Lingkar perut di bawah batas ${batas.roundToInt()} cm. Pertahankan!",
                style = MaterialTheme.typography.bodyMedium,
                color = if (perutAkhir.perut >= batas) cs.error else cs.secondary,
            )
        } else {
            Text(
                "Catat lingkar perut — angka ini paling jujur untuk memantau perut buncit. Caranya ada di menu Panduan.",
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BarisUkur(u: Ukur) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(tanggalIndah(u.tanggal), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            listOfNotNull(
                if (u.berat > 0f) "${angka1(u.berat)} kg" else null,
                if (u.perut > 0f) "${angka1(u.perut)} cm" else null,
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant,
        )
        IconButton(onClick = { Store.hapusUkur(u.tanggal) }) {
            Icon(Icons.Rounded.DeleteOutline, "Hapus", tint = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun DialogCatat(onTutup: () -> Unit) {
    val akhir = Store.ukur.lastOrNull()
    var berat by remember { mutableStateOf(angka1(akhir?.berat?.takeIf { it > 0f } ?: Store.berat)) }
    var perut by remember { mutableStateOf(akhir?.perut?.takeIf { it > 0f }?.let { angka1(it) } ?: "") }
    fun baca(t: String): Float = t.trim().replace(',', '.').toFloatOrNull() ?: 0f
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Catat hari ini") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = berat, onValueChange = { berat = it }, label = { Text("Berat badan (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = perut, onValueChange = { perut = it }, label = { Text("Lingkar perut (cm)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Text(
                    "Ukur lingkar perut di pertengahan antara rusuk terbawah dan tulang panggul, setelah buang napas biasa.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val b = baca(berat)
                val p = baca(perut)
                if (b > 0f || p > 0f) Store.simpanUkur(LocalDate.now().toString(), b.coerceIn(0f, 400f), p.coerceIn(0f, 300f))
                onTutup()
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    )
}

@Composable
fun GrafikGaris(nilai: List<Float>, modifier: Modifier, warna: Color) {
    val cs = MaterialTheme.colorScheme
    val min = nilai.minOrNull() ?: 0f
    val max = nilai.maxOrNull() ?: 1f
    Row(modifier) {
        Column(Modifier.fillMaxHeight().padding(end = 6.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(angka1(max), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            Text(angka1(min), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
        Canvas(Modifier.weight(1f).fillMaxHeight()) {
            val rentang = if (max - min < 0.5f) 0.5f else max - min
            val n = nilai.size
            val pad = 8f
            fun titik(i: Int): Offset {
                val x = pad + (size.width - 2 * pad) * (if (n > 1) i.toFloat() / (n - 1) else 0.5f)
                val y = pad + (size.height - 2 * pad) * (1f - (nilai[i] - min) / rentang)
                return Offset(x, y)
            }
            for (k in 0..3) {
                val y = pad + (size.height - 2 * pad) * k / 3f
                drawLine(cs.outlineVariant, Offset(0f, y), Offset(size.width, y), 1f)
            }
            val path = Path()
            for (i in 0 until n) {
                val t = titik(i)
                if (i == 0) path.moveTo(t.x, t.y) else path.lineTo(t.x, t.y)
            }
            drawPath(path, warna, style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            for (i in 0 until n) drawCircle(warna, 7f, titik(i))
        }
    }
}

