package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.*
import momo.kikiplus.com.kbucket.databinding.ConfActivityBinding
import momo.kikiplus.com.kbucket.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.http.IHttpReceive
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : ConfigurationActivity
 * @Description : 환경설정
 * @since 2015-11-02
 */
class ConfigurationActivity : Activity(), View.OnClickListener, IHttpReceive, android.os.Handler.Callback {

    private var mMarketVersionName: String? = null
    private var mCurrentVersionName: String? = null
    private var mHandler: android.os.Handler? = null

    private val START_VERSION = 10
    private val SHOW_GOOGLE_VERSION = 20
    private val FILE_SELECT_CODE = 30

    private lateinit var mBinder : ConfActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinder = ConfActivityBinding.inflate(layoutInflater)
        setContentView(mBinder.root)

        setBackgroundColor()

        mHandler = android.os.Handler(this)
        mCurrentVersionName = AppUtils.getVersionName(this)
        (findViewById<View>(R.id.conf_current_version) as TextView).text = mCurrentVersionName
        mHandler!!.sendEmptyMessage(START_VERSION)

        (findViewById<View>(R.id.conf_password_on_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_update_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_question_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_import_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_export_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_password_off_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_guide_btn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_userSetting) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.conf_userBackSetting) as Button).setOnClickListener(this)
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.conf_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View) {
        when (v.id) {
            //암호설정
            R.id.conf_password_on_btn -> {
                var intent = Intent(this, PassWordActivity::class.java)
                intent.putExtra("SET", "SET")
                startActivity(intent)
            }
            //암호해제
            R.id.conf_password_off_btn -> {
                var message = getString(R.string.password_cancle_string)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                SharedPreferenceUtils.write(applicationContext, ContextUtils.KEY_CONF_PASSWORD, "")
            }
            //업데이트
            R.id.conf_update_btn -> if (mMarketVersionName == null || mMarketVersionName == "-") {
                var message = getString(R.string.version_fail_string)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            } else if (StringUtils.compareVersion(mCurrentVersionName!!, mMarketVersionName!!) > 0) {
                var message = getString(R.string.version_update_string)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                AppUtils.locationMarket(this, packageName)
            } else {
                var message = getString(R.string.version_lastest_string)
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
            //문의하기
            R.id.conf_question_btn -> {
                intent = Intent(this, QuestionActivity::class.java)
                startActivity(intent)
            }
            //복원하기
            R.id.conf_import_btn -> showFileChooser()
            //백업하기
            R.id.conf_export_btn -> {
                val date = Date()
                val newDBName = DateUtils.getStringDateFormat(DateUtils.KBUCKET_DB_DATE_PATTER, date)
                val isResult = DataUtils.exportDB(newDBName)
                if (isResult) {
                    val mssage = getString(R.string.db_backup_path_string)
                    Toast.makeText(applicationContext, mssage + "\n" + ContextUtils.KEY_FILE_FOLDER + "/" + newDBName + ".db", Toast.LENGTH_LONG).show()
                } else {
                    val mssage = getString(R.string.db_backup_faile_string)
                    Toast.makeText(applicationContext, mssage, Toast.LENGTH_LONG).show()
                }
            }
            //튜토리얼
            R.id.conf_guide_btn -> {
                intent = Intent(this, TutorialActivity::class.java)
                startActivity(intent)
            }
            //별명설정
            R.id.conf_userSetting -> {
                intent = Intent(this, SetNickNameActivity::class.java)
                startActivity(intent)
            }
            //배경 색상 설정
            R.id.conf_userBackSetting -> {
                intent = Intent(this, SetBackColorActivity::class.java)
                startActivity(intent)
            }
        }
    }


    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, " @@ onHttpReceive type:$type, object: $obj")
        if (actionId == IHttpReceive.UPDATE_VERSION) {
            if (type == IHttpReceive.HTTP_OK) {
                val mData = obj as String
                try {
                    val json = JSONObject(mData)
                    mMarketVersionName = json.getString("versionName")
                } catch (e: JSONException) {
                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                    mMarketVersionName = "-"
                }

            } else {
                mMarketVersionName = "-"
            }
            mHandler!!.sendEmptyMessage(SHOW_GOOGLE_VERSION)
        }
    }

    /**
     * 구글 앱스토어에서 버전 명 변환하는 메소드
     *
     * @param data 구글 앱스토어 정보
     * @return 버전명
     */
    @Deprecated("")
    private fun getAppVersionData(data: String): String? {
        var mVer: String?
        val startToken = "softwareVersion\">"
        val endToken = "<"
        val index = data.indexOf(startToken)
        if (index == -1) {
            mVer = null
        } else {
            mVer = data.substring(index + startToken.length, index
                    + startToken.length + 100)
            mVer = mVer.substring(0, mVer.indexOf(endToken)).trim { it <= ' ' }
        }
        return mVer
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            SHOW_GOOGLE_VERSION -> (findViewById<View>(R.id.conf_lastest_version) as TextView).text = mMarketVersionName
            START_VERSION -> {
                val urlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_VERSION_UPDATE_URL, false, this, IHttpReceive.UPDATE_VERSION)
                urlTaskManager.execute()
            }
        }
        return false
    }

    /**
     * 파일 선택
     */
    private fun showFileChooser() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "application/zip"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File"),
                    FILE_SELECT_CODE)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "파일 선택 오류 발생",
                    Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            FILE_SELECT_CODE -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data
                KLog.d(ContextUtils.TAG, "@@ select path : " + uri!!.path!!)
                val isResult = DataUtils.importDB(uri.path!!)
                if (isResult) {
                    val msaage = getString(R.string.db_import_success_string)
                    Toast.makeText(applicationContext, msaage, Toast.LENGTH_LONG).show()
                } else {
                    val msaage = getString(R.string.db_import_fail_string)
                    Toast.makeText(applicationContext, msaage, Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


}