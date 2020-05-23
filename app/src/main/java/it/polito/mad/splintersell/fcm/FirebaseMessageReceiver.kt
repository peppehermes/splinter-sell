package it.polito.mad.splintersell.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel
import it.polito.mad.splintersell.ui.items_of_interest.ItemsOfInterestListFragment
import it.polito.mad.splintersell.ui.my_item_list.ItemListFragment


class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.d("ricezione", " ${p0.notification!!.title} ")

        if (p0.notification != null) {
            showNotification(p0.notification!!.title, p0.notification!!.body)
        }
    }


    private fun showNotification(title: String?, message: String?) {

        Log.d("notification","sto mostrando la notifica")

        val channelId = "web_app_channel"
        val pending = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.nav_items_of_interest_list)
            .createPendingIntent()
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setSound(uri)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo_final)
            .setColor(resources.getColor(R.color.colorAccent))


        val notMan: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, "web_app", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(uri,null)
            notMan.createNotificationChannel(notificationChannel)
        }

        notMan.notify(0, builder.build())

    }

    override fun onNewToken(token: String) {
        if (FirestoreViewModel().user != null)
            FirestoreViewModel().updateToken(token)
    }

}