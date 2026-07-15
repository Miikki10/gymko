package com.example.gymko.data.local.database

import androidx.room.TypeConverter
import com.example.gymko.data.model.UnitSystem
import com.example.gymko.data.model.WorkoutStatus

class Converters {
    @TypeConverter
    fun fromWorkoutStatus(status: WorkoutStatus): String = status.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)

    @TypeConverter
    fun fromUnitSystem(unit: UnitSystem): String = unit.name

    @TypeConverter
    fun toUnitSystem(value: String): UnitSystem = UnitSystem.valueOf(value)
}
