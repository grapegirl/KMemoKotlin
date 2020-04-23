package momo.kikiplus.refactoring.obj

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.view.Activity.WriteMemoActivity
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finall.KMemoConst

/**
 * Created by cs on 2017-04-05.
 */
class KMemoWidget : AppWidgetProvider() {


   override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
       //Toast.makeText(context, "onUpdate appWidgetIds : " + appWidgetIds , Toast.LENGTH_LONG).show()

       super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (i in appWidgetIds.indices) {
            val appWidgetId = appWidgetIds[i]
            val views = RemoteViews(context.packageName, R.layout.activity_layout_memo_widget)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            KLog.d(ContextUtils.TAG, "@@ onUpdate appWidgetId : " + appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        //val extras = intent.extras
        val manager = AppWidgetManager.getInstance(context)

        //val nID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val action = intent.action

        //Toast.makeText(context, "onReceive action : " + action , Toast.LENGTH_LONG).show()
        //기본 Reciver
        if (AppWidgetManager.ACTION_APPWIDGET_ENABLED == action) {
        } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED == action) {
        } else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED == action) {
        } else {
            initUI(context, manager, manager.getAppWidgetIds(ComponentName(context, javaClass)))
        }


    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        //Toast.makeText(context, "onDeleted : " + appWidgetIds , Toast.LENGTH_LONG).show()

        SharedPreferenceUtils.write(context!!, KMemoConst.KEY_USER_MEMO, "")

    }


    /**
     * UI 설정 이벤트 설정
     */
    private fun initUI(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = RemoteViews(context.packageName, R.layout.activity_layout_memo_widget)
        val intent = Intent(context, KWidgetReceiver::class.java)
        intent.action = KWidgetReceiver.WIDGET_ACTION_MEMO_WRITE_EVENT
        val writeEventPIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        views.setOnClickPendingIntent(R.id.widget_memo_content, writeEventPIntent)
        views.setOnClickPendingIntent(R.id.widget_memo_modify, writeEventPIntent)

        val memo = SharedPreferenceUtils.read(context, KMemoConst.KEY_USER_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        views.setTextViewText(R.id.widget_memo_content, memo)
        intent.action = KWidgetReceiver.WIDGET_ACTION_MEMO_WRITE_EVENT
        intent.putExtra(WriteMemoActivity.Intent_WID, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            KLog.d(ContextUtils.TAG, "@@ initUI appWidgetId : " + appWidgetId)
            SharedPreferenceUtils.write(context, KMemoConst.KEY_USER_MEMO_WIDGET, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {

        fun updateWidget(context: Context, nID: Int) {
            val remoteViews = RemoteViews(context.packageName, R.layout.activity_layout_memo_widget)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            KLog.d(ContextUtils.TAG, "@@ updateWidget  appWidgetManager : " + appWidgetManager)
            val memo = SharedPreferenceUtils.read(context, KMemoConst.KEY_USER_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
            remoteViews.setTextViewText(R.id.widget_memo_content, memo)
            KLog.d(ContextUtils.TAG, "@@ updateWidget memo : " + memo)
            KLog.d(ContextUtils.TAG, "@@ updateWidget nId : " + nID)

            appWidgetManager.updateAppWidget(nID, remoteViews)

        }
    }

}

