package com.example.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.data.Sudoku

@Composable
fun GamePreview(modifier: Modifier = Modifier, game: Sudoku) {
    Card(modifier = modifier) {
        Column {
            Column(Modifier.padding(3.dp)) {
                game.grid.forEach { row ->
                    Row {
                        row.forEach { cell ->
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black)
                            ){
                                Text(
                                    "${if (cell.value > 0) cell.value else ""}",
                                    fontSize = 10.sp,
                                )
                            }

                        }
                    }
                }
            }
            Box {
                Text(game.difficulty)
            }
        }
    }

}