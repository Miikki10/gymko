package com.example.gymko

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymko.ui.mvi.ActiveWorkoutIntent
import com.example.gymko.ui.mvi.ActiveWorkoutViewModel
import com.example.gymko.ui.mvi.MainIntent
import com.example.gymko.ui.mvi.MainViewModel
import com.example.gymko.ui.mvi.TrainIntent
import com.example.gymko.ui.navigation.NavGraph
import com.example.gymko.ui.navigation.Screen
import com.example.gymko.ui.theme.AntonFontFamily
import com.example.gymko.ui.theme.GymKoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymKoTheme {
                GymKoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymKoApp(
    viewModel: MainViewModel = viewModel(),
    activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel()
) {
    val permissionsToRequest = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        permissionsToRequest.add(Manifest.permission.BODY_SENSORS)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    
    val activeState by activeWorkoutViewModel.state.collectAsState()

    val screens = listOf(
        Screen.Overview,
        Screen.Train,
        Screen.History,
        Screen.Settings
    )

    val showBars = currentRoute != Screen.Onboarding.route && 
                   currentRoute != null && 
                   !currentRoute.startsWith("active_workout")

    Scaffold(
        topBar = {
            Column {
                if (showBars) {
                    CenterAlignedTopAppBar(
                        windowInsets = WindowInsets.statusBars,
                        title = {
                            Text(
                                text = "GYMKO",
                                fontFamily = AntonFontFamily,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
                
                // Sticky Active Workout Bar
                if (activeState.workout != null && (activeState.isHidden || showBars)) {
                    val h = activeState.durationSeconds / 3600
                    val m = (activeState.durationSeconds % 3600) / 60
                    val s = activeState.durationSeconds % 60
                    val time = String.format("%02d:%02d:%02d", h, m, s)
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                activeWorkoutViewModel.onIntent(ActiveWorkoutIntent.ShowWorkout)
                                navController.navigate(Screen.ActiveWorkout.createRoute(activeState.workout!!.workout.id)) 
                            },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVE SESSION: ${activeState.workout!!.workout.name.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = time,
                                fontFamily = AntonFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (showBars) {
                NavigationBar(
                    /*
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))*/

                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier
                        .navigationBarsPadding()
                        //bottom je bio 24
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    screens.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(imageVector = it, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                viewModel.onIntent(MainIntent.NavigateTo(screen))
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    val startRoute = navController.graph.startDestinationRoute ?: Screen.Overview.route
                                    popUpTo(startRoute) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.secondaryContainer,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                //.padding(innerPadding),
                .padding(top = innerPadding.calculateTopPadding(),
                    bottom = 0.dp),

            color = MaterialTheme.colorScheme.background
        ) {
            //NavGraph(navController = navController)
            // KLJUČNA PROMJENA: Koristimo Box za preklapanje sadržaja i gradienta
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Sadržaj (NavGraph)
                NavGraph(navController = navController)

                // 2. POVEĆANI GRADIENT SCRIM
                if (showBars) {
                    val backgroundColor = MaterialTheme.colorScheme.background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // Povećano na 220dp kako bi blijeđenje počelo puno ranije
                            .height(220.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    // 0.0f (vrh) do 0.3f (30% visine) je potpuno prozirno,
                                    // a onda se od 30% do 100% polako prelijeva u punu boju pozadine.
                                    // To stvara iznimno gladak i dugačak prijelaz.
                                    0.0f to Color.Transparent,
                                    0.4f to backgroundColor.copy(alpha = 0.5f),
                                    0.45f to backgroundColor,
                                    1.0f to backgroundColor
                                )
                            )
                    )
                }
            }
        }
    }
}
