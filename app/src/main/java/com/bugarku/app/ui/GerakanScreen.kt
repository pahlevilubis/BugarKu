package com.bugarku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Isi
import com.bugarku.app.data.NAMA_LEVEL
import com.bugarku.app.data.Store
import com.bugarku.app.data.jamMenit
import com.bugarku.app.data.jumlahUntuk
import com.bugarku.app.data.rencanaSatu

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GerakanScreen(id: String, aksi: Aksi) {
    val g = Isi.gerakan(id)
    val cs = MaterialTheme.colorScheme
    if (g == null) {
        Column(Modifier.fillMaxSize()) { BarAtas("Gerakan", aksi.kembali) }
        return
    }
    val n = jumlahUntuk(g, g.n, Store.level, 1.0)
    Column(Modifier.fillMaxSize().background(cs.background)) {
        BarAtas(g.nama, aksi.kembali)
        Box(Modifier.weight(1f)) {
            Tengah {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Figur(g, Modifier.fillMaxWidth().aspectRatio(1.1f), animasi = true, sudut = 26.dp)
                    Text(g.nama, style = MaterialTheme.typography.headlineMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Lencana(Isi.namaKategori(g.kat))
                        Lencana(NAMA_LEVEL[(g.level - 1).coerceIn(0, 2)], cs.tertiaryContainer, cs.onTertiaryContainer)
                        Lencana(
                            (if (g.detik) jamMenit(n) else "x$n") + if (g.sisi) " tiap sisi" else "",
                            cs.surfaceContainerHighest, cs.onSurface,
                        )
                        if (g.ringan) Lencana("Ramah lutut / tanpa lompat", cs.secondaryContainer, cs.onSecondaryContainer)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFFF6014)))
                        Spacer(Modifier.width(8.dp))
                        Text("Otot: ${g.otot}", style = MaterialTheme.typography.bodyLarge)
                    }

                    Bagian("Cara melakukan", Icons.Rounded.Accessibility) {
                        g.langkah.forEachIndexed { i, t ->
                            Row(Modifier.padding(vertical = 4.dp)) {
                                Box(
                                    Modifier.size(28.dp).clip(CircleShape).background(cs.primary),
                                    contentAlignment = Alignment.Center,
                                ) { Text("${i + 1}", color = cs.onPrimary, fontWeight = FontWeight.Bold) }
                                Spacer(Modifier.width(12.dp))
                                Text(t, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    if (g.tips.isNotEmpty()) {
                        Bagian("Tips", Icons.Rounded.Lightbulb) {
                            g.tips.forEach { Poin(it, Icons.Rounded.CheckCircle, cs.secondary) }
                        }
                    }
                    if (g.salah.isNotEmpty()) {
                        Bagian("Hindari", Icons.Rounded.Warning) {
                            g.salah.forEach { Poin(it, Icons.Rounded.Warning, cs.error) }
                        }
                    }
                    if (g.mudah.isNotBlank()) {
                        Kartu(warna = cs.secondaryContainer) {
                            Text("Terlalu berat?", style = MaterialTheme.typography.titleMedium, color = cs.onSecondaryContainer)
                            Text(g.mudah, style = MaterialTheme.typography.bodyLarge, color = cs.onSecondaryContainer)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        Tengah {
            Box(Modifier.navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
                TombolBesar(
                    "Coba gerakan ini", ikon = Icons.Rounded.PlayArrow,
                    onClick = { aksi.buka(Layar.Main(rencanaSatu(g, Store.level))) },
                )
            }
        }
    }
}

@Composable
private fun Bagian(judul: String, ikon: ImageVector, isi: @Composable () -> Unit) {
    Kartu {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(ikon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(judul, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        isi()
    }
}

@Composable
fun Poin(teks: String, ikon: ImageVector, warna: Color) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Icon(ikon, null, tint = warna, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Text(teks, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}
