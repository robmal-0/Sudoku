package com.example.sudoku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudoku.data.Sudoku
import com.example.sudoku.ui.components.GamePreview
import com.example.sudoku.ui.theme.SudokuViewModel

@Composable
fun DraftList(
    viewModel: SudokuViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val cols = 3

    Column {
        Row {
            Button(onClick={
                val game = Sudoku.generateSudoku()
                viewModel.startEditor(game)
                navController.navigate(SelectedScreen.Editor.name)
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new game button")
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            var i = 0
            while (i < uiState.draftList.size) {
                Row(Modifier.fillMaxSize()) {
                    (1..cols).forEach { _ ->
                        if (i < uiState.draftList.size) {
                            val game = uiState.draftList[i]
                            GamePreview(game = game, modifier = Modifier.weight(1f).padding(3.dp).clickable {
                                viewModel.startEditor(game = game)
                                navController.navigate(SelectedScreen.Editor.name)
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