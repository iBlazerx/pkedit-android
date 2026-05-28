package com.pkedit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pkedit.core.data.NamedEntry
import com.pkedit.core.data.Natures
import com.pkedit.core.pkm.Pk7
import com.pkedit.ui.EditorViewModel
import com.pkedit.ui.components.LabeledIntField
import com.pkedit.ui.components.SearchableDropdown
import com.pkedit.ui.components.SliderRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonEditorScreen(
    vm: EditorViewModel,
    slot: Int,
    onBack: () -> Unit,
) {
    val ui by vm.ui.collectAsState()
    val pkOriginal = ui.party.getOrNull(slot)
    if (pkOriginal == null) {
        // Slot vacío: nada que editar
        Scaffold(topBar = {
            TopAppBar(title = { Text("Slot ${slot + 1}") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            })
        }) { padding ->
            Box(Modifier.padding(padding).padding(16.dp)) {
                Text("Slot vacío. La creación de Pokémon desde cero no está implementada en esta versión.")
            }
        }
        return
    }

    // Trabajamos sobre una copia, confirmamos con "Guardar"
    val pk = remember(slot) { Pk7(pkOriginal.raw.copyOf()) }
    val gd = vm.gameData
    val growth = gd.growthRateOf(pk.species)

    var species by remember { mutableStateOf(pk.species) }
    var level by remember { mutableStateOf(pk.calcLevelFromExp(growth)) }
    var nature by remember { mutableStateOf(pk.nature) }
    var ability by remember { mutableStateOf(pk.ability) }
    var heldItem by remember { mutableStateOf(pk.heldItem) }
    var nickname by remember { mutableStateOf(pk.nickname) }
    var shiny by remember { mutableStateOf(pk.isShiny) }

    var ivHp by remember { mutableStateOf(pk.ivHp) }
    var ivAtk by remember { mutableStateOf(pk.ivAtk) }
    var ivDef by remember { mutableStateOf(pk.ivDef) }
    var ivSpa by remember { mutableStateOf(pk.ivSpa) }
    var ivSpd by remember { mutableStateOf(pk.ivSpd) }
    var ivSpe by remember { mutableStateOf(pk.ivSpe) }

    var evHp by remember { mutableStateOf(pk.evHp) }
    var evAtk by remember { mutableStateOf(pk.evAtk) }
    var evDef by remember { mutableStateOf(pk.evDef) }
    var evSpa by remember { mutableStateOf(pk.evSpa) }
    var evSpd by remember { mutableStateOf(pk.evSpd) }
    var evSpe by remember { mutableStateOf(pk.evSpe) }

    var move1 by remember { mutableStateOf(pk.getMove(0)) }
    var move2 by remember { mutableStateOf(pk.getMove(1)) }
    var move3 by remember { mutableStateOf(pk.getMove(2)) }
    var move4 by remember { mutableStateOf(pk.getMove(3)) }

    val evTotal = evHp + evAtk + evDef + evSpa + evSpd + evSpe

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Pokémon") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = {
                        // Maximizar IVs
                        ivHp = 31; ivAtk = 31; ivDef = 31; ivSpa = 31; ivSpd = 31; ivSpe = 31
                    }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Maximizar IVs")
                    }
                    IconButton(onClick = {
                        // Aplicar y guardar
                        pk.species = species
                        pk.nature = nature
                        pk.ability = ability
                        pk.heldItem = heldItem
                        pk.nickname = nickname
                        pk.isNicknamed = nickname.isNotEmpty() && nickname != gd.speciesName(species)
                        pk.isShiny = shiny
                        pk.setLevel(level, gd.growthRateOf(species))
                        pk.ivHp = ivHp; pk.ivAtk = ivAtk; pk.ivDef = ivDef
                        pk.ivSpa = ivSpa; pk.ivSpd = ivSpd; pk.ivSpe = ivSpe
                        pk.evHp = evHp; pk.evAtk = evAtk; pk.evDef = evDef
                        pk.evSpa = evSpa; pk.evSpd = evSpd; pk.evSpe = evSpe
                        pk.setMove(0, move1); pk.setMove(1, move2)
                        pk.setMove(2, move3); pk.setMove(3, move4)
                        vm.updatePartySlot(slot, pk)
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            SectionCard("Identidad") {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(12) },
                    label = { Text("Apodo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                SearchableDropdown(
                    label = "Especie",
                    items = gd.species,
                    selectedId = species,
                    onSelect = { species = it.id },
                )
                LabeledIntField(
                    label = "Nivel",
                    value = level,
                    onValueChange = { level = it.coerceIn(1, 100) },
                    range = 1..100,
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = shiny, onCheckedChange = { shiny = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Shiny")
                }
            }

            SectionCard("Habilidad, objeto, naturaleza") {
                SearchableDropdown(
                    label = "Naturaleza",
                    items = Natures.ALL.map { NamedEntry(it.index, it.name) },
                    selectedId = nature,
                    onSelect = { nature = it.id },
                )
                SearchableDropdown(
                    label = "Habilidad",
                    items = gd.abilities,
                    selectedId = ability,
                    onSelect = { ability = it.id },
                )
                SearchableDropdown(
                    label = "Objeto en uso",
                    items = gd.items,
                    selectedId = heldItem,
                    onSelect = { heldItem = it.id },
                )
            }

            SectionCard("IVs (0..31)") {
                SliderRow("PS", ivHp, 0..31)  { ivHp = it }
                SliderRow("Atq", ivAtk, 0..31) { ivAtk = it }
                SliderRow("Def", ivDef, 0..31) { ivDef = it }
                SliderRow("AtqEsp", ivSpa, 0..31) { ivSpa = it }
                SliderRow("DefEsp", ivSpd, 0..31) { ivSpd = it }
                SliderRow("Vel", ivSpe, 0..31) { ivSpe = it }
            }

            SectionCard("EVs (total $evTotal / 510)") {
                SliderRow("PS", evHp, 0..252) { evHp = it }
                SliderRow("Atq", evAtk, 0..252) { evAtk = it }
                SliderRow("Def", evDef, 0..252) { evDef = it }
                SliderRow("AtqEsp", evSpa, 0..252) { evSpa = it }
                SliderRow("DefEsp", evSpd, 0..252) { evSpd = it }
                SliderRow("Vel", evSpe, 0..252) { evSpe = it }
                if (evTotal > 510) {
                    Text(
                        "⚠️ Total supera el máximo legal (510)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            SectionCard("Movimientos") {
                SearchableDropdown("Mov. 1", gd.moves, move1) { move1 = it.id }
                SearchableDropdown("Mov. 2", gd.moves, move2) { move2 = it.id }
                SearchableDropdown("Mov. 3", gd.moves, move3) { move3 = it.id }
                SearchableDropdown("Mov. 4", gd.moves, move4) { move4 = it.id }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
