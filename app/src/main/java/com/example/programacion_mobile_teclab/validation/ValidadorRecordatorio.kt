package com.example.programacion_mobile_teclab.validation

import com.example.programacion_mobile_teclab.domain.CatalogosDominio
import com.example.programacion_mobile_teclab.domain.Categoria
import com.example.programacion_mobile_teclab.domain.Prioridad

object ValidadorRecordatorio {

    fun validarTitulo(titulo: String): String? {
        val limpio = titulo.trim()
        return when {
            limpio.isEmpty() -> "El título es obligatorio"
            limpio.length < 4 -> "Ingresá al menos cuatro caracteres"
            else -> null
        }
    }

    fun validarDescripcion(descripcion: String): String? {
        return if (descripcion.length > 200) {
            "La descripción no debe superar los 200 caracteres (${descripcion.length}/200)"
        } else {
            null
        }
    }

    fun validarCategoria(categoria: Categoria?): String? {
        return if (categoria == null || !CatalogosDominio.categoriasDisponibles.contains(categoria)) {
            "Seleccioná una categoría válida"
        } else {
            null
        }
    }

    fun validarPrioridad(prioridad: Prioridad?): String? {
        return if (prioridad == null) {
            "Seleccioná una prioridad"
        } else {
            null
        }
    }

    fun formatearDatoOpcional(dato: String?, textoPorDefecto: String): String {
        return dato?.trim()?.takeIf { it.isNotEmpty() } ?: textoPorDefecto
    }
}