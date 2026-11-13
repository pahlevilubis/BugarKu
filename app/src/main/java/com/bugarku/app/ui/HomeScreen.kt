package com.bugarku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Isi
import com.bugarku.app.data.Store
import com.bugarku.app.data.buatRencana
import java.time.LocalDate
import java.time.LocalTime

/** Gerakan contoh untuk gambar setiap bagian tubuh. */
val CONTOH_KATEGORI = mapOf(
    "atas" to "push_up", "lengan" to "dips_kursi", "punggung" to "bird_dog", "perut" to "crunch",
    "kaki" to "squat", "kardio" to "jumping_jack", "regang" to "regang_samping",
)

fun salam(): String {
    val j = LocalTime.now().hour
    return when {
        j < 11 -> "Selamat pagi"
        j < 15 -> "Selamat siang"
        j < 18 -> "Selamat sore"
        else -> "Selamat malam"
    }
}

@Composable
fun HomeScreen(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    Tengah {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Column {
                Text(
                    if (Store.nama.isNotBlank()) "${salam()}, ${Store.nama}" else "${salam()}!",
                    style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant,
                )
                Text("Ayo bergerak hari ini", style = MaterialTheme.typography.headlineMedium)
            }

            KartuMinggu()
            KartuProgram(aksi)

            JudulBagian("Latihan per bagian tubuh")
            val kategori = Isi.kategori
            Grid(kategori, kolomGrid()) { k ->
                val contoh = Isi.gerakan(CONTOH_KATEGORI[k.id] ?: "") ?: Isi.gerakan.first { it.kat == k.id }
                Column(
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(cs.surfaceContainer)
                        .clickable {
                            val s = Isi.sesiUntuk(k.id, Store.level)
                            if (s != null) aksi.buka(Layar.DetailSesi(s.id))
                        }
                        .padding(10.dp)
                ) {
                    Figur(contoh, Modifier.fillMaxWidth().aspectRatio(1.15f), animasi = false, sorot = false, sudut = 16.dp)
                    Spacer(Modifier.height(8.dp))
                    Text(k.nama, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val s = Isi.sesiUntuk(k.id, Store.level)
                    if (s != null) {
                        val r = buatRencana(s, Store.level)
                        Text("±${r.perkiraanMenit} menit", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    }
                }
            }

            KartuGradasi(GradasiToska, onClick = { aksi.buka(Layar.DetailSesi("regang_kantor")) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.SelfImprovement, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Peregangan Kantor", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "3 menit sambil berdiri di dekat meja. Bagus setiap 1–2 jam duduk.",
                            color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            KartuTips(aksi)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun KartuMinggu() {
    val menit = Store.menitPerHariMinggu()
    val total = menit.sum()
    val runtut = Store.runtutan()
    val hariIni = (LocalDate.now().dayOfWeek.value - 1)
    KartuGradasi(GradasiApi) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Minggu ini", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
                Text("$total / 150 menit", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (total >= 150) "Target WHO tercapai. Mantap!" else "Target WHO: 150 menit aktivitas per minggu",
                    color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(34.dp))
                Text("$runtut hari", color = Color.White, fontWeight = FontWeight.Bold)
                Text("beruntun", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        val maks = maxOf(30, menit.maxOrNull() ?: 0)
        Row(Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val nama = listOf("S", "S", "R", "K", "J", "S", "M")
            for (i in 0..6) {
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        val f = (menit[i].toFloat() / maks).coerceIn(0f, 1f)
                        Box(
                            Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(maxOf(0.06f, f))
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = if (menit[i] > 0) 0.95f else 0.3f))
                        )
                    }
                    Text(
                        nama[i], color = Color.White,
                        fontWeight = if (i == hariIni) FontWeight.ExtraBold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun KartuProgram(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    val p = Isi.program(Store.programAktif)
    if (p == null) {
        val unggulan = Isi.program("perut_28") ?: return
        Kartu(onClick = { aksi.buka(Layar.DetailProgram(unggulan.id)) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Figur(Isi.gerakan("mountain_climber") ?: Isi.gerakan.first(), Modifier.size(84.dp), animasi = false, sudut = 18.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Lencana("Program 28 hari")
                    Spacer(Modifier.height(4.dp))
                    Text(unggulan.judul, style = MaterialTheme.typography.titleMedium)
                    Text(unggulan.ringkas, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
            }
        }
        return
    }
    val selesai = Store.jumlahSelesai(p.id)
    val h = Store.hariBerikutnya(p)
    Kartu {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable { aksi.buka(Layar.DetailProgram(p.id)) }) {
                Text("Program aktif", style = MaterialTheme.typography.labelLarge, color = cs.primary)
                Text(p.judul, style = MaterialTheme.typography.titleLarge)
                Text("$selesai dari ${p.hari.size} hari selesai", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            Text("${(selesai * 100) / p.hari.size}%", style = MaterialTheme.typography.headlineSmall, color = cs.primary)
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { selesai.toFloat() / p.hari.size },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = cs.primary, trackColor = cs.surfaceContainerHighest,
        )
        Spacer(Modifier.height(14.dp))
        if (h < 0) {
            Text("Program selesai! Luar biasa. Coba program lain atau ulangi dengan level lebih tinggi.", style = MaterialTheme.typography.bodyMedium)
        } else {
            val hari = p.hari[h]
            val s = Isi.sesi(hari.sesi)
            if (!hari.libur && s != null) {
                val r = buatRencana(s, Store.level, hari.kali)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoKecil(Icons.Rounded.Timer, "±${r.perkiraanMenit} mnt")
                    Spacer(Modifier.width(14.dp))
                    Text(s.judul, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                TombolBesar("Mulai Hari ${h + 1}", onClick = { aksi.buka(Layar.DetailSesi(s.id, p.id, h)) }, ikon = Icons.Rounded.PlayArrow)
            } else {
                Text("Hari ${h + 1}: ${hari.istirahat}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                TombolBesar("Lihat Hari ${h + 1}", onClick = { aksi.buka(Layar.DetailProgram(p.id)) }, gradasi = GradasiToska)
            }
        }
    }
}

@Composable
private fun KartuTips(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    val tips = Isi.tips
    if (tips.isEmpty()) return
    val tip = tips[(LocalDate.now().dayOfYear) % tips.size]
    Kartu(onClick = { aksi.keTab(3) }, warna = cs.tertiaryContainer) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.TipsAndUpdates, null, tint = cs.onTertiaryContainer)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Tips hari ini", style = MaterialTheme.typography.labelLarge, color = cs.onTertiaryContainer)
                Text(tip, style = MaterialTheme.typography.bodyLarge, color = cs.onTertiaryContainer)
            }
        }
    }
}
