package com.example.gymko.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymko.data.local.entity.ExerciseEntity
import com.example.gymko.data.local.entity.SetEntity

data class SetWithExercise(
    @Embedded val set: SetEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)
