package com.bugarku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Artikel
import com.bugarku.app.data.Isi

fun ikonArtikel(nama: String): ImageVector = when (nama) {
    "perut" -> Icons.Rounded.LocalFireDepartment
    "piring" -> Icons.Rounded.LocalDining
    "jam" -> Icons.Rounded.Schedule
    "aman" -> Icons.Rounded.HealthAndSafety
    "kantor" -> Icons.Rounded.Business
    "meteran" -> Icons.Rounded.Straighten
    "jalan" -> Icons.AutoMirrored.Rounded.DirectionsWalk
    else -> Icons.Rounded.Lightbulb
}

@Composable
fun PanduanScreen(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    val daftar = Isi.artikel
    val utama = daftar.firstOrNull()
    Tengah {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Panduan", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Cara mengecilkan perut buncit, pola makan, dan berolahraga dengan aman.",
                style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant,
            )
            if (utama != null) {
                KartuGradasi(GradasiApi, onClick = { aksi.buka(Layar.Baca(utama.id)) }) {
                    Icon(ikonArtikel(utama.ikon), null, tint = Color.White, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(utama.judul, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text(utama.ringkas, color = Color.White.copy(alpha = 0.92f), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(10.dp))
                    Text("Baca panduan →", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
            daftar.drop(1).forEach { a -> BarisArtikel(a) { aksi.buka(Layar.Baca(a.id)) } }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BarisArtikel(a: Artikel, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Kartu(onClick = onClick, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(ikonArtikel(a.ikon), null, tint = cs.onPrimaryContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(a.judul, style = MaterialTheme.typography.titleMedium)
                Text(a.ringkas, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = cs.onSurfaceVariant)
        }
    }
}

@Composable
fun BacaScreen(id: String, aksi: Aksi) {
    val a = Isi.artikel(id)
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(cs.background)) {
        BarAtas("Panduan", aksi.kembali)
        if (a != null) Tengah {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(a.judul, style = MaterialTheme.typography.headlineMedium)
                Text(a.ringkas, style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                a.isi.forEach { b ->
                    when (b.t) {
                        "h" -> Text(
                            b.teks, style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        "p" -> Text(b.teks, style = MaterialTheme.typography.bodyLarge)
                        "d" -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            b.daftar.forEach { t -> Poin(t, Icons.Rounded.FiberManualRecord, cs.primary) }
                        }
                        "c" -> Kartu(warna = cs.primaryContainer) {
                            Text(b.teks, style = MaterialTheme.typography.bodyLarge, color = cs.onPrimaryContainer)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Sumber: anjuran WHO tentang aktivitas fisik dan pedoman gizi Kementerian Kesehatan RI. " +
                        "Panduan ini bersifat umum dan bukan pengganti saran dokter.",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
