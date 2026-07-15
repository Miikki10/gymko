package com.example.gymko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.navigation.NavController
import com.example.gymko.data.local.entity.ExerciseEntity
import com.example.gymko.data.local.entity.WorkoutEntity
import com.example.gymko.data.local.relation.WorkoutWithSets
import com.example.gymko.ui.mvi.*
import com.example.gymko.ui.navigation.Screen
import com.example.gymko.ui.theme.AntonFontFamily
import kotlinx.coroutines.flow.SharedFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainScreen(
    state: TrainState,
    onIntent: (TrainIntent) -> Unit,
    effect: SharedFlow<TrainEffect>,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        effect.collect { effect ->
            when (effect) {
                TrainEffect.NavigateToCreateWorkout -> navController.navigate(Screen.CreateWorkout.route)
                TrainEffect.NavigateBack -> navController.popBackStack()
                is TrainEffect.StartWorkoutSession -> navController.navigate(Screen.ActiveWorkout.createRoute(effect.workoutId))
            }
        }
    }

    // Scaffold automatski rješava prostor koji zauzima bottom bar
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Primjenjujemo padding od Scaffold-a
                .padding(16.dp) // Dodajemo tvoj originalni padding oko ekrana
        ) {
            // Searchbar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(TrainIntent.UpdateSearchQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search exercises or routines...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(modifier = Modifier.fillMaxWidth()) {
                TabButton(
                    text = "WORKOUTS",
                    isSelected = state.selectedTab == TrainTab.Workouts,
                    onClick = { onIntent(TrainIntent.SelectTab(TrainTab.Workouts)) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "EXERCISES",
                    isSelected = state.selectedTab == TrainTab.Exercises,
                    onClick = { onIntent(TrainIntent.SelectTab(TrainTab.Exercises)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when (state.selectedTab) {
                TrainTab.Workouts -> WorkoutsTab(state, onIntent)
                TrainTab.Exercises -> ExercisesTab(state, onIntent)
            }
        }
    } // Ovdje završava Scaffold

    // Reusable Dialogs (ostaju izvan Scaffolda, na dnu TrainScreen-a)
    state.exerciseDialog?.let { dialogState ->
        ExerciseDialog(
            state = dialogState,
            onNameChange = { onIntent(TrainIntent.UpdateExerciseDialogName(it)) },
            onDescriptionChange = { onIntent(TrainIntent.UpdateExerciseDialogDescription(it)) },
            onCategoryToggle = { onIntent(TrainIntent.ToggleExerciseDialogCategory(it)) },
            onMuscleSearchChange = { onIntent(TrainIntent.UpdateExerciseDialogMuscleSearch(it)) },
            onMuscleToggle = { onIntent(TrainIntent.ToggleExerciseDialogMuscle(it)) },
            onSave = { onIntent(TrainIntent.SaveExercise) },
            onDismiss = { onIntent(TrainIntent.DismissExerciseDialog) }
        )
    }

    state.workoutDialog?.let { dialogState ->
        WorkoutDialog(
            state = dialogState,
            onIntent = onIntent
        )
    }

    state.deleteExerciseConfirmation?.let { exercise ->
        DeleteConfirmationDialog(
            title = "Delete Exercise",
            message = "Are you sure you want to permanently delete '${exercise.name}'?",
            onConfirm = { onIntent(TrainIntent.DeleteExercise) },
            onDismiss = { onIntent(TrainIntent.DismissDeleteConfirmation) }
        )
    }

    state.deleteWorkoutConfirmation?.let { workout ->
        DeleteConfirmationDialog(
            title = "Delete Workout",
            message = "Are you sure you want to permanently delete '${workout.name}'?",
            onConfirm = { onIntent(TrainIntent.DeleteWorkout) },
            onDismiss = { onIntent(TrainIntent.DismissDeleteConfirmation) }
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
        )
    }
}

@Composable
fun ExercisesTab(state: TrainState, onIntent: (TrainIntent) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            bottom = 120.dp
        )
    ) {
        item {
            AddButton(
                title = "Add Custom Exercise",
                subtitle = "Create your own movements",
                onClick = { onIntent(TrainIntent.ShowAddExerciseDialog) }
            )
        }

        val grouped = state.exercises.groupBy { it.muscles.uppercase() }
        grouped.forEach { (muscle, exercises) ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = muscle,
                        fontFamily = AntonFontFamily,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${exercises.size} EXERCISES",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onEdit = { onIntent(TrainIntent.ShowEditExerciseDialog(exercise)) },
                    onDelete = { onIntent(TrainIntent.ShowDeleteExerciseConfirmation(exercise)) }
                )
            }

        }
    }
}

@Composable
fun WorkoutsTab(state: TrainState, onIntent: (TrainIntent) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                bottom = 120.dp
                )
    ) {
        item {
            AddButton(
                title = "Create New Workout",
                subtitle = "Build your custom training routine",
                onClick = { onIntent(TrainIntent.ShowCreateWorkoutScreen) },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconBorderColor = MaterialTheme.colorScheme.background
            )
        }

        item {
            Text(
                text = "MY WORKOUTS",
                fontFamily = AntonFontFamily,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(state.workouts) { workoutWithSets ->
            WorkoutCard(
                workoutWithSets = workoutWithSets,
                onEdit = { onIntent(TrainIntent.ShowEditWorkoutScreen(workoutWithSets)) },
                onStart = { onIntent(TrainIntent.StartWorkout(workoutWithSets.workout.id)) },
                onDelete = { onIntent(TrainIntent.ShowDeleteWorkoutConfirmation(workoutWithSets.workout)) }
            )
        }
    }
}

@Composable
fun AddButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconBorderColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconBorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = contentColor)
                Text(text = subtitle, fontSize = 12.sp, color = contentColor.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: ExerciseEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator line
            Box(modifier = Modifier.width(4.dp).height(48.dp).background(MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.name, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Badge(exercise.category)
                    Badge(exercise.muscles)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun Badge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WorkoutCard(workoutWithSets: WorkoutWithSets, onEdit: () -> Unit, onStart: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = workoutWithSets.workout.name, fontFamily = AntonFontFamily, fontSize = 24.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconText(Icons.AutoMirrored.Filled.List, "${workoutWithSets.sets.size} Exercises")
                        IconText(Icons.Default.Schedule, "65 min") // Hardcoded for now
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("START WORKOUT")
            }
        }
    }
}

@Composable
fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDialog(
    state: ExerciseDialogState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryToggle: (String) -> Unit,
    onMuscleSearchChange: (String) -> Unit,
    onMuscleToggle: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val predefinedCategories = listOf("Push", "Pull", "Legs", "Upper", "Lower")
    val allMuscles = listOf(
        "Chest", "Back", "Shoulders", "Biceps", "Triceps", 
        "Quads", "Hamstrings", "Calves", "Abs", "Forearms", 
        "Glutes", "Traps", "Lats", "Obliques", "Lower Back"
    ).sorted()
    
    val filteredMuscles = allMuscles.filter { 
        it.contains(state.muscleSearchQuery, ignoreCase = true) 
    }

    val isSaveEnabled = state.name.isNotBlank() && 
                       state.selectedCategories.isNotEmpty() && 
                       state.selectedMuscles.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.id == null) "Add Custom Exercise" else "Edit Exercise") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Categories
                Text(
                    text = "Category * (Select one or more)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    predefinedCategories.forEach { category ->
                        FilterChip(
                            selected = state.selectedCategories.contains(category),
                            onClick = { onCategoryToggle(category) },
                            label = { Text(category) }
                        )
                    }
                }

                // Muscle Groups
                Text(
                    text = "Muscle Group * (Search and select)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Selected Muscles as Chips
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedMuscles.forEach { muscle ->
                        InputChip(
                            selected = true,
                            onClick = { onMuscleToggle(muscle) },
                            label = { Text(muscle) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = state.muscleSearchQuery,
                    onValueChange = onMuscleSearchChange,
                    placeholder = { Text("Search muscles...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (state.muscleSearchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onMuscleSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    } else null,
                    singleLine = true
                )

                // Search Results
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredMuscles.filter { !state.selectedMuscles.contains(it) }.forEach { muscle ->
                        FilterChip(
                            selected = false,
                            onClick = { onMuscleToggle(muscle) },
                            label = { Text(muscle) }
                        )
                    }
                }
                
                if (state.name.isBlank() || state.selectedCategories.isEmpty() || state.selectedMuscles.isEmpty()) {
                    Text(
                        text = "* Required fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = isSaveEnabled
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WorkoutDialog(state: WorkoutDialogState, onIntent: (TrainIntent) -> Unit) {
    // ... logic ...
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    state: TrainState,
    onIntent: (TrainIntent) -> Unit,
    effect: SharedFlow<TrainEffect>,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        effect.collect { effect ->
            if (effect == TrainEffect.NavigateBack) onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CREATE WORKOUT", fontFamily = AntonFontFamily) },
                navigationIcon = {
                    IconButton(onClick = { onIntent(TrainIntent.CancelCreateWorkout) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onIntent(TrainIntent.SaveNewWorkout) },
                        enabled = state.createWorkoutName.isNotBlank() && state.createWorkoutExercises.isNotEmpty()
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                OutlinedTextField(
                    value = state.createWorkoutName,
                    onValueChange = { onIntent(TrainIntent.UpdateCreateWorkoutName(it)) },
                    label = { Text("Workout Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            itemsIndexed(state.createWorkoutExercises) { exIndex, exState ->
                ExerciseInWorkoutCard(
                    exState = exState,
                    exIndex = exIndex,
                    onIntent = onIntent
                )
            }

            item {
                Button(
                    onClick = { onIntent(TrainIntent.ShowAddExerciseToWorkoutDialog) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD EXERCISE")
                }
            }
        }
    }

    if (state.showAddExerciseToWorkoutDialog) {
        AddExerciseToWorkoutDialog(state, onIntent)
    }
}

@Composable
fun ExerciseInWorkoutCard(
    exState: WorkoutExerciseState,
    exIndex: Int,
    onIntent: (TrainIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = exState.exercise.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = { onIntent(TrainIntent.RemoveExerciseFromWorkout(exIndex)) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sets Header
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text("SET", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.labelSmall)
                Text("WEIGHT (KG)", modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.labelSmall)
                Text("REPS", modifier = Modifier.weight(0.4f), style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(40.dp)) // space for delete icon
            }

            exState.sets.forEachIndexed { setIndex, setState ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${setIndex + 1}", modifier = Modifier.weight(0.2f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    
                    OutlinedTextField(
                        value = setState.weight,
                        onValueChange = { onIntent(TrainIntent.UpdateSetWeight(exIndex, setIndex, it)) },
                        modifier = Modifier.weight(0.4f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    OutlinedTextField(
                        value = setState.reps,
                        onValueChange = { onIntent(TrainIntent.UpdateSetReps(exIndex, setIndex, it)) },
                        modifier = Modifier.weight(0.4f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )

                    IconButton(
                        onClick = { onIntent(TrainIntent.RemoveSetFromExercise(exIndex, setIndex)) },
                        enabled = exState.sets.size > 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove Set", tint = if (exState.sets.size > 1) MaterialTheme.colorScheme.error else Color.Gray)
                    }
                }
            }

            TextButton(
                onClick = { onIntent(TrainIntent.AddSetToExercise(exIndex)) },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ADD SET")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseToWorkoutDialog(state: TrainState, onIntent: (TrainIntent) -> Unit) {
    val filteredExercises = state.exercises.filter {
        it.name.contains(state.exerciseSearchQuery, ignoreCase = true) ||
        it.muscles.contains(state.exerciseSearchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = { onIntent(TrainIntent.DismissAddExerciseToWorkoutDialog) },
        title = { Text("Select Exercise") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                OutlinedTextField(
                    value = state.exerciseSearchQuery,
                    onValueChange = { onIntent(TrainIntent.UpdateExerciseSearchQuery(it)) },
                    placeholder = { Text("Search exercises...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        Card(
                            onClick = { onIntent(TrainIntent.AddExerciseToWorkout(exercise)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = exercise.name, fontWeight = FontWeight.Bold)
                                Text(text = exercise.muscles, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onIntent(TrainIntent.DismissAddExerciseToWorkoutDialog) }) {
                Text("Cancel")
            }
        }
    )
}
