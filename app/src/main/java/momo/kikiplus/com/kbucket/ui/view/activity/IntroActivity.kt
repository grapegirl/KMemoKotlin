package momo.kikiplus.com.kbucket.ui.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.view.animation.AlphaAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import momo.kikiplus.com.kbucket.databinding.IntroActivityBinding
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.finally.DataConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : IntroActivity
 * @Description : 인트로
 * @since 2015-11-02
 */
class IntroActivity : AppCompatActivity(), Handler.Callback {

    private var mHandler: Handler = Handler(Looper.getMainLooper(), this)

    private lateinit var mBinding : IntroActivityBinding

    private lateinit var mStartView : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱이 시스템 바 영역까지 배치되고, 인셋을 직접 처리하도록 설정
        enableEdgeToEdge()
       // WindowCompat.setDecorFitsSystemWindows(window, false)

        mBinding = IntroActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // 처음 설정된 디자인 패딩을 저장
        val initialLeft = mBinding.root.paddingLeft
        val initialTop = mBinding.root.paddingTop
        val initialRight = mBinding.root.paddingRight
        val initialBottom = mBinding.root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { view, insets ->

            val safe = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )

            // 계산한 인셋을 실제 화면 여백에 반영
            view.setPadding(
                initialLeft + safe.left,
                initialTop + safe.top,
                initialRight + safe.right,
                initialBottom + safe.bottom
            )

            insets
        }

        supportActionBar?.hide()

        mBinding.root.post{
            val controller = WindowCompat.getInsetsController(
                window,
                mBinding.root
            )

            // 상태바와 하단 내비게이션 바를 표시
            controller.show(WindowInsetsCompat.Type.systemBars())

            // 표시된 시스템 바의 크기로 여백을 다시 반영
            ViewCompat.requestApplyInsets(mBinding.root)
        }

        setBackgroundColor()

        val anim1 = AlphaAnimation(0.0f, 1.0f)
        anim1.duration = 500
        val anim2 = AlphaAnimation(0.0f, 1.0f)
        anim2.duration = 1000
        val anim3 = AlphaAnimation(0.0f, 1.0f)
        anim3.duration = 1000

        mBinding.introImageview.animation = anim1
        mBinding.introImageview2.animation = anim2
        mBinding.introImageview3.animation = anim3
        val timerTask = object : TimerTask() {
            override fun run() {

                //위젯으로부터 화면 전환
                val intent = intent
                mStartView = intent.getStringExtra("DATA").toString()
                KLog.d("@@ IntroActivity onCreate mStartView : $mStartView")
                val password = SharedPreferenceUtils.read(applicationContext, PreferConst.KEY_CONF_PASSWORD, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                KLog.d("@@ IntroActivity onCreate password : $password")
                if (!password.isNullOrEmpty()) {
                    mHandler.sendEmptyMessage(1)
                } else {

                    when (mStartView) {
                        DataConst.WIDGET_WRITE_BUCKET -> {
                            mHandler.sendEmptyMessage(2)
                        }
                        DataConst.WIDGET_BUCKET_LIST -> {
                            mHandler.sendEmptyMessage(3)
                        }
                        DataConst.WIDGET_OURS_BUCKET -> {
                            mHandler.sendEmptyMessage(4)
                        }
                        DataConst.WIDGET_SHARE -> {
                            mHandler.sendEmptyMessage(5)
                        }
                        else -> {
                            mHandler.sendEmptyMessage(0)
                        }
                    }
                }
            }
        }

        val timer: Timer = Timer()
        timer.schedule(timerTask, 2000)

    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.introBackColor.setBackgroundColor(color)
        }
    }


    override fun handleMessage(msg: Message): Boolean {
        KLog.d("@@ IntroActivity msg : " + msg.what)
        KLog.d("@@ IntroActivity handler mStartView : $mStartView")
        when (msg.what) {
            0//바로 실행할때
            -> {
                val intent = Intent(this, MainFragmentActivity::class.java)
                startActivity(intent)
                finish()
            }
            1//비밀번호 맞추기
            -> {
                val intent = Intent(this, MainFragmentActivity::class.java)
                intent.putExtra("SET", "GET")
                intent.putExtra("DATA", mStartView)
                intent.putExtra(DataConst.WIDGET_SEND_DATA, DataConst.WIDGET_PASS)
                KLog.log("@@ IntroActivity intent : $intent")
                KLog.log("@@ IntroActivity intent.data : "+  intent.extras)
                startActivity(intent)
                finish()
            }
            2//가지 작성 화면
            -> {
                val intent = Intent(this, MainFragmentActivity::class.java)
                intent.putExtra(DataConst.WIDGET_SEND_DATA, DataConst.WIDGET_WRITE_BUCKET)
                startActivity(intent)
                finish()

            }
            3//리스트 화면
            -> {
                val intent = Intent(this, MainFragmentActivity::class.java)
                intent.putExtra(DataConst.WIDGET_SEND_DATA, DataConst.WIDGET_BUCKET_LIST)
                startActivity(intent)
                finish()
            }
            4//모두의 가지화면
            -> {
                val intent = Intent(this, MainFragmentActivity::class.java)
                intent.putExtra(DataConst.WIDGET_SEND_DATA, DataConst.WIDGET_OURS_BUCKET)
                startActivity(intent)
                finish()
            }
            //공유
            5 -> {
                val intent = Intent(this, MainFragmentActivity::class.java)
                intent.putExtra(DataConst.WIDGET_SEND_DATA, DataConst.WIDGET_SHARE)
                startActivity(intent)
                finish()
            }
        }
        return false
    }
}

