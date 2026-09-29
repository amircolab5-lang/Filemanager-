@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

import androidx.compose.ui.text.font.FontVariation

// Authentic League Spartan Font Family (User's real brand font for Ticno)
// Since league_spartan.ttf is a variable font with weight axis 0..210 (Thin to Black),
// we explicitly configure high-weight font variation settings so headings are bold, solid, and easily readable!
val LeagueSpartanFontFamily = FontFamily(
    Font(R.font.league_spartan, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.league_spartan, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.league_spartan, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(800))),
    Font(R.font.league_spartan, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(900))),
    Font(R.font.league_spartan, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(900))),
    Font(R.font.league_spartan, FontWeight.Black, variationSettings = FontVariation.Settings(FontVariation.weight(900)))
)

// Authentic Josefin Sans Font Family (Ticnodevelopers Official Brand Logo Font)
val JosefinSansFontFamily = FontFamily(
    Font(R.font.josefin_sans, FontWeight.Normal),
    Font(R.font.josefin_sans, FontWeight.Medium),
    Font(R.font.josefin_sans, FontWeight.SemiBold),
    Font(R.font.josefin_sans, FontWeight.Bold)
)

// Professional Sans-Serif System Font Family (Clean, crisp Material Design 3)
val ProfessionalFontFamily = FontFamily.SansSerif

// Clean, high-readability styles for data readouts and branding
val LeagueSpartanTitleLarge = TextStyle(
    fontFamily = LeagueSpartanFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.5.sp
)

val LeagueSpartanTitleMedium = TextStyle(
    fontFamily = LeagueSpartanFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.25.sp
)

val LeagueSpartanLabel = TextStyle(
    fontFamily = LeagueSpartanFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.2.sp
)

val LeagueSpartanStatValue = TextStyle(
    fontFamily = LeagueSpartanFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

// Main Application Typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ProfessionalFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

