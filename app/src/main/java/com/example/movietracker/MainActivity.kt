package com.example.movietracker

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.movietracker.ui.theme.MovieTrackerTheme
import com.example.movietracker.data.MovieEntity
import com.example.movietracker.ui.screen.AddMovieScreen
import com.example.movietracker.utils.AppTab
import com.example.movietracker.utils.SortOrder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val lastTab = viewModel.getLastTab()
    val pagerState = rememberPagerState(initialPage = lastTab, pageCount = {viewModel.sections.size})
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex by remember {derivedStateOf {pagerState.currentPage}}
    var showSortMenu by remember {mutableStateOf(false)}
    val currentSortOrder by viewModel.sortOrder.collectAsState()

    LaunchedEffect(key1 = selectedIndex) {
        when (selectedIndex){
            0 -> viewModel.setLastTab(AppTab.WATCHING)
            1 -> viewModel.setLastTab(AppTab.WANT_TO_WATCH)
            2 -> viewModel.setLastTab(AppTab.WATCHED)
        }
    }

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
            Box(modifier = Modifier.padding(4.dp)){
                Button(onClick = {showSortMenu = true}) {
                    Text(stringResource(id = currentSortOrder.displayNameId))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = {showSortMenu = false}
                ){
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {Text(stringResource(id = order.displayNameId))},
                            onClick = {
                                viewModel.updateSortOrder(order)
                                showSortMenu = false
                            }
                        )
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
                movie -> MovieItem(movie = movie, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MovieItem(movie: MovieEntity, viewModel: MainViewModel)
{
    var showMenu by remember {mutableStateOf(false)}
    var showDeleteDialog by remember {mutableStateOf(false)}
    val sections = viewModel.sections

    Card(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
                movie.year?.let {
                    Text(text = "Year: $it", style = MaterialTheme.typography.bodySmall)
                }
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val formattedDate = dateFormat.format(Date(movie.addedDate))
                Text(text = "Added: $formattedDate", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = {showMenu = true}) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {showMenu = false}
            ){
                sections.forEach {category ->
                    if (category != movie.category)
                    {
                        DropdownMenuItem(
                            text = {Text("Movie to $category")},
                            onClick = {
                                viewModel.moveMovie(movie, category)
                                showMenu = false
                            }
                        )
                    }
                }
                DropdownMenuItem(
                    text = {Text("Delete Movie")},
                    onClick = {
                        showDeleteDialog = true
                        showMenu = false
                    }
                )
            }
        }
    }
    if (showDeleteDialog)
    {
        AlertDialog(
            onDismissRequest = {showDeleteDialog = false},
            title = {Text("Confirm Deletion")},
            text = {Text("Are you sure you want to delete ${movie.title}?")},
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMovie(movie)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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