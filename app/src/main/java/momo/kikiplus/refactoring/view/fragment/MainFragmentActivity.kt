package momo.kikiplus.refactoring.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainFragmentActivityBinding
import momo.kikiplus.com.kbucket.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Activity.SetNickNameActivity
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.FireMessingService
import momo.kikiplus.refactoring.task.AppUpdateTask
import momo.kikiplus.refactoring.task.UserUpdateTask
import momo.kikiplus.refactoring.util.AppUtils
import momo.kikiplus.refactoring.util.DataUtils
import momo.kikiplus.refactoring.util.ErrorLogUtils
import momo.kikiplus.refactoring.util.KLog

class MainFragmentActivity : AppCompatActivity(), Handler.Callback {

    private var backKeyPressedTime = 0L
    private var finishToast: Toast? = null

    private var mHandler: Handler = Handler(this)
    private val CHECK_VERSION           : Int = 1000
    private val MY_PERMISSION_REQUEST   : Int = 1001
    private val TOAST_MASSEGE           : Int = 1002
    private val UPDATE_USER             : Int = 1003

    private var mbInitialUserUpdate : Boolean = false
    private lateinit var mBinding : MainFragmentActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KLog.log("@@ MainFragmentActivity onCreate")
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = MainFragmentActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar!!.hide()

        initialize()

        val getIntent = intent
        Log.d("mhkim", "@@ getIntent : " + getIntent)
        val data = getIntent.getStringExtra(ContextUtils.WIDGET_SEND_DATA)
        if (data != null && data == ContextUtils.WIDGET_SHARE) {
            ShareSocial()
        }

        checkPermision()
        mHandler.sendEmptyMessage(CHECK_VERSION)

        AppUtils.sendTrackerScreen(this, "메인화면")
    }

    private fun initialize() {
        FirebaseApp.initializeApp(this)

        Thread.setDefaultUncaughtExceptionHandler(ErrorLogUtils.UncaughtExceptionHandlerApplication())

        val sqlQuery = SQLQuery()
        sqlQuery.createTable(applicationContext)
        sqlQuery.createChatTable(applicationContext)
        sqlQuery.createImageTable(applicationContext)

        setBackgroundColor()
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.mainFragementActivityBackground.setBackgroundColor(color)
        }
    }

    override fun onStart() {
        super.onStart()

        var userNickName = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        val sqlQuery = SQLQuery()
        val list = sqlQuery.selectUserTable(applicationContext)
        val strDBNickName = list?.get("nickname")

        if (strDBNickName != null) {
            SharedPreferenceUtils.write(this, ContextUtils.KEY_USER_NICKNAME, strDBNickName)
            userNickName = strDBNickName
        }

        if (userNickName == null || userNickName == "null") {
            val intent = Intent(this, SetNickNameActivity::class.java)
            startActivity(intent)
        }

        var strToken = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_FCM, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        KLog.d(ContextUtils.TAG, "@@ onStart strToken : " + strToken)
        if(strToken == null){
            val intent = Intent(this, FireMessingService::class.java)
            startService(intent)
        }

        if (!mbInitialUserUpdate && userNickName != null && strToken != null) {
            mbInitialUserUpdate = true
            mHandler.sendEmptyMessage(UPDATE_USER)
        }
    }
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            CHECK_VERSION//버전 체크
            -> {
                val appUpdateTask =
                    AppUpdateTask(this)
                appUpdateTask.execute()
            }
            TOAST_MASSEGE//메시지 출력
            -> Toast.makeText(this, msg.obj as String, Toast.LENGTH_LONG).show()
            UPDATE_USER//사용자 정보 없데이트
            -> {
                val userUpdateTask = UserUpdateTask(this)
                userUpdateTask.execute()
            }
        }
        return false
    }
    /**
     * 소셜로 가지 앱 홍보하기
     */
    private fun ShareSocial() {
        val msg = Intent(Intent.ACTION_SEND)
        msg.addCategory(Intent.CATEGORY_DEFAULT)
        msg.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.share_title))
        msg.putExtra(Intent.EXTRA_TEXT, this.getString(R.string.share_contents))
        msg.type = "text/plain"
        startActivity(Intent.createChooser(msg, "공유"))
    }

    override fun onBackPressed() {
        openExitToast()
    }

    /**
     * 두번 뒤로가기 누를 시 종료됨
     */
    private fun openExitToast() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            val msg = getString(R.string.back_string)
            finishToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            finishToast!!.show()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            if (finishToast != null) {
                finishToast!!.cancel()
            }
            finish()
        }
    }

    private fun checkPermision() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSION_REQUEST)

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_REQUEST -> {
                val isReulst = DataUtils.createFile()
                if (!isReulst) {
                    mHandler.sendMessage(mHandler.obtainMessage(TOAST_MASSEGE, "권한을 모두 허용해주셔야 앱을 정상적으로 사용할 수 있습니다."))
                }
            }
        }
    }

}
