package com.example.programacion_mobile_teclab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.programacion_mobile_teclab.data.RecordatoriosRepository
import com.example.programacion_mobile_teclab.domain.*
import com.example.programacion_mobile_teclab.validation.ValidadorRecordatorio
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- CONSIGNA 3: UiState tipados e inmutables ---

sealed interface ListaRecordatoriosUiState {
    data object Cargando : ListaRecordatoriosUiState
    data object Vacia : ListaRecordatoriosUiState
    data class ConDatos(val recordatorios: List<Recordatorio>) : ListaRecordatoriosUiState
    data class Error(val mensaje: String) : ListaRecordatoriosUiState
}

data class CrearRecordatorioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoriaSeleccionada: Categoria? = CatalogosDominio.catEstudio,
    val prioridadSeleccionada: Prioridad? = Prioridad.Media,
    val ubicacionOpcional: String = "",
    val avisoOpcional: String = "",
    val tituloTocado: Boolean = false,
    val descripcionTocada: Boolean = false,
    val intentoDeEnvio: Boolean = false,
    val errorTitulo: String? = null,
    val errorDescripcion: String? = null,
    val errorCategoria: String? = null,
    val errorPrioridad: String? = null,
    val enviando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val errorGeneral: String? = null
) {
    val formularioValido: Boolean
        get() = ValidadorRecordatorio.validarTitulo(titulo) == null &&
                ValidadorRecordatorio.validarDescripcion(descripcion) == null &&
                ValidadorRecordatorio.validarCategoria(categoriaSeleccionada) == null &&
                ValidadorRecordatorio.validarPrioridad(prioridadSeleccionada) == null
}

sealed interface DetalleRecordatorioUiState {
    data object Cargando : DetalleRecordatorioUiState
    data class Exito(val recordatorio: Recordatorio) : DetalleRecordatorioUiState
    data class Error(val mensaje: String) : DetalleRecordatorioUiState
}

// --- CONSIGNA 3 y 4: ViewModels con StateFlow y viewModelScope ---

class ListaRecordatoriosViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    private var listaActualCache: List<Recordatorio> = emptyList()

    private val _uiState = MutableStateFlow<ListaRecordatoriosUiState>(ListaRecordatoriosUiState.Cargando)
    val uiState: StateFlow<ListaRecordatoriosUiState> = _uiState.asStateFlow()

    init {
        observarRepositorio()
    }

    private fun observarRepositorio() {
        viewModelScope.launch {
            _uiState.update { ListaRecordatoriosUiState.Cargando }
            delay(400)
            repository.observarRecordatorios().collect { lista ->
                listaActualCache = lista
                _uiState.update {
                    if (lista.isEmpty()) ListaRecordatoriosUiState.Vacia
                    else ListaRecordatoriosUiState.ConDatos(lista)
                }
            }
        }
    }

    fun cargarRecordatorios() {
        viewModelScope.launch {
            _uiState.update { ListaRecordatoriosUiState.Cargando }
            delay(500)
            _uiState.update {
                if (listaActualCache.isEmpty()) ListaRecordatoriosUiState.Vacia
                else ListaRecordatoriosUiState.ConDatos(listaActualCache)
            }
        }
    }

    // Mecanismo de simulación solicitado en la Consigna 3 para probar todos los estados
    fun simularEstadoVacio() {
        _uiState.update { ListaRecordatoriosUiState.Vacia }
    }

    fun simularEstadoError() {
        _uiState.update {
            ListaRecordatoriosUiState.Error("No se pudo cargar la información de los recordatorios.")
        }
    }

    fun simularEstadoCarga() {
        _uiState.update { ListaRecordatoriosUiState.Cargando }
    }
}

class CrearRecordatorioViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearRecordatorioUiState())
    val uiState: StateFlow<CrearRecordatorioUiState> = _uiState.asStateFlow()

    fun onTituloChange(nuevoTitulo: String) {
        _uiState.update { estado ->
            estado.copy(
                titulo = nuevoTitulo,
                tituloTocado = true,
                errorTitulo = ValidadorRecordatorio.validarTitulo(nuevoTitulo),
                errorGeneral = null
            )
        }
    }

    fun onDescripcionChange(nuevaDescripcion: String) {
        _uiState.update { estado ->
            estado.copy(
                descripcion = nuevaDescripcion,
                descripcionTocada = true,
                errorDescripcion = ValidadorRecordatorio.validarDescripcion(nuevaDescripcion),
                errorGeneral = null
            )
        }
    }

    fun onCategoriaChange(nuevaCategoria: Categoria) {
        _uiState.update { estado ->
            estado.copy(
                categoriaSeleccionada = nuevaCategoria,
                errorCategoria = ValidadorRecordatorio.validarCategoria(nuevaCategoria)
            )
        }
    }

    fun onPrioridadChange(nuevaPrioridad: Prioridad) {
        _uiState.update { estado ->
            estado.copy(
                prioridadSeleccionada = nuevaPrioridad,
                errorPrioridad = ValidadorRecordatorio.validarPrioridad(nuevaPrioridad)
            )
        }
    }

    fun onUbicacionChange(nuevaUbicacion: String) {
        _uiState.update { it.copy(ubicacionOpcional = nuevaUbicacion) }
    }

    fun onAvisoChange(nuevoAviso: String) {
        _uiState.update { it.copy(avisoOpcional = nuevoAviso) }
    }

    fun guardarRecordatorio() {
        val actual = _uiState.value
        val errTitulo = ValidadorRecordatorio.validarTitulo(actual.titulo)
        val errDesc = ValidadorRecordatorio.validarDescripcion(actual.descripcion)
        val errCat = ValidadorRecordatorio.validarCategoria(actual.categoriaSeleccionada)
        val errPrio = ValidadorRecordatorio.validarPrioridad(actual.prioridadSeleccionada)

        if (!actual.formularioValido) {
            _uiState.update {
                it.copy(
                    intentoDeEnvio = true,
                    errorTitulo = errTitulo,
                    errorDescripcion = errDesc,
                    errorCategoria = errCat,
                    errorPrioridad = errPrio
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true, errorGeneral = null) }

            val nuevoRecordatorio = Recordatorio(
                id = 0L,
                titulo = actual.titulo.trim(),
                descripcion = actual.descripcion.trim(),
                categoria = actual.categoriaSeleccionada!!,
                prioridad = actual.prioridadSeleccionada!!,
                estado = EstadoRecordatorio.Pendiente,
                ubicacionOpcional = actual.ubicacionOpcional.ifBlank { null },
                avisoOpcional = actual.avisoOpcional.ifBlank { null }
            )

            val resultado = repository.guardar(nuevoRecordatorio)
            _uiState.update { estado ->
                resultado.fold(
                    onSuccess = { estado.copy(enviando = false, guardadoExitoso = true) },
                    onFailure = { error ->
                        estado.copy(
                            enviando = false,
                            errorGeneral = error.message ?: "No se pudo guardar el recordatorio"
                        )
                    }
                )
            }
        }
    }

    fun consumirEventoGuardado() {
        _uiState.update { CrearRecordatorioUiState() }
    }
}

class DetalleRecordatorioViewModel(
    private val repository: RecordatoriosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleRecordatorioUiState>(DetalleRecordatorioUiState.Cargando)
    val uiState: StateFlow<DetalleRecordatorioUiState> = _uiState.asStateFlow()

    fun cargarDetalle(recordatorioId: Long) {
        viewModelScope.launch {
            _uiState.update { DetalleRecordatorioUiState.Cargando }
            val encontrado = repository.obtenerPorId(recordatorioId)
            _uiState.update {
                if (encontrado != null) {
                    DetalleRecordatorioUiState.Exito(encontrado)
                } else {
                    DetalleRecordatorioUiState.Error("No se encontró el recordatorio con ID #$recordatorioId")
                }
            }
        }
    }
}