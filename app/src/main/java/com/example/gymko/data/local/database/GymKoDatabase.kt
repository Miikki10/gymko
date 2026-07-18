package com.example.gymko.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gymko.data.local.dao.GymKoDao
import com.example.gymko.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        SetEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymKoDatabase : RoomDatabase() {
    abstract fun gymKoDao(): GymKoDao

    companion object {
        @Volatile
        private var INSTANCE: GymKoDatabase? = null

        fun getDatabase(context: Context): GymKoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymKoDatabase::class.java,
                    "gymko_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.gymKoDao().insertExercises(DEFAULT_EXERCISES)
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Osiguravamo da su vježbe tu čak i ako je baza već kreirana
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.gymKoDao().insertExercises(DEFAULT_EXERCISES)
                    }
                }
            }
        }

        private val DEFAULT_EXERCISES = listOf(
            ExerciseEntity(name = "Push-Up", category = "push", muscles = "chest", description = "A classic bodyweight exercise targeting the chest, shoulders, and triceps by pressing the body up from the floor."),
            ExerciseEntity(name = "Bench Press", category = "push", muscles = "chest", description = "A compound barbell exercise performed on a flat bench to build strength and mass in the chest and triceps."),
            ExerciseEntity(name = "Incline Dumbbell Press", category = "push", muscles = "chest", description = "Dumbbell press performed on an incline bench to emphasize the upper chest and front shoulders."),
            ExerciseEntity(name = "Dips", category = "push", muscles = "chest", description = "A powerhouse bodyweight or weighted movement on parallel bars targeting the lower chest and triceps."),
            ExerciseEntity(name = "Diamond Push-Up", category = "push", muscles = "triceps", description = "A push-up variation with hands close together, shifting the focus heavily onto the triceps and inner chest."),
            ExerciseEntity(name = "Overhead Press", category = "push", muscles = "shoulders", description = "A fundamental standing barbell press that builds raw power and size in the shoulders and triceps."),
            ExerciseEntity(name = "Handstand Push-Up", category = "push", muscles = "shoulders", description = "An advanced street workout movement performed against a wall or freestanding, targeting shoulders and triceps."),
            ExerciseEntity(name = "Pike Push-Up", category = "push", muscles = "shoulders", description = "An accessible bodyweight alternative to handstand pushups, focusing on shoulder strength."),
            ExerciseEntity(name = "Lateral Raise", category = "push", muscles = "shoulders", description = "Dumbbell isolation movement to build width by targeting the lateral head of the shoulders."),
            ExerciseEntity(name = "Triceps Pushdown", category = "push", muscles = "triceps", description = "Cable machine isolation exercise to lock in and pump the triceps."),
            ExerciseEntity(name = "Skull Crusher", category = "push", muscles = "triceps", description = "An isolation exercise using an EZ bar or dumbbells to target the long head of the triceps."),
            ExerciseEntity(name = "Pull-Up", category = "pull", muscles = "lats", description = "The king of back exercises, lifting bodyweight on a bar with an overhand grip to build wide lats."),
            ExerciseEntity(name = "Chin-Up", category = "pull", muscles = "biceps", description = "A pull-up variation using an underhand grip, shifting a massive portion of the load to the biceps and lats."),
            ExerciseEntity(name = "Barbell Row", category = "pull", muscles = "back", description = "A heavy bent-over rowing movement targeting total back thickness, including lats and middle back."),
            ExerciseEntity(name = "One-Arm Dumbbell Row", category = "pull", muscles = "back", description = "A unilateral rowing exercise allowing a deep stretch and hard contraction of the lats and back."),
            ExerciseEntity(name = "Australian Pull-Up", category = "pull", muscles = "back", description = "Also known as bodyweight rows, performed on a low bar to build foundational back strength."),
            ExerciseEntity(name = "Lat Pulldown", category = "pull", muscles = "lats", description = "Cable machine exercise that mimics the pull-up motion, perfect for isolating the lats."),
            ExerciseEntity(name = "Face Pull", category = "pull", muscles = "shoulders", description = "Cable exercise using a rope to target the rear shoulders, traps, and upper back for shoulder health."),
            ExerciseEntity(name = "Barbell Bicep Curl", category = "pull", muscles = "biceps", description = "The classic barbell curl designed to build strength and size in the biceps."),
            ExerciseEntity(name = "Hammer Curl", category = "pull", muscles = "forearms", description = "Dumbbell curl performed with a neutral grip to target the biceps, brachialis, and forearms."),
            ExerciseEntity(name = "Preacher Curl", category = "pull", muscles = "biceps", description = "An isolated bicep curl performed on a slanted bench to eliminate momentum and maximize tension."),
            ExerciseEntity(name = "Deadlift", category = "legs", muscles = "lower back", description = "The ultimate compound lift targeting the entire posterior chain, including lower back, glutes, and hamstrings."),
            ExerciseEntity(name = "Barbell Back Squat", category = "legs", muscles = "quads", description = "The foundational leg exercise for overall lower body strength, primarily targeting the quads and glutes."),
            ExerciseEntity(name = "Bulgarian Split Squat", category = "legs", muscles = "quads", description = "A brutal single-leg squat with one foot elevated, focusing on quads, glutes, and balance."),
            ExerciseEntity(name = "Leg Press", category = "legs", muscles = "quads", description = "Machine press to overload the quads and glutes without loading the spine directly."),
            ExerciseEntity(name = "Leg Extension", category = "legs", muscles = "quads", description = "Isolation machine exercise designed to target and sculpt the quads."),
            ExerciseEntity(name = "Lying Leg Curl", category = "legs", muscles = "hamstrings", description = "Isolation machine movement targeting the hamstrings through knee flexion."),
            ExerciseEntity(name = "Romanian Deadlift", category = "legs", muscles = "hamstrings", description = "A deadlift variation focusing on hip hinge to heavily load the hamstrings and glutes."),
            ExerciseEntity(name = "Hip Thrust", category = "legs", muscles = "glutes", description = "The best exercise for isolating and overloading the glutes using a barbell and bench."),
            ExerciseEntity(name = "Standing Calf Raise", category = "legs", muscles = "calves", description = "An exercise designed to stretch and compress the calves to build lower leg mass."),
            ExerciseEntity(name = "Pistol Squat", category = "legs", muscles = "quads", description = "An advanced calisthenics single-leg squat requiring extreme strength, mobility, and balance."),
            ExerciseEntity(name = "Glute Ham Raise", category = "legs", muscles = "hamstrings", description = "A bodyweight movement targeting the hamstrings and glutes through an intense range of motion."),
            ExerciseEntity(name = "Deficit Sumo Deadlift", category = "legs", muscles = "glutes", description = "A wide-stance deadlift performed from an elevated platform to increase range of motion for glutes and hamstrings."),
            ExerciseEntity(name = "Muscle-Up", category = "upper", muscles = "lats", description = "An advanced street workout transition from a pull-up to a dip, targeting lats, chest, and triceps."),
            ExerciseEntity(name = "Clean and Press", category = "upper", muscles = "shoulders", description = "A dynamic olympic lift transitioning from floor to shoulders to overhead, hitting shoulders, back, and legs."),
            ExerciseEntity(name = "Renegade Row", category = "upper", muscles = "back", description = "A push-up position row using dumbbells, targeting back, chest, and deep core stability."),
            ExerciseEntity(name = "Kettlebell Swing", category = "lower", muscles = "glutes", description = "A power movement focusing on hip hinge to build explosive glutes, hamstrings, and lower back."),
            ExerciseEntity(name = "Goblet Squat", category = "lower", muscles = "quads", description = "A front-loaded squat holding a dumbbell or kettlebell, great for quad development and posture."),
            ExerciseEntity(name = "Walking Lunges", category = "lower", muscles = "quads", description = "Unilateral moving lunges targeting the quads, glutes, and hamstring development."),
            ExerciseEntity(name = "Hanging Leg Raise", category = "upper", muscles = "abs", description = "Hanging from a bar and lifting the legs to the bar, targeting the abs and hip flexors."),
            ExerciseEntity(name = "Cable Crunch", category = "upper", muscles = "abs", description = "Kneeling cable crunch designed to add progressive overload to the abdominal muscles."),
            ExerciseEntity(name = "Ab Wheel Rollout", category = "upper", muscles = "abs", description = "An intense core rollout exercise targeting the abs, lower back, and shoulder stability."),
            ExerciseEntity(name = "Plank", category = "upper", muscles = "abs", description = "An isometric hold designed to build endurance and strength across the entire abdominal wall."),
            ExerciseEntity(name = "Russian Twist", category = "upper", muscles = "obliques", description = "A seated rotational exercise targeting the obliques and deep rotational core strength."),
            ExerciseEntity(name = "Side Plank", category = "upper", muscles = "obliques", description = "An isometric lateral hold targeting the obliques and lateral core stability."),
            ExerciseEntity(name = "Decline Bench Press", category = "push", muscles = "chest", description = "Bench press performed on a decline to place emphasis on the lower chest fibers."),
            ExerciseEntity(name = "Incline Push-Up", category = "push", muscles = "chest", description = "Hands elevated push-up making the movement easier, focusing on the lower chest."),
            ExerciseEntity(name = "Shrimp Squat", category = "legs", muscles = "quads", description = "A calisthenics single-leg squat variation where you hold one foot behind your back."),
            ExerciseEntity(name = "Farmer's Walk", category = "upper", muscles = "forearms", description = "Heavy carry exercise that builds extreme grip strength, forearms, traps, and core stability."),
            ExerciseEntity(name = "Shrugs", category = "pull", muscles = "traps", description = "Heavy barbell or dumbbell shoulder shrugs to build mass in the upper traps.")
        )
    }
}
