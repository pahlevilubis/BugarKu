package com.bugarku.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.bugarku.app.data.NAMA_LEVEL
import com.bugarku.app.data.Store
import com.bugarku.app.notif.Pengingat

private val NAMA_HARI = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

fun teksJam(menit: Int): String = "%02d.%02d".format(menit / 60, menit % 60)

@Composable
fun SettingsScreen(aksi: Aksi) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    var segarkan by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { segarkan++ }
    val izinNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { segarkan++ }
    var pilihJam by remember { mutableStateOf<String?>(null) }

    val notifBoleh = remember(segarkan) { Pengingat.bolehNotif(context) }
    val tepatWaktu = remember(segarkan) { Pengingat.bolehTepatWaktu(context) }

    Tengah {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Atur", style = MaterialTheme.typography.headlineMedium)

            // ── Profil ──
            JudulBagian("Profil")
            Kartu {
                var nama by remember { mutableStateOf(Store.nama) }
                OutlinedTextField(
                    value = nama, onValueChange = { nama = it.take(30); Store.aturNama(nama.trim()) },
                    label = { Text("Nama panggilan") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Text("Karakter & jenis kelamin", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Segmen(listOf("Pria", "Wanita"), if (Store.pria) 0 else 1, { Store.aturPria(it == 0) })
                Text(
                    "Dipakai untuk gambar karakter dan batas lingkar perut (pria 90 cm, wanita 80 cm).",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(Modifier.height(10.dp))
                Stepper("Tinggi badan", "${Store.tinggi} cm",
                    onKurang = { Store.aturTinggi((Store.tinggi - 1).coerceAtLeast(100)) },
                    onTambah = { Store.aturTinggi((Store.tinggi + 1).coerceAtMost(230)) })
                Stepper("Berat badan", "${angka1(Store.berat)} kg",
                    onKurang = { Store.aturBerat((Store.berat - 0.5f).coerceAtLeast(30f)) },
                    onTambah = { Store.aturBerat((Store.berat + 0.5f).coerceAtMost(250f)) })
                Spacer(Modifier.height(6.dp))
                Text("Level latihan", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Segmen(NAMA_LEVEL, Store.level - 1, { Store.aturLevel(it + 1) })
            }

            // ── Pengingat ──
            JudulBagian("Pengingat")
            if (!notifBoleh) {
                Peringatan(
                    "Notifikasi BugarKu belum diizinkan, jadi pengingat tidak akan muncul.",
                    "Izinkan",
                ) {
                    if (Build.VERSION.SDK_INT >= 33) izinNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else bukaPengaturanNotif(context)
                }
            }
            if (!tepatWaktu && Build.VERSION.SDK_INT >= 31) {
                Peringatan("Izinkan \"Alarm & pengingat\" supaya pengingat muncul tepat waktu.", "Buka pengaturan") {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + context.packageName))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: Exception) {
                        bukaInfoApp(context)
                    }
                }
            }
            Kartu {
                BarisSaklar("Pengingat olahraga", "Hari & jam latihan", Store.olahragaAktif) { Store.aturOlahraga(aktif = it) }
                if (Store.olahragaAktif) {
                    BarisJam("Jam", Store.olahragaJam) { pilihJam = "olahraga" }
                    PilihHari(Store.olahragaHari) { Store.aturOlahraga(hari = it) }
                }
            }
            Kartu {
                BarisSaklar(
                    "Gerak saat kerja", "Ajakan berdiri & peregangan setelah duduk lama", Store.gerakAktif,
                ) { Store.aturGerak(aktif = it) }
                if (Store.gerakAktif) {
                    Text("Setiap", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                    PilihAngka(listOf(30, 45, 60, 90, 120), Store.gerakInterval, "mnt") { Store.aturGerak(interval = it) }
                    BarisJam("Mulai kerja", Store.gerakMulai) { pilihJam = "gerakMulai" }
                    BarisJam("Selesai kerja", Store.gerakSelesai) { pilihJam = "gerakSelesai" }
                    PilihHari(Store.gerakHari) { Store.aturGerak(hari = it) }
                }
            }
            Kartu {
                BarisSaklar("Minum air", "Pengingat minum air putih", Store.minumAktif) { Store.aturMinum(aktif = it) }
                if (Store.minumAktif) {
                    Text("Setiap", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                    PilihAngka(listOf(60, 90, 120, 180), Store.minumInterval, "mnt") { Store.aturMinum(interval = it) }
                    BarisJam("Dari jam", Store.minumMulai) { pilihJam = "minumMulai" }
                    BarisJam("Sampai jam", Store.minumSelesai) { pilihJam = "minumSelesai" }
                }
            }
            Kartu(warna = cs.surfaceContainerHigh) {
                Text(
                    "Pengingat tidak muncul? Di HP Samsung/Xiaomi/Oppo, buka Info aplikasi → Baterai → pilih \"Tidak dibatasi\" untuk BugarKu.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { bukaInfoApp(context) }) { Text("Info aplikasi") }
                    TextButton(onClick = { Pengingat.tampilkan(context, Pengingat.OLAHRAGA, uji = true) }) {
                        Icon(Icons.Rounded.NotificationsActive, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Coba notifikasi")
                    }
                }
            }

            // ── Latihan ──
            JudulBagian("Saat latihan")
            Kartu {
                BarisSaklar("Suara pemandu", "Aba-aba suara bahasa Indonesia", Store.suara) { Store.aturSuara(it) }
                HorizontalDivider(color = cs.outlineVariant)
                BarisSaklar("Bunyi bip", "Hitung mundur 3-2-1", Store.bip) { Store.aturBip(it) }
                HorizontalDivider(color = cs.outlineVariant)
                BarisSaklar("Getar", "Getar kecil saat hitung mundur", Store.getar) { Store.aturGetar(it) }
                Spacer(Modifier.height(8.dp))
                Text("Istirahat antar gerakan", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                PilihAngka(listOf(10, 15, 20, 30, 45, 60), Store.istirahat, "dtk") { Store.aturIstirahat(it) }
                Spacer(Modifier.height(6.dp))
                Text("Waktu bersiap", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                PilihAngka(listOf(5, 10, 15), Store.bersiap, "dtk") { Store.aturBersiap(it) }
                Text(
                    "Suara memakai mesin Text-to-Speech HP. Kalau tidak terdengar, pasang suara Bahasa Indonesia di Pengaturan HP → Text-to-speech.",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp),
                )
            }

            // ── Tampilan ──
            JudulBagian("Tampilan")
            Kartu {
                val pilihan = listOf("terang", "gelap", "sistem")
                Segmen(listOf("Terang", "Gelap", "Ikuti HP"), pilihan.indexOf(Store.tema).coerceAtLeast(0), { Store.aturTema(pilihan[it]) })
            }

            Kartu(onClick = { aksi.buka(Layar.Tentang) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tentang BugarKu", style = MaterialTheme.typography.titleMedium)
                        Text("Versi 1.0 · by Pahlevi Lubis", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    val jenis = pilihJam
    if (jenis != null) {
        val awal = when (jenis) {
            "olahraga" -> Store.olahragaJam
            "gerakMulai" -> Store.gerakMulai
            "gerakSelesai" -> Store.gerakSelesai
            "minumMulai" -> Store.minumMulai
            else -> Store.minumSelesai
        }
        DialogJam(awal, onPilih = { m ->
            when (jenis) {
                "olahraga" -> Store.aturOlahraga(jam = m)
                "gerakMulai" -> Store.aturGerak(mulai = m)
                "gerakSelesai" -> Store.aturGerak(selesai = m)
                "minumMulai" -> Store.aturMinum(mulai = m)
                else -> Store.aturMinum(selesai = m)
            }
        }, onTutup = { pilihJam = null })
    }
}

private fun bukaInfoApp(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {
    }
}

private fun bukaPengaturanNotif(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= 26) {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            bukaInfoApp(context)
        }
    } catch (_: Exception) {
        bukaInfoApp(context)
    }
}

@Composable
private fun Peringatan(teks: String, tombol: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Kartu(warna = cs.errorContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Warning, null, tint = cs.error)
            Spacer(Modifier.width(10.dp))
            Text(teks, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
        TextButton(onClick = onClick) { Text(tombol, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun BarisSaklar(judul: String, ket: String, nilai: Boolean, onUbah: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onUbah(!nilai) }.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(judul, style = MaterialTheme.typography.titleMedium)
            Text(ket, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = nilai, onCheckedChange = onUbah)
    }
}

@Composable
private fun BarisJam(judul: String, menit: Int, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(judul, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            teksJam(menit), style = MaterialTheme.typography.titleLarge, color = cs.primary,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(cs.primaryContainer).padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun PilihHari(mask: Int, onUbah: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        NAMA_HARI.forEachIndexed { i, n ->
            val aktif = mask and (1 shl i) != 0
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(if (aktif) cs.primary else cs.surfaceContainerHigh)
                    .clickable { onUbah(mask xor (1 shl i)) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    n, color = if (aktif) cs.onPrimary else cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PilihAngka(opsi: List<Int>, nilai: Int, satuan: String, onPilih: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opsi.forEach { o -> Pilihan("$o $satuan", o == nilai, { onPilih(o) }) }
    }
}

@Composable
private fun Stepper(judul: String, nilai: String, onKurang: () -> Unit, onTambah: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(judul, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = onKurang) { Icon(Icons.Rounded.Remove, "Kurangi", tint = cs.primary) }
        Text(nilai, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(80.dp), maxLines = 1)
        IconButton(onClick = onTambah) { Icon(Icons.Rounded.Add, "Tambah", tint = cs.primary) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogJam(menit: Int, onPilih: (Int) -> Unit, onTutup: () -> Unit) {
    val state = rememberTimePickerState(initialHour = menit / 60, initialMinute = menit % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text("Pilih jam") },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onPilih(state.hour * 60 + state.minute)
                onTutup()
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    )
}

@Composable
fun TentangScreen(aksi: Aksi) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().background(cs.background)) {
        BarAtas("Tentang", aksi.kembali)
        Tengah {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(84.dp).clip(RoundedCornerShape(24.dp)).background(GradasiApi), contentAlignment = Alignment.Center) {
                    Text("B", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.displaySmall)
                }
                Text("BugarKu", style = MaterialTheme.typography.headlineLarge)
                Text("Versi 1.0 · by Pahlevi Lubis", style = MaterialTheme.typography.titleMedium, color = cs.primary)
                Text(
                    "Latihan di rumah tanpa alat untuk badan atas, lengan, perut, kaki, kardio, dan peregangan — " +
                        "lengkap dengan karakter bergerak, suara pemandu, program 28 hari, pengingat, dan panduan mengecilkan perut buncit.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Kartu(warna = cs.surfaceContainerHigh) {
                    Text("Privasi", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Semua data (riwayat, berat, lingkar perut) hanya tersimpan di HP ini. BugarKu tidak memakai internet dan tidak mengirim data ke mana pun.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Kartu(warna = cs.surfaceContainerHigh) {
                    Text("Penting", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "BugarKu memberi panduan umum, bukan pengganti saran dokter. Konsultasikan dulu bila punya penyakit jantung, " +
                            "tekanan darah tinggi, diabetes, cedera, atau sedang hamil. Hentikan latihan bila nyeri dada, pusing, atau sesak berlebihan. " +
                            "Angka kalori adalah perkiraan.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Kartu(warna = cs.surfaceContainerHigh) {
                    Text("Rujukan", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "• WHO Guidelines on Physical Activity and Sedentary Behaviour (2020)\n" +
                            "• Kementerian Kesehatan RI: Isi Piringku, batas konsumsi gula-garam-lemak, klasifikasi IMT dan obesitas sentral",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
