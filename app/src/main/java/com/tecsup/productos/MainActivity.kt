package com.tecsup.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tecsup.productos.notification.NotificationPermissionEffect
import com.tecsup.productos.ui.navigation.AppNav
import com.tecsup.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val modoOscuro by container.preferenciasRepository.modoOscuro.collectAsState(
                initial = false,
            )
            ProductosTheme(darkTheme = modoOscuro) {
                NotificationPermissionEffect()
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    AppNav(container = container, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
