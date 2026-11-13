package com.bugarku.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.bugarku.app.data.Isi
import com.bugarku.app.data.Rencana
import com.bugarku.app.data.Store

/** Permintaan membuka halaman tertentu (misal dari notifikasi). */
object Nav {
    var permintaan by mutableStateOf<String?>(null)

    fun minta(tujuan: String) {
        permintaan = tujuan
    }
}

sealed interface Layar {
    data class DetailGerakan(val id: String) : Layar
    data class DetailSesi(val sesiId: String, val programId: String? = null, val hari: Int? = null) : Layar
    data class DetailProgram(val id: String) : Layar
    class Main(val rencana: Rencana) : Layar
    data class Baca(val id: String) : Layar
    data object Tentang : Layar
}

/** Aksi navigasi yang diteruskan ke setiap layar. */
class Aksi(
    val buka: (Layar) -> Unit,
    val kembali: () -> Unit,
    val keTab: (Int) -> Unit,
)

private class Tab(val label: String, val ikon: ImageVector)

private val TABS = listOf(
    Tab("Beranda", Icons.Rounded.Home),
    Tab("Latihan", Icons.Rounded.FitnessCenter),
    Tab("Progres", Icons.Rounded.BarChart),
    Tab("Panduan", Icons.Rounded.Lightbulb),
    Tab("Atur", Icons.Rounded.Settings),
)

@Composable
fun BugarRoot() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tumpukan = remember { mutableStateListOf<Layar>() }
    val penyimpan = rememberSaveableStateHolder()
    val context = LocalContext.current

    // Izin menampilkan notifikasi pengingat (Android 13+)
    val izinNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            izinNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val aksi = remember {
        Aksi(
            buka = { tumpukan.add(it) },
            kembali = { if (tumpukan.isNotEmpty()) tumpukan.removeAt(tumpukan.lastIndex) },
            keTab = { tumpukan.clear(); tab = it },
        )
    }

    // Dibuka dari notifikasi
    val minta = Nav.permintaan
    LaunchedEffect(minta) {
        if (minta != null) {
            Nav.permintaan = null
            when (minta) {
                "olahraga" -> {
                    tumpukan.clear()
                    val p = Isi.program(Store.programAktif)
                    val h = if (p != null) Store.hariBerikutnya(p) else -1
                    if (p != null && h >= 0 && !p.hari[h].libur) {
                        tumpukan.add(Layar.DetailSesi(p.hari[h].sesi, p.id, h))
                    } else if (p != null) {
                        tumpukan.add(Layar.DetailProgram(p.id))
                    } else {
                        tab = 1
                    }
                }
                else -> if (Isi.sesi(minta) != null) {
                    tumpukan.clear()
                    tumpukan.add(Layar.DetailSesi(minta))
                }
            }
        }
    }

    BackHandler(enabled = tumpukan.isNotEmpty() && tumpukan.last() !is Layar.Main) { aksi.kembali() }
    BackHandler(enabled = tumpukan.isEmpty() && tab != 0) { tab = 0 }

    when (val atas = tumpukan.lastOrNull()) {
        is Layar.DetailGerakan -> GerakanScreen(atas.id, aksi)
        is Layar.DetailSesi -> SesiScreen(atas.sesiId, atas.programId, atas.hari, aksi)
        is Layar.DetailProgram -> ProgramScreen(atas.id, aksi)
        is Layar.Main -> PlayerScreen(atas.rencana, onTutup = aksi.kembali)
        is Layar.Baca -> BacaScreen(atas.id, aksi)
        Layar.Tentang -> TentangScreen(aksi)
        null -> {
            val isiTab: @Composable () -> Unit = {
                penyimpan.SaveableStateProvider("tab$tab") {
                    when (tab) {
                        0 -> HomeScreen(aksi)
                        1 -> LatihanScreen(aksi)
                        2 -> ProgresScreen(aksi)
                        3 -> PanduanScreen(aksi)
                        else -> SettingsScreen(aksi)
                    }
                }
            }
            if (layarLebar()) {
                Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
                    Row(Modifier.fillMaxSize().padding(pad)) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            windowInsets = WindowInsets(0, 0, 0, 0),
                        ) {
                            Spacer(Modifier.weight(1f))
                            TABS.forEachIndexed { i, t ->
                                NavigationRailItem(
                                    selected = tab == i, onClick = { tab = i },
                                    icon = { Icon(t.ikon, null) }, label = { Text(t.label) },
                                )
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        Box(Modifier.weight(1f).fillMaxHeight()) { isiTab() }
                    }
                }
            } else {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                            TABS.forEachIndexed { i, t ->
                                NavigationBarItem(
                                    selected = tab == i, onClick = { tab = i },
                                    icon = { Icon(t.ikon, null) }, label = { Text(t.label, maxLines = 1) },
                                )
                            }
                        }
                    },
                ) { pad ->
                    Box(Modifier.fillMaxSize().padding(pad)) { isiTab() }
                }
            }
        }
    }
}
