package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.utils.AppUtils
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.KLog
import momo.kikiplus.modify.SharedPreferenceUtils
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : PassWordActivity
 * @Description : 패스워드 액티비티
 * @since 2015-11-02
 */
class PassWordActivity : Activity(), View.OnClickListener {

    private var mButton: Array<Button?> = arrayOfNulls<Button>(15)
    private var mPasswordData: ArrayList<String>? = null
    private var isPasswordset = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.password_activity)
        setBackgroundColor()
        mPasswordData = ArrayList()

        mButton[0] = findViewById<Button>(R.id.password_clear)  as Button
        mButton[1] = findViewById<View>(R.id.password_btn1) as Button
        mButton[2] = findViewById<View>(R.id.password_btn2) as Button
        mButton[3] = findViewById<View>(R.id.password_btn3) as Button
        mButton[4] = findViewById<View>(R.id.password_btn4) as Button
        mButton[5] = findViewById<View>(R.id.password_num0) as Button
        mButton[6] = findViewById<View>(R.id.password_num1) as Button
        mButton[7] = findViewById<View>(R.id.password_num2) as Button
        mButton[8] = findViewById<View>(R.id.password_num3) as Button
        mButton[9] = findViewById<View>(R.id.password_num4) as Button
        mButton[10] = findViewById<View>(R.id.password_num5) as Button
        mButton[11] = findViewById<View>(R.id.password_num6) as Button
        mButton[12] = findViewById<View>(R.id.password_num7) as Button
        mButton[13] = findViewById<View>(R.id.password_num8) as Button
        mButton[14] = findViewById<View>(R.id.password_num9) as Button

        for (i in 0..14) {
            mButton[i]!!.setOnClickListener(this)
        }

        val intent = intent
        val setting = intent.getStringExtra("SET")
        //암호 설정
        if (setting != null && setting == "SET") {
            isPasswordset = true
        }// 암호 맞추기
        else if (setting != null && setting == "GET") {
            isPasswordset = false
        }
        AppUtils.sendTrackerScreen(this, "암호설정화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.password_back_color).setBackgroundColor(color)
        }
    }


    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.password_num0, R.id.password_num1, R.id.password_num2, R.id.password_num3, R.id.password_num4, R.id.password_num5, R.id.password_num6, R.id.password_num7, R.id.password_num8, R.id.password_num9 -> {
                val data = (findViewById<View>(v.id) as Button).text.toString()
                if (mPasswordData!!.size < 4) {
                    mPasswordData!!.add(data)
                    printPassword()
                    setButtonText()
                }
            }
            R.id.password_clear -> {
                val size = mPasswordData!!.size
                if (size > 0 && size <= 4) {
                    mPasswordData!!.remove(mPasswordData!![size - 1])
                    printPassword()
                    setButtonText()
                }
            }
        }
    }

    private fun printPassword() {
        KLog.d(this.javaClass.simpleName, "@@ start")
        for (i in mPasswordData!!.indices) {
            KLog.d(this.javaClass.simpleName, "@@ " + i + " 번쨰:" + mPasswordData!![i])
        }
        KLog.d(this.javaClass.simpleName, "@@ end")
    }

    private fun setButtonText() {
        KLog.d(this.javaClass.simpleName, "@@ mPasswordData size : " + mPasswordData!!.size)
        for (i in 1..mPasswordData!!.size) mButton[i]?.text = "*"

        for (i in 1..4 - mPasswordData!!.size) {
            KLog.d(this.javaClass.simpleName, "@@ delete index : " + (5 - i))
            mButton[5 - i]?.text = ""
        }

        if (mPasswordData!!.size == 4) {
            if (isPasswordset) {
                val pawd = mPasswordData!![0] + "" + mPasswordData!![1] + "" + mPasswordData!![2] + "" + mPasswordData!![3]
                SharedPreferenceUtils.write(applicationContext, ContextUtils.KEY_CONF_PASSWORD, pawd)
                finish()
            } else {
                val pawd = mPasswordData!![0] + "" + mPasswordData!![1] + "" + mPasswordData!![2] + "" + mPasswordData!![3]
                val password = SharedPreferenceUtils.read(applicationContext, ContextUtils.KEY_CONF_PASSWORD, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                if (pawd != password) {
                    val message = getString(R.string.password_fail_string)
                    Toast.makeText(application, message, Toast.LENGTH_LONG).show()
                    mPasswordData!!.clear()
                    for (i in 1..4) {
                        mButton[i]?.text = ""
                    }
                } else {
                    val getIntent = intent
                    val startView = getIntent.getStringExtra("DATA")
                    if (startView != null && startView == ContextUtils.WIDGET_WRITE_BUCKET) {
                        val intent = Intent(this, WriteActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (startView != null && startView == ContextUtils.WIDGET_BUCKET_LIST) {
                        val intent = Intent(this, BucketListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (startView != null && startView == ContextUtils.WIDGET_OURS_BUCKET) {
                        val intent = Intent(this, ShareListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (startView != null && startView == ContextUtils.WIDGET_SHARE) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra(ContextUtils.WIDGET_SEND_DATA, ContextUtils.WIDGET_SHARE)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
