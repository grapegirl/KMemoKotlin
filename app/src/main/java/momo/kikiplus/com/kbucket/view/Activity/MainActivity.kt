package momo.kikiplus.com.kbucket.view.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainActivityBinding
import momo.kikiplus.com.kbucket.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.http.IHttpReceive
import momo.kikiplus.com.kbucket.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Bean.MobileUser
import momo.kikiplus.com.kbucket.view.Object.KProgressDialog
import momo.kikiplus.com.kbucket.view.popup.AIPopup
import momo.kikiplus.com.kbucket.view.popup.BasicPopup
import momo.kikiplus.com.kbucket.view.popup.OnPopupEventListener
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.FireMessingService
import momo.kikiplus.refactoring.task.AppUpdateTask
import momo.kikiplus.refactoring.task.UserUpdateTask
import momo.kikiplus.refactoring.util.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : MainActivity
 * @Description : 메인 목록
 * @since 2015-07-22.
 */
class MainActivity : Activity(), View.OnClickListener, Handler.Callback, OnPopupEventListener, IHttpReceive {

    private var mHandler: Handler? = null
    private var backKeyPressedTime = 0L
    private var finishToast: Toast? = null
    private var mBasicPopup: BasicPopup? = null
    private var mAIPopup: AIPopup? = null

    private val TOAST_MASSEGE : Int = 0
    private val WRITE_BUCEKT : Int = 10
    private val BUCKET_LIST : Int = 20
    private val SHARE_THE_WORLD : Int = 40
    private val NOTICE : Int = 50
    private val UPDATE_USER : Int = 60
    private val REQUEST_AI : Int = 70
    private val FAIL_AI : Int = 71
    private val RESPOND_AI : Int = 72
    private val CHECK_VERSION : Int = 80

    private val MY_PERMISSION_REQUEST : Int = 1000
    private var mbInitialUserUpdate = false

    internal var mDrawerList: ListView? = null
    internal var mDrawer: DrawerLayout? = null
    lateinit var mAdView : AdView

    private lateinit var mBinding : MainActivityBinding
    /**
     * 사용자 정보업데이트 가공 데이타 만드는 메소드
     *
     * @return 사용자 정보
     */
    private val userData: MobileUser
        get() {
            val mobileUser = MobileUser()
            val userNickName = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
            mobileUser.userNickName = userNickName
            mobileUser.versionName = AppUtils.getVersionName(this)
            mobileUser.lanuage = AppUtils.getUserPhoneLanuage(this)
            mobileUser.country = AppUtils.getUserPhoneCoutry(this)
            val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, Date())
            mobileUser.createDt = date
            val gcmToken = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_FCM, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
            mobileUser.gcmToken = gcmToken!!
            return mobileUser
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        initialize()

        val confDatas = resources.getStringArray(R.array.confList)
        confDatas[7] += ": " + AppUtils.getVersionName(this)!!
        mDrawerList?.adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, confDatas)


        mDrawerList?.onItemClickListener = DrawerItemClickListener()

        mHandler = Handler(this)
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogUtils.UncaughtExceptionHandlerApplication())

        val sqlQuery = SQLQuery()
        sqlQuery.createTable(applicationContext)
        sqlQuery.createChatTable(applicationContext)
        sqlQuery.createImageTable(applicationContext)

        MobileAds.initialize(this, ContextUtils.KBUCKET_AD_UNIT_ID)
        mAdView = findViewById<View>(R.id.main_ad_layout) as AdView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d("mhkim", "@@ onAdLoaded  ")
            }

            override fun onAdFailedToLoad(errorCode : Int) {
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

        val getIntent = intent

        Log.d("mhkim", "@@ getIntent : " + getIntent)
        val data = getIntent.getStringExtra(ContextUtils.WIDGET_SEND_DATA)
        if (data != null && data == ContextUtils.WIDGET_SHARE) {
            ShareSocial()
        }
        checkPermision()
        mHandler!!.sendEmptyMessage(CHECK_VERSION)

        AppUtils.sendTrackerScreen(this, "메인화면")

        mBinding.mainWriteBtn.setOnClickListener(this)
        mBinding.mainUpdateBtn.setOnClickListener(this)
        mBinding.mainAiBtn.setOnClickListener(this)
        mBinding.mainListBtn.setOnClickListener(this)
        mBinding.mainBucketlistBtn.setOnClickListener(this)
        mBinding.mainConfBtn.setOnClickListener(this)
        mBinding.mainBucketRankBtn.setOnClickListener(this)
    }

    private fun initialize() {
        FirebaseApp.initializeApp(this)

        setBackgroundColor()

        mDrawerList = findViewById<ListView>(R.id.drawer_list)
        mDrawer = findViewById<DrawerLayout>(R.id.dl_activity_main_drawer)
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
           // findViewById<View>(R.id.main_back_color).setBackgroundColor(color)
            mBinding.mainBackColor.setBackgroundColor(color)
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
            mHandler!!.sendEmptyMessage(UPDATE_USER)
        }
    }


    @SuppressLint("WrongConstant")
    override fun onClick(view: View?) {
        KLog.d(ContextUtils.TAG, "@@ onClick ")
        backKeyPressedTime = 0
        when (view?.id) {
            R.id.main_writeBtn -> mHandler!!.sendEmptyMessage(WRITE_BUCEKT)
            R.id.main_listBtn -> mHandler!!.sendEmptyMessage(BUCKET_LIST)
            R.id.main_bucketlistBtn -> mHandler!!.sendEmptyMessage(SHARE_THE_WORLD)
            R.id.main_conf_btn -> if (!mDrawer!!.isDrawerOpen(Gravity.START)) {
                mDrawer!!.openDrawer(Gravity.START)
            }
            R.id.main_update_btn -> mHandler!!.sendEmptyMessage(NOTICE)
            R.id.main_ai_btn -> {
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                mHandler!!.sendEmptyMessage(REQUEST_AI)
            }
            R.id.main_bucketRankBtn -> {
                val intent = Intent(this, RankListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun handleMessage(message: Message): Boolean {
        when (message.what) {
            TOAST_MASSEGE//메시지 출력
            -> Toast.makeText(applicationContext, message.obj as String, Toast.LENGTH_LONG).show()
            WRITE_BUCEKT//버킷 작성
            -> {
                var intent = Intent(this, WriteActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(this, "가지작성화면")
            }
            BUCKET_LIST//리스트 목록 보여주기
            -> {
                intent = Intent(this, BucketListActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(this, "완료가지화면")
            }
            SHARE_THE_WORLD//공유화면 보여주기
            -> {
                intent = Intent(this, ShareListActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(this, "모두가지화면")
            }
            NOTICE//공지사항 화면 보여주기
            -> {
                intent = Intent(this, NoticeActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(this, "공지화면")
            }
            UPDATE_USER//사용자 정보 없데이트
            -> {
                val userUpdateTask =
                    UserUpdateTask(userData)
                userUpdateTask.execute()
            }
            REQUEST_AI -> {
                val userNickName = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                val httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_AI, true, this, IHttpReceive.REQUEST_AI)
                val map = HashMap<String, Any>()
                map["nickname"] = userNickName!!
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            FAIL_AI -> {
                KProgressDialog.setDataLoadingDialog(this, false, null, false)
                val title = getString(R.string.popup_title)
                val content = getString(R.string.popup_prepare_string)
                mBasicPopup = BasicPopup(this, title, content, R.layout.popup_basic, this, OnPopupEventListener.POPUP_BASIC)
                mBasicPopup!!.showDialog()
            }
            RESPOND_AI// AI 대답
            -> {
                KProgressDialog.setDataLoadingDialog(this, false, null, false)
                val content : String = message.obj as String
                KLog.d(ContextUtils.TAG, "@@ Respond AI msg : " + content)
                mAIPopup = AIPopup(this, content, R.layout.popup_ai, this, OnPopupEventListener.POPUP_AI)
                mAIPopup!!.showDialog()
            }
            CHECK_VERSION//버전 체크
            -> {
                val appUpdateTask =
                    AppUpdateTask(this)
                appUpdateTask.execute()
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
            finishToast = Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT)
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

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == OnPopupEventListener.POPUP_BASIC) {
            if (what == OnPopupEventListener.POPUP_BTN_OK || what == OnPopupEventListener.POPUP_BTN_CLOSEE || what == OnPopupEventListener.POPUP_DISPOSE) {
                mBasicPopup!!.closeDialog()
            }
        }
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive : $obj")
        // 버킷 공유 결과
        val mData = obj as String
        var message: String? = null
        if (actionId == IHttpReceive.REQUEST_AI) {
            if (type == IHttpReceive.HTTP_OK) {
                if (mData.isNotEmpty()) {
                    try {
                        val json = JSONObject(mData)
                        message = json.getString("replay")
                    } catch (e: JSONException) {
                        ErrorLogUtils.saveFileEror("@@ AI Respond jsonException message : " + e.message)
                        mHandler!!.sendEmptyMessage(FAIL_AI)
                    }

                }
                mHandler!!.sendMessage(mHandler!!.obtainMessage(RESPOND_AI, message))
            } else {
                mHandler!!.sendEmptyMessage(FAIL_AI)
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
                intent = Intent(this, TutorialActivity::class.java)
                startActivity(intent)
            }
            6//문의하기
            -> {
                intent = Intent(this, QuestionActivity::class.java)
                startActivity(intent)
            }
            7//버전체크
            -> mHandler!!.sendEmptyMessage(CHECK_VERSION)
            8//공유하기
            -> ShareSocial()
            9//관심 버킷 추가하기
            -> {
                intent = Intent(this, AddBucketActivity::class.java)
                startActivity(intent)
            }
        }
        mDrawer!!.closeDrawer(mDrawerList!!)
    }


    private fun checkPermision() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

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
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, "권한을 모두 허용해주셔야 앱을 정상적으로 사용할 수 있습니다."))
                }
            }
        }
    }
}
