package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.InterestBucketListActivityBinding
import momo.kikiplus.deprecated.adapter.ListAdpater
import momo.kikiplus.deprecated.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.DateUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.view.popup.ConfirmPopup
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import java.util.*

class AddFragement : Fragment(), IBackReceive, View.OnClickListener {

    companion object {
        fun newInstance() = AddFragement()
    }

    private lateinit var binding: InterestBucketListActivityBinding

    private var mBucketDataList: ArrayList<Bucket> =ArrayList()
    private var mDataList: ArrayList<String> = ArrayList()
    private var mListAdapter: ListAdpater? = null

    private var mSqlQuery: SQLQuery? = null
    private var mbVisible = true
    private val mConfirmPopup: ConfirmPopup? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.interest_bucket_list_activity, container, false)
        binding = InterestBucketListActivityBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.bucketlistBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mSqlQuery = SQLQuery()
        val strArray = resources.getStringArray(R.array.dream100)
        for (i in strArray.indices) {
            mDataList.add(strArray[i])
        }
        mListAdapter = ListAdpater(
            requireContext(),
            R.layout.interest_bucket_list_line,
            mDataList,
            this
        )
        binding.interestBucketListListview.adapter = mListAdapter
        binding.interestBucketListAdd.setOnClickListener(this)

    }

    override fun onBackKey() {
        KLog.log("@@ AddFragement onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_AddFragement_to_MainFragement)
        mDataList.clear()
        mBucketDataList.clear()
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  AddFragement onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.interest_bucket_list_add -> {
                mbVisible = !mbVisible

                if (mbVisible) {
                    binding.interestBucketListAdd.setText(R.string.interest_bucket_list_add)
                } else {
                    binding.interestBucketListAdd.setText(R.string.interest_bucket_list_view)
                }
                mListAdapter!!.setDataVisible(mbVisible)
            }
            R.id.bucket_list_modifyBtn//추가
            -> {
                var index = Integer.valueOf(v.tag as String)
                var data = mDataList[index]
                addDBData(data)
            }
            R.id.bucket_list_deleteBtn//삭제
            -> {
                val index = Integer.valueOf(v.tag as String)
                val data = mDataList[index]
                removeDBData(data)
            }
        }
    }

    /**
     * DB 데이타 동기화하기(삭제)
     */
    private fun removeDBData(Content: String) {
        mSqlQuery!!.deleteUserBucket(requireContext(), Content)
        val inContainsBucket = mSqlQuery!!.containsKbucket(requireContext(), Content)
        if (!inContainsBucket) {
            val message = getString(R.string.share_delete_popup_ok)
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * DB 데이타 동기화하기(추가)
     *
     * @param Content 내용
     */
    private fun addDBData(Content: String) {
        val inContainsBucket = mSqlQuery!!.containsKbucket(requireContext(), Content)
        val message: String
        if (!inContainsBucket) {
            val dateTime = Date()
            val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
            mSqlQuery!!.insertUserSetting(requireContext(), Content, date, "N", "")
            message = getString(R.string.share_add_popup_ok)
        } else {
            message = getString(R.string.check_input_bucket_string)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}