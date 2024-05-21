package com.example.sudoku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sudoku.ui.components.GamePreview
import com.example.sudoku.ui.theme.SudokuViewModel

@Composable
fun GameList(
    viewModel: SudokuViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val cols: Int = 3

    Column(Modifier.padding(3.dp)) {
        Text("Continue game", fontSize = 32.sp)
        Column(modifier = Modifier.fillMaxWidth()) {
            var i = 0
            while (i < uiState.startedGames.size) {
                Row(Modifier.fillMaxSize()) {
                    (1..cols).forEach {
                        if (i < uiState.startedGames.size) {
                            val game = uiState.startedGames[i]
                            GamePreview(game = game, modifier = Modifier.weight(1f).padding(3.dp).clickable {
                                viewModel.startGame(game = game)
                                navController.navigate(SelectedScreen.Game.name)
                            })
                        } else {
                            Box(Modifier.weight(1f))
                        }
                        i++
                    }
                }
            }
        }
        Text("Start new game", fontSize = 32.sp)
        Column(modifier = Modifier.fillMaxWidth()) {
            var i = 0
            while (i < uiState.gameList.size) {
                Row(Modifier.fillMaxSize()) {
                    (1..cols).forEach {
                        if (i < uiState.gameList.size) {
                            val game = uiState.gameList[i]
                            GamePreview(game = game, modifier = Modifier.weight(1f).padding(3.dp).clickable {
                                viewModel.startGame(game = game)
                                navController.navigate(SelectedScreen.Game.name)
                            })
                        } else {
                            Box(Modifier.weight(1f))
                        }
                        i++
                    }
                }
            }
        }
    }

}