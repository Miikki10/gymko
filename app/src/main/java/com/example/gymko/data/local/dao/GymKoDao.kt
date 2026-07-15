package com.example.gymko.data.local.dao

import androidx.room.*
import com.example.gymko.data.local.entity.*
import com.example.gymko.data.local.relation.WorkoutWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface GymKoDao {

    // User
    @Query("SELECT * FROM users WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    // Exercises
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    // Workouts
    @Transaction
    @Query("SELECT * FROM workouts")
    fun getAllWorkoutsWithSets(): Flow<List<WorkoutWithSets>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCompletedWorkouts(limit: Int = 10): Flow<List<WorkoutWithSets>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkoutById(workoutId: Long): Flow<WorkoutWithSets?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutWithSetsById(workoutId: Long): WorkoutWithSets?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("UPDATE workouts SET status = 'INACTIVE' WHERE status != 'COMPLETED'")
    suspend fun deactivateAllWorkouts()

    @Transaction
    suspend fun startWorkoutSession(templateWorkoutId: Long): Long {
        deactivateAllWorkouts()
        val template = getWorkoutWithSetsById(templateWorkoutId) ?: return -1
        
        val newWorkoutId = insertWorkout(
            WorkoutEntity(
                name = template.workout.name,
                status = com.example.gymko.data.model.WorkoutStatus.ACTIVE,
                timestamp = System.currentTimeMillis()
            )
        )
        
        template.sets.forEach { setWithExercise ->
            insertSet(
                SetEntity(
                    workoutId = newWorkoutId,
                    exerciseId = setWithExercise.set.exerciseId,
                    weight = setWithExercise.set.weight,
                    reps = setWithExercise.set.reps,
                    order = setWithExercise.set.order
                )
            )
        }
        
        return newWorkoutId
    }

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    // Sets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(setEntity: SetEntity): Long
}
