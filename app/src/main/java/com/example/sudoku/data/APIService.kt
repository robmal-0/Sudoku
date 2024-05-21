package com.example.sudoku.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

const val BASE_URL = "https://sudoku-api.vercel.app/api/"

data class Board (
    val grids: List<APISudoku>
)

data class NewBoard (
    val newboard: Board
)

interface APIService {
    @Headers("Content-Type: application/json")
    @GET("dosuku/")
    suspend fun getSudoku(
        @Query("amount") amount: Int = 2,
        @Query("query") query: String = "{newboard(limit:$amount){grids{value,solution,difficulty},results,message}}",

        ): NewBoard

    companion object {
        private var INSTANCE: APIService? = null

        fun getInstance(): APIService {
            if (INSTANCE == null) {
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(APIService::class.java)
            }

            return INSTANCE!!
        }
    }
}
