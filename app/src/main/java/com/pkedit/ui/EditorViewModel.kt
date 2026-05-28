package com.pkedit.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pkedit.core.data.GameData
import com.pkedit.core.pkm.Pk7
import com.pkedit.core.save.BagItem
import com.pkedit.core.save.GameVersion
import com.pkedit.core.save.PouchInfo
import com.pkedit.core.save.SaveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel(app: Application) : AndroidViewModel(app) {

    private val _ui = MutableStateFlow(EditorUiState())
    val ui: StateFlow<EditorUiState> = _ui.asStateFlow()

    val gameData: GameData by lazy { GameData.load(getApplication()) }

    private var save: SaveData? = null
    private var sourceUri: Uri? = null

    fun loadSave(uri: Uri) {
        sourceUri = uri
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, errorMessage = null)
            try {
                val bytes = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver
                        .openInputStream(uri)?.use { it.readBytes() }
                } ?: error("No se pudo abrir el archivo")

                val s = SaveData.load(bytes)
                    ?: error("Tamaño no reconocido (${bytes.size} bytes). Esta versión solo soporta USUM (${GameVersion.USUM.saveSize} bytes).")

                if (s.version != GameVersion.USUM) {
                    error("Esta versión solo soporta Ultra Sun / Ultra Moon. Detectado: ${s.version.displayName}")
                }

                save = s

                val party = s.readParty()
                val badChecksums = s.verifyChecksums()
                _ui.value = _ui.value.copy(
                    loading = false,
                    saveLoaded = true,
                    version = s.version,
                    party = party,
                    pouches = s.bag.pouches,
                    badChecksumBlocks = badChecksums,
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, errorMessage = t.message)
            }
        }
    }

    fun refreshParty() {
        val s = save ?: return
        _ui.value = _ui.value.copy(party = s.readParty())
    }

    fun updatePartySlot(slot: Int, pk: Pk7) {
        val s = save ?: return
        s.writePartySlot(slot, pk)
        refreshParty()
    }

    fun readPouch(pouch: PouchInfo): List<BagItem> = save?.bag?.read(pouch) ?: emptyList()

    fun writePouch(pouch: PouchInfo, items: List<BagItem>) {
        save?.bag?.write(pouch, items)
    }

    fun exportSave(destUri: Uri, onResult: (Throwable?) -> Unit) {
        val s = save
        if (s == null) {
            onResult(IllegalStateException("No hay save cargado")); return
        }
        viewModelScope.launch {
            val err = try {
                withContext(Dispatchers.IO) {
                    val bytes = s.export()
                    getApplication<Application>().contentResolver
                        .openOutputStream(destUri, "wt")?.use { it.write(bytes) }
                        ?: error("No se pudo abrir destino")
                }
                null
            } catch (t: Throwable) {
                t
            }
            onResult(err)
        }
    }
}

data class EditorUiState(
    val loading: Boolean = false,
    val saveLoaded: Boolean = false,
    val version: GameVersion? = null,
    val party: List<Pk7?> = emptyList(),
    val pouches: List<PouchInfo> = emptyList(),
    val badChecksumBlocks: List<Int> = emptyList(),
    val errorMessage: String? = null,
)
