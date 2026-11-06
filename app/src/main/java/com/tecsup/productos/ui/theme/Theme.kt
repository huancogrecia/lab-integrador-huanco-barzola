package com.tecsup.productos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Colores = lightColorScheme(
    primary = Morado,
    primaryContainer = MoradoClaro
)

@Composable
fun ProductosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colores,
        typography = Tipografia,
        content = content
    )
}
