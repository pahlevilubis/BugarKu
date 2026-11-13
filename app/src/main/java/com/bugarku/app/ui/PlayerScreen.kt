package com.bugarku.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bugarku.app.data.Catatan
import com.bugarku.app.data.Isi
import com.bugarku.app.data.Langkah
import com.bugarku.app.data.Rencana
import com.bugarku.app.data.Store
import com.bugarku.app.data.jamMenit
import com.bugarku.app.latihan.Pemandu
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.roundToInt

private enum class Fase { BERSIAP, KERJA, ISTIRAHAT, SELESAI }

private fun ucapJumlah(l: Langkah): String {
    val dasar = if (l.g.detik) "${l.n} detik" else "${l.n} kali"
    return if (l.g.sisi) "$dasar tiap sisi" else dasar
}

private fun lamaKerja(l: Langkah): Int = if (l.g.detik) l.n * (if (l.g.sisi) 2 else 1) else 0

@Composable
fun PlayerScreen(r: Rencana, onTutup: () -> Unit) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val pemandu = remember { Pemandu(context) }
    DisposableEffect(Unit) { onDispose { pemandu.tutup() } }
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    if (r.langkah.isEmpty()) {
        LaunchedEffect(Unit) { onTutup() }
        return
    }

    var fase by remember { mutableStateOf(Fase.BERSIAP) }
    var idx by remember { mutableIntStateOf(0) }
    var sisa by remember { mutableFloatStateOf(Store.bersiap.toFloat()) }
    var totalFase by remember { mutableFloatStateOf(Store.bersiap.toFloat()) }
    var berjalan by remember { mutableFloatStateOf(0f) }
    var jeda by remember { mutableStateOf(false) }
    var totalAktif by remember { mutableFloatStateOf(0f) }
    var kkal by remember { mutableDoubleStateOf(0.0) }
    var selesaiN by remember { mutableIntStateOf(0) }
    var tanyaKeluar by remember { mutableStateOf(false) }
    var tersimpan by remember { mutableStateOf(false) }

    val langkah = r.langkah[idx.coerceIn(0, r.langkah.lastIndex)]

    fun mulaiKerja(i: Int) {
        idx = i.coerceIn(0, r.langkah.lastIndex)
        val l = r.langkah[idx]
        val lama = lamaKerja(l).toFloat()
        sisa = lama
        totalFase = lama
        berjalan = 0f
        fase = Fase.KERJA
    }

    fun selesaiLangkah(dihitung: Boolean) {
        if (dihitung) selesaiN++
        if (idx >= r.langkah.lastIndex) {
            fase = Fase.SELESAI
            return
        }
        val berikut = r.langkah[idx + 1]
        val kini = r.langkah[idx]
        val rehat = if (berikut.putaran != kini.putaran) r.istirahatPutaran else r.istirahat
        if (rehat <= 0) {
            mulaiKerja(idx + 1)
        } else {
            idx += 1
            sisa = rehat.toFloat()
            totalFase = rehat.toFloat()
            berjalan = 0f
            fase = Fase.ISTIRAHAT
        }
    }

    // Suara pemandu setiap pergantian fase
    LaunchedEffect(fase, idx) {
        val l = r.langkah[idx]
        when (fase) {
            Fase.BERSIAP -> {
                delay(800) // tunggu mesin suara siap
                pemandu.bicara("Bersiap. ${l.g.nama}, ${ucapJumlah(l)}")
            }
            Fase.KERJA -> {
                pemandu.bip(panjang = true)
                pemandu.bicara(if (l.g.sisi && l.g.detik) "Mulai, sisi kanan" else "Mulai")
            }
            Fase.ISTIRAHAT -> pemandu.bicara(
                "Istirahat ${totalFase.roundToInt()} detik. Selanjutnya, ${l.g.nama}, ${ucapJumlah(l)}"
            )
            Fase.SELESAI -> {
                pemandu.bip(panjang = true)
                pemandu.bicara("Latihan selesai. Kerja bagus!")
            }
        }
    }

    // Jam latihan
    LaunchedEffect(fase, idx, jeda) {
        if (jeda || fase == Fase.SELESAI) return@LaunchedEffect
        var lalu = System.nanoTime()
        while (true) {
            delay(100)
            val kini = System.nanoTime()
            val dt = (kini - lalu) / 1_000_000_000f
            lalu = kini
            totalAktif += dt
            berjalan += dt
            val l = r.langkah[idx]
            val met = if (fase == Fase.KERJA) l.g.met else 1.5
            kkal += met * Store.berat * dt / 3600.0
            val hitungMundur = fase != Fase.KERJA || l.g.detik
            if (!hitungMundur) continue
            val sebelum = sisa
            sisa -= dt
            val detikSebelum = ceil(sebelum).toInt()
            val detikSesudah = ceil(sisa).toInt()
            if (detikSesudah < detikSebelum && detikSesudah in 1..3) pemandu.bip()
            if (fase == Fase.KERJA && l.g.sisi) {
                val tengah = totalFase / 2f
                if (sebelum > tengah && sisa <= tengah) {
                    pemandu.bip(panjang = true)
                    pemandu.bicara("Ganti sisi")
                }
            }
            if (fase == Fase.KERJA && totalFase >= 30f && sebelum > 10f && sisa <= 10f) {
                pemandu.bicara("Sepuluh detik lagi")
            }
            if (sisa <= 0f) {
                when (fase) {
                    Fase.BERSIAP, Fase.ISTIRAHAT -> mulaiKerja(idx)
                    Fase.KERJA -> selesaiLangkah(true)
                    Fase.SELESAI -> Unit
                }
                break
            }
        }
    }

    // Simpan ke riwayat saat selesai
    LaunchedEffect(fase) {
        if (fase == Fase.SELESAI && !tersimpan) {
            tersimpan = true
            if (totalAktif >= 10f) {
                Store.catatLatihan(
                    Catatan(
                        waktu = System.currentTimeMillis(),
                        tanggal = LocalDate.now().toString(),
                        judul = r.judul,
                        sesiId = r.sesiId,
                        detik = totalAktif.roundToInt(),
                        kalori = kkal.roundToInt(),
                        gerakan = selesaiN,
                    )
                )
            }
            val pid = r.programId
            val h = r.hariKe
            if (pid != null && h != null) {
                Store.tandaiHari(pid, h)
                if (Store.programAktif.isEmpty()) Store.mulaiProgram(pid)
            }
        }
    }

    BackHandler {
        if (fase == Fase.SELESAI) {
            onTutup()
        } else {
            tanyaKeluar = true
        }
    }

    if (tanyaKeluar) {
        AlertDialog(
            onDismissRequest = { tanyaKeluar = false },
            title = { Text("Akhiri latihan?") },
            text = { Text("Latihan yang belum selesai tidak dicatat di riwayat.") },
            confirmButton = { TextButton(onClick = { tanyaKeluar = false; onTutup() }) { Text("Keluar") } },
            dismissButton = { TextButton(onClick = { tanyaKeluar = false }) { Text("Lanjut latihan") } },
        )
    }

    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding().navigationBarsPadding()) {
        if (fase == Fase.SELESAI) {
            LayarSelesai(r, totalAktif, kkal, selesaiN, onTutup)
        } else {
        // Bar atas: tutup + kemajuan
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { tanyaKeluar = true }) { Icon(Icons.Rounded.Close, "Tutup") }
            Row(Modifier.weight(1f).height(6.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                val n = r.langkah.size
                for (i in 0 until n) {
                    val warna = when {
                        i < idx -> cs.primary
                        i == idx -> cs.primary.copy(alpha = 0.45f)
                        else -> cs.surfaceContainerHighest
                    }
                    Box(Modifier.weight(1f).height(6.dp).clip(CircleShape).background(warna))
                }
            }
            Text(
                "${idx + 1}/${r.langkah.size}", style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        if (fase == Fase.ISTIRAHAT) {
            LayarIstirahat(
                langkah = langkah,
                urutan = idx + 1,
                total = r.langkah.size,
                sisa = sisa,
                jeda = jeda,
                onTambah = { sisa += 20f; totalFase += 20f },
                onLewati = { mulaiKerja(idx) },
            )
        } else {
        // Bersiap / kerja
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
            Figur(langkah.g, Modifier.fillMaxSize(), animasi = true, jalan = !jeda, sudut = 28.dp)
            if (fase == Fase.BERSIAP) {
                Column(
                    Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                        .clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("BERSIAP", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("${ceil(sisa).toInt().coerceAtLeast(0)}", color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Tengah {
            Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(langkah.g.nama, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                val ket = buildString {
                    if (langkah.totalPutaran > 1) append("Putaran ${langkah.putaran} dari ${langkah.totalPutaran} · ")
                    append(Isi.namaKategori(langkah.g.kat))
                }
                Text(ket, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                if (langkah.g.detik && fase == Fase.KERJA) {
                    Text(jamMenit(ceil(sisa).toInt()), fontSize = 64.sp, fontWeight = FontWeight.Black, color = cs.onBackground)
                    if (langkah.g.sisi) {
                        Text(
                            if (sisa > totalFase / 2f) "Sisi kanan" else "Sisi kiri",
                            style = MaterialTheme.typography.titleMedium, color = cs.primary,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (totalFase > 0f) (1f - sisa / totalFase).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp).clip(CircleShape),
                        color = cs.primary, trackColor = cs.surfaceContainerHighest,
                    )
                } else {
                    val teks = if (langkah.g.detik) jamMenit(lamaKerja(langkah)) else "x${langkah.n}"
                    Text(teks, fontSize = 64.sp, fontWeight = FontWeight.Black, color = cs.onBackground)
                    if (langkah.g.sisi) Text("tiap sisi", style = MaterialTheme.typography.titleMedium, color = cs.primary)
                    if (fase == Fase.KERJA) {
                        Text(
                            "Waktu ${jamMenit(berjalan.toInt())}", style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TombolBulat(Icons.Rounded.SkipPrevious, "Sebelumnya", aktif = idx > 0 || fase == Fase.KERJA) {
                        if (fase == Fase.KERJA && berjalan > 3f) mulaiKerja(idx) else mulaiKerja(idx - 1)
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) {
                        if (fase == Fase.KERJA && !langkah.g.detik) {
                            TombolBesar("Selesai", ikon = Icons.Rounded.Check, onClick = { selesaiLangkah(true) })
                        } else {
                            TombolBesar(
                                if (jeda) "Lanjut" else "Jeda",
                                ikon = if (jeda) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                onClick = { jeda = !jeda; if (jeda) pemandu.diam() },
                                gradasi = if (jeda) GradasiApi else GradasiToska,
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    TombolBulat(Icons.Rounded.SkipNext, "Lewati", aktif = true) {
                        if (fase == Fase.BERSIAP) mulaiKerja(idx) else selesaiLangkah(false)
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        }
        }
    }
}

@Composable
private fun TombolBulat(
    ikon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    aktif: Boolean,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.size(60.dp).clip(CircleShape).background(cs.surfaceContainerHigh).clickable(enabled = aktif, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(ikon, label, tint = if (aktif) cs.onSurface else cs.outline, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun LayarIstirahat(
    langkah: Langkah,
    urutan: Int,
    total: Int,
    sisa: Float,
    jeda: Boolean,
    onTambah: () -> Unit,
    onLewati: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp).clip(RoundedCornerShape(28.dp)).background(GradasiToska),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ISTIRAHAT", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, fontSize = 20.sp)
                Text(jamMenit(ceil(sisa).toInt()), color = Color.White, fontSize = 84.sp, fontWeight = FontWeight.Black)
                Text("Tarik napas dalam-dalam, minum seteguk air", color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.22f)).clickable(onClick = onTambah)
                            .padding(horizontal = 22.dp, vertical = 12.dp)
                    ) { Text("+20 detik", color = Color.White, fontWeight = FontWeight.Bold) }
                    Box(
                        Modifier.clip(CircleShape).background(Color.White).clickable(onClick = onLewati)
                            .padding(horizontal = 26.dp, vertical = 12.dp)
                    ) { Text("Lewati", color = Color(0xFF168FA9), fontWeight = FontWeight.Bold) }
                }
            }
        }
        Tengah {
            Column(Modifier.padding(16.dp)) {
                Text("Selanjutnya  $urutan/$total", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Figur(langkah.g, Modifier.size(110.dp), animasi = true, jalan = !jeda, sudut = 20.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(langkah.g.nama, style = MaterialTheme.typography.titleLarge)
                        Text(langkah.teksJumlah(), style = MaterialTheme.typography.titleMedium, color = cs.primary)
                        Text(langkah.g.otot, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
private fun LayarSelesai(r: Rencana, detik: Float, kkal: Double, gerakan: Int, onTutup: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Tengah {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(20.dp))
            Box(Modifier.size(120.dp).clip(CircleShape).background(GradasiApi), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(70.dp))
            }
            Text("Latihan selesai!", style = MaterialTheme.typography.headlineLarge)
            Text(r.judul, style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            val pid = r.programId
            val h = r.hariKe
            if (pid != null && h != null) {
                Lencana("Hari ${h + 1} ${Isi.program(pid)?.judul ?: ""} selesai ✓", cs.secondaryContainer, cs.onSecondaryContainer)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Angka("Durasi", jamMenit(detik.roundToInt()), Modifier.weight(1f))
                Angka("Kalori", "±${kkal.roundToInt()}", Modifier.weight(1f))
                Angka("Gerakan", "$gerakan", Modifier.weight(1f))
            }
            Text(
                "Kalori adalah perkiraan (berdasarkan berat badan ${Store.berat.roundToInt()} kg). " +
                    "Jangan lupa minum air dan lakukan peregangan ringan.",
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            TombolBesar("Selesai", onClick = onTutup, ikon = Icons.Rounded.Check)
        }
    }
}

@Composable
fun Angka(label: String, nilai: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainer).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(nilai, style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
    }
}
