package momo.kikiplus.deprecated.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.deprecated.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.AppUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : SetNickNameActivity
 * @Description : 사용자 닉네임 설정
 * @since 2015-12-22
 */
class SetNickNameActivity : Activity(), View.OnClickListener {

    private var mButton: Button? = null
    private var mSqlQuery: SQLQuery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.set_nickname_activity)
        setBackgroundColor()

        mSqlQuery = SQLQuery()
        mButton = findViewById<View>(R.id.nickname_okBtn) as Button
        mButton!!.setOnClickListener(this)

        val nickname = SharedPreferenceUtils.read(applicationContext, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        if (nickname != null) {
            (findViewById<View>(R.id.nickname_editText) as EditText).setText(nickname)
            (findViewById<View>(R.id.nickname_editText) as EditText).requestFocus(nickname.length)
        }
        AppUtils.sendTrackerScreen(this, "닉네임변경화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.nickname_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View) {
        var nickname: String? = (findViewById<View>(R.id.nickname_editText) as EditText).text.toString()
        KLog.d("@@ nickname : " + nickname!!)
        nickname = nickname.replace(" ".toRegex(), "")
        if (nickname.isEmpty()) {
            val message = getString(R.string.nickname_fail_string)
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            return
        }
        SharedPreferenceUtils.write(applicationContext, PreferConst.KEY_USER_NICKNAME, nickname)
        if (mSqlQuery!!.containsUserTable(applicationContext)) {
            mSqlQuery!!.updateUserNickName(applicationContext, nickname)
        } else {
            mSqlQuery!!.insertUserNickName(applicationContext, nickname)
        }
        finish()
    }

    override fun onBackPressed() {
        finish()
    }
}
