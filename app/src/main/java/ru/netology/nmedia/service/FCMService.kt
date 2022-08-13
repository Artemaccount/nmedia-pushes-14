package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.json.JSONObject
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth


class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val notificationId = 1
    private val gson = Gson()


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val recipientId = message.data["recipientId"]
        val messageInNotification = message.data["content"]

        val obj = JSONObject(messageInNotification)
        val onlyMessage = obj.getString("content")

        println("message = $onlyMessage")
        println("recipientId = $recipientId")
        val appAuthToken = AppAuth.getInstance().getIdKey()
        if (recipientId.equals(appAuthToken)) {
            showNotification(onlyMessage)
        } else {
            when (recipientId) {
                "0" -> AppAuth.getInstance().sendPushToken()
                null -> showNotification(onlyMessage)
                else -> AppAuth.getInstance().sendPushToken()
            }
        }

    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun showNotification(message: String?) {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_photo_24dp)
            .setContentTitle("Уведомление")
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }
}
