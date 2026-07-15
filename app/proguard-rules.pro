# Room pravila za R8 / Proguard
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.limits.Limit
-keep class * {
    @androidx.room.Database *;
    @androidx.room.Dao *;
    @androidx.room.Entity *;
}