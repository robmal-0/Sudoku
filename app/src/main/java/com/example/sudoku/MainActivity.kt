package com.example.sudoku

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.sudoku.data.Firestore
import com.example.sudoku.ui.theme.SudokuTheme
import com.example.sudoku.ui.theme.SudokuViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


enum class SelectedScreen(@StringRes val title: Int) {
    Home(title = R.string.home),
    Finished(title = R.string.finished),
    Game(title = R.string.sudoku),
    Picker(title = R.string.picker),
    Draft(title = R.string.draft),
    Editor(title = R.string.editor),
    Login(title = R.string.login)
}

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: Firestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = Firestore(Firebase.firestore, auth)
        println("ON CREATE:")
        println(Firebase.app)


        setContent {
            SudokuTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(modifier = Modifier.fillMaxSize(), auth = auth, db = db)
                }
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) {}

    fun createSignInIntent() {
        val providers = arrayListOf(
            EmailBuilder()
                .build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
    viewModel: SudokuViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    auth: FirebaseAuth,
    db: com.example.sudoku.data.Firestore
) {
    val uiState by viewModel.uiState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = SelectedScreen.valueOf(
        backStackEntry?.destination?.route ?: SelectedScreen.Home.name
    )

    val signedIn = remember { mutableStateOf(false) }

    auth.addAuthStateListener {
        signedIn.value = auth.currentUser !== null
    }

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
                    if (currentScreen !in listOf(SelectedScreen.Home)) {
                        IconButton(onClick = {
                            if (currentScreen == SelectedScreen.Game && uiState.testing) {
                                viewModel.stopTesting()
                                navController.navigate(SelectedScreen.Editor.name)
                            } else {
                                navController.navigate(SelectedScreen.Home.name)
                            }
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
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            composable(route = SelectedScreen.Home.name) {

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(508f / 339f),
                        painter = rememberAsyncImagePainter(model = stringResource(R.string.mainImage)),
                        contentDescription = "Sudoku image"
                    )

                    Button(onClick = {
                        viewModel.generateGameList(db)
                        navController.navigate(SelectedScreen.Picker.name)
                    }) {
                        Text(stringResource(id = R.string.play))
                    }
                    if (signedIn.value) {
                        Button(onClick = {
                            navController.navigate(SelectedScreen.Draft.name)
                        }) {
                            Text(stringResource(id = R.string.create))
                        }
                        Text("Hello ${auth.currentUser!!.displayName}")
                        Button(onClick = {
                            auth.signOut()
                        }) {
                            Text(stringResource(id = R.string.logout))
                        }
                    } else {
                        val activity = (LocalContext.current as MainActivity)
                        Button(onClick = {
                            activity.createSignInIntent()
                        }) {
                            Text(stringResource(id = R.string.login))
                        }
                    }
                }
            }
            composable(route = SelectedScreen.Picker.name) {
                GameList(
                    viewModel,
                    navController
                )
            }
            composable(route = SelectedScreen.Draft.name) {
                DraftList(
                    viewModel,
                    navController
                )
            }
            composable(route = SelectedScreen.Game.name) {
                GameBoard(viewModel = viewModel, navController = navController, db = db)
            }
            composable(route = SelectedScreen.Editor.name) {
                BoardEditor(
                    viewModel,
                    navController,
                    db = db
                )
            }
        }
    }
}