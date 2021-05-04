package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.ShareListActivityBinding
import momo.kikiplus.deprecated.adapter.ShareListAdpater
import momo.kikiplus.deprecated.http.HttpUrlTaskManager
import momo.kikiplus.deprecated.http.IHttpReceive
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.NetworkUtils
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.util.StringUtils
import momo.kikiplus.refactoring.common.view.KProgressDialog
import momo.kikiplus.refactoring.kbucket.action.net.CategoryList
import momo.kikiplus.refactoring.kbucket.action.net.NetRetrofit
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.data.vo.Category
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*

class ShareFragment : Fragment(), IBackReceive, IHttpReceive, View.OnClickListener,
    Handler.Callback {

    companion object {
        fun newInstance() = ShareFragment()
    }

    private lateinit var binding: ShareListActivityBinding
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.share_list_activity, container, false)
        binding = ShareListActivityBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.log("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(
            requireContext(),
            PreferConst.BACK_MEMO,
            SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER
        ) as Int?)!!
        if (color != -1) {
            binding.shareBackColor.setBackgroundColor(color)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mHandler.sendEmptyMessage(CHECK_NETWORK)
    }

    override fun onBackKey() {
        KLog.log("@@ ShareFragment onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_ShareFragement_to_MainFragement)
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  ShareFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }


    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d("@@ onHttpReceive actionId: $actionId")
        KLog.d("@@ onHttpReceive  type: $type")
        KLog.d("@@ onHttpReceive  obj: $obj")
        val mData = obj as String
        var isValid = false
        if (mData.length > 0) {
            try {
                val json = JSONObject(mData)

                isValid = json.getBoolean("isValid")
            } catch (e: JSONException) {
                KLog.log("@@ jsonException message : " + e.message)
            }

        }
        if (actionId == IHttpReceive.Companion.BUCKET_LIST) {
            KProgressDialog.setDataLoadingDialog(requireContext(), false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                try {
                    val json = JSONObject(mData)
                    val jsonArray = json.getJSONArray("bucketList")
                    KLog.d("@@ jsonArray :   $jsonArray")
                    val size = jsonArray.length()
                    mBucketDataList.clear()
                    for (i in 0 until size) {
                        val jsonObject = jsonArray.get(i) as JSONObject
                        val bucket = Bucket()
                        bucket.content = jsonObject.getString("content")
                        bucket.idx = jsonObject.getInt("idx")
                        bucket.imageUrl = jsonObject.getString("imageUrl")
                        bucket.nickName = jsonObject.getString("nickName")
                        var category: Category = Category("", jsonObject.getInt("categoryCode"))
                        bucket.category = category
                        bucket.date = jsonObject.getString("createDt")

                        mBucketDataList.add(bucket)
                    }
                    mHandler.sendEmptyMessage(SET_BUCKETLIST)
                } catch (e: JSONException) {
                    KLog.log("@@ jsonException message : " + e.message)
                    mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
                }

            } else {
                mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE -> Toast.makeText(
                requireContext(),
                msg.obj as String,
                Toast.LENGTH_LONG
            ).show()
            CATEGORY_LIST -> {
                KProgressDialog.setDataLoadingDialog(
                    requireContext(),
                    true,
                    this.getString(R.string.loading_string),
                    true
                )

                val res = NetRetrofit.instance.service.cateryList
                res.enqueue(object : retrofit2.Callback<CategoryList> {
                    override fun onResponse(
                        call: Call<CategoryList>,
                        response: Response<CategoryList>
                    ) {
                        if (response.body()!!.bIsValid) {
                            KProgressDialog.setDataLoadingDialog(requireContext(), false, null, false)
                            try {
                                val size = response.body()!!.categoryList.size
                                if (size > 0) {
                                    mCategoryList.clear()
                                    for (i in 0 until size) {
                                        val item: CategoryList.Category =
                                            response.body()!!.categoryList.get(i)
                                        var category: Category =
                                            Category(item.mCategoryName!!, item.mCategoryCode)
                                        mCategoryList.add(category)
                                    }
                                }
                                mHandler.sendEmptyMessage(SET_CATEGORY)
                            } catch (e: JSONException) {
                                KLog.log("@@ jsonException message : " + e.message)
                                mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
                            }
                        }
                    }

                    override fun onFailure(call: Call<CategoryList>, t: Throwable) {
                        KLog.log("@@ NoticeList onFailure call : " + call.request())
                        KLog.log("@@ NoticeList onFailure message : " + t.message)
                        mHandler.sendEmptyMessage(SERVER_LOADING_FAIL)
                    }
                })

            }
            SET_CATEGORY -> {
                setButton()

                binding.shareCategoryView.shareCategoryView.visibility = View.VISIBLE
                KLog.log("@@ SET_CATEGORY")
                mHandler.sendEmptyMessage(SHARE_BUCKET_LIST)
            }
            SERVER_LOADING_FAIL -> {
            }
            SHARE_BUCKET_LIST -> {
                var data = msg.obj as String?
                if (data == null) {
                    data = DataConst.DEFULAT_SHARE_BUCKET_IDX
                    setButtonSelected(R.id.category_item0)
                }
                KProgressDialog.setDataLoadingDialog(
                    requireContext(),
                    true,
                    this.getString(R.string.loading_string),
                    true
                )
                val httpUrlTaskManager = HttpUrlTaskManager(
                    NetworkConst.KBUCKET_BUCKET_LIST_URL,
                    true,
                    this,
                    IHttpReceive.BUCKET_LIST
                )
                val map = HashMap<String, Any>()
                map["idx"] = data
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            SET_BUCKETLIST -> {
                mListView = binding.shareListListview as ListView
                mListAdapter = ShareListAdpater(
                    requireContext(), R.layout.share_list_line,
                    mBucketDataList, this
                )
                mListView!!.adapter = mListAdapter
            }
            CHECK_NETWORK -> {
                val isConnect = NetworkUtils.isConnectivityStatus(requireContext())
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
        mButton[0] = binding.shareCategoryView.categoryItem0 as Button
        mButton[1] = binding.shareCategoryView.categoryItem1 as Button
        mButton[2] = binding.shareCategoryView.categoryItem2 as Button
        mButton[3] = binding.shareCategoryView.categoryItem3 as Button
        mButton[4] = binding.shareCategoryView.categoryItem4 as Button
        mButton[5] = binding.shareCategoryView.categoryItem5 as Button
        mButton[6] = binding.shareCategoryView.categoryItem6 as Button
        mButton[7] = binding.shareCategoryView.categoryItem7 as Button
        mButton[8] = binding.shareCategoryView.categoryItem8 as Button

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
                KLog.log("@@ onclick detail mBucketDataList[sharedIdx] : " + mBucketDataList[sharedIdx])
//                val intent = Intent(this, ShareDetailActivity::class.java)
//                intent.putExtra(DataConst.NUM_SHARE_BUCKET_IDX, idx.toString())
//                intent.putExtra(DataConst.OBJ_SHARE_BUCKET, mBucketDataList[sharedIdx])
//                startActivity(intent)
            }
        }
    }
}