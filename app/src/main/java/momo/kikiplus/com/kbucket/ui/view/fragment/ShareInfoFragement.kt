package momo.kikiplus.com.kbucket.ui.view.fragment

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.ShareDetailActivityBinding
import momo.kikiplus.data.http.HttpUrlTaskManager
import momo.kikiplus.data.http.IHttpReceive
import momo.kikiplus.data.sqlite.SQLQuery
import momo.kikiplus.com.common.util.DateUtils
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.common.util.StringUtils
import momo.kikiplus.com.common.view.KProgressDialog
import momo.kikiplus.com.common.view.popup.ConfirmPopup
import momo.kikiplus.com.common.view.popup.IPopupReceive
import momo.kikiplus.com.kbucket.data.finally.DataConst
import momo.kikiplus.com.kbucket.data.finally.NetworkConst
import momo.kikiplus.com.kbucket.data.finally.PopupConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.data.vo.Bucket
import momo.kikiplus.com.kbucket.data.vo.Comment
import momo.kikiplus.com.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.com.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.com.kbucket.ui.view.adapter.CommentListAdpater
import momo.kikiplus.com.kbucket.ui.view.popup.DetailImagePopup
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class ShareInfoFragement : Fragment(), IBackReceive ,  IHttpReceive, View.OnClickListener, Handler.Callback,
    IPopupReceive {

    companion object {
        fun newInstance() = ShareInfoFragement()
    }
    private var back: String? = null

    private lateinit var binding: ShareDetailActivityBinding

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(arguments != null){
            back = requireArguments().getString("BACK")
            KLog.d("@@ back : "+ back)
        }
        val view = inflater.inflate(R.layout.share_detail_activity, container, false)
        binding = ShareDetailActivityBinding.bind(view)
        setBackgroundColor()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.bucketdetailBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mHandler = Handler(Looper.getMainLooper(), this)
        mCommentList = ArrayList()
        mSqlQuery = SQLQuery()

        mUserNickname = SharedPreferenceUtils.read(requireContext(), PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?

        if(arguments != null){
            val idx = requireArguments().getString(DataConst.NUM_SHARE_BUCKET_IDX)
            mBucket = requireArguments().getParcelable(DataConst.OBJ_SHARE_BUCKET)
            mHandler!!.sendMessage(mHandler!!.obtainMessage(LOAD_COMMENT_LIST, idx))
            setData(mBucket!!)

        }else{
            KLog.d("@@ argument is null")
        }

        (binding.shareComment.commentLayoutSendBtn as Button).setOnClickListener(this)
        (binding.shareAdd as Button).setOnClickListener(this)
        (binding.shareContentsImageview as ImageView).setOnClickListener(this)

    }

    override fun onBackKey() {
        KLog.log("@@ ShareInfoFragement onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        if(back == DataConst.VIEW_SHARE){

            val fragment = ShareFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            bundle.putString("BACK", DataConst.VIEW_MAIN)

            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                //.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_main, fragment)
                .commit()
        }
        deleteImageResource()
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  ShareInfoFragement onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    /**
     * 데이타 초기화
     */
    private fun setData(bucket: Bucket) {
        KLog.d("@@ setData")
        mBucketNo = bucket.idx
        KLog.log("@@ image exists : " + bucket.imageUrl!!)

        (binding.shareContentsTextview as TextView).text = bucket.content
        (binding.shareTitleTextview as TextView).text = bucket.date

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
            KProgressDialog.setDataLoadingDialog(requireContext(), false, null, false)
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
            KProgressDialog.setDataLoadingDialog(requireContext(), false, null, false)
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
                val text = (binding.shareComment.commentLayoutText as EditText).text.toString()
                if (text.replace(" ".toRegex(), "") == "") {
                    return
                }
                KProgressDialog.setDataLoadingDialog(requireContext(), true, this.getString(R.string.loading_string), true)
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.INSERT_COMMENT_URL, true, this, IHttpReceive.INSERT_COMMENT)
                val map = HashMap<String, Any>()
                map["NICKNAME"] = mUserNickname!!
                map["CONTENT"] = text
                map["BUCKET_NO"] = mBucketNo.toString() + ""
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
                (binding.shareComment.commentLayoutText as EditText).setText("")
            }
            R.id.share_contents_imageview -> if (mDetailImageFileName != null) {

                val popup =
                    DetailImagePopup(
                        requireContext(),
                        R.layout.popup_img,
                        mDetailImageFileName,
                        requireActivity().window
                    )
                popup.showDialog()
            }
            R.id.share_add -> {
                val title = getString(R.string.share_add_popup_title)
                val content = getString(R.string.share_add_popup_content)
                mConfirmPopup =
                    ConfirmPopup(
                        requireContext(),
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
                val target = binding.shareContentsImageview as ImageView
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
                mListView = binding.shareCommentListview as ListView
                mListAdapter =
                    CommentListAdpater(
                        requireContext(),
                        R.layout.comment_list_line,
                        mCommentList!!,
                        this
                    )
                mListView!!.adapter = mListAdapter
            }
            TOAST_MASSEGE -> Toast.makeText(requireContext(), msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KLog.log("@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                onBackKey()
            }
            SET_IMAGE -> {
                binding.shareContentsLoadingbar.visibility = View.INVISIBLE
                try {
                    //Bitmap bitmap = BitmapFactory.decodeFile(mDetailImageFileName);
                    val options = BitmapFactory.Options()
                    options.outWidth = 150
                    options.outHeight = 150
                    val bitmap = BitmapFactory.decodeFile(mDetailImageFileName, options)
                    (binding.shareContentsImageview as ImageView).scaleType = ImageView.ScaleType.FIT_XY
                    (binding.shareContentsImageview as ImageView).setImageBitmap(bitmap)
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
        (binding.shareContentsImageview as ImageView).setImageBitmap(null)
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == PopupConst.POPUP_BUCKET_ADD) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
                val contents = (binding.shareContentsTextview as TextView).text.toString()
                val inContainsBucket = mSqlQuery!!.containsKbucket(requireContext(), contents)
                if (!inContainsBucket) {
                    val dateTime = Date()
                    val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
                    mSqlQuery!!.insertUserSetting(requireContext(), contents, date, "N", "")
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