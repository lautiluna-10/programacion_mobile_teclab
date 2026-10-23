package com.example.programacion_mobile_teclab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.programacion_mobile_teclab.domain.*
import com.example.programacion_mobile_teclab.validation.ValidadorRecordatorio

@Composable
fun PantallaRecordatorios(modifier: Modifier = Modifier) {
    var filtroSeleccionado by remember { mutableStateOf("Todos") }

    val recordatoriosMostrados = remember(filtroSeleccionado) {
        when (filtroSeleccionado) {
            "Pendientes" -> RepositorioSimulado.filtrarPorEstado(EstadoRecordatorio.Pendiente)
            "Prioridad Alta" -> RepositorioSimulado.filtrarPorPrioridad(Prioridad.Alta)
            else -> RepositorioSimulado.listaRecordatorios.filter {
                ValidadorRecordatorio.esRecordatorioValido(it)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Recordatorios Contextuales",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Categorías activas (${RepositorioSimulado.categoriasUnicas.size}): " +
                        RepositorioSimulado.categoriasUnicas.joinToString { it.nombre },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Todos", "Pendientes", "Prioridad Alta").forEach { filtro ->
                    Button(
                        onClick = { filtroSeleccionado = filtro },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (filtroSeleccionado == filtro) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = if (filtroSeleccionado == filtro) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    ) {
                        Text(text = filtro, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recordatoriosMostrados) { recordatorio ->
                    TarjetaRecordatorio(recordatorio = recordatorio)
                }
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio) {
    val colorCategoria = RepositorioSimulado.coloresPorCategoria[recordatorio.categoria.nombre]
        ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recordatorio.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(color = colorCategoria, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = recordatorio.categoria.nombre,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Prioridad: ${recordatorio.prioridad.nivel}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Estado: ${recordatorio.estado.descripcion}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val ubicacionTexto = ValidadorRecordatorio.formatearDatoOpcional(
                recordatorio.ubicacionOpcional, "Sin ubicación asignada"
            )
            val avisoTexto = ValidadorRecordatorio.formatearDatoOpcional(
                recordatorio.avisoOpcional, "Sin horario de aviso"
            )

            Text(
                text = "Ubicación: $ubicacionTexto | Aviso: $avisoTexto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaRecordatoriosPreview() {
    MaterialTheme {
        PantallaRecordatorios()
    }
}