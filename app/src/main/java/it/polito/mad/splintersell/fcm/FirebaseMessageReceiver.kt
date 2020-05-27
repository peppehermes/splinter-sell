package it.polito.mad.splintersell.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import it.polito.mad.splintersell.MainActivity
import it.polito.mad.splintersell.R
import it.polito.mad.splintersell.data.FirestoreViewModel


class FirebaseMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        if (p0.notification != null) {
            showNotification(p0.notification!!.title, p0.notification!!.body, p0.data["fragment"])
        }
    }


    private fun showNotification(
        title: String?,
        message: String?,
        fragment: String?
    ) {
        var destination:Int = R.id.nav_on_sale_list

        if(fragment == "bought")
            destination = R.id.nav_bought_items_list
        else if (fragment == "onsale")
            destination = R.id.nav_on_sale_list
        else if (fragment == "myitems")
            destination = R.id.nav_item_list

        val channelId = "web_app_channel"
        val pending = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(destination)
            .createPendingIntent()
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setSound(uri)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo_final)
            .setColor(applicationContext.getColor(R.color.colorAccent))


        val notMan: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, "web_app", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(uri, null)
            notMan.createNotificationChannel(notificationChannel)
        }

        notMan.notify(0, builder.build())

    }

    override fun onNewToken(token: String) {
        if (FirestoreViewModel().createdUserLiveData != null)
            FirestoreViewModel().updateToken(token)
    }

}