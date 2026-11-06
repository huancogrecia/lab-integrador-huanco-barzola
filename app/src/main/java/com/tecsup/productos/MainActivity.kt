package com.tecsup.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.lista.ListaViewModelFactory
import com.tecsup.productos.ui.theme.ProductosTheme

class MainActivity : ComponentActivity() {
    private val container = AppContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val factory = remember { ListaViewModelFactory(container) }
            val vm: ListaViewModel = viewModel(factory = factory)
            ProductosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    ListaScreen(vm = vm, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
