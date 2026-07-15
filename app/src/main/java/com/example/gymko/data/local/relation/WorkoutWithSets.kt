package com.example.gymko.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymko.data.local.entity.SetEntity
import com.example.gymko.data.local.entity.WorkoutEntity

data class WorkoutWithSets(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = SetEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val sets: List<SetWithExercise>
)
