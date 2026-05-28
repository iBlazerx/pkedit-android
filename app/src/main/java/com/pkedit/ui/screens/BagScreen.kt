package com.pkedit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pkedit.core.data.NamedEntry
import com.pkedit.core.save.BagItem
import com.pkedit.core.save.PouchInfo
import com.pkedit.ui.EditorViewModel
import com.pkedit.ui.components.SearchableDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagScreen(vm: EditorViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsState()
    val pouches = ui.pouches
    if (pouches.isEmpty()) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Mochila") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            })
        }) { padding ->
            Box(Modifier.padding(padding).padding(16.dp)) {
                Text("No hay mochila disponible para esta versión.")
            }
        }
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    val currentPouch = pouches[selectedTab]

    var items by remember(currentPouch) {
        mutableStateOf(vm.readPouch(currentPouch).toMutableList())
    }
    // Flag explícito de "cambios pendientes". Solo guardamos al pulsar Guardar.
    var dirty by remember(currentPouch) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mochila") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (dirty) {
                        IconButton(onClick = {
                            vm.writePouch(currentPouch, items)
                            dirty = false
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar pouch")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                items = items.toMutableList().also {
                    it.add(BagItem(id = 0, count = 1, isNew = true))
                }
                dirty = true
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "⚠️ Mochila experimental. Solo se guarda al pulsar el icono Guardar.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                pouches.forEachIndexed { i, p ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(p.name) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "${items.size} / ${currentPouch.maxSlots} ranuras (max count ${currentPouch.maxCount})" +
                        if (dirty) " · sin guardar" else "",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (dirty) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    BagItemRow(
                        item = item,
                        catalog = vm.gameData.items,
                        maxCount = currentPouch.maxCount,
                        onChange = { updated ->
                            items = items.toMutableList().also { it[index] = updated }
                            dirty = true
                        },
                        onDelete = {
                            items = items.toMutableList().also { it.removeAt(index) }
                            dirty = true
                        }
                    )
                }
            }
        }
    }
    // NO uso DisposableEffect que escriba en background.
    // Solo se escribe al pulsar el botón Guardar.
}

@Composable
private fun BagItemRow(
    item: BagItem,
    catalog: List<NamedEntry>,
    maxCount: Int,
    onChange: (BagItem) -> Unit,
    onDelete: () -> Unit,
) {
    Card {
        Column(Modifier.padding(12.dp)) {
            SearchableDropdown(
                label = "Objeto",
                items = catalog,
                selectedId = item.id,
                onSelect = { onChange(item.copy(id = it.id)) },
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.count.toString(),
                    onValueChange = { s ->
                        val n = s.filter { c -> c.isDigit() }
                            .toIntOrNull()?.coerceIn(0, maxCount) ?: 0
                        onChange(item.copy(count = n))
                    },
                    label = { Text("Cantidad (max $maxCount)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
    }
}
