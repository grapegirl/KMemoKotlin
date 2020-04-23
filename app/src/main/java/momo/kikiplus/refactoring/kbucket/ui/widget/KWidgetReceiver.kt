package momo.kikiplus.refactoring.obj

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import momo.kikiplus.com.kbucket.view.Activity.WriteMemoActivity
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IntroActivity

class KWidgetReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        var newIntent = intent
        KLog.log("@@ KWidgetReceiver action : " + intent.action!!)

        val action = newIntent.action

        if (WIDGET_ACTION_WRITE_EVENT == action) {
            callActivity(context, DataConst.WIDGET_WRITE_BUCKET)
        } else if (WIDGET_ACTION_LIST_EVENT == action) {
            callActivity(context, DataConst.WIDGET_BUCKET_LIST)
        } else if (WIDGET_ACTION_OURS_BUCKET_EVENT == action) {
            callActivity(context, DataConst.WIDGET_OURS_BUCKET)
        } else if (WIDGET_ACTION_SHARE_EVENT == action) {
            callActivity(context, DataConst.WIDGET_SHARE)
        } else if (WIDGET_ACTION_MEMO_WRITE_EVENT == action) {
            newIntent = Intent(context, WriteMemoActivity::class.java)
            newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(newIntent)
        } else if(WIDGET_ACTION_REFRESH == action){

        }
    }

    /**
     * Activity 호출 (Intent.FLAG_ACTIVITY_NEW_TASK)
     */
    private fun callActivity(context: Context, message: String) {
        KLog.log("@@ widget callActivity message : $message")
        val intent = Intent(context, IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("DATA", message)
        context.startActivity(intent)
    }

    companion object {

        val WIDGET_ACTION_WRITE_EVENT = "com.kiki.View.widget.ACTION_WRITE_EVENT"
        val WIDGET_ACTION_LIST_EVENT = "com.kiki.View.widget.ACTION_LIST_EVENT"
        val WIDGET_ACTION_SHARE_EVENT = "com.kiki.View.widget.ACTION_SHARE_EVENT"
        val WIDGET_ACTION_OURS_BUCKET_EVENT = "com.kiki.View.widget.ACTION_OURS_BUCKET_EVENT"
        val WIDGET_ACTION_MEMO_WRITE_EVENT = "com.kiki.View.widget.ACTION_MEMO_WRITE_EVENT"
        val WIDGET_ACTION_REFRESH = "com.kiki.View.widget.ACTION_WIDGET_UPDATE"
    }
}
