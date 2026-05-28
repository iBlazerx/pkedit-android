package com.pkedit.core.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NamedEntry(val id: Int, val name: String)

@Serializable
private data class SpeciesFile(val species: List<NamedEntry>, val growthRates: List<Int>)

@Serializable
private data class ItemsFile(val items: List<NamedEntry>)

@Serializable
private data class MovesFile(val moves: List<NamedEntry>)

@Serializable
private data class AbilitiesFile(val abilities: List<NamedEntry>)

/**
 * Catálogo estático con los nombres y datos de especies/items/movimientos/habilidades de Gen 7.
 * Los JSON viven en assets. Substituir por listas más completas si se desea.
 */
class GameData(
    val species: List<NamedEntry>,
    val growthRates: List<Int>,
    val items: List<NamedEntry>,
    val moves: List<NamedEntry>,
    val abilities: List<NamedEntry>,
) {
    fun speciesName(id: Int): String =
        species.getOrNull(id)?.name ?: "Species $id"

    fun itemName(id: Int): String =
        items.getOrNull(id)?.name ?: "Item $id"

    fun moveName(id: Int): String =
        moves.getOrNull(id)?.name ?: "Move $id"

    fun abilityName(id: Int): String =
        abilities.getOrNull(id)?.name ?: "Ability $id"

    fun growthRateOf(speciesId: Int): Int =
        growthRates.getOrElse(speciesId) { 0 }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun load(context: Context): GameData {
            val sp = json.decodeFromString<SpeciesFile>(read(context, "species_gen7.json"))
            val it = json.decodeFromString<ItemsFile>(read(context, "items_gen7.json"))
            val mv = json.decodeFromString<MovesFile>(read(context, "moves_gen7.json"))
            val ab = json.decodeFromString<AbilitiesFile>(read(context, "abilities_gen7.json"))
            return GameData(
                species = sp.species,
                growthRates = sp.growthRates,
                items = it.items,
                moves = mv.moves,
                abilities = ab.abilities,
            )
        }

        private fun read(context: Context, asset: String): String =
            context.assets.open(asset).bufferedReader().use { it.readText() }
    }
}
