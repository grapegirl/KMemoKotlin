package momo.kikiplus.com.kbucket.view.Object

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast

import momo.kikiplus.com.kbucket.R
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.refactoring.util.KLog

/**
 * Created by cs on 2015-11-19.
 */
class KWidget : AppWidgetProvider() {

    override
            /**
             * 위젯이 처음 생성할 때 호출되는 메소드, 위젯을 여러개 설치시, 첫번째 위젯을 띄울때만 호출된다고 함.
             */
    fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override
            /**
             * 위젯이 삭제될때도 호출되는 메소드
             */
    fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    override
            /**
             *
             */
    fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override
            /**
             * 콜백 메소드
             */
    fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        KLog.d(ContextUtils.TAG, "@@ onReceive action : " + action!!)

        if (AppWidgetManager.ACTION_APPWIDGET_ENABLED == action) {
            Toast.makeText(context, "메모가지 위젯 추가", Toast.LENGTH_LONG).show()
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == action) {
            val manager = AppWidgetManager.getInstance(context)
            initUI(context, manager, manager.getAppWidgetIds(ComponentName(context, javaClass)))
        } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED == action) {
            Toast.makeText(context, "메모가지 위젯 삭제", Toast.LENGTH_LONG).show()
        } else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED == action) {
        }

    }

    override
            /**
             * 위젯이 삭제될때 호출되는 메소드
             */
    fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }


    /**
     * UI 설정 이벤트 설정
     */
    private fun initUI(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        KLog.d(ContextUtils.TAG, "@@ widget initUI")

        val views = RemoteViews(context.packageName, R.layout.activity_layout_mywidget)

        val writeEventIntent = Intent(context, KWidgetReceiver::class.java)
        writeEventIntent.action = KWidgetReceiver.WIDGET_ACTION_WRITE_EVENT

        val listEventIntent = Intent(context, KWidgetReceiver::class.java)
        listEventIntent.action = KWidgetReceiver.WIDGET_ACTION_LIST_EVENT

        val oursEventIntent = Intent(context, KWidgetReceiver::class.java)
        oursEventIntent.action = KWidgetReceiver.WIDGET_ACTION_OURS_BUCKET_EVENT

        val shareEventIntent = Intent(context, KWidgetReceiver::class.java)
        shareEventIntent.action = KWidgetReceiver.WIDGET_ACTION_SHARE_EVENT

        //KLog.d(ContextUtils.TAG, "@@ widget initUI sdk : " + Build.VERSION.SDK_INT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val writeEventPIntent = PendingIntent.getBroadcast(context, 0, writeEventIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val listEventPIntent = PendingIntent.getBroadcast(context, 0, listEventIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val shareEventPIntent = PendingIntent.getBroadcast(context, 0, shareEventIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val oursEventPIntent = PendingIntent.getBroadcast(context, 0, oursEventIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            views.setOnClickPendingIntent(R.id.widget_writebtn, writeEventPIntent)
            views.setOnClickPendingIntent(R.id.widget_listbtn, listEventPIntent)
            views.setOnClickPendingIntent(R.id.widget_sharebtn, oursEventPIntent)
            views.setOnClickPendingIntent(R.id.widget_snsbtn, shareEventPIntent)

        } else {
            val writeEventPIntent = PendingIntent.getBroadcast(context, 0, writeEventIntent, 0)
            val listEventPIntent = PendingIntent.getBroadcast(context, 0, listEventIntent, 0)
            val shareEventPIntent = PendingIntent.getBroadcast(context, 0, shareEventIntent, 0)
            val oursEventPIntent = PendingIntent.getBroadcast(context, 0, oursEventIntent, 0)

            views.setOnClickPendingIntent(R.id.widget_writebtn, writeEventPIntent)
            views.setOnClickPendingIntent(R.id.widget_listbtn, listEventPIntent)
            views.setOnClickPendingIntent(R.id.widget_sharebtn, oursEventPIntent)
            views.setOnClickPendingIntent(R.id.widget_snsbtn, shareEventPIntent)
        }

        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
