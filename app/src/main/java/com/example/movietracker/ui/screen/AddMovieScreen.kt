package com.example.movietracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movietracker.MainViewModel
import com.example.movietracker.data.MovieSearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieScreen(navController: NavController, viewModel: MainViewModel)
{
    val state by viewModel.addScreenState.collectAsState()
    var selectedCategory by remember {mutableStateOf(viewModel.sections.first())}
    val sections = viewModel.sections
    var expanded by remember {mutableStateOf(false)}

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding())
    {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = {query ->
                viewModel.searchMovies(query)
            },
            label = {Text("Search films")},
            leadingIcon = {Icon(Icons.Default.Search, contentDescription = null)},
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
            Button(onClick = {expanded = true}) {
                Text(selectedCategory)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false}
            ) {
                sections.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedCategory = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.isLoading)
        {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        else if (state.error != null)
        {
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }
        else
        {
            LazyColumn {
                items(state.searchResult, key = {it.id}) {movie->
                    MovieSearchResultItem(movie = movie) {selectedMovie ->
                        viewModel.addMovieFromSearch(selectedMovie, selectedCategory)
                        viewModel.searchMovies("")
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun MovieSearchResultItem(movie: MovieSearchResult, onMovieClick: (MovieSearchResult)->Unit)
{
    Card(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clickable {onMovieClick(movie)}
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Spacer(modifier = Modifier.width(10.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w185${movie.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Movie poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f))
            {
                Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
                movie.releaseDate?.let {
                    Text(text = "Year: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}