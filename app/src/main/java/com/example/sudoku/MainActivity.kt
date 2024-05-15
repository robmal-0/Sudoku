package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.sudoku.ui.theme.SudokuTheme
import com.example.sudoku.ui.theme.SudokuViewModel

enum class SelectedScreen(@StringRes val title: Int) {
    Home(title = R.string.home),
    Finished(title = R.string.finished),
    Game(title = R.string.sudoku),
    Picker(title = R.string.picker),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: SudokuViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = SelectedScreen.valueOf(
        backStackEntry?.destination?.route ?: SelectedScreen.Home.name
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(currentScreen.title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.hsl(155f, 0.3f, 0.5f)
                ),
                navigationIcon = {
                    if (currentScreen != SelectedScreen.Home) {
                        IconButton(onClick = {
                            navController.navigate(SelectedScreen.Home.name)
                        }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back arrow")
                        }
                    }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = SelectedScreen.Home.name,
            modifier = Modifier.padding(it)
        ) {
            composable(route = SelectedScreen.Home.name) {

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(modifier = Modifier.fillMaxWidth().height(260.dp),painter = rememberAsyncImagePainter(model = stringResource(R.string.mainImage)), contentDescription = "Sudoku image")

                    Button(onClick = {
                        viewModel.generateGameList()
                        navController.navigate(SelectedScreen.Picker.name)
                    }) {
                        Text("Play")
                    }
                    Button(onClick = {
                        navController.navigate(SelectedScreen.Game.name)
                    }) {
                        Text("Create")
                    }
                }
            }
            composable(route = SelectedScreen.Picker.name) {
                GameList(
                    viewModel,
                    navController
                )
            }
            composable(route = SelectedScreen.Game.name) {
                GameBoard(viewModel = viewModel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SudokuTheme {
        App()
    }
}