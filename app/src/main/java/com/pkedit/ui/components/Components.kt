package com.pkedit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.pkedit.core.data.NamedEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    label: String,
    items: List<NamedEntry>,
    selectedId: Int,
    onSelect: (NamedEntry) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val current = items.firstOrNull { it.id == selectedId }

    OutlinedTextField(
        value = current?.let { "${it.name} (#${it.id})" } ?: "#$selectedId",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { open = true }
    )

    if (open) {
        SearchableListSheet(
            title = label,
            items = items,
            onPick = {
                onSelect(it)
                open = false
            },
            onDismiss = { open = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableListSheet(
    title: String,
    items: List<NamedEntry>,
    onPick: (NamedEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.name.contains(query, ignoreCase = true) || it.id.toString() == query.trim()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true,
            )
            LazyColumn {
                items(filtered, key = { it.id }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.name) },
                        supportingContent = { Text("#${entry.id}") },
                        modifier = Modifier.clickable { onPick(entry) }
                    )
                }
            }
        }
    }
}

@Composable
fun SliderRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.width(72.dp))
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                modifier = Modifier.weight(1f),
            )
            Text(value.toString(), modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun LabeledIntField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = { s ->
            text = s.filter { it.isDigit() }
            text.toIntOrNull()?.coerceIn(range.first, range.last)?.let(onValueChange)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}
