package com.tecsup.productos.ui.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.productos.data.repository.ItemRepository
import com.tecsup.productos.domain.model.Item
import com.tecsup.productos.notification.Notificador
import com.tecsup.productos.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ListaViewModel(
    private val repo: ItemRepository,
    private val notificador: Notificador,
) : ViewModel() {
    private val _estado = MutableStateFlow<UiState<List<Item>>>(UiState.Cargando)
    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()

    private val _orden = MutableStateFlow(OrdenProductos.NOMBRE_ASC)
    val orden = _orden.asStateFlow()

    val estado = combine(_estado, _busqueda, _orden) { actual, texto, seleccionado ->
        when (actual) {
            is UiState.Exito -> {
                val consulta = texto.trim()
                val filtrados = actual.datos.filter { item ->
                    item.titulo.contains(consulta, ignoreCase = true) ||
                        item.subtitulo.substringBefore(" · ").contains(consulta, ignoreCase = true)
                }
                val ordenados = when (seleccionado) {
                    OrdenProductos.NOMBRE_ASC -> filtrados.sortedBy { it.titulo.lowercase() }
                    OrdenProductos.NOMBRE_DESC -> filtrados.sortedByDescending { it.titulo.lowercase() }
                    OrdenProductos.PRECIO_ASC -> filtrados.sortedWith(
                        compareBy<Item> { precioDe(it) == null }.thenBy { precioDe(it) }
                    )
                    OrdenProductos.PRECIO_DESC -> filtrados.sortedWith(
                        compareBy<Item> { precioDe(it) == null }.thenByDescending { precioDe(it) }
                    )
                }
                UiState.Exito(ordenados)
            }
            else -> actual
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarOrden(orden: OrdenProductos) {
        _orden.value = orden
    }

    private fun precioDe(item: Item): Double? = item.subtitulo
        .substringAfterLast(" · ")
        .removePrefix("$")
        .trim()
        .toDoubleOrNull()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso = _aviso.asStateFlow()

    private var carga: Job? = null
    private var primeraCargaTerminada = false
    private var refrescoEnCurso = false
    private var errorSinDatos: Throwable? = null

    private var observador: Job? = null

    init {
        cargar()
    }

    private fun observarProductos() {
        if (observador?.isActive == true) return
        observador = viewModelScope.launch {
            repo.items
                .catch { error ->
                    errorSinDatos = error
                    _estado.value = UiState.Error(mensajeDe(error))
                }
                .collect { lista ->
                    when {
                        lista.isNotEmpty() -> _estado.value = UiState.Exito(lista)
                        primeraCargaTerminada && !refrescoEnCurso && errorSinDatos == null -> {
                            _estado.value = UiState.Exito(lista)
                        }
                    }
                }
        }
    }

    fun cargar() {
        carga?.cancel()
        observarProductos()
        carga = viewModelScope.launch {
            try {
                actualizarCatalogo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorSinDatos = e
                refrescoEnCurso = false
                _aviso.value = null
                _estado.value = UiState.Error("No pudimos cargar los productos guardados.")
            }
        }
    }

    private suspend fun actualizarCatalogo() {
        val actuales = repo.items.first()
        refrescoEnCurso = true
        if (actuales.isEmpty()) {
            _estado.value = UiState.Cargando
        }

        val resultado = repo.refrescar()
        val guardados = repo.items.first()
        primeraCargaTerminada = true
        resultado.fold(
            onSuccess = { cantidad ->
                errorSinDatos = null
                refrescoEnCurso = false
                _aviso.value = null
                _estado.value = UiState.Exito(guardados)
                notificador.mostrar("Datos actualizados", "Se guardaron $cantidad elementos")
            },
            onFailure = { error ->
                if (guardados.isEmpty()) {
                    errorSinDatos = error
                    refrescoEnCurso = false
                    _aviso.value = null
                    _estado.value = UiState.Error(mensajeDe(error))
                } else {
                    errorSinDatos = null
                    refrescoEnCurso = false
                    _aviso.value = if (error is IOException) {
                        "Mostramos los productos guardados. Revisa tu conexión para actualizarlos."
                    } else {
                        "No pudimos actualizar el catálogo. Mostramos los productos guardados."
                    }
                    _estado.value = UiState.Exito(guardados)
                }
            },
        )
    }

    private val _errorFavorito = MutableStateFlow<String?>(null)
    val errorFavorito = _errorFavorito.asStateFlow()

    fun alternarFavorito(id: Int) {
        viewModelScope.launch {
            _errorFavorito.value = null
            try {
                repo.alternarFavorito(id)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _errorFavorito.value = "No pudimos cambiar tus favoritos. Vuelve a tocar el corazón."
            }
        }
    }

    private fun mensajeDe(error: Throwable): String = when (error) {
        is IOException -> "No pudimos actualizar los productos. Revisa tu conexión."
        is HttpException -> "El catálogo no está disponible en este momento."
        else -> "No pudimos cargar los productos."
    }
}
