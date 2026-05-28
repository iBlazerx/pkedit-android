package com.pkedit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pkedit.core.pkm.Pk7
import com.pkedit.ui.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyScreen(
    vm: EditorViewModel,
    onBack: () -> Unit,
    onEdit: (slot: Int) -> Unit,
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equipo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(ui.party) { slot, pk ->
                PartySlotCard(slot, pk, vm, onEdit)
            }
        }
    }
}

@Composable
private fun PartySlotCard(
    slot: Int,
    pk: Pk7?,
    vm: EditorViewModel,
    onEdit: (Int) -> Unit,
) {
    if (pk == null) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            ListItem(
                leadingContent = {
                    Box(
                        Modifier.size(40.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("${slot + 1}", style = MaterialTheme.typography.titleLarge)
                    }
                },
                headlineContent = { Text("— Vacío —", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            )
        }
        return
    }

    val gd = vm.gameData
    val specName = gd.speciesName(pk.species)
    val nick = pk.nickname.ifBlank { specName }
    val growth = gd.growthRateOf(pk.species)
    val lv = pk.calcLevelFromExp(growth)

    Card(onClick = { onEdit(slot) }) {
        ListItem(
            leadingContent = {
                Box(
                    Modifier.size(48.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(Icons.Default.Pets, null)
                }
            },
            headlineContent = {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(nick, fontWeight = FontWeight.SemiBold)
                    if (pk.isShiny) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                    }
                }
            },
            supportingContent = {
                Text("$specName · Nv. $lv · ${com.pkedit.core.data.Natures.byIndex(pk.nature).name}")
            },
            trailingContent = { Icon(Icons.Default.ChevronRight, null) }
        )
    }
}
