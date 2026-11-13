package com.bugarku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Isi
import com.bugarku.app.data.NAMA_LEVEL
import com.bugarku.app.data.Store
import com.bugarku.app.data.buatRencana
import com.bugarku.app.data.kalori
import kotlin.math.roundToInt

@Composable
fun SesiScreen(sesiId: String, programId: String?, hari: Int?, aksi: Aksi) {
    val s = Isi.sesi(sesiId)
    val cs = MaterialTheme.colorScheme
    if (s == null) {
        Column(Modifier.fillMaxSize()) { BarAtas("Latihan", aksi.kembali) }
        return
    }
    val program = programId?.let { Isi.program(it) }
    val kali = if (program != null && hari != null) program.hari.getOrNull(hari)?.kali ?: 1.0 else 1.0
    var level by remember { mutableIntStateOf(Store.level) }
    val rencana = remember(level, s.id, kali, Store.istirahat) {
        buatRencana(s, level, kali, programId = programId, hariKe = hari)
    }
    val satuPutaran = rencana.langkah.filter { it.putaran == 1 }
    val totalPutaran = rencana.langkah.firstOrNull()?.totalPutaran ?: 1
    val kal = rencana.langkah.sumOf { kalori(it.g.met, Store.berat, it.perkiraanDetik) }.roundToInt()
    val judul = if (program != null && hari != null) "Hari ${hari + 1}" else s.judul

    Column(Modifier.fillMaxSize().background(cs.background)) {
        BarAtas(if (program != null) program.judul else "", aksi.kembali)
        Box(Modifier.weight(1f)) {
            Tengah {
                LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 16.dp)) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(judul, style = MaterialTheme.typography.displaySmall)
                            if (program != null) Text(s.judul, style = MaterialTheme.typography.titleLarge, color = cs.primary)
                            Text(s.ringkas, style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                                InfoKecil(Icons.Rounded.Timer, "${rencana.perkiraanMenit} menit")
                                InfoKecil(Icons.Rounded.LocalFireDepartment, "±$kal kkal")
                                if (totalPutaran > 1) InfoKecil(Icons.Rounded.Repeat, "$totalPutaran putaran")
                            }
                            Segmen(NAMA_LEVEL, level - 1, {
                                level = it + 1
                                Store.aturLevel(level)
                            })
                            Spacer(Modifier.height(2.dp))
                            Row {
                                Text("Gerakan", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                Text("${satuPutaran.size} gerakan", style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
                            }
                            if (totalPutaran > 1) {
                                Text(
                                    "Daftar di bawah diulang $totalPutaran kali, dengan istirahat ${rencana.istirahatPutaran} detik di antara putaran.",
                                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    itemsIndexed(satuPutaran) { i, l ->
                        BarisGerakan(l.g, l.teksJumlah(), keterangan = l.g.otot) { aksi.buka(Layar.DetailGerakan(l.g.id)) }
                        if (i < satuPutaran.lastIndex) {
                            HorizontalDivider(color = cs.outlineVariant, modifier = Modifier.padding(start = 106.dp))
                        }
                    }
                    item {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Istirahat ${rencana.istirahat} detik di antara gerakan (bisa diubah di menu Atur). Dengarkan tubuhmu — berhenti bila nyeri dada, pusing, atau sesak berlebihan.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Tengah {
            Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
                TombolBesar("Mulai", ikon = Icons.Rounded.PlayArrow, onClick = { aksi.buka(Layar.Main(rencana)) })
            }
        }
    }
}
