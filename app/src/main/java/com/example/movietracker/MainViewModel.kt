package com.example.movietracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.movietracker.data.MovieDB
import com.example.movietracker.data.MovieEntity
import com.example.movietracker.utils.AppSettingsManager
import com.example.movietracker.utils.AppTab
import com.example.movietracker.utils.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application)
{
    val sections = listOf(application.applicationContext.getString(R.string.watching), application.applicationContext.getString(R.string.want_to_watch), application.applicationContext.getString(R.string.watched))

    private val settingsManager = AppSettingsManager(application.applicationContext)
    private val _sortOrder = MutableStateFlow(settingsManager.getSortOrder())
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val database = MovieDB.getDB(application)
    private val movieDAO = database.movieDAO()

    val moviesBySection: StateFlow<Map<String, List<MovieEntity>>> =
        _sortOrder.flatMapLatest { currentSortOrder ->
            val flows: List<Flow<List<MovieEntity>>> = sections.map { category ->
                val query = buildQuery(category, currentSortOrder)
                movieDAO.getMoviesByCategory(query)
            }
            val combinedFlow: Flow<Map<String, List<MovieEntity>>> = combine(flows) { lists: Array<List<MovieEntity>> ->
                sections.zip(lists.toList()).toMap()
            }
            combinedFlow
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private fun buildQuery(category: String, sortOrder: SortOrder): SupportSQLiteQuery{
        val queryBuilder = StringBuilder("SELECT * FROM movies WHERE category = ? ORDER BY ")
        queryBuilder.append(sortOrder.sqlRepresentation)
        return SimpleSQLiteQuery(queryBuilder.toString(), arrayOf(category))
    }

    fun updateSortOrder(newSortOrder: SortOrder)
    {
        viewModelScope.launch {
            _sortOrder.value = newSortOrder
            settingsManager.setSortOrder(newSortOrder)
        }
    }

    fun addMovie(movie: MovieEntity)
    {
        viewModelScope.launch {
            movieDAO.insertMovie(movie)
        }
    }

    fun moveMovie(movie: MovieEntity, newCategory: String)
    {
        val updatedMovie = movie.copy(category = newCategory, addedDate = System.currentTimeMillis())
        viewModelScope.launch {
            movieDAO.updateMovie(updatedMovie)
        }
    }

    fun deleteMovie(movie: MovieEntity)
    {
        viewModelScope.launch {
            movieDAO.deleteMovie(movie)
        }
    }

    fun setLastTab(tab: AppTab)
    {
        viewModelScope.launch {
            settingsManager.setLastTab(tab)
        }
    }

    fun getLastTab(): Int
    {
        val savedTab = settingsManager.getLastTab()
        return AppTab.values().indexOf(savedTab)
    }
}