package com.example.programacion_mobile_teclab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.programacion_mobile_teclab.data.RecordatoriosEnMemoriaRepository
import com.example.programacion_mobile_teclab.data.RecordatoriosRepository
import com.example.programacion_mobile_teclab.domain.*
import com.example.programacion_mobile_teclab.validation.ValidadorRecordatorio

// --- CONSIGNA 5: Definición de Rutas de Navegación ---
object RutasNavegacion {
    const val LISTADO = "listado_recordatorios"
    const val CREACION = "crear_recordatorio"
    const val DETALLE_BASE = "detalle_recordatorio"
    const val ARG_ID = "recordatorioId"
    const val DETALLE_CON_ARG = "$DETALLE_BASE/{$ARG_ID}"

    fun construirRutaDetalle(id: Long): String = "$DETALLE_BASE/$id"
}

@Composable
fun RecordatoriosApp(
    repository: RecordatoriosRepository = remember { RecordatoriosEnMemoriaRepository() }
) {
    val navController = rememberNavController()
    val listaViewModel = remember { ListaRecordatoriosViewModel(repository) }
    val crearViewModel = remember { CrearRecordatorioViewModel(repository) }
    val detalleViewModel = remember { DetalleRecordatorioViewModel(repository) }

    RecordatoriosNavHost(
        navController = navController,
        listaViewModel = listaViewModel,
        crearViewModel = crearViewModel,
        detalleViewModel = detalleViewModel
    )
}

@Composable
fun RecordatoriosNavHost(
    navController: NavHostController,
    listaViewModel: ListaRecordatoriosViewModel,
    crearViewModel: CrearRecordatorioViewModel,
    detalleViewModel: DetalleRecordatorioViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = RutasNavegacion.LISTADO,
        modifier = modifier
    ) {
        composable(route = RutasNavegacion.LISTADO) {
            val uiState by listaViewModel.uiState.collectAsStateWithLifecycle()
            ListaRecordatoriosScreen(
                uiState = uiState,
                onCrearRecordatorio = {
                    navController.navigate(RutasNavegacion.CREACION)
                },
                onRecordatorioClick = { id ->
                    navController.navigate(RutasNavegacion.construirRutaDetalle(id))
                },
                onReintentar = listaViewModel::cargarRecordatorios,
                onSimularVacio = listaViewModel::simularEstadoVacio,
                onSimularError = listaViewModel::simularEstadoError,
                onSimularCarga = listaViewModel::simularEstadoCarga
            )
        }

        composable(route = RutasNavegacion.CREACION) {
            val uiState by crearViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.guardadoExitoso) {
                if (uiState.guardadoExitoso) {
                    crearViewModel.consumirEventoGuardado()
                    navController.popBackStack()
                }
            }

            CrearRecordatorioScreen(
                uiState = uiState,
                onTituloChange = crearViewModel::onTituloChange,
                onDescripcionChange = crearViewModel::onDescripcionChange,
                onCategoriaChange = crearViewModel::onCategoriaChange,
                onPrioridadChange = crearViewModel::onPrioridadChange,
                onUbicacionChange = crearViewModel::onUbicacionChange,
                onAvisoChange = crearViewModel::onAvisoChange,
                onGuardar = crearViewModel::guardarRecordatorio,
                onCancelar = {
                    crearViewModel.consumirEventoGuardado()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = RutasNavegacion.DETALLE_CON_ARG,
            arguments = listOf(navArgument(RutasNavegacion.ARG_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(RutasNavegacion.ARG_ID) ?: -1L
            LaunchedEffect(id) {
                detalleViewModel.cargarDetalle(id)
            }
            val uiState by detalleViewModel.uiState.collectAsStateWithLifecycle()

            DetalleRecordatorioScreen(
                uiState = uiState,
                onVolver = { navController.popBackStack() },
                onReintentar = { detalleViewModel.cargarDetalle(id) }
            )
        }
    }
}

// --- CONSIGNA 1: Pantalla 1 - Listado de Recordatorios ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaRecordatoriosScreen(
    uiState: ListaRecordatoriosUiState,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Long) -> Unit,
    onReintentar: () -> Unit,
    onSimularVacio: () -> Unit,
    onSimularError: () -> Unit,
    onSimularCarga: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis Recordatorios") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearRecordatorio,
                modifier = Modifier.semantics {
                    contentDescription = "Crear nuevo recordatorio"
                }
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barra de simulación para probar todos los estados de la Consigna 3
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedButton(onClick = onReintentar) { Text("Estado: Con Datos") }
                }
                item {
                    OutlinedButton(onClick = onSimularVacio) { Text("Simular Vacío") }
                }
                item {
                    OutlinedButton(onClick = onSimularError) { Text("Simular Error") }
                }
                item {
                    OutlinedButton(onClick = onSimularCarga) { Text("Simular Carga") }
                }
            }

            HorizontalDivider()

            // Evaluamos el UiState con un when exhaustivo (Consigna 3)
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is ListaRecordatoriosUiState.Cargando -> {
                        EstadoFeedbackComponente(
                            titulo = "Cargando recordatorios...",
                            mensaje = "Obteniendo registros disponibles.",
                            mostrarProgreso = true
                        )
                    }
                    is ListaRecordatoriosUiState.Vacia -> {
                        EstadoFeedbackComponente(
                            titulo = "No hay recordatorios por el momento",
                            mensaje = "Todavía no registraste ninguna tarea. Creá tu primer recordatorio para comenzar.",
                            textoBoton = "Crear recordatorio",
                            onAccionClick = onCrearRecordatorio
                        )
                    }
                    is ListaRecordatoriosUiState.Error -> {
                        EstadoFeedbackComponente(
                            titulo = "Ocurrió un problema",
                            mensaje = uiState.mensaje,
                            esError = true,
                            textoBoton = "Reintentar",
                            onAccionClick = onReintentar
                        )
                    }
                    is ListaRecordatoriosUiState.ConDatos -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.recordatorios,
                                key = { recordatorio -> recordatorio.id } // Clave estable (Consigna 1 / Unidad 1)
                            ) { recordatorio ->
                                RecordatorioCard(
                                    recordatorio = recordatorio,
                                    onClick = { onRecordatorioClick(recordatorio.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- CONSIGNA 1 y 2: Pantalla 2 - Formulario de Creación ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRecordatorioScreen(
    uiState: CrearRecordatorioUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (Categoria) -> Unit,
    onPrioridadChange: (Prioridad) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onAvisoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Recordatorio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val mostrarErrorTitulo = (uiState.tituloTocado || uiState.intentoDeEnvio) && uiState.errorTitulo != null
            CampoTextoControlado(
                valor = uiState.titulo,
                onValorChange = onTituloChange,
                etiqueta = "Título obligatorio (mín. 4 caracteres)",
                mostrarError = mostrarErrorTitulo,
                mensajeError = uiState.errorTitulo,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            val mostrarErrorDesc = (uiState.descripcionTocada || uiState.intentoDeEnvio) && uiState.errorDescripcion != null
            CampoTextoControlado(
                valor = uiState.descripcion,
                onValorChange = onDescripcionChange,
                etiqueta = "Descripción (máx. 200 caracteres: ${uiState.descripcion.length}/200)",
                mostrarError = mostrarErrorDesc,
                mensajeError = uiState.errorDescripcion,
                minLines = 3,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            // Selector de Categoría controlado
            Text(
                text = "Categoría:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CatalogosDominio.categoriasDisponibles.toList(), key = { it.id }) { cat ->
                    FilterChip(
                        selected = uiState.categoriaSeleccionada == cat,
                        onClick = { onCategoriaChange(cat) },
                        label = { Text(cat.nombre) }
                    )
                }
            }
            if (uiState.errorCategoria != null) {
                Text(
                    text = uiState.errorCategoria,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Selector de Prioridad controlado con RadioButton
            Text(
                text = "Prioridad:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CatalogosDominio.prioridadesDisponibles.forEach { prio ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.prioridadSeleccionada == prio,
                            onClick = { onPrioridadChange(prio) }
                        )
                        Text(text = prio.nivel, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Campos opcionales
            CampoTextoControlado(
                valor = uiState.ubicacionOpcional,
                onValorChange = onUbicacionChange,
                etiqueta = "Ubicación (Opcional)",
                mostrarError = false,
                mensajeError = null,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            CampoTextoControlado(
                valor = uiState.avisoOpcional,
                onValorChange = onAvisoChange,
                etiqueta = "Horario de aviso (Opcional)",
                mostrarError = false,
                mensajeError = null,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )

            if (uiState.errorGeneral != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorGeneral,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.enviando) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Guardando recordatorio...")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelar,
                    enabled = !uiState.enviando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = onGuardar,
                    enabled = uiState.formularioValido && !uiState.enviando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar recordatorio")
                }
            }
        }
    }
}

// --- CONSIGNA 1: Pantalla 3 - Detalle del Recordatorio ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecordatorioScreen(
    uiState: DetalleRecordatorioUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Recordatorio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (uiState) {
                is DetalleRecordatorioUiState.Cargando -> {
                    EstadoFeedbackComponente(
                        titulo = "Cargando detalle...",
                        mensaje = "Recuperando datos del registro seleccionado.",
                        mostrarProgreso = true
                    )
                }
                is DetalleRecordatorioUiState.Error -> {
                    EstadoFeedbackComponente(
                        titulo = "Error al cargar el detalle",
                        mensaje = uiState.mensaje,
                        esError = true,
                        textoBoton = "Reintentar",
                        onAccionClick = onReintentar
                    )
                }
                is DetalleRecordatorioUiState.Exito -> {
                    val rec = uiState.recordatorio
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "ID de Registro: #${rec.id}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = rec.titulo,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = rec.descripcion.ifBlank { "Sin descripción adicional." },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                HorizontalDivider()
                                Text("Categoría: ${rec.categoria.nombre}", style = MaterialTheme.typography.bodyMedium)
                                Text("Prioridad: ${rec.prioridad.nivel}", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Estado actual: ${rec.estado.descripcion}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Ubicación: ${
                                        ValidadorRecordatorio.formatearDatoOpcional(
                                            rec.ubicacionOpcional, "Sin ubicación asignada"
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Aviso: ${
                                        ValidadorRecordatorio.formatearDatoOpcional(
                                            rec.avisoOpcional, "Sin horario de aviso"
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Button(
                            onClick = onVolver,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver al listado")
                        }
                    }
                }
            }
        }
    }
}

// --- CONSIGNA 2: Componentes Reutilizables ---

@Composable
fun RecordatorioCard(
    recordatorio: Recordatorio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorCategoria = CatalogosDominio.coloresPorCategoria[recordatorio.categoria.nombre]
        ?: MaterialTheme.colorScheme.primary

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Recordatorio ${recordatorio.titulo}, prioridad ${recordatorio.prioridad.nivel}"
            },
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = recordatorio.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
        }
    }
}

@Composable
fun CampoTextoControlado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    mostrarError: Boolean,
    mensajeError: String?,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {}
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = mostrarError,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        supportingText = {
            if (mostrarError && mensajeError != null) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics {
                        contentDescription = "Error en $etiqueta: $mensajeError"
                    }
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun EstadoFeedbackComponente(
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    mostrarProgreso: Boolean = false,
    esError: Boolean = false,
    textoBoton: String? = null,
    onAccionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (mostrarProgreso) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (textoBoton != null && onAccionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAccionClick) {
                Text(textoBoton)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordatoriosAppPreview() {
    MaterialTheme {
        RecordatoriosApp()
    }
}