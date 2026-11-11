package com.tecsup.productos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val TipografiaBase = Typography()

val Tipografia = Typography(
    headlineLarge = TipografiaBase.headlineLarge.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = TipografiaBase.headlineMedium.copy(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = TipografiaBase.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = TipografiaBase.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = TipografiaBase.labelMedium.copy(fontWeight = FontWeight.SemiBold),
)
