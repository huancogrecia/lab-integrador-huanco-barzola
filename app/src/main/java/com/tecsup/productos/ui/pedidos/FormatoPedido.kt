package com.tecsup.productos.ui.pedidos

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

internal fun formatoImporte(centavos: Long?): String = centavos?.let {
    NumberFormat.getCurrencyInstance(Locale.US).format(BigDecimal.valueOf(it, 2))
} ?: "Importe no registrado"
