package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainActivityBinding
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.util.AppUtils


/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : MainActivity
 * @Description : 메인 목록
 * @since 2015-07-22.
 */
class MainActivity : Activity() {

    internal var mDrawerList: ListView? = null
    internal var mDrawer: DrawerLayout? = null
    lateinit var mAdView : AdView

    private lateinit var mBinding : MainActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mDrawerList = findViewById<ListView>(R.id.drawer_list)
        mDrawer = findViewById<DrawerLayout>(R.id.dl_activity_main_drawer)

        val confDatas = resources.getStringArray(R.array.confList)
        confDatas[7] += ": " + AppUtils.getVersionName(this)!!
        mDrawerList?.adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, confDatas)
        mDrawerList?.onItemClickListener = DrawerItemClickListener()

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
            9//관심 버킷 추가하기
            -> {
                intent = Intent(this, AddBucketActivity::class.java)
                startActivity(intent)
            }
        }
        mDrawer!!.closeDrawer(mDrawerList!!)
    }



}
