package com.example.movietracker.data

import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService{
    @GET("3/search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): MovieApiResponse
}