package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.view.Object.KMemoWidget
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.util.KLog

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : WriteMemoActivity
 * @Description : 메모 작성 클래스
 */
class WriteMemoActivity : Activity(), View.OnClickListener {

    private var mWidgetId = -1
    private var mIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wriet_meo_activity)

        mIntent = intent

        (findViewById<View>(R.id.write_meo_modify) as Button).setOnClickListener(this)
        val memo = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        (findViewById<View>(R.id.write_meo_content) as EditText).setText(memo)

        val widgetId = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_MEMO_WIDGET, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?
        mWidgetId = widgetId!!
        KLog.d(ContextUtils.TAG, "@@ WriteMemoActivity  mWidgetId : " + mWidgetId)

    }

    override fun onClick(v: View) {
        val memo = (findViewById<View>(R.id.write_meo_content) as EditText).text.toString()
        SharedPreferenceUtils.write(this, ContextUtils.KEY_USER_MEMO, memo)

        KMemoWidget.updateWidget(this, mWidgetId)

        finish()

    }

    companion object {
        val Intent_WID = "WID"        // appWidgetIds
    }
}
