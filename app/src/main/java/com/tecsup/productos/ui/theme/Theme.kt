package com.tecsup.productos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colores = lightColorScheme(
    primary = Morado,
    primaryContainer = MoradoClaro
)

private val ColoresOscuros = darkColorScheme(
    primary = MoradoClaro,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = MoradoClaro,
)

@Composable
fun ProductosTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresOscuros else Colores,
        typography = Tipografia,
        content = content
    )
}
