package com.example.movietracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val year: String?,
    val category: String, // Watching, Want to Watch, Watched
    val addedDate: Long = System.currentTimeMillis()
)