package momo.kikiplus.com.kbucket.ui.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainFragmentActivityBinding
import momo.kikiplus.data.sqlite.SQLQuery
import momo.kikiplus.com.common.util.AppUtils
import momo.kikiplus.com.common.util.DataUtils
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.FireMessingService
import momo.kikiplus.com.kbucket.data.finally.DataConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.ui.view.fragment.AddFragement
import momo.kikiplus.com.kbucket.ui.view.fragment.ColorFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.DoneFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.NameFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.PassFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.RankFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.ShareFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.TutorialFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.UpgradeFragment
import momo.kikiplus.com.kbucket.ui.view.fragment.WriteFragment
import momo.kikiplus.com.kbucket.action.task.AppUpdateTask
import momo.kikiplus.com.kbucket.action.task.UserUpdateTask

class MainFragmentActivity : AppCompatActivity(), Handler.Callback,
    NavigationView.OnNavigationItemSelectedListener{

    private var backKeyPressedTime = 0L
    private var finishToast: Toast? = null

    private var handler: Handler = Handler(Looper.getMainLooper(), this)
    private val CHECK_VERSION           : Int = 1000
    private val MY_PERMISSION_REQUEST   : Int = 1001
    private val TOAST_MASSEGE           : Int = 1002
    private val UPDATE_USER             : Int = 1003
    private val OPEN_DRAWER             : Int = 1004

    private var mbInitialUserUpdate : Boolean = false
    private lateinit var mBinding : MainFragmentActivityBinding

    private var backReceive : IBackReceive? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KLog.log("@@ MainFragmentActivity onCreate")

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
           this.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in_right, R.anim.slide_out_left)
        }else{
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        //뒤로가기 함수 변경
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        mBinding = MainFragmentActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        supportActionBar!!.hide()

        initialize()


        val shortcutAction = intent.action
        if(shortcutAction != null){
            Log.d("KMemo", "@@ MainFragmentActivity shortcutAction : $shortcutAction")
            val bundle = Bundle()
            bundle.putString("BACK", DataConst.VIEW_MAIN)

            when (shortcutAction) {
                DataConst.SHORTCUT_LIST -> {
                    val fragment = WriteFragment()
                    fragment.arguments = bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()
                }
                DataConst.SHORTCUT_RANK -> {
                    val fragment = RankFragment()
                    fragment.arguments =bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()
                }
                DataConst.SHORTCUT_SHARE -> {
                    val fragment = ShareFragment()
                    fragment.arguments =bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()
                }
            }

        }

        val data = intent.getStringExtra(DataConst.WIDGET_SEND_DATA)
        Log.d("KMemo", "@@ MainFragmentActivity WIDGET_SEND_DATA : $data")
        Log.d("KMemo", "@@ MainFragmentActivity DATA : " + intent.getStringExtra("DATA"))
        Log.d("KMemo", "@@ MainFragmentActivity SET : " + intent.getStringExtra("SET"))
        if(data != null){
            val bundle = Bundle()
            bundle.putString(DataConst.WIDGET_SEND_DATA, data)

            when (data) {
                DataConst.WIDGET_WRITE_BUCKET -> {
                    val fragment = WriteFragment()
                    fragment.arguments =bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()

                }
                DataConst.WIDGET_BUCKET_LIST -> {

                    val fragment = DoneFragment()
                    fragment.arguments =bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()

                }
                DataConst.WIDGET_OURS_BUCKET -> {

                    val fragment = ShareFragment()
                    fragment.arguments =bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()

                }
                DataConst.WIDGET_SHARE -> {
                    shareSocial()
                }
                DataConst.WIDGET_PASS -> {
                    val fragment = PassFragment()
                    fragment.arguments =bundle
                    bundle.putString("SET", "GET")
                    bundle.putString("DATA", intent.getStringExtra("DATA"))
                    bundle.putString(DataConst.WIDGET_SEND_DATA,intent.getStringExtra(DataConst.WIDGET_SEND_DATA) )

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_main, fragment)
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit()
                }
            }
        }

        checkPermision()
        handler.sendEmptyMessage(CHECK_VERSION)

        AppUtils.sendTrackerScreen(this, "메인화면")
    }

    private fun initialize() {
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true;

        //throw RuntimeException("Test Crash");

        val sqlQuery = SQLQuery()
        sqlQuery.createTable(applicationContext)
        sqlQuery.createChatTable(applicationContext)
        sqlQuery.createImageTable(applicationContext)

        setBackgroundColor()

        mBinding.drawerList.setNavigationItemSelectedListener(this)
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        Log.d("KMemo", "@@ MainFragmentActivity setBackgroundColor : $color")

        if (color != -1) {
            mBinding.mainFragementActivityBackground.setBackgroundColor(color)
        }
    }

    override fun onStart() {
        super.onStart()

        var userNickName = SharedPreferenceUtils.read(this, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        val sqlQuery = SQLQuery()
        val list = sqlQuery.selectUserTable(applicationContext)
        val strDBNickName = list?.get("nickname")
        Log.d("KMemo", "@@ MainFragmentActivity strDBNickName : $strDBNickName")
        if (strDBNickName != null) {
            SharedPreferenceUtils.write(this, PreferConst.KEY_USER_NICKNAME, strDBNickName)
            userNickName = strDBNickName
        }

        if (userNickName == null || userNickName == "null") {
            changeMenu(3)
        }

        val strToken = SharedPreferenceUtils.read(this, PreferConst.KEY_USER_FCM, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        KLog.log("@@ onStart strToken : $strToken")
        if(strToken == null){
            val intent = Intent(this, FireMessingService::class.java)
            startService(intent)
        }

        if (!mbInitialUserUpdate && userNickName != null && strToken != null) {
            mbInitialUserUpdate = true
            handler.sendEmptyMessage(UPDATE_USER)
        }
    }
    override fun handleMessage(msg: Message): Boolean {
        KLog.d("@@ MainFragmentActivity msg : " + msg.what)
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
    fun shareSocial() {
        val msg = Intent(Intent.ACTION_SEND)
        msg.addCategory(Intent.CATEGORY_DEFAULT)
        msg.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.share_title))
        msg.putExtra(Intent.EXTRA_TEXT, this.getString(R.string.share_contents))
        msg.type = "text/plain"
        startActivity(Intent.createChooser(msg, "공유"))
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            KLog.log("@@ onBackPressed backReceive : $backReceive")
            if(backReceive != null ){
                backReceive!!.onBackKey()
            }else{
                openExitToast()
            }
        }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_REQUEST -> {
                val isReulst = DataUtils.createFolder(context = applicationContext)
                if (!isReulst) {
                    handler.sendMessage(handler.obtainMessage(TOAST_MASSEGE, "권한을 모두 허용해주셔야 앱을 정상적으로 사용할 수 있습니다."))
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        KLog.d("@@ selectItem position : $item")
        KLog.d("@@ selectItem position : "  + item.menuInfo)
        when(item.itemId){
            R.id.menu_item1-> changeMenu(0)
            R.id.menu_item2-> changeMenu(1)
           // R.id.menu_item3-> changeMenu(2)
            R.id.menu_item4-> changeMenu(3)
            R.id.menu_item5-> changeMenu(4)
            R.id.menu_item6-> changeMenu(5)
            R.id.menu_item7-> changeMenu(6)
            R.id.menu_item8-> changeMenu(7)
            R.id.menu_item9-> changeMenu(8)
            R.id.menu_item10-> changeMenu(9)

        }

        mBinding.dlActivityMainDrawer.closeDrawer(GravityCompat.START)
        return true
    }


    fun sendUserEvent(screenName : String){
        AppUtils.sendTrackerScreen(this, screenName)
    }

    fun sendConfEvent() {
        handler.sendEmptyMessage(OPEN_DRAWER)
    }

    private fun changeMenu(position: Int){
        KLog.d("@@ changeMenu position : $position")
        when (position) {
            0//암호설정
            -> {
                val fragment = PassFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                bundle.putString("SET", "SET")

                supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .add(R.id.fragment_main, fragment)
                .commit()

                sendUserEvent("암호설정")
            }
            1//암호해제
            -> {
                val message = getString(R.string.password_cancle_string)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                SharedPreferenceUtils.write(applicationContext, PreferConst.KEY_CONF_PASSWORD, "")
            }
            2//DB 관리
            -> {
                intent = Intent(this, DBMgrActivity::class.java)
                startActivity(intent)
            }
            3//별명설정
            -> {
                val fragment = NameFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()

                sendUserEvent("이름설정")
            }
            4//배경설정
            -> {
                val fragment = ColorFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()
                sendUserEvent("배경설정")

            }
            5//튜토리얼
            -> {
                val fragment = TutorialFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()
            }
            6//문의하기
            -> {
                val fragment = UpgradeFragment()
                val bundle = Bundle()
                fragment.arguments = bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()
                sendUserEvent("문의개선")
            }
            7 -> {
                //현재버전
                handler.sendEmptyMessage(CHECK_VERSION)
            }
            8 ->{
                //공유하기
                shareSocial()
            }
            9//관심 버킷 추가하기
            -> {
                val fragment = AddFragement()
                val bundle = Bundle()
                fragment.arguments = bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()
                sendUserEvent("관심버킷추가화면")
            }
        }
    }
    fun setBackReceive(receive: IBackReceive?){
        KLog.log("@@ setBackReceive receive : $receive")
        backReceive = receive
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        KLog.log("@@ MAIN onActivityResult requestCode : $requestCode")
        KLog.log("@@ MAIN onActivityResult resultCode : $resultCode")
        KLog.log("@@ MAIN onActivityResult data : $data")

        super.onActivityResult(requestCode, resultCode, data)
    }
}
