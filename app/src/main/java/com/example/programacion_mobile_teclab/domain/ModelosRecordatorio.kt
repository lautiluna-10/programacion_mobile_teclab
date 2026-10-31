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

data class Recordatorio(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio = EstadoRecordatorio.Pendiente,
    val ubicacionOpcional: String? = null,
    val avisoOpcional: String? = null
)

object CatalogosDominio {
    val catTrabajo = Categoria(1, "Trabajo")
    val catEstudio = Categoria(2, "Estudio")
    val catPersonal = Categoria(3, "Personal")
    val catSalud = Categoria(4, "Salud")

    val categoriasDisponibles: Set<Categoria> = setOf(
        catTrabajo,
        catEstudio,
        catPersonal,
        catSalud
    )

    val prioridadesDisponibles: List<Prioridad> = listOf(
        Prioridad.Alta,
        Prioridad.Media,
        Prioridad.Baja
    )

    val coloresPorCategoria: Map<String, Color> = mapOf(
        "Trabajo" to Color(0xFF1E88E5),
        "Estudio" to Color(0xFF8E24AA),
        "Personal" to Color(0xFF43A047),
        "Salud" to Color(0xFFE53935)
    )
}