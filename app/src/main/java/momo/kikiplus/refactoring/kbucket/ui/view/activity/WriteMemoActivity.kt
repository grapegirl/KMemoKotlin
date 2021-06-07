package momo.kikiplus.refactoring.kbucket.ui.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.WrietMeoActivityBinding
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.widget.KMemoWidget

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : WriteMemoActivity
 * @Description : 메모 작성 클래스
 */
class WriteMemoActivity : Activity(), View.OnClickListener {

    private var mWidgetId = -1
    private var mIntent: Intent? = null
    private lateinit var mBinding: WrietMeoActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = WrietMeoActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mIntent = intent

        (mBinding.writeMeoModify).setOnClickListener(this)
        val memo = SharedPreferenceUtils.read(
            this,
            PreferConst.KEY_USER_MEMO,
            SharedPreferenceUtils.SHARED_PREF_VALUE_STRING
        ) as String?
        (mBinding.writeMeoContent).setText(memo)

        val widgetId = SharedPreferenceUtils.read(
            this,
            PreferConst.KEY_USER_MEMO_WIDGET,
            SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER
        ) as Int?
        mWidgetId = widgetId!!
        KLog.d("@@ WriteMemoActivity  mWidgetId : $mWidgetId")

    }

    override fun onClick(v: View) {
        val memo = (findViewById<View>(R.id.write_meo_content) as EditText).text.toString()
        SharedPreferenceUtils.write(this, PreferConst.KEY_USER_MEMO, memo)

        KMemoWidget.updateWidget(this, mWidgetId)

        finish()
    }

    companion object {
        const val Intent_WID = "WID"        // appWidgetIds
    }
}