package com.example.gymko.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymko.data.model.UnitSystem
import com.example.gymko.ui.mvi.SettingsIntent
import com.example.gymko.ui.mvi.SettingsState
import com.example.gymko.ui.theme.AntonFontFamily

@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SETTINGS",
            fontFamily = AntonFontFamily,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(SettingsIntent.UpdateName(it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.height,
            onValueChange = { onIntent(SettingsIntent.UpdateHeight(it)) },
            label = { Text(if (state.unitSystem == UnitSystem.SI) "Height (cm)" else "Height (in)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.weight,
            onValueChange = { onIntent(SettingsIntent.UpdateWeight(it)) },
            label = { Text(if (state.unitSystem == UnitSystem.SI) "Weight (kg)" else "Weight (lbs)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Unit System: ${state.unitSystem.name}")
            Button(onClick = { onIntent(SettingsIntent.ToggleUnitSystem) }) {
                Text("Switch to ${if (state.unitSystem == UnitSystem.SI) "Imperial" else "SI"}")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onIntent(SettingsIntent.SaveProfile) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE CHANGES")
        }
    }
}
