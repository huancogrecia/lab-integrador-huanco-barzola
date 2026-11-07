package com.tecsup.productos.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecsup.productos.AppContainer
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AdminScreen
import com.tecsup.productos.ui.admin.AdminViewModel
import com.tecsup.productos.ui.admin.AdminViewModelFactory
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.detalle.DetalleScreen
import com.tecsup.productos.ui.detalle.DetalleViewModel
import com.tecsup.productos.ui.detalle.DetalleViewModelFactory
import com.tecsup.productos.ui.favoritos.FavoritosScreen
import com.tecsup.productos.ui.favoritos.FavoritosViewModel
import com.tecsup.productos.ui.favoritos.FavoritosViewModelFactory
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.lista.ListaViewModelFactory
import com.tecsup.productos.ui.login.LoginScreen
import com.tecsup.productos.ui.login.LoginViewModel
import com.tecsup.productos.ui.login.LoginViewModelFactory
import com.tecsup.productos.ui.login.PerfilViewModel
import com.tecsup.productos.ui.login.PerfilViewModelFactory
import com.tecsup.productos.ui.login.PerfilScreen

private const val TAB_CATALOGO = "catalogo"
private const val TAB_FAVORITOS = "favoritos"
private const val TAB_ADMIN = "admin"
private const val TAB_PERFIL = "perfil"

@Composable
fun AppNav(container: AppContainer, modifier: Modifier = Modifier) {
    val loginFactory = remember(container) { LoginViewModelFactory(container) }
    val loginVm: LoginViewModel = viewModel(factory = loginFactory)
    val authState by loginVm.estado.collectAsStateWithLifecycle()

    when (val estado = authState) {
        UiState.Cargando -> Cargando(modifier)
        is UiState.Error -> LoginScreen(loginVm, estado, modifier)
        is UiState.Exito -> {
            val sesion = estado.datos
            if (sesion == null) LoginScreen(loginVm, estado, modifier)
            else NavegacionAutenticada(container, loginVm, sesion, modifier)
        }
    }
}

@Composable
private fun NavegacionAutenticada(
    container: AppContainer,
    loginVm: LoginViewModel,
    sesion: Sesion,
    modifier: Modifier,
) {
    var productoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var tabSeleccionada by rememberSaveable { mutableStateOf(TAB_CATALOGO) }
    LaunchedEffect(sesion.uid, sesion.rol) {
        if (sesion.rol != Rol.ADMIN && tabSeleccionada == TAB_ADMIN) {
            tabSeleccionada = TAB_CATALOGO
        }
    }

    val listaFactory = remember(container) { ListaViewModelFactory(container) }
    val favoritosFactory = remember(container) { FavoritosViewModelFactory(container) }
    val listaVm: ListaViewModel = viewModel(factory = listaFactory)
    val favoritosVm: FavoritosViewModel = viewModel(factory = favoritosFactory)
    val id = productoId

    BackHandler(enabled = id != null || tabSeleccionada != TAB_CATALOGO) {
        if (id != null) productoId = null else tabSeleccionada = TAB_CATALOGO
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (id != null) {
                val factory = remember(container, id) { DetalleViewModelFactory(container, id) }
                val vm: DetalleViewModel = viewModel(key = "detalle_$id", factory = factory)
                DetalleScreen(vm = vm, onVolver = { productoId = null })
            } else when (tabSeleccionada) {
                TAB_FAVORITOS -> FavoritosScreen(
                    vm = favoritosVm,
                    onProducto = { productoId = it },
                )
                TAB_ADMIN -> {
                    if (sesion.rol == Rol.ADMIN) {
                        val adminFactory = remember(container) { AdminViewModelFactory(container) }
                        val adminVm: AdminViewModel = viewModel(factory = adminFactory)
                        AdminScreen(adminVm)
                    } else {
                        ListaScreen(listaVm, onProducto = { productoId = it })
                    }
                }
                TAB_PERFIL -> {
                    val perfilFactory = remember(container) { PerfilViewModelFactory(container) }
                    val perfilVm: PerfilViewModel = viewModel(factory = perfilFactory)
                    PerfilScreen(
                        sesion = sesion,
                        vm = perfilVm,
                        onCerrarSesion = loginVm::cerrarSesion,
                        onVerFavoritos = { tabSeleccionada = TAB_FAVORITOS }
                    )
                }
                else -> ListaScreen(
                    vm = listaVm,
                    onProducto = { productoId = it },
                )
            }
        }

        if (id == null) {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_CATALOGO,
                    onClick = { tabSeleccionada = TAB_CATALOGO },
                    icon = { Text("▦") },
                    label = { Text("Catálogo") },
                )
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_FAVORITOS,
                    onClick = { tabSeleccionada = TAB_FAVORITOS },
                    icon = { Text("♥") },
                    label = { Text("Favoritos") },
                )
                if (sesion.rol == Rol.ADMIN) {
                    NavigationBarItem(
                        selected = tabSeleccionada == TAB_ADMIN,
                        onClick = { tabSeleccionada = TAB_ADMIN },
                        icon = { Text("▣") },
                        label = { Text("Admin") },
                    )
                }
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_PERFIL,
                    onClick = { tabSeleccionada = TAB_PERFIL },
                    icon = { Text("●") },
                    label = { Text("Perfil") },
                )
            }
        }
    }
}
