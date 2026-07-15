package com.example.gymko.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gymko.MainActivity
import com.example.gymko.data.local.database.GymKoDatabase
import com.example.gymko.data.model.WorkoutStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class WorkoutService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private var startTime = 0L

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "workout_channel"

    inner class WorkoutBinder : Binder() {
        fun getService(): WorkoutService = this@WorkoutService
    }

    private val binder = WorkoutBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private var isForeground = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startWorkout()
            "STOP" -> stopWorkout()
            else -> {
                if (!isForeground) startWorkout()
            }
        }
        return START_STICKY
    }

    private fun startWorkout() {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
            _duration.value = 0L
        }
        
        val notification = createNotification(formatDuration(_duration.value))
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForeground = true
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                stopSelf()
                return
            }
        }
        
        if (timerJob?.isActive == true) return
        
        timerJob = serviceScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - startTime
                _duration.value = elapsed / 1000
                
                if (elapsed > TimeUnit.HOURS.toMillis(24)) {
                    autoCloseWorkout()
                    break
                }

                updateNotification(formatDuration(_duration.value))
                delay(1000)
            }
        }
    }

    private fun stopWorkout() {
        timerJob?.cancel()
        timerJob = null
        startTime = 0L
        _duration.value = 0L
        
        if (isForeground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            isForeground = false
        }
        stopSelf()
    }

    private fun autoCloseWorkout() {
        serviceScope.launch {
            val dao = GymKoDatabase.getDatabase(applicationContext).gymKoDao()
            val activeWorkouts = dao.getAllWorkoutsWithSets().first().filter { 
                it.workout.status == WorkoutStatus.ACTIVE
            }
            for (active in activeWorkouts) {
                dao.updateWorkout(active.workout.copy(status = WorkoutStatus.AUTO_CLOSED))
            }
            stopWorkout()
        }
    }

    private fun updateNotification(time: String) {
        if (!isForeground) return
        val notification = createNotification(time)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(time: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active Workout")
            .setContentText("Duration: $time")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
