package com.example.gymko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gymko.data.model.WorkoutStatus

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val status: WorkoutStatus = WorkoutStatus.INACTIVE,
    val timestamp: Long = System.currentTimeMillis()
)
