package com.example.sudoku.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.APIService
import com.example.sudoku.data.Cell
import com.example.sudoku.data.Sudoku
import com.example.sudoku.data.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SudokuViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()


    fun startGame(game: Sudoku) {
        viewModelScope.launch {

            val newGame = game.copy(
                grid = game.value.mapIndexed{ y, row -> row.mapIndexed { x, v -> Cell(value = v, given = v > 0, realValue = game.solution[y][x]) } }
            )

            val valueCount = mutableListOf(0,0,0,0,0,0,0,0,0,0)
            newGame.grid.forEach { row -> row.forEach { cell -> valueCount[cell.value] ++ } }

            _uiState.update { state ->
                state.copy(
                    game = newGame,
                    valueCount = valueCount
                )
            }
        }
    }

    fun makeMove(value: Int, x: Int, y: Int, notes: Boolean = false) {
        val game = uiState.value.game!!
        val newGame = game.copy(
            grid = game.grid.mapIndexed { y1, r -> r.mapIndexed { x1, v ->
                if (!v.given && x == x1 && y == y1) {
                    if (notes) {
                        v.copy(
                            notes = if (value in v.notes) v.notes - listOf(value) else v.notes + listOf(value)
                        )
                    } else {
                        Cell(value, realValue = v.realValue)
                    }
                } else v
            } }
        )
        val valueCount = mutableListOf(0,0,0,0,0,0,0,0,0,0)
        newGame.grid.forEach { row -> row.forEach { cell -> valueCount[cell.value] ++ } }

        _uiState.update {state ->
            state.copy(
                game = newGame,
                valueCount = valueCount,
            )
        }

        if (valueCount[0] == 0) {
            // TODO: Check victory!
            println("IS WIN?")
        }
    }

    fun generateGameList() {
        viewModelScope.launch {
            val games = APIService.getInstance().getSudoku(amount = 10)
            _uiState.update { state ->
                state.copy(
                    gameList = games.newboard.grids
                )
            }
        }
    }
}