package com.example.movietracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.movietracker.MainViewModel
import com.example.movietracker.data.MovieEntity

@Composable
fun AddMovieScreen(navController: NavController, viewModel: MainViewModel)
{
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(viewModel.sections.first()) }
    val sections = viewModel.sections
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it},
            label = { Text("Film title")},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Release year") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
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
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = {
                if (title.isNotBlank()){
                    val newMovie = MovieEntity(
                        title = title,
                        year = year.ifBlank { null },
                        category = selectedCategory
                    )
                    viewModel.addMovie(newMovie)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save film")
        }
    }
}