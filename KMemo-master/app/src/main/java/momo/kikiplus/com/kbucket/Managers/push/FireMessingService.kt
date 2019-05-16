package momo.kikiplus.com.kbucket.Managers.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.KLog
import momo.kikiplus.com.kbucket.view.Activity.MainActivity

/***
 * @author grape girl
 * @version 1.0
 * @Class Name : FireMessingService
 * @Description : FCM 메시지 수신 서비스
 * @since 2017. 3. 11.
 */
class FireMessingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        KLog.d(TAG, "@@ message Message2: " + remoteMessage!!.data.toString())
        sendNotification(remoteMessage.data["message"])

    }

    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("메모가지 알림")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent) as NotificationCompat.Builder

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {

        private val TAG = "FireMessingService"
    }
}
