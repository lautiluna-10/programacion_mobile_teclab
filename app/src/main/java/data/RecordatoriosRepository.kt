package com.example.programacion_mobile_teclab.data

import com.example.programacion_mobile_teclab.domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface RecordatoriosRepository {
    fun observarRecordatorios(): Flow<List<Recordatorio>>
    suspend fun obtenerPorId(id: Long): Recordatorio?
    suspend fun guardar(recordatorio: Recordatorio): Result<Unit>
}

class RecordatoriosEnMemoriaRepository : RecordatoriosRepository {

    private val datosIniciales = listOf(
        Recordatorio(
            id = 1L,
            titulo = "Entregar Actividad 2 de Mobile",
            descripcion = "Implementar pantallas de listado, creación y detalle con ViewModel, StateFlow y Navigation Compose.",
            categoria = CatalogosDominio.catEstudio,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.EnCurso,
            ubicacionOpcional = "Campus Virtual Teclab",
            avisoOpcional = "Hoy 23:59 hs"
        ),
        Recordatorio(
            id = 2L,
            titulo = "Revisión de servidores regionales",
            descripcion = "Verificar estado de sincronización y copias de seguridad en el nodo central.",
            categoria = CatalogosDominio.catTrabajo,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.Pendiente,
            ubicacionOpcional = "Oficina Central",
            avisoOpcional = "Mañana 09:00 hs"
        ),
        Recordatorio(
            id = 3L,
            titulo = "Control anual veterinario",
            descripcion = "Llevar libreta sanitaria actualizada para control general.",
            categoria = CatalogosDominio.catSalud,
            prioridad = Prioridad.Media,
            estado = EstadoRecordatorio.Completado,
            ubicacionOpcional = "Clínica Veterinaria",
            avisoOpcional = "Viernes 18:00 hs"
        )
    )

    private val _recordatorios = MutableStateFlow<List<Recordatorio>>(datosIniciales)

    override fun observarRecordatorios(): Flow<List<Recordatorio>> = _recordatorios.asStateFlow()

    override suspend fun obtenerPorId(id: Long): Recordatorio? {
        delay(200)
        return _recordatorios.value.firstOrNull { it.id == id }
    }

    override suspend fun guardar(recordatorio: Recordatorio): Result<Unit> {
        delay(600) // Simula operación asincrónica sin bloquear la UI
        return if (recordatorio.titulo.contains("error", ignoreCase = true)) {
            Result.failure(Exception("No se pudo guardar el recordatorio. Reintentá nuevamente."))
        } else {
            val nuevoId = (_recordatorios.value.maxOfOrNull { it.id } ?: 0L) + 1L
            val nuevoRegistro = recordatorio.copy(id = nuevoId)
            _recordatorios.update { actuales -> listOf(nuevoRegistro) + actuales }
            Result.success(Unit)
        }
    }
}