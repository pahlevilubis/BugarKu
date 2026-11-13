package com.bugarku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Isi
import com.bugarku.app.data.Store

@Composable
fun ProgramScreen(id: String, aksi: Aksi) {
    val p = Isi.program(id)
    val cs = MaterialTheme.colorScheme
    if (p == null) {
        Column(Modifier.fillMaxSize()) { BarAtas("Program", aksi.kembali) }
        return
    }
    var hariLibur by remember { mutableStateOf<Int?>(null) }
    var tanyaUlang by remember { mutableStateOf(false) }
    val selesai = Store.jumlahSelesai(p.id)
    val berikut = Store.hariBerikutnya(p)
    val aktif = Store.programAktif == p.id
    val contoh = Isi.gerakan(CONTOH_KATEGORI[p.kat] ?: "") ?: Isi.gerakan.first()

    fun bukaHari(i: Int) {
        val h = p.hari[i]
        if (h.libur) {
            hariLibur = i
        } else {
            aksi.buka(Layar.DetailSesi(h.sesi, p.id, i))
        }
    }

    Column(Modifier.fillMaxSize().background(cs.background)) {
        BarAtas(p.judul, aksi.kembali)
        Box(Modifier.weight(1f)) {
            Tengah {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Figur(contoh, Modifier.fillMaxWidth().height(200.dp), animasi = true, sudut = 26.dp)
                    Text(p.judul, style = MaterialTheme.typography.headlineMedium)
                    Text(p.ringkas, style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                    Kartu(warna = cs.primaryContainer) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Rounded.Info, null, tint = cs.onPrimaryContainer)
                            Spacer(Modifier.size(10.dp))
                            Text(p.catatan, style = MaterialTheme.typography.bodyMedium, color = cs.onPrimaryContainer)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$selesai/${p.hari.size} hari selesai", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (selesai > 0) {
                            TextButton(onClick = { tanyaUlang = true }) { Text("Ulangi dari awal") }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { selesai.toFloat() / p.hari.size },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = cs.primary, trackColor = cs.surfaceContainerHighest,
                    )
                    for (minggu in 0 until p.hari.size / 7) {
                        Text("Minggu ${minggu + 1}", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (d in 0 until 7) {
                                val i = minggu * 7 + d
                                val h = p.hari[i]
                                val sudah = Store.hariSelesai(p.id, i)
                                val berikutnya = i == berikut
                                val latar = when {
                                    sudah -> cs.primary
                                    berikutnya -> cs.primaryContainer
                                    else -> cs.surfaceContainer
                                }
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(0.8f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(latar)
                                        .then(if (berikutnya) Modifier.border(2.dp, cs.primary, RoundedCornerShape(14.dp)) else Modifier)
                                        .clickable { bukaHari(i) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${i + 1}", fontWeight = FontWeight.Bold,
                                            color = if (sudah) cs.onPrimary else cs.onSurface,
                                        )
                                        when {
                                            sudah -> Icon(Icons.Rounded.Check, null, tint = cs.onPrimary, modifier = Modifier.size(16.dp))
                                            h.libur -> Icon(Icons.Rounded.NightsStay, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                            else -> Spacer(Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        "Ketuk nomor hari untuk melihat isinya. Ikon bulan = hari istirahat. Boleh mengulang hari yang terasa berat.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        Tengah {
            Box(Modifier.navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
                if (berikut < 0) {
                    TombolBesar("Program selesai — ulangi?", onClick = { tanyaUlang = true }, ikon = Icons.Rounded.Check)
                } else {
                    TombolBesar(
                        if (aktif || selesai > 0) "Lanjut Hari ${berikut + 1}" else "Mulai Program",
                        ikon = Icons.Rounded.PlayArrow,
                        onClick = {
                            if (!aktif) Store.mulaiProgram(p.id)
                            bukaHari(berikut)
                        },
                    )
                }
            }
        }
    }

    val iLibur = hariLibur
    if (iLibur != null) {
        val h = p.hari[iLibur]
        val sesi = Isi.sesi(h.sesi)
        AlertDialog(
            onDismissRequest = { hariLibur = null },
            icon = { Icon(Icons.Rounded.NightsStay, null) },
            title = { Text("Hari ${iLibur + 1}: Istirahat") },
            text = {
                Text(
                    h.istirahat + if (sesi != null) "\n\nKalau mau, lakukan \"${sesi.judul}\" yang ringan." else "",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (!aktif) Store.mulaiProgram(p.id)
                    Store.tandaiHari(p.id, iLibur, !Store.hariSelesai(p.id, iLibur))
                    hariLibur = null
                }) { Text(if (Store.hariSelesai(p.id, iLibur)) "Batalkan tanda" else "Tandai selesai") }
            },
            dismissButton = {
                if (sesi != null) {
                    TextButton(onClick = {
                        hariLibur = null
                        aksi.buka(Layar.DetailSesi(sesi.id, p.id, iLibur))
                    }) { Text("Buka ${sesi.judul}") }
                } else {
                    TextButton(onClick = { hariLibur = null }) { Text("Tutup") }
                }
            },
        )
    }

    if (tanyaUlang) {
        AlertDialog(
            onDismissRequest = { tanyaUlang = false },
            title = { Text("Ulangi program?") },
            text = { Text("Semua tanda hari selesai di program ini akan dihapus. Riwayat latihan tetap tersimpan.") },
            confirmButton = {
                TextButton(onClick = {
                    Store.ulangProgram(p.id)
                    Store.mulaiProgram(p.id)
                    tanyaUlang = false
                }) { Text("Ulangi") }
            },
            dismissButton = { TextButton(onClick = { tanyaUlang = false }) { Text("Batal") } },
        )
    }
}
