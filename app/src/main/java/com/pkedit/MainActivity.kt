package com.pkedit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pkedit.ui.EditorViewModel
import com.pkedit.ui.screens.BagScreen
import com.pkedit.ui.screens.HomeScreen
import com.pkedit.ui.screens.PartyScreen
import com.pkedit.ui.screens.PokemonEditorScreen
import com.pkedit.ui.theme.PkEditTheme

class MainActivity : ComponentActivity() {

    private val vm: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PkEditTheme {
                Surface(Modifier.fillMaxSize()) {
                    App(vm)
                }
            }
        }
    }
}

@Composable
private fun App(vm: EditorViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                vm = vm,
                onOpenParty = { nav.navigate("party") },
                onOpenBag = { nav.navigate("bag") },
            )
        }
        composable("party") {
            PartyScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onEdit = { slot -> nav.navigate("editor/$slot") },
            )
        }
        composable("editor/{slot}") { backStack ->
            val slot = backStack.arguments?.getString("slot")?.toIntOrNull() ?: 0
            PokemonEditorScreen(
                vm = vm,
                slot = slot,
                onBack = { nav.popBackStack() },
            )
        }
        composable("bag") {
            BagScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}
