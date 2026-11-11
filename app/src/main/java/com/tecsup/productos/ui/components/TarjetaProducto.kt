package com.tecsup.productos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tecsup.productos.R
import com.tecsup.productos.domain.model.Item

@Composable
fun TarjetaProducto(
    item: Item,
    onClick: () -> Unit,
    onAlternarFavorito: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = item.imagenUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                placeholder = painterResource(R.drawable.ic_laptop),
                error = painterResource(R.drawable.ic_laptop),
                fallback = painterResource(R.drawable.ic_laptop),
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp))
                    .padding(8.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.titulo, style = MaterialTheme.typography.titleMedium)
                MarcaYPrecio(item.subtitulo, MaterialTheme.typography.titleMedium)
            }
            onAlternarFavorito?.let { alternar ->
                IconToggleButton(
                    checked = item.esFavorito,
                    onCheckedChange = { alternar() },
                ) {
                    Icon(
                        imageVector = if (item.esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (item.esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (item.esFavorito) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MarcaYPrecio(subtitulo: String, estiloPrecio: TextStyle) {
    val marca = subtitulo.substringBefore(" · ")
    val precio = subtitulo.substringAfter(" · ", missingDelimiterValue = "")
    if (marca.isNotBlank()) {
        Text(
            text = marca,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (precio.isNotBlank()) {
        Text(precio, style = estiloPrecio, color = MaterialTheme.colorScheme.primary)
    }
}
