package com.example.gymko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gymko.data.model.UnitSystem

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1, // Only one user for now
    val name: String,
    val height: Double,
    val weight: Double,
    val unitSystem: UnitSystem
)
