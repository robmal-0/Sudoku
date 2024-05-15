package com.example.sudoku.data

data class Cell(
    val value: Int,
    val given: Boolean = false,
    val notes: List<Int> = arrayListOf(),
    val realValue: Int = 0,
)

data class Sudoku(
    val value: List<List<Int>>,
    val solution: List<List<Int>>,
    val difficulty: String,
    val grid: List<List<Cell>>,
)

data class UiState(
    val game: Sudoku? = null,
    val valueCount: List<Int> = listOf(0,0,0,0,0,0,0,0,0,0),
    val gameList: List<Sudoku> = arrayListOf(),
    val draftList: List<Sudoku> = arrayListOf(),
)