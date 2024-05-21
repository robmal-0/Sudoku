package com.example.sudoku.data

data class Cell(
    val value: Int,
    val given: Boolean = false,
    val notes: List<Int> = arrayListOf(),
    val realValue: Int = 0,
    val difficulty: Int = 0
)

data class APISudoku(
    val value: List<List<Int>>,
    val difficulty: String,
    val solution: List<List<Int>>,
)

fun<E> List<List<E>>.column(col: Int, rows: Int = 9): List<E> {
    return subList(0, rows).map { row ->
        row[col]
    }
}

fun<E> List<List<E>>.box(col: Int, row: Int): List<E> {
    val x = col / 3 * 3
    val y = row / 3 * 3
    val rows = subList(y, y + 3)
    return rows.map { r -> r.subList(x, x + 3) }.flatten()
}

fun List<List<Int>>.generateRow(row: Int): List<Int> {
    var candidates = mutableListOf<Pair<Int, List<Int>>>()
    val res = mutableListOf<Int>()
    (0..8).forEach {i ->
        val b = box(i, row)
        candidates.add(i, Pair(i, ((1..9).shuffled() - (column(i, row) + b).toSet()).toMutableList()))
        res.add(i, 0)
    }
    val initialCandidates = candidates.toList()
    while (true) {
        try {
            while (res.count { i -> i == 0 } > 0) {
                candidates.sortBy { c -> c.second.size }
                val min = candidates[0]
                candidates.removeFirst()
                val candies = if (min.second.size < 4) {
                    val rest = if (candidates.size > 0) candidates.reduce{ acc, pair -> Pair(0, pair.second + acc.second)}.second else arrayListOf()
                    (min.second - rest.toSet())
                } else {
                    listOf()
                }

                res[min.first] = if (candies.isNotEmpty()) candies.random() else min.second.random()
                candidates = candidates.map { candidate ->
                    Pair(
                        candidate.first,
                        candidate.second - listOf(res[min.first]).toSet()
                    )
                }.toMutableList()
            }
            break
        } catch (e: Exception) {
            candidates = initialCandidates.toMutableList()
        }
    }

    return res
}

fun List<Cell>.values(): List<Int> {
    return map { cell ->
        cell.value
    }
}

data class Sudoku(
    val difficulty: String,
    val grid: List<List<Cell>>,
) {
    companion object {
        fun generateSudoku(): Sudoku {
            val values = mutableListOf(
                (1..9).shuffled(),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
                arrayListOf(0,0,0,0,0,0,0,0,0),
            )
            (1..8).forEach { i -> values[i] = values.generateRow(i) }

            return Sudoku (
                difficulty = "Easy",
                grid = values.map { row -> row.map { v -> Cell(value = v, given = false, realValue = v) } }
            )
        }

        fun toNewGame(game: Sudoku): Sudoku {
            return game.copy(
                grid = game.grid.map { row ->
                    row.map { cell ->
                        Cell(
                            if (cell.given) cell.value else 0,
                            realValue = cell.value,
                            given = cell.given,
                        )
                    }
                }
            )
        }

        fun fromApiSudoku(sudoku: APISudoku): Sudoku {
            return Sudoku (
                difficulty = sudoku.difficulty,
                grid = sudoku.value.mapIndexed { y, row ->
                    row.mapIndexed { x, v ->
                        Cell(
                            value = v,
                            given = v != 0,
                            realValue = sudoku.solution[y][x]
                        )
                    }
                }
            )
        }

        fun isSolved(sudoku: Sudoku): Boolean {
            return (
                (0..8).all { row -> (sudoku.grid[row].values() - (1..9)).isEmpty() }
                && (0..8).all { col -> (sudoku.grid.column(col).values() - (1..9)).isEmpty() }
                && (0..2).all { x ->
                    (0..2).all { y ->
                        (sudoku.grid.box(x*3, y*3).values() - (1..9)).isEmpty()
                    }
                }
            )
        }

        fun solver(original: Sudoku): Sudoku {
            if (original.grid.flatten().count { c -> c.value != 0 } < 9) {
                println("Won't solve")
                return original
            }
            println("Solver")
            val solved = original.grid.map { row -> row.toMutableList() }.toMutableList()



            var difficulty = 1

            data class SolveCell (
                val candidates: MutableList<Int> = (1..9).toMutableList(),
                val col: Int,
                val row: Int,
            )

            data class Candidates (
                var grid: MutableList<SolveCell> = mutableListOf()
            )

            val candidates = Candidates()

            (0..8).forEach {y ->
                (0..8).forEach { x ->
                    if (solved[y][x].value == 0) {
                        val box = solved.box(x, y).values()
                        val row = solved[y].values()
                        val col = solved.column(x).values()
                        candidates.grid.add(SolveCell(candidates = ((1..9) - (box + row + col).toSet()).toMutableList(), col = x, row = y))
                    }
                }
            }


            val toRemove = mutableListOf<SolveCell>()

            fun removeCandidates(value: Int, col: Int = -10, row: Int = -10) {
                val c = candidates.grid.filter {cell ->
                    cell.row == row || cell.col == col || (cell.row / 3 * 3 + cell.col / 3 == row / 3 * 3 + col / 3)
                }
                c.forEach { cell ->
                    cell.candidates -= value
                }
            }

            var skipHiddenPairs = false

            while (solved.any { row -> row.any { cell -> cell.value == 0 }}) {
                // Single candidate
                var foundSingleCandidate = false
                candidates.grid.forEach { cell ->
                    if (cell.candidates.size == 1) {
                        println("Found single candidate in cell:")
                        println(cell)
                        toRemove.add(cell)
                        val value = cell.candidates[0]
                        solved[cell.row][cell.col] = solved[cell.row][cell.col].copy(
                            value = value,
                            difficulty = difficulty
                        )
                        removeCandidates(value, col = cell.col, row = cell.row)
                        foundSingleCandidate = true
                    } else if (cell.candidates.isEmpty()) {
                        throw Exception("No candidates!!")
                    }
                }

                if (foundSingleCandidate) {
                    candidates.grid -= toRemove.toSet()
                    toRemove.clear()
                    skipHiddenPairs = false
                    continue
                }

                // Alone candidates
                var foundAloneCandidates = false
                val aloneCandidate = {list: List<SolveCell> ->
                    val count = mutableListOf(0,0,0,0,0,0,0,0,0)
                    (1..9).forEach {value ->
                        count[value - 1] = list.count { cell -> value in cell.candidates }
                    }
                    count.forEachIndexed { i, v ->
                        if (v == 1) {
                            val value = i+1
                            val cell = list.find { cell -> value in cell.candidates }!!

                            println("Found alone candidate in cell:")
                            println(cell)
                            println(list)
                            toRemove.add(cell)
                            solved[cell.row][cell.col] = solved[cell.row][cell.col].copy(
                                value = value,
                                difficulty = difficulty
                            )
                            removeCandidates(value, col = cell.col, row = cell.row)
                            foundAloneCandidates = true
                        }
                    }
                }
                // Alone in row
                (0..8).forEach { row -> aloneCandidate(candidates.grid.filter { c -> c.row == row }) }
                // Alone in col
                (0..8).forEach { col -> aloneCandidate(candidates.grid.filter { c -> c.col == col }) }
                // Alone in box
                (0..8).forEach { box -> aloneCandidate(candidates.grid.filter { c -> c.row / 3 * 3 + c.col / 3 == box }) }

                if (foundAloneCandidates) {
                    candidates.grid -= toRemove.toSet()
                    toRemove.clear()
                    skipHiddenPairs = false
                    continue
                }

                // Hidden pairs
                var foundHiddenPair = false
                val nakedPair = {list: List<SolveCell> ->
                    val count = mutableListOf(0,0,0,0,0,0,0,0,0)
                    (1..9).forEach {value ->
                        count[value - 1] = list.count { cell -> value in cell.candidates }
                    }
                    val pairs = mutableListOf<Pair<List<SolveCell>, Int>>()
                    count.forEachIndexed { i, v ->
                        if (v == 2) {
                            val value = i+1
                            val cells = list.filter { cell -> value in cell.candidates }
                            val pair = pairs.find { pair -> (pair.first - cells.toSet()).isEmpty()}
                            if (pair !== null) {
                                println("Found hidden pair!!")
                                println(pair.first)
                                pairs.remove(pair)
                                cells.forEach {cell ->
                                    cell.candidates.clear()
                                    cell.candidates.add(value)
                                    cell.candidates.add(pair.second)
                                }
                                foundHiddenPair = true
                            } else {
                                pairs.add(Pair(cells, value))
                            }
                        }
                    }
                }

                if (!skipHiddenPairs) {
                    // Pairs in row
                    (0..8).forEach { row -> nakedPair(candidates.grid.filter { c -> c.row == row }) }
                    // Pairs in col
                    (0..8).forEach { col -> nakedPair(candidates.grid.filter { c -> c.col == col }) }
                    // Pairs in box
                    (0..8).forEach { box -> nakedPair(candidates.grid.filter { c -> c.row / 3 * 3 + c.col / 3 == box }) }
                }

                if (foundHiddenPair) {
                    difficulty = 2
                    toRemove.clear()
                    skipHiddenPairs = true
                    continue
                }

                difficulty = -1
                break
            }

            return Sudoku(
                grid =  solved,
                difficulty = when (difficulty) {
                    1 -> "Easy"
                    2 -> "Medium"
                    3 -> "Hard"
                    else -> "Unsolvable"
                }
            )
        }
    }
}

data class UiState(
    val game: Sudoku? = null,
    val valueCount: List<Int> = listOf(0,0,0,0,0,0,0,0,0,0),
    val gameList: List<Sudoku> = arrayListOf(),
    val startedGames: List<Sudoku> = arrayListOf(),
    val draftList: List<Sudoku> = arrayListOf(),
    val gameWon: Boolean = false,
    val testing: Boolean = false,
    val solver: Sudoku? = null
)