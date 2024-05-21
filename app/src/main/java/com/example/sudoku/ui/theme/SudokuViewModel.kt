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
            val valueCount = mutableListOf(0,0,0,0,0,0,0,0,0,0)
            game.grid.forEach { row -> row.forEach { cell -> valueCount[cell.value] ++ } }

            _uiState.update { state ->
                state.copy(
                    game = game,
                    valueCount = valueCount,
                    startedGames = if (game in state.startedGames) state.startedGames else state.startedGames + listOf(game)
                )
            }
        }
    }

    fun startEditor(game: Sudoku) {
        viewModelScope.launch {
            _uiState.update {state ->
                state.copy(
                    game = game,
                    draftList = listOf(game) + state.draftList
                )
            }
        }
    }

    fun startTesting(game: Sudoku) {
        viewModelScope.launch {
            _uiState.update {state ->
                state.copy(
                    game = Sudoku.toNewGame(game),
                    testing = true
                )
            }
        }
    }

    fun stopTesting() {
        _uiState.update {state ->
            state.copy(
                game = state.draftList[0],
                testing = false
            )
        }
    }

    fun setSolver() {
        viewModelScope.launch {
            _uiState.update {state ->
                state.copy(
                    solver = Sudoku.solver(Sudoku.toNewGame(uiState.value.game!!))
                )
            }
        }
    }

    fun makeMove(value: Int, x: Int, y: Int, notes: Boolean = false, editing: Boolean = false, given: Boolean = false) {
        val game = uiState.value.game!!
        val newGame = game.copy(
            grid = game.grid.mapIndexed { y1, r -> r.mapIndexed { x1, v ->
                if (((!v.given) || editing) && x == x1 && y == y1) {
                    if (notes) {
                        v.copy(
                            notes = if (value in v.notes) v.notes - listOf(value).toSet() else v.notes + listOf(value)
                        )
                    } else {
                        if (editing) {
                            Cell(value, realValue = value, given = given)
                        } else {
                            Cell(value, realValue = v.realValue, given = given)
                        }
                    }
                } else v
            } }
        )


        if(editing) {
            _uiState.update {state ->
                state.copy(
                    game = newGame,
                    draftList = listOf(newGame) + (state.draftList - listOf(game).toSet()),
                    valueCount = listOf(0,0,0,0,0,0,0,0,0,0)
                )
            }
            return
        }

        val valueCount = mutableListOf(0,0,0,0,0,0,0,0,0,0)
        newGame.grid.forEach { row -> row.forEach { cell -> valueCount[cell.value] ++ } }
        val win: Boolean = if (valueCount[0] == 0) {
            !newGame.grid.any { row ->
                row.any { cell ->
                    cell.value != cell.realValue
                }
            }
        } else false
        _uiState.update {state ->
            state.copy(
                game = newGame,
                valueCount = valueCount,
                startedGames = if (!uiState.value.testing) listOf(newGame) else listOf<Sudoku>()
                        + (if (win) state.startedGames else state.startedGames - listOf(game).toSet()),
                gameWon = win
            )
        }
    }

    fun toggleGiven(x: Int, y: Int) {
        _uiState.update { state ->
            val game = state.game!!
            val newGame = game.copy(
                grid = game.grid.mapIndexed { y1, r -> r.mapIndexed { x1, v ->
                    if (x == x1 && y == y1) {
                        v.copy(
                            given = !v.given
                        )
                    } else v
                } }
            )
            state.copy(
                game = newGame,
                draftList = listOf(newGame) + (state.draftList - listOf(game).toSet()),
            )
        }
    }

    fun generateGameList() {
        viewModelScope.launch {
            val games = APIService.getInstance().getSudoku(amount = 2)
            _uiState.update { state ->
                state.copy(
                    gameList = games.newboard.grids.map { Sudoku.fromApiSudoku(it) }
                )
            }
        }
    }
}