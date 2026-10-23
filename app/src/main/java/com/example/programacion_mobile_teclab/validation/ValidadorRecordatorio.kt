package com.example.programacion_mobile_teclab.validation

import com.example.programacion_mobile_teclab.domain.Recordatorio

object ValidadorRecordatorio {

    fun validarTitulo(titulo: String): String? {
        val limpio = titulo.trim()
        return when {
            limpio.isEmpty() -> "El título del recordatorio es obligatorio"
            limpio.length < 4 -> "El título debe tener al menos 4 caracteres"
            else -> null
        }
    }

    fun formatearDatoOpcional(dato: String?, textoPorDefecto: String): String {
        return dato?.trim()?.takeIf { it.isNotEmpty() } ?: textoPorDefecto
    }

    fun esRecordatorioValido(recordatorio: Recordatorio): Boolean {
        return validarTitulo(recordatorio.titulo) == null && recordatorio.id > 0
    }
}