package com.pkedit.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pkedit.ui.EditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: EditorViewModel,
    onOpenParty: () -> Unit,
    onOpenBag: () -> Unit,
) {
    val ui by vm.ui.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showExportConfirm by remember { mutableStateOf(false) }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { vm.loadSave(it) } }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            vm.exportSave(it) { err ->
                scope.launch {
                    snackbar.showSnackbar(
                        if (err == null) "✓ Save exportado correctamente"
                        else "Error: ${err.message}"
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Column {
                    Text("PkEdit", fontWeight = FontWeight.SemiBold)
                    Text("Editor Gen 7 · USUM", style = MaterialTheme.typography.labelSmall)
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            ui.errorMessage?.let {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )) {
                    Text(it, modifier = Modifier.padding(16.dp))
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Archivo de partida", style = MaterialTheme.typography.titleMedium)
                    if (ui.saveLoaded) {
                        Text(
                            "Cargado: ${ui.version?.displayName}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val bad = ui.badChecksumBlocks
                        if (bad.isEmpty()) {
                            Text(
                                "✓ Todos los checksums válidos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Text(
                                "⚠️ ${bad.size} bloques con checksum inválido en el save original",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            "Selecciona el archivo `main` de USUM (extraído con Checkpoint o JKSM).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { openLauncher.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.FolderOpen, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Abrir save")
                        }
                        if (ui.saveLoaded) {
                            FilledTonalButton(onClick = { showExportConfirm = true }) {
                                Icon(Icons.Default.Save, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Exportar")
                            }
                        }
                    }
                }
            }

            if (ui.saveLoaded) {
                Card(onClick = onOpenParty) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.Groups, null) },
                        headlineContent = { Text("Equipo") },
                        supportingContent = {
                            val count = ui.party.count { it != null }
                            Text("$count / 6 Pokémon")
                        },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) }
                    )
                }
                Card(onClick = onOpenBag) {
                    ListItem(
                        leadingContent = { Icon(Icons.Default.ShoppingBag, null) },
                        headlineContent = { Text("Mochila") },
                        supportingContent = { Text("Items, MTs, bayas, claves, cristales Z...") },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) }
                    )
                }
            }

            Card(colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )) {
                Column(Modifier.padding(16.dp)) {
                    Text("⚠️ Versión 0.4 — beta", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Equipo: lectura y edición completas ✓\n" +
                        "• Mochila: lectura aproximada (el bit-packing exacto puede variar)\n" +
                        "• Exportar: CRCs verificados, idempotencia comprobada\n\n" +
                        "Antes de exportar, SIEMPRE guarda copia del `main` original.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showExportConfirm) {
        AlertDialog(
            onDismissRequest = { showExportConfirm = false },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Exportar save") },
            text = {
                Text(
                    "Se exportará un nuevo archivo `main` con los CRC recalculados.\n\n" +
                    "Asegúrate de tener copia de seguridad del save original antes de " +
                    "sustituirlo en tu SD."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExportConfirm = false
                    saveLauncher.launch("main")
                }) { Text("Exportar") }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}
