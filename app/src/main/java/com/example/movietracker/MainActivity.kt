package com.example.movietracker

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.movietracker.ui.theme.MovieTrackerTheme
import com.example.movietracker.data.MovieEntity
import com.example.movietracker.data.MovieDB
import com.example.movietracker.ui.screen.AddMovieScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application)
{
    val sections = listOf(application.applicationContext.getString(R.string.watching), application.applicationContext.getString(R.string.want_to_watch), application.applicationContext.getString(R.string.watched))

    private val database = MovieDB.getDB(application)
    private val movieDAO = database.movieDAO()

    private val _moviesBySection = MutableStateFlow<Map<String, List<MovieEntity>>>(emptyMap())
    val moviesBySection: StateFlow<Map<String, List<MovieEntity>>> = _moviesBySection.asStateFlow()

    init {
        sections.forEach {category ->
            viewModelScope.launch {
                movieDAO.getMoviesByCategory(category).collect {movieList ->
                    _moviesBySection.value = _moviesBySection.value + (category to movieList)
                }
            }
        }
    }

    fun addMovie(movie: MovieEntity) {
        viewModelScope.launch {
            movieDAO.insertMovie(movie)
        }
    }
}

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieTrackerTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        MovieTrackerScreen(navController = navController, viewModel = mainViewModel)
                    }
                    composable("addMovie") {
                        AddMovieScreen(navController = navController, viewModel = mainViewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieTrackerScreen(navController: NavController, viewModel: MainViewModel)
{
    val pagerState = rememberPagerState(pageCount = {viewModel.sections.size})
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex by remember {derivedStateOf {pagerState.currentPage}}

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addMovie")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Movie")
            }
        }
    ) {innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                viewModel.sections.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = MaterialTheme.shapes.extraSmall,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        selected = selectedIndex == index,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = label)
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().weight(1f)
            ){
                page -> SectionContent(category = viewModel.sections[page], viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SectionContent(category: String, viewModel: MainViewModel)
{
    val moviesBySection by viewModel.moviesBySection.collectAsState()
    val movieList = moviesBySection[category] ?: emptyList()
    if (movieList.isEmpty()){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = "There are no films in the \"$category\" yet",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    }
    else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            items(movieList, key = {it.id}) {
                movie -> MovieItem(movie = movie)
            }
        }
    }
}

@Composable
fun MovieItem(movie: MovieEntity)
{
    Card(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
            movie.year?.let {
                Text(text = "Year: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MovieTrackerScreenPreview()
{
    MovieTrackerTheme {
        val navController = rememberNavController()
        val mainViewModel: MainViewModel = viewModel()
        MovieTrackerScreen(navController = navController, viewModel = mainViewModel)
    }
}