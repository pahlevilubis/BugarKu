package com.bugarku.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Oranye = Color(0xFFFF6A3D)
val OranyeMuda = Color(0xFFFF9A3C)
val Merah = Color(0xFFFF4D5E)
val Toska = Color(0xFF16A99B)
val Biru = Color(0xFF3D7BFF)

/** Gradasi utama tombol & kartu penting. */
val GradasiApi = Brush.horizontalGradient(listOf(OranyeMuda, Oranye, Merah))
val GradasiToska = Brush.horizontalGradient(listOf(Color(0xFF2CC7A8), Color(0xFF168FA9)))

private val Terang = lightColorScheme(
    primary = Color(0xFFE8562A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE2D6),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = Toska,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFF4EE),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Biru,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDCE6FF),
    onTertiaryContainer = Color(0xFF001A43),
    error = Color(0xFFB3261E),
    background = Color(0xFFF6F7F9),
    onBackground = Color(0xFF16181C),
    surface = Color(0xFFF6F7F9),
    onSurface = Color(0xFF16181C),
    surfaceVariant = Color(0xFFE9ECF0),
    onSurfaceVariant = Color(0xFF5F6670),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF0F2F5),
    surfaceContainerHighest = Color(0xFFE7EAEE),
    outline = Color(0xFF9AA1AB),
    outlineVariant = Color(0xFFE1E4E9),
)

private val Gelap = darkColorScheme(
    primary = Color(0xFFFF8A5C),
    onPrimary = Color(0xFF3A0B00),
    primaryContainer = Color(0xFF5A2410),
    onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFF4FD8C4),
    onSecondary = Color(0xFF00201C),
    secondaryContainer = Color(0xFF0E3E38),
    onSecondaryContainer = Color(0xFFB9F2E8),
    tertiary = Color(0xFF9DB8FF),
    onTertiary = Color(0xFF001A43),
    tertiaryContainer = Color(0xFF1C2F57),
    onTertiaryContainer = Color(0xFFDCE6FF),
    error = Color(0xFFFF8A80),
    background = Color(0xFF101215),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF101215),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF22262C),
    onSurfaceVariant = Color(0xFFA6ADB7),
    surfaceContainerLowest = Color(0xFF0B0C0F),
    surfaceContainerLow = Color(0xFF16191D),
    surfaceContainer = Color(0xFF1A1D22),
    surfaceContainerHigh = Color(0xFF22262C),
    surfaceContainerHighest = Color(0xFF2A2F36),
    outline = Color(0xFF59606A),
    outlineVariant = Color(0xFF30353C),
)

private val Huruf = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Black),
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.Black),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    )
}

/** Gelap/terang dipakai juga oleh gambar karakter (warna latar kartu). */
val LocalGelap = staticCompositionLocalOf { false }

@Composable
fun BugarTheme(gelap: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (gelap) Gelap else Terang, typography = Huruf) {
        androidx.compose.runtime.CompositionLocalProvider(LocalGelap provides gelap, content = content)
    }
}
