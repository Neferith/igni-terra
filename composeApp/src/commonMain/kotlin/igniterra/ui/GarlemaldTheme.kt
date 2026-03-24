package igniterra.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette Garlemald ────────────────────────────────────────────────────────

object GarlemaldColors {
    val Background       = Color(0xFF0A0A0A)   // Noir profond
    val Surface          = Color(0xFF111111)   // Surface légèrement relevée
    val SurfaceVariant   = Color(0xFF1A1A1A)   // Panneaux / cartes
    val Border           = Color(0xFF2A2A2A)   // Bordures métalliques sombres

    val ImperialRed      = Color(0xFFAA1111)   // Rouge impérial principal
    val ImperialRedDark  = Color(0xFF6B0000)   // Variante sombre
    val ImperialRedGlow  = Color(0xFFDD2222)   // Accent / hover

    val ScreenGreen      = Color(0xFF00FF88)   // Écran terminal (lueur magitek)
    val ScreenGreenDim   = Color(0xFF007744)   // Texte secondaire écran
    val ScreenBackground = Color(0xFF001A0D)   // Fond écran

    val MagitekBlue      = Color(0xFF3399FF)   // Lueur bleutée de l'aération
    val MagitekBlueDim   = Color(0xFF114488)

    val MagitekBlueGlow = Color(0xFF66BBFF)

    val MetalLight       = Color(0xFF888888)   // Chrome / vis
    val MetalDark        = Color(0xFF444444)

    val OnBackground     = Color(0xFFCCCCCC)
    val OnSurface        = Color(0xFFAAAAAA)
    val OnImperialRed    = Color(0xFFFFFFFF)
    val DiodRed          = Color(0xFFFF2222)
}

// ── ColorScheme ───────────────────────────────────────────────────────────────

/*private val GarlemaldColorScheme = darkColorScheme(
    primary            = GarlemaldColors.ImperialRed,
    onPrimary          = GarlemaldColors.OnImperialRed,
    primaryContainer   = GarlemaldColors.ImperialRedDark,
    onPrimaryContainer = GarlemaldColors.OnImperialRed,

    secondary          = GarlemaldColors.MagitekBlue,
    onSecondary        = Color.White,
    secondaryContainer = GarlemaldColors.MagitekBlueDim,

    tertiary           = GarlemaldColors.ScreenGreen,
    onTertiary         = GarlemaldColors.ScreenBackground,

    background         = GarlemaldColors.Background,
    onBackground       = GarlemaldColors.OnBackground,

    surface            = GarlemaldColors.Surface,
    onSurface          = GarlemaldColors.OnSurface,
    surfaceVariant     = GarlemaldColors.SurfaceVariant,
    onSurfaceVariant   = GarlemaldColors.MetalLight,

    outline            = GarlemaldColors.Border,
    outlineVariant     = GarlemaldColors.MetalDark,

    error              = GarlemaldColors.DiodRed,
    onError            = Color.White,
)

// ── Typography ────────────────────────────────────────────────────────────────

private val GarlemaldTypography = Typography(
    // Écran de commande — monospace terminal
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 2.sp,
        color = GarlemaldColors.ScreenGreen,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp,
        color = GarlemaldColors.ScreenGreen,
    ),
    // Labels des boutons
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        letterSpacing = 0.5.sp,
        color = GarlemaldColors.OnSurface,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 7.sp,
        letterSpacing = 0.5.sp,
        color = GarlemaldColors.MetalLight,
    ),
    // Corps général
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = GarlemaldColors.OnBackground,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = GarlemaldColors.OnSurface,
    ),
    // Titres de section
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 3.sp,
        color = GarlemaldColors.ImperialRed,
    ),
)*/

// ── Shapes ────────────────────────────────────────────────────────────────────

/*private val GarlemaldShapes = Shapes(
    // Aucune rondeur — esthétique militaire
    extraSmall = RoundedCornerShape(0),
    small = androidx.compose.foundation.shape.RoundedCornerShape(0),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(2),
    large = androidx.compose.foundation.shape.RoundedCornerShape(2),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(4),
)

// ── Composable d'entrée ───────────────────────────────────────────────────────

@Composable
fun GarlemaldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GarlemaldColorScheme,
        typography  = GarlemaldTypography,
        shapes      = GarlemaldShapes,
        content     = content,
    )
}*/