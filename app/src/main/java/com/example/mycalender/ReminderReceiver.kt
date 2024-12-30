package com.example.mycalender

import android.Manifest
import android.app.NotificationChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Vibrator
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Locale
import android.speech.tts.TextToSpeech
import android.util.Log
import android.os.Build
import android.os.VibrationEffect

class ReminderReceiver : BroadcastReceiver() {

    private var tts: TextToSpeech? = null
    private var isTTSReady = false
    private val pendingUtterances = mutableListOf<String>()

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra("eventId", -1)
        val eventName = intent.getStringExtra("eventName") ?: "Event"
        val reminderMethod = intent.getStringExtra("method")

        initializeTTS(context)

        when (reminderMethod) {
            "Notification" -> showNotification(context, eventId, eventName)
            "Ringtone" -> playRingtone(context)
            "Vibration" -> triggerVibration(context)
            "Voice Reminder" -> speakReminder(eventName)
        }
    }

    private fun initializeTTS(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.TAIWAN)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("ReminderReceiver", "TTS 語言不支持或缺少數據")
                    } else {
                        isTTSReady = true
                        Log.i("ReminderReceiver", "TTS 初始化成功")
                        processPendingUtterances() // 處理等待的播報需求
                    }
                } else {
                    Log.e("ReminderReceiver", "TTS 初始化失敗")
                }
            }
        }
    }

    private fun showNotification(context: Context, eventId: Int, eventName: String) {
        val channelId = "default_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒事件即將開始"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("提醒")
            .setContentText("$eventName 即將開始")
            .setSmallIcon(R.drawable.sheep_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(eventId, notification)
        }
    }

    private fun playRingtone(context: Context) {
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        ringtone?.play()
    }

    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            Log.i("ReminderReceiver", "Vibration triggered")
        } else {
            vibrator.vibrate(1000)
            Log.i("ReminderReceiver", "Vibration triggered (old version)")
        }
    }



    private fun speakReminder(eventName: String) {
        if (!isTTSReady) {
            Log.e("ReminderReceiver", "TTS 尚未初始化，加入等待隊列：$eventName")
            pendingUtterances.add("$eventName 即將開始")
            return
        }

        if (tts?.isSpeaking == true) {
            tts?.stop() // 停止當前的語音播報
        }

        val text = "$eventName 即將開始"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ReminderID")
    }

    private fun processPendingUtterances() {
        if (pendingUtterances.isNotEmpty() && isTTSReady) {
            for (utterance in pendingUtterances) {
                tts?.speak(utterance, TextToSpeech.QUEUE_ADD, null, "ReminderID")
            }
            pendingUtterances.clear()
        }
    }

    fun releaseTTS() {
        tts?.apply {
            stop()
            shutdown()
        }
        tts = null
        isTTSReady = false
    }
}
