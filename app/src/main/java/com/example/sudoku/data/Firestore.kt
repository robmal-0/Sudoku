package com.example.sudoku.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.format.DateTimeFormatter

class Firestore(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun publishGame(game: Sudoku) {
        val puzzle = hashMapOf(
            "grid" to Gson().toJson(
                game.grid.map { row ->
                    row.values()
                }
            ),
            "difficulty" to game.difficulty,
            "solution" to Gson().toJson(
                game.grid.map { row ->
                    row.map { cell ->
                        cell.realValue
                    }
                }
            ),
            "voting" to 0,
            "publishedBy" to auth.currentUser!!.displayName,
            "publishedAt" to DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )

        db.collection("puzzles")
            .add(puzzle)
    }

    suspend fun fetchGames(): List<Sudoku> {
        val res = db.collection("puzzles")
            .get()
            .await()

        return res.documents.map {doc ->
            val listType = object : TypeToken<List<List<Int>>>() {}.type
            val grid = Gson().fromJson<List<List<Int>>>(doc.data?.get("grid") as String, listType)
            val solution = Gson().fromJson<List<List<Int>>>(doc.data?.get("solution") as String, listType)

            Sudoku (
                difficulty = doc.data?.get("difficulty") as String? ?: "Easy",
                grid = grid.mapIndexed { y, row ->
                    row.mapIndexed { x, value ->
                        Cell(
                            value = value,
                            realValue = solution[y][x],
                            given = value != 0
                        )
                    }
                }
            )
        }
    }

}