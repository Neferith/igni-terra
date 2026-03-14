package com.garlean.igniterra.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Palette Magitek ───────────────────────────────────────────────────────────
val MagitekBlack      = Color(0xFF080808)
val MagitekDark       = Color(0xFF111111)
val MagitekDarkPanel  = Color(0xFF181818)
val MagitekPanel      = Color(0xFF222222)
val MagitekBorder     = Color(0xFF3A3028)
val MagitekRed        = Color(0xFF8B0000)
val MagitekRedBright  = Color(0xFFCC1111)
val MagitekRedGlow    = Color(0x33CC1111)
val MagitekGold       = Color(0xFFB8860B)
val MagitekGoldLight  = Color(0xFFD4A017)
val MagitekText       = Color(0xFFCCBB99)
val MagitekTextDim    = Color(0xFF887766)
val MagitekTextFaint  = Color(0xFF554433)
val MagitekWarning    = Color(0xFFFF6600)
val MagitekDanger     = Color(0xFFFF2200)
val MagitekSafe       = Color(0xFF558844)

// ─── Styles de texte ──────────────────────────────────────────────────────────
val StyleTitle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    color = MagitekGoldLight,
    letterSpacing = 4.sp
)

val StyleSectionHeader = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    color = MagitekRedBright,
    letterSpacing = 3.sp
)

val StyleSubHeader = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    color = MagitekGold,
    letterSpacing = 2.sp
)

val StyleBody = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    color = MagitekText,
    letterSpacing = 0.5.sp,
    lineHeight = 18.sp
)

val StyleBodyDim = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    color = MagitekTextDim,
    letterSpacing = 0.5.sp,
    lineHeight = 16.sp
)

val StyleWarning = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    color = MagitekWarning,
    letterSpacing = 1.sp,
    lineHeight = 16.sp
)

val StyleDanger = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    color = MagitekDanger,
    letterSpacing = 1.sp,
    lineHeight = 16.sp
)

val StyleSpec = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    color = MagitekText,
    letterSpacing = 0.5.sp,
    lineHeight = 17.sp
)

val StyleLabel = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontSize = 9.sp,
    color = MagitekTextDim,
    letterSpacing = 2.sp
)
