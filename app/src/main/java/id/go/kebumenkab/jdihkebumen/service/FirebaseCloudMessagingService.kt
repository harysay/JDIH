package id.go.kebumenkab.jdihkebumen.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.go.kebumenkab.jdihkebumen.MainActivity
import id.go.kebumenkab.jdihkebumen.R
import java.lang.Exception

class FirebaseCloudMessagingService : FirebaseMessagingService() {

    private var broadcaster: LocalBroadcastManager? = null

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
    //Here notification is recieved from server
        super.onMessageReceived(remoteMessage)
        try {
            sendNotification(
                remoteMessage.getData().get("title"),
                remoteMessage.getData().get("message")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(getApplicationContext(), MainActivity::class.java).also {
            //you can use your launcher Activity insted of SplashActivity, But if the Activity you used here is not launcher Activty than its not work when App is in background.
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            //Add Any key-value to pass extras to intent
            it.putExtra("pushnotification", "yes")
            broadcaster?.sendBroadcast(it)
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mNotifyManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //For Android Version Orio and greater than orio.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance: Int = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel("jdih", "jdih", importance)
            mChannel.setDescription(messageBody)
            mChannel.enableLights(true)
            mChannel.setLightColor(Color.RED)
            mChannel.enableVibration(true)
            mChannel.setVibrationPattern(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            mNotifyManager.createNotificationChannel(mChannel)
        }
        //For Android Version lower than oreo.
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, "jdih")
        mBuilder.setContentTitle(title)
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setColor(Color.parseColor("#FFD600"))
            .setContentIntent(pendingIntent)
            .setChannelId("jdih")
            .setContentInfo("Info")
            .setPriority(NotificationCompat.PRIORITY_LOW)
        mNotifyManager.notify(count, mBuilder.build())
        count++
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)

        Log.e("TOKENFIREBASE", s)
    }


    companion object {
        //    public String TAG = "FIREBASE MESSAGING";
        private const val TAG = "FCM Service"
        private var count = 0
    }
}