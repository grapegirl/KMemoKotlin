package momo.kikiplus.refactoring.kbucket.ui.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainFragmentActivityBinding
import momo.kikiplus.com.kbucket.view.Activity.*
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.*
import momo.kikiplus.refactoring.kbucket.data.FireMessingService
import momo.kikiplus.refactoring.task.AppUpdateTask
import momo.kikiplus.refactoring.task.UserUpdateTask

class MainFragmentActivity : AppCompatActivity(), Handler.Callback {

    private var backKeyPressedTime = 0L
    private var finishToast: Toast? = null

    private var mHandler: Handler = Handler(this)
    private val CHECK_VERSION           : Int = 1000
    private val MY_PERMISSION_REQUEST   : Int = 1001
    private val TOAST_MASSEGE           : Int = 1002
    private val UPDATE_USER             : Int = 1003
    private val OPEN_DRAWER             : Int = 1004

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
        }else if(data != null && data == ContextUtils.START_OPEN_DRAWER){
            mHandler.sendEmptyMessage(OPEN_DRAWER)
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

        val confDatas = resources.getStringArray(R.array.confList)
        confDatas[7] += ": " + AppUtils.getVersionName(this)!!

        mBinding.drawerList.adapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, confDatas)
        mBinding.drawerList.onItemClickListener = DrawerItemClickListener()

        MobileAds.initialize(this, ContextUtils.KBUCKET_AD_UNIT_ID)
        val adRequest = AdRequest.Builder().build()
        mBinding.mainAdLayout.loadAd(adRequest)
        mBinding.mainAdLayout.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d("mhkim", "@@ onAdLoaded  ")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                Log.d("mhkim", "@@ onAdFailedToLoad errorCode :   " + errorCode)
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d("mhkim", "@@ onAdOpened")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.d("mhkim", "@@ onAdClicked")
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d("mhkim", "@@ onAdLeftApplication")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.d("mhkim", "@@ onAdClosed")
            }
        }

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
                AppUpdateTask(this).execute()
            }
            TOAST_MASSEGE//메시지 출력
            -> Toast.makeText(this, msg.obj as String, Toast.LENGTH_LONG).show()
            UPDATE_USER//사용자 정보 없데이트
            -> {
                UserUpdateTask(this).execute()
            }
            OPEN_DRAWER ->{
                if (!mBinding.dlActivityMainDrawer.isDrawerOpen(mBinding.drawerList)) {
                    mBinding.dlActivityMainDrawer.openDrawer(mBinding.drawerList)
                }
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

    private inner class DrawerItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            selectItem(position)
        }
    }

    private fun selectItem(position: Int) {
        when (position) {
            0//암호설정
            -> {
                var intent = Intent(this, PassWordActivity::class.java)
                intent.putExtra("SET", "SET")
                startActivity(intent)
            }
            1//암호해제
            -> {
                val message = getString(R.string.password_cancle_string)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                SharedPreferenceUtils.write(applicationContext, ContextUtils.KEY_CONF_PASSWORD, "")
            }
            2//DB 관리
            -> {
                intent = Intent(this, DBMgrActivity::class.java)
                startActivity(intent)
            }
            3//별명설정
            -> {
                intent = Intent(this, SetNickNameActivity::class.java)
                startActivity(intent)
            }
            4//배경설정
            -> {
                intent = Intent(this, SetBackColorActivity::class.java)
                startActivity(intent)
            }
            5//튜토리얼
            -> {
                //TODO Intent
//                intent = Intent(this, TutorialActivity::class.java)
//                startActivity(intent)
            }
            6//문의하기
            -> {
                intent = Intent(this, QuestionActivity::class.java)
                startActivity(intent)
            }
            9//관심 버킷 추가하기
            -> {
                intent = Intent(this, AddBucketActivity::class.java)
                startActivity(intent)
            }
        }
        mBinding.dlActivityMainDrawer.closeDrawer(mBinding.drawerList)
    }

    fun sendUserEvent(screenName : String){
        AppUtils.sendTrackerScreen(this, screenName)
    }
}
