package it.polito.mad.splintersell.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.ui.my_item_list.ItemListFragment


class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("notification", " ${p0.notification!!.title} ")

        if (p0.data.isNotEmpty()) {
            showNotification(p0.data["title"], p0.data["message"])
        }

        if (p0.notification != null) {
            showNotification(p0.notification!!.title, p0.notification!!.body)
        }
    }


    private fun showNotification(title: String?, message: String?) {

        val channelId = "web_app_channel"
        val intent = Intent(this, ItemListFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.icon)
            .setColor(resources.getColor(R.color.colorAccent))


        val notMan: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, "web_app", NotificationManager.IMPORTANCE_HIGH)
            notMan.createNotificationChannel(notificationChannel)
        }

        notMan.notify(0, builder.build())

    }

    override fun onNewToken(token: String) {
        FirestoreViewModel().updateToken(token)
    }

}