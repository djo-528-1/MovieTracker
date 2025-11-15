package com.example.movietracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movietracker.ui.theme.MovieTrackerTheme
import kotlinx.coroutines.launch

class MainViewModel : ViewModel()
{
    val sections = listOf("Watching", "Want to Watch", "Watched")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    MovieTrackerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieTrackerScreen(viewModel: MainViewModel = viewModel()){
    val pagerState = rememberPagerState(pageCount = {viewModel.sections.size})
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex by remember {derivedStateOf {pagerState.currentPage}}

    Column(modifier = Modifier.fillMaxSize()){
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ){
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
                ){
                    Text(text = label)
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().weight(1f)
        ){
            page -> SectionContent(title = viewModel.sections[page])
        }
    }
}

@Composable
fun SectionContent(title: String){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        // Потом надо добавить UI для списка фильмов
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun MovieTrackerScreenPreview(){
    MovieTrackerTheme {
        MovieTrackerScreen()
    }
}