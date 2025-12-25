package com.example.movietracker.data

import com.google.gson.annotations.SerializedName

data class MovieApiResponse(
    val results: List<MovieApiResult>?
)

data class MovieApiResult(
    val id: Int,
    val title: String,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("poster_path")
    val posterPath: String?
)

data class MovieSearchResult(
    val id: Int,
    val title: String,
    val releaseDate: String?,
    val posterPath: String?
)