package com.example.sudoku

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.sudoku.ui.theme.SudokuViewModel

@Composable
fun GameList(
    viewModel: SudokuViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        items(uiState.gameList) { game ->
            Button(onClick = {
                viewModel.startGame(game = game)
                navController.navigate(SelectedScreen.Game.name)
            }) {
                Text(game.difficulty)
            }
        }
    }
}