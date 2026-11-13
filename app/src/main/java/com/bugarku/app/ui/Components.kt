package com.bugarku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Layar lebar (tablet/landscape): menu pindah ke samping. */
@Composable
fun layarLebar(): Boolean = LocalConfiguration.current.screenWidthDp >= 720

/** Isi halaman dibatasi lebarnya supaya nyaman dibaca di tablet. */
@Composable
fun Tengah(modifier: Modifier = Modifier, maks: Dp = 680.dp, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maks).fillMaxWidth()) { content() }
    }
}

@Composable
fun BarAtas(judul: String, onKembali: (() -> Unit)?, aksi: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onKembali != null) {
            IconButton(onClick = onKembali) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali") }
        } else {
            Spacer(Modifier.width(12.dp))
        }
        Text(
            judul, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        aksi()
    }
}

@Composable
fun Kartu(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    warna: Color = MaterialTheme.colorScheme.surfaceContainer,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bentuk = RoundedCornerShape(22.dp)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = bentuk,
        color = warna,
        shadowElevation = 1.dp,
    ) {
        Column(
            (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(padding),
            content = content,
        )
    }
}

/** Kartu dengan latar gradasi. */
@Composable
fun KartuGradasi(
    gradasi: Brush,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradasi)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(18.dp)
    ) {
        Column(content = content)
    }
}

/** Tombol besar bergradasi seperti tombol "Mulai". */
@Composable
fun TombolBesar(
    teks: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ikon: ImageVector? = null,
    gradasi: Brush = GradasiApi,
    aktif: Boolean = true,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(CircleShape)
            .background(if (aktif) gradasi else Brush.horizontalGradient(listOf(Color.Gray, Color.Gray)))
            .clickable(enabled = aktif, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (ikon != null) {
                Icon(ikon, null, tint = Color.White, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(teks, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp)
        }
    }
}

/** Pilihan kecil yang bisa dipilih (chip). */
@Composable
fun Pilihan(teks: String, aktif: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .clip(CircleShape)
            .background(if (aktif) cs.primary else cs.surfaceContainerHigh)
            .border(1.dp, if (aktif) cs.primary else cs.outlineVariant, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            teks, color = if (aktif) cs.onPrimary else cs.onSurface, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge, maxLines = 1,
        )
    }
}

/** Kontrol segmen (misalnya Pemula / Menengah / Lanjut). */
@Composable
fun Segmen(opsi: List<String>, pilih: Int, onPilih: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(cs.surfaceContainerHigh)
            .padding(4.dp),
    ) {
        opsi.forEachIndexed { i, t ->
            val aktif = i == pilih
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (aktif) cs.surfaceContainerLowest else Color.Transparent)
                    .clickable { onPilih(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    t, fontWeight = if (aktif) FontWeight.Bold else FontWeight.Medium,
                    color = if (aktif) cs.primary else cs.onSurfaceVariant, maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun Lencana(teks: String, warna: Color = MaterialTheme.colorScheme.primaryContainer, warnaTeks: Color = MaterialTheme.colorScheme.onPrimaryContainer) {
    Text(
        teks,
        color = warnaTeks,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(warna)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
fun JudulBagian(teks: String, modifier: Modifier = Modifier, aksi: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(teks, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        aksi?.invoke()
    }
}

@Composable
fun InfoKecil(ikon: ImageVector, teks: String, warna: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(ikon, null, tint = warna, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(5.dp))
        Text(teks, color = warna, style = MaterialTheme.typography.titleMedium)
    }
}

/** Titik level (1–3). */
@Composable
fun TitikLevel(level: Int, warna: Color = MaterialTheme.colorScheme.primary) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..3) {
            Box(
                Modifier
                    .size(width = 12.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (i <= level) warna else MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

/** Grid sederhana yang bisa dipakai di dalam Column yang sudah bisa digulir. */
@Composable
fun <T> Grid(item: List<T>, kolom: Int, jarak: Dp = 12.dp, isi: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(jarak)) {
        item.chunked(kolom).forEach { baris ->
            Row(horizontalArrangement = Arrangement.spacedBy(jarak)) {
                baris.forEach { Box(Modifier.weight(1f)) { isi(it) } }
                repeat(kolom - baris.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Jumlah kolom grid sesuai lebar layar (isi dibatasi 680dp). */
@Composable
fun kolomGrid(minLebar: Int = 165): Int =
    (minOf(LocalConfiguration.current.screenWidthDp, 680) / minLebar).coerceIn(2, 4)
