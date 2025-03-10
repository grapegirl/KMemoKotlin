package momo.kikiplus.com.kbucket.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.ui.view.activity.MainFragmentActivity

/***
 * @author grape girl
 * @version 1.0
 * @Class Name : FireMessingService
 * @Description : FCM 메시지 수신 서비스
 * @since 2017. 3. 11.
 */
class FireMessingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        KLog.d("@@ FireMessingService token : $p0")
        SharedPreferenceUtils.write(this, PreferConst.KEY_USER_FCM, p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        KLog.d( "@@ FireMessingService onMessageReceived: " + p0.data.toString())
        sendNotification(p0.data["message"])

    }

    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, MainFragmentActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel("channelId","channelName", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
            val notificationBuilder = NotificationCompat.Builder(this,"channelId")

            notificationManager.notify(0, notificationBuilder.build())
        }else{
            val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("메모가지 알림")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)


            notificationManager.notify(0, notificationBuilder.build())
        }

    }
}
