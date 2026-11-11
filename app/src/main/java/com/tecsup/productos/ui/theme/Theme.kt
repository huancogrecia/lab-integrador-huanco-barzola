package com.tecsup.productos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colores = lightColorScheme(
    primary = AzulCobalto,
    onPrimary = Color.White,
    primaryContainer = AzulHielo,
    onPrimaryContainer = Color(0xFF182A63),
    secondary = Color(0xFF4A5C81),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4EAF6),
    onSecondaryContainer = Color(0xFF243657),
    background = BlancoCalido,
    onBackground = Grafito,
    surface = Color(0xFFFFFEFC),
    onSurface = Grafito,
    surfaceVariant = Color(0xFFE9EDF3),
    onSurfaceVariant = Color(0xFF485164),
    surfaceContainer = Color(0xFFF0F2F6),
    outline = Color(0xFF6A7285),
    outlineVariant = Color(0xFFD4DAE4),
)

private val ColoresOscuros = darkColorScheme(
    primary = AzulClaro,
    onPrimary = Color(0xFF182A63),
    primaryContainer = Color(0xFF283864),
    onPrimaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFFBECBE9),
    onSecondary = Color(0xFF263655),
    secondaryContainer = Color(0xFF29354D),
    onSecondaryContainer = Color(0xFFDCE5F9),
    background = AzulNoche,
    onBackground = Color(0xFFECEEF7),
    surface = Color(0xFF181D2A),
    onSurface = Color(0xFFECEEF7),
    surfaceVariant = Color(0xFF2E3649),
    onSurfaceVariant = Color(0xFFBFC8DA),
    surfaceContainer = Color(0xFF202638),
    outline = Color(0xFF939DB3),
    outlineVariant = Color(0xFF414A60),
)

@Composable
fun ProductosTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresOscuros else Colores,
        typography = Tipografia,
        content = content,
    )
}
