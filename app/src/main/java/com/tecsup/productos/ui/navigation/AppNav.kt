package com.tecsup.productos.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.productos.AppContainer
import com.tecsup.productos.domain.model.Rol
import com.tecsup.productos.domain.model.Sesion
import com.tecsup.productos.ui.UiState
import com.tecsup.productos.ui.admin.AdminScreen
import com.tecsup.productos.ui.admin.AdminViewModel
import com.tecsup.productos.ui.admin.AdminViewModelFactory
import com.tecsup.productos.ui.components.Cargando
import com.tecsup.productos.ui.carrito.CarritoScreen
import com.tecsup.productos.ui.carrito.CarritoViewModel
import com.tecsup.productos.ui.detalle.DetalleScreen
import com.tecsup.productos.ui.detalle.DetalleViewModel
import com.tecsup.productos.ui.detalle.DetalleViewModelFactory
import com.tecsup.productos.ui.favoritos.FavoritosScreen
import com.tecsup.productos.ui.favoritos.FavoritosViewModel
import com.tecsup.productos.ui.favoritos.FavoritosViewModelFactory
import com.tecsup.productos.ui.inventario.InventarioScreen
import com.tecsup.productos.ui.inventario.InventarioViewModel
import com.tecsup.productos.ui.lista.ListaScreen
import com.tecsup.productos.ui.lista.ListaViewModel
import com.tecsup.productos.ui.lista.ListaViewModelFactory
import com.tecsup.productos.ui.login.LoginScreen
import com.tecsup.productos.ui.login.LoginViewModel
import com.tecsup.productos.ui.login.LoginViewModelFactory
import com.tecsup.productos.ui.login.PerfilViewModel
import com.tecsup.productos.ui.login.PerfilViewModelFactory
import com.tecsup.productos.ui.login.PerfilScreen
import com.tecsup.productos.ui.login.RegistroScreen
import com.tecsup.productos.ui.pedidos.DetallePedidoScreen
import com.tecsup.productos.ui.pedidos.PedidosScreen
import com.tecsup.productos.ui.pedidos.PedidosViewModel

private const val TAB_CATALOGO = "catalogo"
private const val TAB_FAVORITOS = "favoritos"
private const val TAB_ADMIN = "admin"
private const val TAB_PERFIL = "perfil"
private const val TAB_PEDIDOS = "pedidos"
private const val TAB_CARRITO = "carrito"
private const val TAB_INVENTARIO = "inventario"

@Composable
fun AppNav(container: AppContainer, modifier: Modifier = Modifier) {
    val loginFactory = remember(container) { LoginViewModelFactory(container) }
    val loginVm: LoginViewModel = viewModel(factory = loginFactory)
    val authState by loginVm.estado.collectAsStateWithLifecycle()
    val registro by loginVm.registro.collectAsStateWithLifecycle()
    var mostrarRegistro by rememberSaveable { mutableStateOf(false) }
    val sesion = (authState as? UiState.Exito)?.datos

    LaunchedEffect(sesion) {
        if (sesion != null) mostrarRegistro = false
    }

    when {
        authState is UiState.Cargando -> Cargando(modifier)
        sesion != null -> NavegacionAutenticada(container, loginVm, sesion, modifier)
        mostrarRegistro -> RegistroScreen(
            estado = registro,
            onRegistrar = loginVm::registrar,
            onVolver = {
                loginVm.limpiarRegistro()
                mostrarRegistro = false
            },
            modifier = modifier,
        )
        else -> LoginScreen(
            vm = loginVm,
            estado = authState,
            onCrearCuenta = {
                loginVm.limpiarRegistro()
                mostrarRegistro = true
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun NavegacionAutenticada(
    container: AppContainer,
    loginVm: LoginViewModel,
    sesion: Sesion,
    modifier: Modifier,
) {
    val esAdmin = sesion.rol == Rol.ADMIN
    var productoId by rememberSaveable(sesion.uid) { mutableStateOf<Int?>(null) }
    var pedidoId by rememberSaveable(sesion.uid, sesion.rol) { mutableStateOf<String?>(null) }
    var recienCreado by rememberSaveable(sesion.uid, sesion.rol) { mutableStateOf(false) }
    var tabSeleccionada by rememberSaveable(sesion.uid, sesion.rol) { mutableStateOf(TAB_CATALOGO) }
    LaunchedEffect(sesion.uid, sesion.rol) {
        if ((esAdmin && tabSeleccionada in listOf(TAB_FAVORITOS, TAB_CARRITO)) ||
            (!esAdmin && tabSeleccionada in listOf(TAB_ADMIN, TAB_INVENTARIO))
        ) {
            tabSeleccionada = TAB_CATALOGO
        }
    }

    val listaFactory = remember(container, sesion.uid) { ListaViewModelFactory(container, sesion.uid) }
    val listaVm: ListaViewModel = viewModel(key = "lista_${sesion.uid}", factory = listaFactory)
    val id = productoId

    val carritoVm = if (esAdmin) null else {
        val factory = remember(container, sesion) {
            viewModelFactory {
                initializer { CarritoViewModel(container.carritoRepository(sesion.uid), container.pedidoRepository, sesion) }
            }
        }
        viewModel<CarritoViewModel>(key = "carrito_${sesion.uid}", factory = factory)
    }
    if (carritoVm != null) {
        val creado by carritoVm.pedidoCreado.collectAsStateWithLifecycle()
        LaunchedEffect(creado) {
            creado?.let {
                productoId = null
                tabSeleccionada = TAB_PEDIDOS
                pedidoId = it
                recienCreado = true
                carritoVm.confirmarNavegacion()
            }
        }
    }

    BackHandler(enabled = id != null || tabSeleccionada != TAB_CATALOGO) {
        when {
            id != null -> productoId = null
            pedidoId != null -> {
                pedidoId = null
                recienCreado = false
            }
            else -> tabSeleccionada = if (esAdmin && tabSeleccionada in listOf(TAB_PEDIDOS, TAB_INVENTARIO)) TAB_ADMIN else TAB_CATALOGO
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (id != null) {
                val factory = remember(container, sesion, id) { DetalleViewModelFactory(container, sesion, id) }
                val vm: DetalleViewModel = viewModel(key = "detalle_${sesion.uid}_$id", factory = factory)
                DetalleScreen(
                    vm = vm,
                    onVolver = { productoId = null },
                    onVerCarrito = {
                        productoId = null
                        tabSeleccionada = TAB_CARRITO
                    },
                )
            } else when (tabSeleccionada) {
                TAB_FAVORITOS -> {
                    if (esAdmin) {
                        ListaScreen(listaVm, puedeGuardarFavoritos = false, onProducto = { productoId = it })
                    } else {
                        val factory = remember(container, sesion.uid) {
                            FavoritosViewModelFactory(container, sesion.uid)
                        }
                        val vm: FavoritosViewModel = viewModel(key = "favoritos_${sesion.uid}", factory = factory)
                        FavoritosScreen(vm = vm, onProducto = { productoId = it })
                    }
                }
                TAB_ADMIN -> {
                    if (sesion.rol == Rol.ADMIN) {
                        val adminFactory = remember(container, sesion.uid) { AdminViewModelFactory(container, sesion.uid) }
                        val adminVm: AdminViewModel = viewModel(key = "admin_${sesion.uid}", factory = adminFactory)
                        AdminScreen(
                            adminVm,
                            onVerPedidos = { tabSeleccionada = TAB_PEDIDOS },
                            onVerInventario = { tabSeleccionada = TAB_INVENTARIO },
                        )
                    } else {
                        ListaScreen(listaVm, onProducto = { productoId = it })
                    }
                }
                TAB_INVENTARIO -> {
                    if (esAdmin) {
                        val factory = remember(container, sesion) {
                            viewModelFactory {
                                initializer {
                                    InventarioViewModel(
                                        container.itemRepository(sesion.uid),
                                        container.inventarioRepository,
                                        sesion,
                                    )
                                }
                            }
                        }
                        val vm: InventarioViewModel = viewModel(key = "inventario_${sesion.uid}", factory = factory)
                        InventarioScreen(vm, onVolver = { tabSeleccionada = TAB_ADMIN })
                    }
                }
                TAB_CARRITO -> {
                    if (carritoVm != null) {
                        CarritoScreen(carritoVm, onCatalogo = { tabSeleccionada = TAB_CATALOGO })
                    }
                }
                TAB_PEDIDOS -> {
                    val factory = remember(container, sesion) {
                        viewModelFactory { initializer { PedidosViewModel(container.pedidoRepository, sesion) } }
                    }
                    val vm: PedidosViewModel = viewModel(key = "pedidos_${sesion.uid}_${sesion.rol}", factory = factory)
                    val seleccionado = pedidoId
                    if (seleccionado == null) {
                        PedidosScreen(
                            vm, esAdmin,
                            onVolver = { tabSeleccionada = TAB_ADMIN },
                            onPedido = { pedidoId = it },
                        )
                    } else {
                        DetallePedidoScreen(vm, seleccionado, esAdmin, recienCreado, onVolver = {
                            pedidoId = null
                            recienCreado = false
                        })
                    }
                }
                TAB_PERFIL -> {
                    val perfilFactory = remember(container, sesion.uid) { PerfilViewModelFactory(container, sesion.uid) }
                    val perfilVm: PerfilViewModel = viewModel(key = "perfil_${sesion.uid}", factory = perfilFactory)
                    PerfilScreen(
                        sesion = sesion,
                        vm = perfilVm,
                        onCerrarSesion = loginVm::cerrarSesion,
                        onVerFavoritos = if (esAdmin) null else {
                            { tabSeleccionada = TAB_FAVORITOS }
                        },
                    )
                }
                else -> ListaScreen(
                    vm = listaVm,
                    puedeGuardarFavoritos = !esAdmin,
                    onProducto = { productoId = it },
                )
            }
        }

        if (id == null && pedidoId == null) {
            val esquema = MaterialTheme.colorScheme
            val coloresPestana = NavigationBarItemDefaults.colors(
                indicatorColor = esquema.primary,
                selectedIconColor = esquema.onPrimary,
                selectedTextColor = esquema.primary,
                unselectedIconColor = esquema.onSurfaceVariant,
                unselectedTextColor = esquema.onSurfaceVariant,
            )
            NavigationBar(containerColor = esquema.surface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_CATALOGO,
                    onClick = { tabSeleccionada = TAB_CATALOGO },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Catálogo") },
                    colors = coloresPestana,
                )
                if (!esAdmin) {
                    NavigationBarItem(
                        selected = tabSeleccionada == TAB_FAVORITOS,
                        onClick = { tabSeleccionada = TAB_FAVORITOS },
                        icon = { Icon(Icons.Filled.FavoriteBorder, contentDescription = null) },
                        label = { Text("Favoritos") },
                        colors = coloresPestana,
                    )
                }
                if (esAdmin) {
                    NavigationBarItem(
                        selected = tabSeleccionada in listOf(TAB_ADMIN, TAB_PEDIDOS, TAB_INVENTARIO),
                        onClick = { tabSeleccionada = TAB_ADMIN },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Admin") },
                        colors = coloresPestana,
                    )
                } else {
                    NavigationBarItem(
                        selected = tabSeleccionada == TAB_CARRITO,
                        onClick = { tabSeleccionada = TAB_CARRITO },
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                        label = { Text("Carrito") },
                        colors = coloresPestana,
                    )
                    NavigationBarItem(
                        selected = tabSeleccionada == TAB_PEDIDOS,
                        onClick = { tabSeleccionada = TAB_PEDIDOS },
                        icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                        label = { Text("Pedidos") },
                        colors = coloresPestana,
                    )
                }
                NavigationBarItem(
                    selected = tabSeleccionada == TAB_PERFIL,
                    onClick = { tabSeleccionada = TAB_PERFIL },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Perfil") },
                    colors = coloresPestana,
                )
            }
        }
    }
}
