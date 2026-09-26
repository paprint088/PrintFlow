package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "printflow_channel"
    private const val CHANNEL_NAME = "PrintFlow Dispatch Notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Alerts for incoming print requests and auto-replies"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notifyNoAttachment(context: Context, sender: String, subject: String) {
        showNotification(
            context = context,
            notificationId = 1001,
            title = "PrintFlow: No Files Attached",
            message = "From $sender ($subject). Auto-replied that no files were attached."
        )
    }

    fun notifyPhotographRejected(context: Context, sender: String, filename: String) {
        showNotification(
            context = context,
            notificationId = 1002,
            title = "PrintFlow: Document Photo Rejected",
            message = "From $sender. '$filename' is a photograph of a document. Auto-replied non-standard format notice."
        )
    }

    fun notifyJobApprovalNeeded(
        context: Context,
        sender: String,
        printerName: String,
        pages: Int,
        costText: String
    ) {
        showNotification(
            context = context,
            notificationId = 1003,
            title = "PrintFlow: Approval Needed ($costText)",
            message = "From $sender: $pages page(s) -> Printer: $printerName. Tap to review settings & approve."
        )
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted on 13+ yet, in-app notification handles it
        }
    }
}
