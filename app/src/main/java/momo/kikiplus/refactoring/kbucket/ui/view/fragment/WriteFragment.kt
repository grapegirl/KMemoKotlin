package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.WriteFragmentBinding
import momo.kikiplus.com.kbucket.view.Activity.WriteDetailActivity
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.adapter.RecyclerViewAdapter
import momo.kikiplus.refactoring.kbucket.ui.view.fragment.viewmodel.WriteViewModel
import java.util.*

class WriteFragment : Fragment(), View.OnClickListener, View.OnKeyListener {

    companion object {
        fun newInstance() = WriteFragment()
    }

    private lateinit var viewModel: WriteViewModel
    private lateinit var mBinding : WriteFragmentBinding

    private var mAdapter: RecyclerViewAdapter =
        RecyclerViewAdapter(this)
    private var mDataList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.write_fragment, container, false)
        mBinding = WriteFragmentBinding.bind(view)
        setBackgroundColor()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WriteViewModel::class.java)

        mBinding.writeLayoutAddBtn.setOnClickListener(this)
        mBinding.sortDate.setOnClickListener(this)
        mBinding.sortDeadline.setOnClickListener(this)
        mBinding.sortMemo.setOnClickListener(this)
        mBinding.writeLayoutTitleView.setOnKeyListener(this)

        var layoutMgr : LinearLayoutManager = LinearLayoutManager(activity)
        mBinding.writeListListview.layoutManager = layoutMgr
        mBinding.writeListListview.adapter = mAdapter

        setListData()
    }


    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(activity!!.applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.writeBackColor.setBackgroundColor(color)
        }
    }

    override fun onClick(v: View) {
        KLog.d( "@@ onClick id: $v.id")
        when (v.id) {
            R.id.write_layout_addBtn -> {
                val editText = mBinding.writeLayoutTitleView.text.toString()
                if (viewModel.checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(activity!!.applicationContext, message, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addData(editText, activity!!)
                    setListData()
                }
                mBinding.writeLayoutTitleView.setText("")
            }
            // 삭제 버튼
            R.id.bucket_list_deleteBtn -> {
                var index = Integer.valueOf(v.tag as String)
                viewModel.removeData(mDataList[index], activity!!)
                setListData()
            }
            // 수정 버튼
            R.id.bucket_list_modifyBtn -> {
                val index = Integer.valueOf(v.tag as String)
                val intent = Intent(activity!!.applicationContext, WriteDetailActivity::class.java)
                intent.putExtra("CONTENTS", mDataList[index])
                intent.putExtra("BACK", DataConst.VIEW_WRITE)
                startActivity(intent)

                //fragmentManager!!.popBackStack();
            }
            //메모 정렬순
            R.id.sort_memo -> {
                SharedPreferenceUtils.write(activity!!.applicationContext, DataConst.KBUCKET_SORT_KEY, DataConst.SORT_MEMO)
                setListData()
            }
            //날짜 정렬순
            R.id.sort_date -> {
                SharedPreferenceUtils.write(activity!!.applicationContext, DataConst.KBUCKET_SORT_KEY, DataConst.SORT_DATE)
                setListData()
            }
            //기한 정렬순
            R.id.sort_deadline -> {
                SharedPreferenceUtils.write(activity!!.applicationContext, DataConst.KBUCKET_SORT_KEY, DataConst.SORT_DEADLINE)
                setListData()
            }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //엔터입력시 할일 처리
                val editText = mBinding.writeLayoutTitleView.text.toString()
                if (viewModel.checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(activity!!.applicationContext, message, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addData(editText, activity!!)
                    setListData()
                }
                mBinding.writeLayoutTitleView.setText("")
                mBinding.writeLayoutTitleView.nextFocusDownId = R.id.write_layout_titleView
            }

        }
        return false
    }


    private fun setListData() {
        viewModel.initLocalData(activity!!)
        mDataList = viewModel.getListDoing()

        val sortPrf = SharedPreferenceUtils.read(activity!!.applicationContext, DataConst.KBUCKET_SORT_KEY, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        KLog.d("@@ sort sort: $sortPrf")
        if (sortPrf == null) {
            mAdapter.updateItems(mDataList)
            return
        }
        viewModel.sort(sortPrf)
        mDataList.clear()
        mDataList = viewModel.getListDoing()
        mAdapter.updateItems(mDataList)


    }

    override fun onStop() {
        super.onStop()
        mDataList.clear()
    }
}