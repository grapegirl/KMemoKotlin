package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.AppUtils
import momo.kikiplus.com.kbucket.Utils.ContextUtils
import momo.kikiplus.com.kbucket.Utils.DataUtils
import momo.kikiplus.com.kbucket.Utils.SharedPreferenceUtils
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : IntroActivity
 * @Description : 인트로
 * @since 2015-11-02
 */
class IntroActivity : Activity(), android.os.Handler.Callback {

    private var mHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity)
        setBackgroundColor()

        val typeFace = DataUtils.getHannaFont(applicationContext)
        (findViewById<View>(R.id.intro_textview1) as TextView).typeface = typeFace
        (findViewById<View>(R.id.intro_textview2) as TextView).typeface = typeFace
        (findViewById<View>(R.id.intro_textview3) as TextView).text = AppUtils.getVersionName(applicationContext)
        (findViewById<View>(R.id.intro_textview3) as TextView).typeface = typeFace

        val anim1 = AlphaAnimation(0.0f, 1.0f)
        anim1.duration = 500
        val anim2 = AlphaAnimation(0.0f, 1.0f)
        anim2.duration = 1000
        val anim3 = AlphaAnimation(0.0f, 1.0f)
        anim3.duration = 1000
        findViewById<View>(R.id.intro_imageview).setAnimation(anim1)
        findViewById<View>(R.id.intro_imageview2).setAnimation(anim2)
        findViewById<View>(R.id.intro_imageview3).setAnimation(anim3)

        mHandler = Handler(this)
        val timer: Timer
        val timerTask = object : TimerTask() {
            override fun run() {
                val password = SharedPreferenceUtils.read(applicationContext, ContextUtils.KEY_CONF_PASSWORD, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                if (password != null && password != "") {
                    mHandler!!.sendEmptyMessage(1)
                } else {
                    //위젯으로부터 화면 전환
                    val intent = intent
                    val startView = intent.getStringExtra("DATA")
                    if (startView != null && startView == ContextUtils.WIDGET_WRITE_BUCKET) {
                        mHandler!!.sendEmptyMessage(2)
                    } else if (startView != null && startView == ContextUtils.WIDGET_BUCKET_LIST) {
                        mHandler!!.sendEmptyMessage(3)
                    } else if (startView != null && startView == ContextUtils.WIDGET_OURS_BUCKET) {
                        mHandler!!.sendEmptyMessage(4)
                    } else if (startView != null && startView == ContextUtils.WIDGET_SHARE) {
                        mHandler!!.sendEmptyMessage(5)
                    } else {
                        mHandler!!.sendEmptyMessage(0)
                    }
                }
            }
        }

        timer = Timer()
        timer.schedule(timerTask, 2000)

    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.intro_back_color).setBackgroundColor(color)
        }
    }


    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            0//바로 실행할때
            -> {
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            1//비밀번호 맞추기
            -> {
                intent = Intent(this, PassWordActivity::class.java)
                val intent2 = intent
                val startView = intent2.getStringExtra("DATA")
                intent.putExtra("SET", "GET")
                intent.putExtra("DATA", startView)
                startActivity(intent)
                finish()
            }
            2//가지 작성 화면
            -> {
                intent = Intent(this, WriteActivity::class.java)
                startActivity(intent)
                finish()
            }
            3//리스트 화면
            -> {
                intent = Intent(this, BucketListActivity::class.java)
                startActivity(intent)
                finish()
            }
            4//모두의 가지화면
            -> {
                intent = Intent(this, ShareListActivity::class.java)
                startActivity(intent)
                finish()
            }
            5 -> {
                intent = Intent(this, MainActivity::class.java)
                intent.putExtra(ContextUtils.WIDGET_SEND_DATA, ContextUtils.WIDGET_SHARE)
                startActivity(intent)
                finish()
            }
        }
        return false
    }

    override fun onBackPressed() {}
}

