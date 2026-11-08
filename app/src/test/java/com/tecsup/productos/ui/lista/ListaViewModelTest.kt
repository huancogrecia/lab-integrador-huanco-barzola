package com.tecsup.productos.ui.lista

import app.cash.turbine.test
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cuandoSincronizacionEsExitosaViewModelEntregaProductosEnUiStateExito() = runTest {
        val itemsPrueba = listOf(Item(1, "Laptop Pro", "$1000", null, false))
        val repoFalso = FakeItemRepository(initialItems = itemsPrueba, refreshResult = Result.success(1))
        val notificadorFalso = FakeNotificador()

        val viewModel = ListaViewModel(repoFalso, notificadorFalso)

        viewModel.estado.test {
            assertEquals(UiState.Cargando, awaitItem())
            val estadoExito = awaitItem()
            assertTrue(estadoExito is UiState.Exito)
            assertEquals(itemsPrueba, (estadoExito as UiState.Exito).datos)
        }
    }

    @Test
    fun cuandoFallaConexionYNoHayProductosGuardadosViewModelDevuelveUiStateError() = runTest {
        val repoFalso = FakeItemRepository(initialItems = emptyList(), refreshResult = Result.failure(IOException()))
        val notificadorFalso = FakeNotificador()

        val viewModel = ListaViewModel(repoFalso, notificadorFalso)

        viewModel.estado.test {
            assertEquals(UiState.Cargando, awaitItem())
            val estadoError = awaitItem()
            assertTrue(estadoError is UiState.Error)
            assertEquals("No hay conexión a internet", (estadoError as UiState.Error).mensaje)
        }
    }

    @Test
    fun cuandoFallaConexionYYaHayProductosGuardadosViewModelConservaListaYMuestraAviso() = runTest {
        val itemsGuardados = listOf(Item(1, "Laptop Air", "$800", null, false))
        val repoFalso = FakeItemRepository(initialItems = itemsGuardados, refreshResult = Result.failure(IOException()))
        val notificadorFalso = FakeNotificador()

        val viewModel = ListaViewModel(repoFalso, notificadorFalso)

        viewModel.estado.test {
            assertEquals(UiState.Cargando, awaitItem())
            val estadoExito = awaitItem()
            assertTrue(estadoExito is UiState.Exito)
            assertEquals(itemsGuardados, (estadoExito as UiState.Exito).datos)
            assertEquals("Sin conexión: mostrando datos guardados", viewModel.aviso.value)
        }
    }

    private class FakeItemRepository(
        initialItems: List<Item>,
        private val refreshResult: Result<Int>,
    ) : ItemRepository {
        private val _items = MutableStateFlow(initialItems)
        override val items: Flow<List<Item>> = _items
        override val favoritos: Flow<List<Item>> = MutableStateFlow(emptyList())

        override suspend fun refrescar(): Result<Int> {
            return refreshResult
        }

        override suspend fun obtenerDetalle(id: Int): Result<Item> = Result.success(_items.value.first { it.id == id })

        override suspend fun alternarFavorito(id: Int) {}

        override suspend fun cantidadGuardada(): Int = _items.value.size

        override suspend fun cantidadFavoritos(): Int = 0

        override suspend fun limpiarCache() {
            _items.value = emptyList()
        }
    }

    private class FakeNotificador : Notificador {
        var tituloUltimo: String? = null
        var textoUltimo: String? = null

        override fun mostrar(titulo: String, texto: String) {
            tituloUltimo = titulo
            textoUltimo = texto
        }
    }
}
