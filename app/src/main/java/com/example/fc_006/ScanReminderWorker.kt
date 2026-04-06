package com.example.fc_006

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ScanReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        sendReminderNotification()
        return Result.success()
    }

    private fun sendReminderNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            REMINDER_NOTIF_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val icon = R.drawable.ic_notification_scan
        val largeIcon = getBitmapFromVectorDrawable(applicationContext, icon)

        val builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
            .setSmallIcon(icon)
            .setLargeIcon(largeIcon)
            .setContentTitle("MISSION CONTROL IDLE")
            .setContentText("You have not scanned space for any threats recently. Deep space monitoring is highly recommended.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("You have not scanned space for any threats recently. Deep space monitoring is highly recommended. Stay vigilant, Commander."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(applicationContext, R.color.star_gold))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(REMINDER_NOTIF_ID, builder.build())
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        const val REMINDER_NOTIF_ID = 999
    }
}
