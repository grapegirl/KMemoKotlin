package momo.kikiplus.refactoring.kbucket.ui.view.activity

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.deprecated.http.HttpUrlFileUploadManager
import momo.kikiplus.deprecated.http.IHttpReceive
import momo.kikiplus.deprecated.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.*
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import java.util.*

class DBMgrActivity : Activity(), View.OnClickListener, IHttpReceive, Handler.Callback {

    private val FILE_SELECT_CODE = 1000
    private var mHandler: Handler? = null

    private val TOAST_MASSEGE = 10
    private val UPLOAD_DB = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dbmgr)
        initialize()

        mHandler = Handler(this)

        setBtnClickListener()
    }

    private fun initialize() {
        setBackgroundColor()
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(
            applicationContext,
            PreferConst.BACK_MEMO,
            SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER
        ) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.bg_db_view).setBackgroundColor(color)
        }
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
            Toast.makeText(
                this, "파일 선택 오류 발생",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            FILE_SELECT_CODE -> if (resultCode == RESULT_OK) {
                val uri = data.data
                KLog.log("@@ onActivityResult path :  " + uri!!.path!!)

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

    fun setBtnClickListener() {
        val btn1 = findViewById<Button>(R.id.btn_db_menu01)
        btn1.setOnClickListener(this)
        val btn2 = findViewById<Button>(R.id.btn_db_menu02)
        btn2.setOnClickListener(this)
        val btn3 = findViewById<Button>(R.id.btn_db_menu03)
        btn3.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_db_menu01 //DB 원복
            -> showFileChooser()
            R.id.btn_db_menu02 //DB 백업
            -> {
                val date = Date()
                val newDBName =
                    DateUtils.getStringDateFormat(DateUtils.KBUCKET_MEMO_DATE_PATTER, date)
                KLog.log("@@ newDBName: " + newDBName)
                val isResult = DataUtils.exportDB(applicationContext, newDBName)
                KLog.log("@@ isResult: " + isResult)
                if (isResult) {
                    val mssage = getString(R.string.db_backup_path_string)
                    val path = DataConst.KEY_FILE_FOLDER + "/" + newDBName + ".db"
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(UPLOAD_DB, path))
                    Toast.makeText(
                        applicationContext,
                        mssage + "\n"  + "/" + newDBName + ".db",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val message = getString(R.string.db_backup_faile_string)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                }
            }
            R.id.btn_db_menu03 -> {
                val sqlQuery = SQLQuery()
                sqlQuery.DeleteBucketContents(applicationContext)
                val message = getString(R.string.db_delete_bucket)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE//메시지 출력
            -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            UPLOAD_DB // DB 백업
            -> {
                val path = msg.obj as String
                val nIndex = path.indexOf(DataConst.KEY_FILE_FOLDER + "/")
                val fileName = path.substring(nIndex + 6, path.length)
                val userNickName = SharedPreferenceUtils.read(
                    this,
                    PreferConst.KEY_USER_NICKNAME,
                    SharedPreferenceUtils.SHARED_PREF_VALUE_STRING
                ) as String?
                val bytes = ByteUtils.getByteArrayFromFile(context = applicationContext, path)
                val httpUrlFileUploadManager = HttpUrlFileUploadManager(
                    NetworkConst.KBUCKET_UPLOAD_DB_URL,
                    this,
                    IHttpReceive.UPLOAD_DB,
                    bytes!!
                )
                httpUrlFileUploadManager.execute(path, "nickname", userNickName, fileName)
            }
        }
        return false
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d("@@ onHttpReceive : $obj")
        // 버킷 공유 결과
        if (actionId == IHttpReceive.UPLOAD_DB) {
            if (type == IHttpReceive.HTTP_OK) {
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, "메모가지 서버에 DB를 업로드하였습니다\nDB 파일이 필요하시면 문의해주세요"))
            } else {
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, "메모가지 서버에 DB를 업로드하는데 실패하였습니다"))
            }
        }
    }
}