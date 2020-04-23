package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.BucketListActivityBinding
import momo.kikiplus.com.kbucket.view.Adapter.CardViewListAdpater
import momo.kikiplus.modify.http.HttpUrlFileUploadManager
import momo.kikiplus.modify.http.HttpUrlTaskManager
import momo.kikiplus.modify.http.IHttpReceive
import momo.kikiplus.modify.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.ByteUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.util.StringUtils
import momo.kikiplus.refactoring.common.view.popup.ConfirmPopup
import momo.kikiplus.refactoring.common.view.popup.IPopupReceive
import momo.kikiplus.refactoring.common.view.popup.SpinnerListPopup
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PopupConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.data.vo.Category
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : BucketListActivity
 * @Description : 완료 가지 리스트 목록
 * @since 2015-11-02
 */
class BucketListActivity : Activity(), View.OnClickListener, View.OnLongClickListener,
    IPopupReceive, IHttpReceive, android.os.Handler.Callback {

    private var mDataList: ArrayList<Bucket>? = null
    private var mListAdapter: CardViewListAdpater? = null
    private var mSqlQuery: SQLQuery? = null

    private var mConfirmPopup: ConfirmPopup? = null
    private var mCategoryPopup: SpinnerListPopup? = null

    private var mShareIdx = -1
    private var mImageIdx = -1
    private var mCategory = 1

    private var mHandler: android.os.Handler? = null
    private val TOAST_MASSEGE = 10
    private val UPLOAD_IMAGE = 20
    private val UPLOAD_BUCKET = 30
    private val SELECT_BUCKET_CATEGORY = 40

    private lateinit var mBinder : BucketListActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinder = BucketListActivityBinding.inflate(layoutInflater)
        setContentView(mBinder.root)


        initialize()

        mHandler = android.os.Handler(this)
        mDataList = ArrayList()
        mSqlQuery = SQLQuery()
        setListData()
        Collections.reverse(mDataList)

        mListAdapter = CardViewListAdpater(this, R.layout.cardview_list_line, mDataList!!, this, this)
        mBinder.bucketListListview.adapter = mListAdapter

    }

    private fun initialize() {
        setBackgroundColor()
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        KLog.log("@@ color : $color")
        if (color != -1) {
            findViewById<View>(R.id.bucketlist_back_color).setBackgroundColor(color)
        }
    }


    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * DB 데이타 불러와서 데이타 표시하기
     */
    private fun setListData() {
        val map = mSqlQuery!!.selectKbucket(applicationContext) ?: return
        for (i in map.indices) {
            val memoMap = map[i]
            //KLog.d(this.getClass().getSimpleName(), "@@ memoMap" + memoMap.toString());

            if (memoMap["complete_yn"] == "N") {
                continue
            }
            val postData = Bucket("", memoMap["contents"]!!, memoMap["date"]!!, i)
            postData.imageUrl = memoMap["image_path"]
            postData.completeYN = memoMap["complete_yn"]
            mDataList!!.add(postData)
        }
    }

    override fun onClick(v: View) {
        val index = v.id
        val intent = Intent(this, WriteDetailActivity::class.java)
        intent.putExtra("CONTENTS", mDataList!![index].content)
        intent.putExtra("BACK", DataConst.VIEW_COMPLETE_LIST)
        startActivity(intent)
        finish()
    }

    override fun onLongClick(v: View): Boolean {
        mShareIdx = v.id
        val memo = mDataList!![mShareIdx].content

        val title = getString(R.string.share_popup_title)
        val content = getString(R.string.share_popup_content)
        mConfirmPopup = ConfirmPopup(
            this,
            title,
            ": $memo\n\n $content",
            R.layout.popup_confirm,
            this,
            PopupConst.POPUP_BUCKET_SHARE
        )
        mConfirmPopup!!.showDialog()
        return true
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == PopupConst.POPUP_BUCKET_SHARE) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
                mHandler!!.sendEmptyMessage(SELECT_BUCKET_CATEGORY)
            }
            mConfirmPopup!!.closeDialog()
        } else if (popId == PopupConst.POPUP_BUCKET_CATEGORY) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
                val json = obj as JSONObject
                try {
                    mCategory = Integer.valueOf(json.getString("styleCode"))
                } catch (e: JSONException) {
                    mCategory = 1
                }

                KLog.log("@@ mCategory : $mCategory")
                mHandler!!.sendEmptyMessage(UPLOAD_BUCKET)
            }
            mCategoryPopup!!.closeDialog()
        }
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d("@@ onHttpReceive : $obj")
        KLog.d("@@ onHttpReceive type : $type")
        // 버킷 공유 결과
        val mData = obj as String
        var isValid = false
        if (actionId == IHttpReceive.INSERT_BUCKET) {
            if (type == IHttpReceive.HTTP_FAIL) {
                val message = getString(R.string.write_bucekt_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
            } else {
                if (mData.isNotEmpty()) {
                    try {
                        val json = JSONObject(mData)
                        isValid = json.getBoolean("isValid")
                        mImageIdx = json.getInt("idx")
                    } catch (e: JSONException) {
                        KLog.log("@@ jsonException message : " + e.message)
                    }

                    if (isValid == true) {
                        // 이미지가 있는 경우 전송함
                        if (mDataList!![mShareIdx].imageUrl != null && mDataList!![mShareIdx].imageUrl != "") {
                            mHandler!!.sendEmptyMessage(UPLOAD_IMAGE)
                        } else {
                            val message = getString(R.string.write_bucekt_success_string)
                            mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                        }
                    }
                }
            }
        }// 이미지 업로드 결과
        else if (actionId == IHttpReceive.INSERT_IMAGE) {
            if (type == IHttpReceive.HTTP_FAIL) {
                val message = getString(R.string.upload_image_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
            } else {
                if (mData.isNotEmpty()) {
                    try {
                        val json = JSONObject(mData)
                        isValid = json.getBoolean("isValid")
                    } catch (e: JSONException) {
                        KLog.log( "@@ jsonException message : " + e.message)
                    }

                }
                if (isValid == true) {
                    val message = getString(R.string.write_bucekt_success_string)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                }
            }
        }
    }

    /**
     * 서버로 전송할 데이타 만들기
     *
     * @return 전송 데이타
     */
    private fun shareBucket(): HashMap<String, Any> {
        val bucket = Bucket()
        bucket.category!!.categoryCode = 1
        val userNickName = SharedPreferenceUtils.read(this, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        bucket.nickName = userNickName!!
        bucket.content = mDataList!![mShareIdx].content
        bucket.imageUrl = ""
        bucket.date = mDataList!![mShareIdx].date
        return bucket.toHasnMap()
    }

    /**
     * 서버로 전송할 데이타 만들기
     *
     * @return 전송 데이타
     */
    private fun shareBucketImage(): HashMap<String, Any> {
        val bucket = Bucket()
        val userNickName = SharedPreferenceUtils.read(this, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        bucket.nickName = userNickName!!
        bucket.content = mDataList!![mShareIdx].content
        bucket.imageUrl = ""
        bucket.date = mDataList!![mShareIdx].date
        bucket.category!!.categoryCode = mCategory
        return bucket.toHasnMap()
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            UPLOAD_IMAGE -> {
                val photoPath = mDataList!![mShareIdx].imageUrl
                KLog.log("@@ UPLOAD IMAGE 전송 시작 !")
                if (photoPath != null && photoPath != "") {
                    val bitmap = ByteUtils.getFileBitmap(photoPath)
                    val calendar = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyyMMdd_hhmmss")
                    val fileName = sdf.format(calendar.time)

                    val bytes = ByteUtils.getByteArrayFromBitmap(bitmap)
                    val httpUrlFileUploadManager = HttpUrlFileUploadManager(NetworkConst.KBUCKET_UPLOAD_IMAGE_URL, this, IHttpReceive.INSERT_IMAGE, bytes)
                    httpUrlFileUploadManager.execute(photoPath, "idx", mImageIdx.toString() + "", "$fileName.jpg")
                } else {
                    KLog.log("@@ UPLOAD IMAGE NO !")
                }
            }
            UPLOAD_BUCKET -> {
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_INSERT_BUCKET_URL, true, this, IHttpReceive.INSERT_BUCKET)
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(shareBucketImage()))
            }
            SELECT_BUCKET_CATEGORY -> {
                val title = getString(R.string.category_popup_title)
                val content = getString(R.string.category_popup_content)
                val list = ArrayList<Category>()
                list.add(Category("LIEF", 1))
                list.add(Category("LOVE", 2))
                list.add(Category("WORK", 3))
                list.add(
                    Category(
                        "EDUCATION",
                        4
                    )
                )
                list.add(Category("FAMILY", 5))
                list.add(Category("FINANCE", 6))
                list.add(Category("DEVELOP", 7))
                list.add(Category("HEALTH", 8))
                list.add(Category("ETC", 9))
                mCategoryPopup =
                    SpinnerListPopup(
                        this,
                        title,
                        content,
                        list,
                        R.layout.popupview_spinner_list,
                        this,
                        PopupConst.POPUP_BUCKET_CATEGORY
                    )
                mCategoryPopup!!.showDialog()
            }
        }
        return false
    }

}
