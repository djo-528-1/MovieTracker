package com.example.movietracker.data

import com.example.movietracker.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieRepository {
    private val API_KEY = BuildConfig.TMDB_API_KEY
    private val BASE_URL = "https://api.themoviedb.org/"
    private val apiService: MovieApiService

    init{
        val loggingInterceptor = HttpLoggingInterceptor().apply{
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(MovieApiService::class.java)
    }

    suspend fun searchMovies(query: String): List<MovieSearchResult> {
        if (query.isBlank()) return emptyList()

        val response = apiService.searchMovies(apiKey = API_KEY, query = query)

        return response.results?.map {apiResult ->
            MovieSearchResult(
                id = apiResult.id,
                title = apiResult.title,
                releaseDate = apiResult.releaseDate,
                posterPath = apiResult.posterPath
            )
        } ?: emptyList()
    }
}