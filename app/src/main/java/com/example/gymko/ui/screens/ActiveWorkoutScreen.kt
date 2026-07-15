package com.example.gymko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymko.data.local.relation.SetWithExercise
import com.example.gymko.ui.mvi.*
import com.example.gymko.ui.theme.AntonFontFamily
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: Long,
    state: ActiveWorkoutState,
    onIntent: (ActiveWorkoutIntent) -> Unit,
    effect: SharedFlow<ActiveWorkoutEffect>,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(workoutId) {
        onIntent(ActiveWorkoutIntent.LoadWorkout(workoutId))
    }

    LaunchedEffect(Unit) {
        effect.collect { effect ->
            when (effect) {
                ActiveWorkoutEffect.NavigateToOverview -> onNavigateBack()
                ActiveWorkoutEffect.WorkoutAutoClosed -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        state.workout?.workout?.name?.uppercase() ?: "ACTIVE WORKOUT", 
                        fontFamily = AntonFontFamily,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(ActiveWorkoutIntent.HideWorkout) }) {
                        Icon(Icons.Default.Close, contentDescription = "Hide", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* more options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = { onIntent(ActiveWorkoutIntent.EndWorkout) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("END WORKOUT", fontFamily = AntonFontFamily, fontSize = 20.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DurationCard(seconds = state.durationSeconds)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onIntent(ActiveWorkoutIntent.ToggleAllSets) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val allChecked = state.workout?.sets?.all { state.completedSets.contains(it.set.id) } == true
                        Text(if (allChecked) "DESELECT ALL" else "SELECT ALL SETS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACTIVE SESSION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    }
                }
            }

            val groupedSets = state.workout?.sets?.groupBy { it.set.exerciseId } ?: emptyMap()
            
            items(groupedSets.values.toList()) { exerciseSets ->
                val exercise = exerciseSets.first().exercise
                ActiveExerciseCard(
                    exerciseName = exercise.name,
                    muscleGroups = exercise.muscles,
                    sets = exerciseSets,
                    completedSets = state.completedSets,
                    onToggleSet = { onIntent(ActiveWorkoutIntent.ToggleSet(it)) },
                    onCheckAll = { onIntent(ActiveWorkoutIntent.ToggleExerciseSets(exercise.id)) }
                )
            }

            // Ovdje više ne trebamo END WORKOUT gumb jer je u bottomBar-u
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DurationCard(seconds: Long) {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    val time = String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("DURATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(time, fontFamily = AntonFontFamily, fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ActiveExerciseCard(
    exerciseName: String,
    muscleGroups: String,
    sets: List<SetWithExercise>,
    completedSets: Set<Long>,
    onToggleSet: (Long) -> Unit,
    onCheckAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(exerciseName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(muscleGroups.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onCheckAll) {
                    val allChecked = sets.all { completedSets.contains(it.set.id) }
                    Text(if (allChecked) "UNCHECK ALL" else "CHECK ALL", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                    Text("SET", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("WEIGHT (KG)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("REPS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("DONE", modifier = Modifier.width(40.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Right)
                }

                sets.forEachIndexed { index, setWithReps ->
                    val isDone = completedSets.contains(setWithReps.set.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${setWithReps.set.weight}", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text("${setWithReps.set.reps}", modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                        
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(32.dp)
                                .clickable { onToggleSet(setWithReps.set.id) },
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, if (isDone) MaterialTheme.colorScheme.secondaryContainer else Color.White.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
