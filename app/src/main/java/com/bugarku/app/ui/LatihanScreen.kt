package com.bugarku.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bugarku.app.data.Gerakan
import com.bugarku.app.data.Isi
import com.bugarku.app.data.NAMA_LEVEL
import com.bugarku.app.data.Program
import com.bugarku.app.data.Sesi
import com.bugarku.app.data.Store
import com.bugarku.app.data.buatRencana
import com.bugarku.app.data.jamMenit
import com.bugarku.app.data.jumlahUntuk

@Composable
fun LatihanScreen(aksi: Aksi) {
    var bagian by rememberSaveable { mutableIntStateOf(0) }
    Tengah {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(8.dp))
                Text("Latihan", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(10.dp))
                Segmen(listOf("Sesi", "Program", "Gerakan"), bagian, { bagian = it })
                Spacer(Modifier.height(6.dp))
            }
            when (bagian) {
                0 -> DaftarSesi(aksi)
                1 -> DaftarProgram(aksi)
                else -> DaftarGerakan(aksi)
            }
        }
    }
}

@Composable
private fun DaftarSesi(aksi: Aksi) {
    val kelompok = Isi.kategori.mapNotNull { k ->
        val s = Isi.sesi.filter { it.kat == k.id }
        if (s.isEmpty()) null else k to s
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        kelompok.forEach { (k, daftar) ->
            item(key = "k_" + k.id) { JudulBagian(k.nama) }
            items(daftar, key = { it.id }) { s -> BarisSesi(s) { aksi.buka(Layar.DetailSesi(s.id)) } }
        }
    }
}

@Composable
fun BarisSesi(s: Sesi, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val contoh = Isi.gerakan(s.item.getOrNull(1)?.g ?: s.item.first().g) ?: Isi.gerakan.first()
    val r = buatRencana(s, Store.level)
    Kartu(onClick = onClick, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Figur(contoh, Modifier.size(88.dp), animasi = false, sorot = false, sudut = 18.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(s.judul, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(s.ringkas, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TitikLevel(s.level)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "±${r.perkiraanMenit} mnt · ${s.item.size} gerakan",
                        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun DaftarProgram(aksi: Aksi) {
    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Program bertahap 28 hari: jadwal harian sudah disusun, tinggal ikuti. Hari istirahat juga bagian dari program.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
        items(Isi.program, key = { it.id }) { p -> KartuProgramDaftar(p) { aksi.buka(Layar.DetailProgram(p.id)) } }
    }
}

@Composable
private fun KartuProgramDaftar(p: Program, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val contoh = Isi.gerakan(CONTOH_KATEGORI[p.kat] ?: "") ?: Isi.gerakan.first()
    val selesai = Store.jumlahSelesai(p.id)
    Kartu(onClick = onClick, padding = 0.dp) {
        Figur(contoh, Modifier.fillMaxWidth().height(170.dp), animasi = false, sorot = false, sudut = 0.dp)
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Lencana("28 hari")
                if (Store.programAktif == p.id) {
                    Spacer(Modifier.width(8.dp))
                    Lencana("Aktif · $selesai/28", cs.secondaryContainer, cs.onSecondaryContainer)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(p.judul, style = MaterialTheme.typography.titleLarge)
            Text(p.ringkas, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun DaftarGerakan(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    var kat by rememberSaveable { mutableStateOf("") }
    var cari by rememberSaveable { mutableStateOf("") }
    val daftar = Isi.gerakan.filter { g ->
        (kat.isEmpty() || g.kat == kat) &&
            (cari.isBlank() || g.nama.contains(cari.trim(), ignoreCase = true) || g.otot.contains(cari.trim(), ignoreCase = true))
    }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = cari, onValueChange = { cari = it },
            placeholder = { Text("Cari gerakan atau otot (mis. perut)") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true, shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp),
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pilihan("Semua", kat.isEmpty(), { kat = "" })
            Isi.kategori.forEach { k -> Pilihan(k.nama, kat == k.id, { kat = k.id }) }
        }
        LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 24.dp)) {
            items(daftar, key = { it.id }) { g ->
                BarisGerakan(g, if (g.detik) jamMenit(jumlahUntuk(g, g.n, Store.level, 1.0)) else "x${jumlahUntuk(g, g.n, Store.level, 1.0)}") {
                    aksi.buka(Layar.DetailGerakan(g.id))
                }
                HorizontalDivider(color = cs.outlineVariant, modifier = Modifier.padding(start = 106.dp))
            }
            if (daftar.isEmpty()) {
                item {
                    Text("Tidak ada gerakan yang cocok.", color = cs.onSurfaceVariant, modifier = Modifier.padding(24.dp))
                }
            }
        }
    }
}

/** Baris gerakan bergaya daftar latihan: gambar di kartu abu-abu, nama, durasi/jumlah. */
@Composable
fun BarisGerakan(g: Gerakan, jumlah: String, keterangan: String? = null, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Figur(g, Modifier.size(92.dp), animasi = false, sorot = false, sudut = 18.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(g.nama, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(jumlah, style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
            Text(
                keterangan ?: "${Isi.namaKategori(g.kat)} · ${NAMA_LEVEL[(g.level - 1).coerceIn(0, 2)]}",
                style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, maxLines = 1,
            )
        }
        if (g.ringan) {
            Lencana("Ramah lutut", cs.secondaryContainer, cs.onSecondaryContainer)
        }
    }
}
