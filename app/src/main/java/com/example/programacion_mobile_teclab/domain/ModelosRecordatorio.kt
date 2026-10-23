package com.example.programacion_mobile_teclab.domain

import androidx.compose.ui.graphics.Color

data class Categoria(
    val id: Int,
    val nombre: String
)

sealed class Prioridad(val nivel: String) {
    object Alta : Prioridad("Alta")
    object Media : Prioridad("Media")
    object Baja : Prioridad("Baja")
}

sealed class EstadoRecordatorio(val descripcion: String) {
    object Pendiente : EstadoRecordatorio("Pendiente")
    object EnCurso : EstadoRecordatorio("En curso")
    object Completado : EstadoRecordatorio("Completado")
}

sealed class EstadoPantalla {
    object Inicial : EstadoPantalla()
    object Cargando : EstadoPantalla()
    data class Exito(val recordatorios: List<Recordatorio>) : EstadoPantalla()
    data class Error(val mensaje: String) : EstadoPantalla()
}

data class Recordatorio(
    val id: Long,
    val titulo: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val ubicacionOpcional: String? = null,
    val avisoOpcional: String? = null
)

object RepositorioSimulado {

    private val catTrabajo = Categoria(1, "Trabajo")
    private val catEstudio = Categoria(2, "Estudio")
    private val catPersonal = Categoria(3, "Personal")
    private val catSalud = Categoria(4, "Salud")

    val categoriasUnicas: Set<Categoria> = setOf(
        catTrabajo,
        catEstudio,
        catPersonal,
        catSalud,
        catTrabajo
    )

    val coloresPorCategoria: Map<String, Color> = mapOf(
        "Trabajo" to Color(0xFF1E88E5),
        "Estudio" to Color(0xFF8E24AA),
        "Personal" to Color(0xFF43A047),
        "Salud" to Color(0xFFE53935)
    )

    val listaRecordatorios: List<Recordatorio> = listOf(
        Recordatorio(
            id = 1L,
            titulo = "Entregar Actividad 1 de Mobile",
            categoria = catEstudio,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.EnCurso,
            ubicacionOpcional = "Plataforma virtual",
            avisoOpcional = "Hoy 23:59 hs"
        ),
        Recordatorio(
            id = 2L,
            titulo = "Revisión de servidores regionales",
            categoria = catTrabajo,
            prioridad = Prioridad.Alta,
            estado = EstadoRecordatorio.Pendiente,
            ubicacionOpcional = "Oficina Central",
            avisoOpcional = "Mañana 09:00 hs"
        ),
        Recordatorio(
            id = 3L,
            titulo = "Comprar repuestos de impresora",
            categoria = catPersonal,
            prioridad = Prioridad.Baja,
            estado = EstadoRecordatorio.Pendiente,
            ubicacionOpcional = null,
            avisoOpcional = null
        ),
        Recordatorio(
            id = 4L,
            titulo = "Control anual veterinario",
            categoria = catSalud,
            prioridad = Prioridad.Media,
            estado = EstadoRecordatorio.Completado,
            ubicacionOpcional = "Clínica Veterinaria",
            avisoOpcional = "18:00 hs"
        )
    )

    fun filtrarPorEstado(estadoBuscado: EstadoRecordatorio): List<Recordatorio> {
        return listaRecordatorios.filter { it.estado == estadoBuscado }
    }

    fun filtrarPorPrioridad(prioridadBuscada: Prioridad): List<Recordatorio> {
        return listaRecordatorios.filter { it.prioridad == prioridadBuscada }
    }
}