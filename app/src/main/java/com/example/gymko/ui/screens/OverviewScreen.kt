package com.example.gymko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymko.data.local.relation.WorkoutWithSets
import com.example.gymko.ui.mvi.*
import com.example.gymko.ui.theme.AntonFontFamily
import kotlinx.coroutines.flow.SharedFlow
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.gymko.ui.navigation.Screen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    state: OverviewState,
    onIntent: (OverviewIntent) -> Unit,
    effect: SharedFlow<OverviewEffect>,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        effect.collect { effect ->
            when (effect) {
                OverviewEffect.NavigateToCreateWorkout -> navController.navigate(Screen.CreateWorkout.route)
                OverviewEffect.NavigateToTrain -> {
                    navController.navigate(Screen.Train.route) {
                        popUpTo(Screen.Overview.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                OverviewEffect.NavigateToHistory -> {
                    navController.navigate(Screen.History.route) {
                        popUpTo(Screen.Overview.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                is OverviewEffect.StartWorkoutSession -> {
                    navController.navigate(Screen.ActiveWorkout.createRoute(effect.workoutId))
                }
            }
        }
    }

    Scaffold(

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            //inace je bilo 24*****
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Start Card
            item {
                StartCard(
                    selectedWorkout = state.selectedWorkout,
                    onCreateWorkout = { onIntent(OverviewIntent.CreateFirstWorkout) },
                    onStartWorkout = { id -> onIntent(OverviewIntent.StartWorkout(id)) }
                )
            }

            // Your Workouts Section
            item {
                WorkoutsSection(
                    workouts = state.workouts,
                    onSelectWorkout = { onIntent(OverviewIntent.SelectWorkout(it)) },
                    onSeeAll = { onIntent(OverviewIntent.SeeAllWorkouts) },
                    onCreateNew = { onIntent(OverviewIntent.CreateFirstWorkout) }
                )
            }

            // Add New Exercise Button
            item {
                AddNewExerciseButton(onClick = { onIntent(OverviewIntent.ShowAddExerciseDialog) })
            }

            // Recent Activity Section
            item {
                RecentActivitySection(
                    activities = state.recentActivity,
                    onViewAll = { onIntent(OverviewIntent.SeeHistory) }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // Reusable Exercise Dialog
    state.exerciseDialog?.let { dialogState ->
        ExerciseDialog(
            state = dialogState,
            onNameChange = { onIntent(OverviewIntent.UpdateExerciseDialogName(it)) },
            onDescriptionChange = { onIntent(OverviewIntent.UpdateExerciseDialogDescription(it)) },
            onCategoryToggle = { onIntent(OverviewIntent.ToggleExerciseDialogCategory(it)) },
            onMuscleSearchChange = { onIntent(OverviewIntent.UpdateExerciseDialogMuscleSearch(it)) },
            onMuscleToggle = { onIntent(OverviewIntent.ToggleExerciseDialogMuscle(it)) },
            onSave = { onIntent(OverviewIntent.SaveExercise) },
            onDismiss = { onIntent(OverviewIntent.DismissExerciseDialog) }
        )
    }
}

@Composable
fun StartCard(
    selectedWorkout: WorkoutWithSets?,
    onCreateWorkout: () -> Unit,
    onStartWorkout: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp),
                tint = Color.White.copy(alpha = 0.05f)
            )

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (selectedWorkout == null) "Welcome to GymKo" else "Selected Workout",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedWorkout?.workout?.name?.uppercase() ?: "READY TO BUILD YOUR LEGACY?",
                        fontFamily = AntonFontFamily,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 36.sp
                    )
                    
                    if (selectedWorkout != null) {
                        val exerciseNames = selectedWorkout.sets.take(4).map { it.exercise.name }.joinToString(", ")
                        Text(
                            text = if (exerciseNames.isNotEmpty()) "Next up: $exerciseNames" else "Let's crush this workout!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Your journey starts with your first rep. Set up your workout and track your progress.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (selectedWorkout == null) onCreateWorkout()
                        else onStartWorkout(selectedWorkout.workout.id)
                    },
                    modifier = Modifier.padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedWorkout == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (selectedWorkout == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (selectedWorkout == null) Icons.Default.AddCircle else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedWorkout == null) "CREATE NEW WORKOUT" else "START WORKOUT",
                        fontFamily = AntonFontFamily,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutsSection(
    workouts: List<WorkoutWithSets>,
    onSelectWorkout: (WorkoutWithSets) -> Unit,
    onSeeAll: () -> Unit,
    onCreateNew: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Workouts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (workouts.isEmpty()) {
            EmptySectionCard(
                message = "Organize your training days to stay consistent.",
                title = "No Active Split",
                buttonText = "CREATE WORKOUT",
                onButtonClick = onCreateNew
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                workouts.forEach { workout ->
                    WorkoutOverviewCard(workout = workout, onClick = { onSelectWorkout(workout) })
                }
                
                TextButton(
                    onClick = onSeeAll,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("SEE ALL WORKOUTS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WorkoutOverviewCard(workout: WorkoutWithSets, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = workout.workout.name, fontWeight = FontWeight.Bold)
                Text(text = "${workout.sets.size} Exercises", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmptySectionCard(message: String, title: String, buttonText: String, onButtonClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onButtonClick) {
                Text(buttonText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddNewExerciseButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "ADD NEW EXERCISE",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<WorkoutWithSets>, onViewAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "VIEW ALL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "No Activity Yet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = "Complete your first workout to see your progress here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activities.forEach { activity ->
                    HistoryItem(workout = activity)
                }
            }
        }
    }
}
