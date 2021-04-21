package momo.kikiplus.deprecated.activity

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.ShareDetailActivityBinding
import momo.kikiplus.com.kbucket.databinding.ShareListActivityBinding
import momo.kikiplus.deprecated.adapter.CommentListAdpater
import momo.kikiplus.deprecated.http.HttpUrlTaskManager
import momo.kikiplus.deprecated.http.IHttpReceive
import momo.kikiplus.deprecated.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.*
import momo.kikiplus.refactoring.common.view.KProgressDialog
import momo.kikiplus.refactoring.common.view.popup.ConfirmPopup
import momo.kikiplus.refactoring.common.view.popup.IPopupReceive
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PopupConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.data.vo.Comment
import momo.kikiplus.refactoring.kbucket.ui.view.popup.DetailImagePopup
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : ShareDetailActivity
 * @Description :공유 싱세 화면
 * @since 2015-12-27.
 */
class ShareDetailActivity : Activity(), IHttpReceive, View.OnClickListener, Handler.Callback,
    IPopupReceive {

    private var mHandler: android.os.Handler? = null
    private var mCommentList: ArrayList<Comment>? = null
    private var mListAdapter: CommentListAdpater? = null
    private var mListView: ListView? = null

    private var mBucketNo = -1
    private var mBucket: Bucket? = null
    private var mUserNickname: String? = null
    private val mDetailImageFileName: String? = null

    private val TOAST_MASSEGE = 10
    private val DOWNLOAD_IMAGE = 20
    private val LOAD_COMMENT_LIST = 30
    private val SERVER_LOADING_FAIL = 40
    private val SET_COMMENT_LIST = 50
    private val SET_IMAGE = 60

    private var mSqlQuery: SQLQuery? = null
    private var mConfirmPopup: ConfirmPopup? = null
    private lateinit var mBinding : ShareDetailActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = ShareDetailActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setBackgroundColor()

        KLog.d("@@ oncreate")
        mHandler = Handler(this)
        mCommentList = ArrayList()
        mSqlQuery = SQLQuery()
        val Intent = intent
        val idx = Intent.getStringExtra(DataConst.NUM_SHARE_BUCKET_IDX)
        mBucket = Intent.getParcelableExtra<Bucket>(DataConst.OBJ_SHARE_BUCKET);

        mUserNickname = SharedPreferenceUtils.read(this, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        mHandler!!.sendMessage(mHandler!!.obtainMessage(LOAD_COMMENT_LIST, idx))

        (findViewById<View>(R.id.comment_layout_sendBtn) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.share_add) as Button).setOnClickListener(this)
        (findViewById<View>(R.id.share_contents_imageview) as ImageView).setOnClickListener(this)
        setData(mBucket!!)
        AppUtils.sendTrackerScreen(this, "모두가지상세화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.bucketdetail_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteImageResource()
    }

    /**
     * 데이타 초기화
     */
    private fun setData(bucket: Bucket) {
        KLog.d("@@ setData")
        mBucketNo = bucket.idx
        KLog.log("@@ image exists : " + bucket.imageUrl!!)

        (findViewById<View>(R.id.share_contents_textview) as TextView).text = bucket.content
        (findViewById<View>(R.id.share_title_textview) as TextView).text = bucket.date

        if (bucket.imageUrl != null && bucket.imageUrl != "N") {
            mHandler!!.sendEmptyMessage(DOWNLOAD_IMAGE)
        }
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d( "@@ onHttpReceive actionId: $actionId")
        KLog.d( "@@ onHttpReceive  type: $type")
        KLog.d("@@ onHttpReceive  obj: $obj")
        val mData = obj as String
        var isValid = false
        try {
            val json = JSONObject(mData)
            isValid = json.getBoolean("isValid")
        } catch (e: JSONException) {
            KLog.log("@@ jsonException message : " + e.message)
        }

        if (actionId == IHttpReceive.COMMENT_LIST) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                try {
                    val json = JSONObject(mData)
                    val jsonArray = json.getJSONArray("CommentVOList")
                    KLog.d( "@@ jsonArray :   $jsonArray")
                    val size = jsonArray.length()
                    mCommentList!!.clear()
                    for (i in 0 until size) {
                        val jsonObject = jsonArray.get(i) as JSONObject
                        val comment = Comment()
                        comment.nickName = jsonObject.getString("nickName")
                        comment.date = jsonObject.getString("createDt")
                        comment.content = jsonObject.getString("content")
                        mCommentList!!.add(comment)
                    }
                    mHandler!!.sendEmptyMessage(SET_COMMENT_LIST)
                } catch (e: Exception) {
                    KLog.log("@@ jsonException message : " + e.message)
                    mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                }

            }
        } else if (actionId == IHttpReceive.INSERT_COMMENT) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                mHandler!!.sendMessage(mHandler!!.obtainMessage(LOAD_COMMENT_LIST, mBucketNo))
            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.comment_layout_sendBtn -> {
                val text = (findViewById<View>(R.id.comment_layout_text) as EditText).text.toString()
                if (text.replace(" ".toRegex(), "") == "") {
                   return
                }
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.INSERT_COMMENT_URL, true, this, IHttpReceive.INSERT_COMMENT)
                val map = HashMap<String, Any>()
                map["NICKNAME"] = mUserNickname!!
                map["CONTENT"] = text
                map["BUCKET_NO"] = mBucketNo.toString() + ""
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
                (findViewById<View>(R.id.comment_layout_text) as EditText).setText("")
            }
            R.id.share_contents_imageview -> if (mDetailImageFileName != null) {
                val popup =
                    DetailImagePopup(
                        this,
                        R.layout.popup_img,
                        mDetailImageFileName,
                        window
                    )
                popup.showDialog()
            }
            R.id.share_add -> {
                val title = getString(R.string.share_add_popup_title)
                val content = getString(R.string.share_add_popup_content)
                mConfirmPopup =
                    ConfirmPopup(
                        this,
                        title,
                        content,
                        R.layout.popup_confirm,
                        this,
                        PopupConst.POPUP_BUCKET_ADD
                    )
                mConfirmPopup!!.showDialog()
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            DOWNLOAD_IMAGE -> {
                val target = findViewById<View>(R.id.share_contents_imageview) as ImageView
                target.scaleType = ImageView.ScaleType.FIT_XY
                val url = NetworkConst.KBUCKET_DOWNLOAD_IAMGE + "?idx=" + mBucketNo
                KLog.log("@@ download image url : $url")
                Glide.with(this)
                        .load(url)
                        .into(target)
            }
            LOAD_COMMENT_LIST -> {
                //KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string));
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_COMMENT_URL, true, this, IHttpReceive.COMMENT_LIST)
                val map = HashMap<String, Any>()
                map["idx"] = mBucketNo
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            SET_COMMENT_LIST -> {
                mListView = findViewById<View>(R.id.share_comment_listview) as ListView
                mListAdapter =
                    CommentListAdpater(
                        this,
                        R.layout.comment_list_line,
                        mCommentList!!,
                        this
                    )
                mListView!!.adapter = mListAdapter
            }
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KLog.log("@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                finish()
            }
            SET_IMAGE -> {
                findViewById<View>(R.id.share_contents_loadingbar).visibility = View.INVISIBLE
                try {
                    //Bitmap bitmap = BitmapFactory.decodeFile(mDetailImageFileName);
                    val options = BitmapFactory.Options()
                    options.outWidth = 150
                    options.outHeight = 150
                    val bitmap = BitmapFactory.decodeFile(mDetailImageFileName, options)
                    (findViewById<View>(R.id.share_contents_imageview) as ImageView).scaleType = ImageView.ScaleType.FIT_XY
                    (findViewById<View>(R.id.share_contents_imageview) as ImageView).setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    KLog.log( "@@ set image : " + e.toString())
                }

            }
        }
        return false
    }

    /**
     * 이미지 리소스 해제하기
     */
    private fun deleteImageResource() {
        (findViewById<View>(R.id.share_contents_imageview) as ImageView).setImageBitmap(null)
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == PopupConst.POPUP_BUCKET_ADD) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
                val contents = (findViewById<View>(R.id.share_contents_textview) as TextView).text.toString()
                val inContainsBucket = mSqlQuery!!.containsKbucket(applicationContext, contents)
                if (!inContainsBucket) {
                    val dateTime = Date()
                    val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
                    mSqlQuery!!.insertUserSetting(applicationContext, contents, date, "N", "")
                    mConfirmPopup!!.closeDialog()

                    val message = getString(R.string.share_add_popup_ok)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                } else {
                    val message = getString(R.string.check_input_bucket_string)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                }
            } else {
                mConfirmPopup!!.closeDialog()
            }
        }
    }
}
