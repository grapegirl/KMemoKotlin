package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.ShareListActivityBinding
import momo.kikiplus.com.kbucket.view.Adapter.ShareListAdpater
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.modify.http.HttpUrlTaskManager
import momo.kikiplus.modify.http.IHttpReceive
import momo.kikiplus.refactoring.net.CategoryList
import momo.kikiplus.refactoring.net.NetRetrofit
import momo.kikiplus.refactoring.obj.KProgressDialog
import momo.kikiplus.refactoring.util.KLog
import momo.kikiplus.refactoring.util.NetworkUtils
import momo.kikiplus.refactoring.util.StringUtils
import momo.kikiplus.refactoring.vo.Bucket
import momo.kikiplus.refactoring.vo.Category
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : ShareListActivity
 * @Description :공유 목록
 * @since 2015-12-22.
 */
class ShareListActivity : Activity(), IHttpReceive, View.OnClickListener, Handler.Callback {

    private var mCategoryList: ArrayList<Category> = ArrayList()
    private var mHandler: Handler = Handler(this)
    private val mButton = arrayOfNulls<Button>(9)

    private var mBucketDataList: ArrayList<Bucket> = ArrayList()
    private var mListAdapter: ShareListAdpater? = null
    private var mListView: ListView? = null

    private val TOAST_MASSEGE = 10
    private val CATEGORY_LIST = 20
    private val SET_CATEGORY = 30
    private val SERVER_LOADING_FAIL = 40
    private val SHARE_BUCKET_LIST = 50
    private val SET_BUCKETLIST = 60
    private val CHECK_NETWORK = 70

    private lateinit var mBinding : ShareListActivityBinding
    private lateinit var mContext : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = ShareListActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mContext = applicationContext

        setBackgroundColor()
        mHandler.sendEmptyMessage(CHECK_NETWORK)

    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.share_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive actionId: $actionId")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive  type: $type")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive  obj: $obj")
        val mData = obj as String
        var isValid = false
        if (mData.length > 0) {
            try {
                val json = JSONObject(mData)

                isValid = json.getBoolean("isValid")
            } catch (e: JSONException) {
                KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
            }

        }
        if (actionId == IHttpReceive.CATEGORY_LIST) {

        } else if (actionId == IHttpReceive.Companion.BUCKET_LIST) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                try {
                    val json = JSONObject(mData)
                    val jsonArray = json.getJSONArray("bucketList")
                    KLog.d(this.javaClass.simpleName, "@@ jsonArray :   $jsonArray")
                    val size = jsonArray.length()
                    mBucketDataList.clear()
                    for (i in 0 until size) {
                        val jsonObject = jsonArray.get(i) as JSONObject
                        val bucket = Bucket()
                        bucket.content = jsonObject.getString("content")
                        bucket.idx = jsonObject.getInt("idx")
                        bucket.imageUrl = jsonObject.getString("imageUrl")
                        bucket.nickName = jsonObject.getString("nickName")
                        var category : Category = Category("", jsonObject.getInt("categoryCode"))
                        bucket.category = category
                        bucket.date = jsonObject.getString("createDt")

                        mBucketDataList.add(bucket)
                    }
                    mHandler.sendEmptyMessage(SET_BUCKETLIST)
                } catch (e: JSONException) {
                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                    mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
                }

            } else {
                mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            CATEGORY_LIST -> {
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)

                val res = NetRetrofit.instance.service.cateryList
                res.enqueue(object : retrofit2.Callback<CategoryList> {
                    override fun onResponse(call: Call<CategoryList>, response: Response<CategoryList>) {
                        if (response.body()!!.bIsValid) {
                            KProgressDialog.setDataLoadingDialog(mContext, false, null, false)
                                try {
                                    val size = response.body()!!.categoryList.size
                                    if(size > 0){
                                        mCategoryList.clear()
                                        for (i in 0 until size) {
                                            val item : CategoryList.Category = response.body()!!.categoryList.get(i)
                                            var category : Category = Category(item.mCategoryName!!, item.mCategoryCode)
                                           mCategoryList.add(category)
                                        }
                                    }
                                    mHandler.sendEmptyMessage(SET_CATEGORY)
                                } catch (e: JSONException) {
                                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                                    mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
                                }
                        }
                    }

                    override fun onFailure(call: Call<CategoryList>, t: Throwable) {
                        KLog.d(ContextUtils.TAG, "@@ NoticeList onFailure call : " + call.request())
                        KLog.d(ContextUtils.TAG, "@@ NoticeList onFailure message : " + t.message)
                        mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
                    }
                })

            }
            SET_CATEGORY -> {
                setButton()
                findViewById<View>(R.id.share_category_view).visibility = View.VISIBLE
                KLog.d(ContextUtils.TAG, "@@ SET_CATEGORY")
                mHandler.sendEmptyMessage(SHARE_BUCKET_LIST)
            }
            SERVER_LOADING_FAIL -> {
            }
            SHARE_BUCKET_LIST -> {
                var data = msg.obj as String?
                if (data == null) {
                    data = ContextUtils.DEFULAT_SHARE_BUCKET_IDX
                    setButtonSelected(R.id.category_item0)
                }
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                val httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_BUCKET_LIST_URL, true, this, IHttpReceive.BUCKET_LIST)
                val map = HashMap<String, Any>()
                map["idx"] = data
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            SET_BUCKETLIST -> {
                mListView = findViewById<View>(R.id.share_list_listview) as ListView
                mListAdapter = ShareListAdpater(this, R.layout.share_list_line,
                    mBucketDataList, this)
                mListView!!.adapter = mListAdapter
            }
            CHECK_NETWORK -> {
                val isConnect = NetworkUtils.isConnectivityStatus(this)
                if (!isConnect) {
                    val connectMsg = getString(R.string.check_network)
                    mHandler.sendMessage(mHandler.obtainMessage(TOAST_MASSEGE, connectMsg))
                } else {
                    mHandler.sendEmptyMessage(CATEGORY_LIST)
                }
            }
        }
        return false
    }

    /**
     * 버튼에 해당 키값 설정하기
     */
    private fun setButton() {
        mButton[0] = findViewById<View>(R.id.category_item0) as Button
        mButton[1] = findViewById<View>(R.id.category_item1) as Button
        mButton[2] = findViewById<View>(R.id.category_item2) as Button
        mButton[3] = findViewById<View>(R.id.category_item3) as Button
        mButton[4] = findViewById<View>(R.id.category_item4) as Button
        mButton[5] = findViewById<View>(R.id.category_item5) as Button
        mButton[6] = findViewById<View>(R.id.category_item6) as Button
        mButton[7] = findViewById<View>(R.id.category_item7) as Button
        mButton[8] = findViewById<View>(R.id.category_item8) as Button

        for (i in mButton.indices) {
            mButton[i]!!.setOnClickListener(this)
            mButton[i]!!.text = mCategoryList[i].categoryName
            mButton[i]!!.tag = mCategoryList[i].categoryCode
        }
    }

    /**
     * 선택한 카데고리 버튼 색상 변경하는 메소드
     *
     * @param id
     */
    private fun setButtonSelected(id: Int) {
        for (i in mButton.indices) {
            if (mButton[i]!!.id == id) {
                mButton[i]!!.setBackgroundColor(-0x1)
                mButton[i]!!.setTextColor(-0x663400)
            } else {
                mButton[i]!!.setBackgroundColor(-0x663400)
                mButton[i]!!.setTextColor(-0x1)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.category_item0, R.id.category_item1, R.id.category_item2, R.id.category_item3, R.id.category_item4, R.id.category_item5, R.id.category_item6, R.id.category_item7, R.id.category_item8 -> {
                val tag = v.tag as Int
                setButtonSelected(v.id)
                mHandler.sendMessage(mHandler.obtainMessage(SHARE_BUCKET_LIST, tag.toString() + ""))
            }
            R.id.share_list_detailBtn -> {
                val sharedIdx = v.tag as Int
                val idx = mBucketDataList[sharedIdx].idx
                KLog.log("@@ onclick detail idx : " + idx)
                val intent = Intent(this, ShareDetailActivity::class.java)
                intent.putExtra(ContextUtils.NUM_SHARE_BUCKET_IDX, idx.toString() + "")
                intent.putExtra(ContextUtils.OBJ_SHARE_BUCKET, mBucketDataList[sharedIdx])
                startActivity(intent)
            }
        }
    }
}