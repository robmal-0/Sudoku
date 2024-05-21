package com.example.sudoku

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sudoku.data.Cell
import com.example.sudoku.ui.theme.SudokuViewModel
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BoardEditor(
    viewModel: SudokuViewModel,
    navController: NavController
) {

    val uiState by viewModel.uiState.collectAsState()
    val game = uiState.game

    if (game !== null) {
        val selected = remember { mutableStateOf(intArrayOf(-1, -1)) }
        val sX = selected.value[0]
        val sY = selected.value[1]
        val sB =  if (sY >= 0 && sX >= 0) sY / 3 * 3 + sX / 3 else -1
        val selectedValue = if (sX >= 0 && sY >= 0) game.grid[sY][sX] else Cell(0)
        val takingNotes = remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Column(modifier = Modifier
                .padding(5.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(2.dp, color = Color.Black, shape = RoundedCornerShape(size = 10.dp))
                ) {
                    game.grid.forEachIndexed { y, row ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)) {
                            row.forEachIndexed { x, cell ->
                                val isSelected = sX == x && sY == y
                                val valSelected = cell.value == selectedValue.value && selectedValue.value != 0
                                val box = y / 3 * 3 + x / 3
                                val affected = box == sB || x == sX || y == sY
                                val solvedBySolver = if (uiState.solver !== null) !cell.given && uiState.solver!!.grid[y][x].value != 0 else false
                                val difficulty = uiState.solver?.grid?.get(y)?.get(x)?.difficulty ?: 0

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            if (isSelected) Color.hsl(0f, 0f, 0.60f)
                                            else if (affected) Color.hsl(0f, 0f, 0.83f)
                                            else if (valSelected) Color.hsl(0f, 0f, 0.73f)
                                            // else if (solvedBySolver) Color.hsl(0f, 1f, 0.5f)
                                            else Color.White
                                        )
                                        .border(
                                            if (isSelected || valSelected) 1.dp else 0.dp,
                                            color =
                                            if (isSelected) Color.Magenta
                                            else if (valSelected) Color.Blue
                                            else Color.White,
                                            shape = RoundedCornerShape(size = 5.dp)
                                        )
                                        .clickable {
                                            selected.value = intArrayOf(x, y)
                                        },

                                    ) {
                                    if (cell.value > 0) {
                                        // val wrong = cell.value != cell.realValue
                                        Text(
                                            "${cell.value}",
                                            modifier = Modifier,
                                            fontSize = 32.sp,
                                            color =
                                                if (cell.given) Color.Black
                                                else if (solvedBySolver) when(difficulty) {
                                                    2 -> Color.hsl(160f, 0.7f, 0.45f)
                                                    else -> Color.hsl(230f, 0.7f, 0.45f)
                                                }
                                                else Color.Red)
                                    } else if (cell.notes.isNotEmpty()){
                                        Column {
                                            (0..2).forEach { row ->
                                                Row {
                                                    (0..2).forEach {col ->
                                                        val i = row * 3 + col+1
                                                        if (i in cell.notes) {
                                                            Text("$i", modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f), fontSize = 10.sp, textAlign = TextAlign.Center)
                                                        } else {
                                                            Text("", modifier = Modifier
                                                                .weight(1f)
                                                                .aspectRatio(1f))
                                                        }

                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        Text("")
                                    }
                                }
                                if (x == 2 || x == 5) {
                                    Spacer(
                                        // color = Color.Black,
                                        // thickness = 2.dp,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(2.dp)
                                            .background(Color.Black)
                                    )
                                } else if (x < 8) {
                                    Spacer(
                                        // color = Color.Black,
                                        // thickness = 1.dp,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(Color.Black)
                                    )
                                }
                            }
                        }
                        if (y == 2 || y == 5) {
                            Spacer(
                                modifier = Modifier
                                    .height(2.dp)
                                    .fillMaxWidth()
                                    .background(Color.Black)
                            )
                        } else if (y < 8) {
                            Spacer(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .background(Color.Black)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.padding(horizontal = 15.dp)) {
                    val buttonMods = Modifier
                        .padding(3.dp)
                        .weight(1f)
                        .aspectRatio(1f)
                        .border(1.dp, color = Color.Black)
                    val fontSize = if (takingNotes.value) 24.sp else 32.sp

                    val vertArr = arrayOf(
                        Arrangement.Top,
                        Arrangement.Center,
                        Arrangement.Bottom
                    )

                    val horAlign = arrayOf(
                        Alignment.Start,
                        Alignment.CenterHorizontally,
                        Alignment.End,
                    )

                    Row {
                        Column(modifier = Modifier.weight(3f)) {
                            (0..2).forEach { row ->
                                Row {
                                    (0..2).forEach { col ->
                                        val i = 3 * row + col + 1
                                        val vArr = vertArr[if (takingNotes.value) row else 1]
                                        val hAlign = horAlign[if (takingNotes.value) col else 1]
                                        val disabled =
                                            uiState.valueCount[i] >= 9 && !takingNotes.value
                                        Column(
                                            modifier = buttonMods
                                                .background(if (disabled) Color.LightGray else Color.White)
                                                .clickable(enabled = !disabled) {
                                                    viewModel.makeMove(
                                                        i,
                                                        sX,
                                                        sY,
                                                        takingNotes.value,
                                                        editing = true,
                                                        given = true
                                                    )
                                                },
                                            verticalArrangement = vArr,
                                            horizontalAlignment = hAlign
                                        ) {
                                            Text(
                                                "$i",
                                                modifier = Modifier.padding(5.dp),
                                                fontSize = fontSize,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(2f)) {
                            Row {
                                Column(
                                    modifier = buttonMods
                                        .clickable {
                                            takingNotes.value = !takingNotes.value
                                        },
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Eraser"
                                    )
                                }
                                Column(
                                    modifier = buttonMods.clickable {
                                        viewModel.makeMove(0, sX, sY, editing = true)
                                    },
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Eraser"
                                    )
                                }
                            }
                            Row {
                                Column(
                                    modifier = buttonMods
                                        .background(
                                            if (takingNotes.value) Color.hsl(
                                                210f,
                                                0.4f,
                                                0.8f
                                            ) else Color.White
                                        )
                                        .clickable {
                                            viewModel.toggleGiven(sX, sY)
                                            viewModel.setSolver()
                                        },
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Toggle Lock"
                                    )
                                }
                                Column(
                                    modifier = buttonMods.clickable {
                                        viewModel.startTesting(game)
                                        navController.navigate(SelectedScreen.Game.name)
                                    },
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Test and Publish"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Text("Loading...")
    }
}