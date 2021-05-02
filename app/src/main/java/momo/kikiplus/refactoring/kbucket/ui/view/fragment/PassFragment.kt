package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.PasswordActivityBinding
import momo.kikiplus.deprecated.activity.BucketListActivity
import momo.kikiplus.deprecated.activity.ShareListActivity
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import java.util.*

class PassFragment: Fragment(), IBackReceive,  View.OnClickListener  {

    companion object {
        fun newInstance() = PassFragment()
    }

    private lateinit var binding : PasswordActivityBinding

    private var mButton: Array<Button?> = arrayOfNulls<Button>(15)
    private var mPasswordData: ArrayList<String>? = null
    private var isPasswordset = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.password_activity, container, false)
        binding = PasswordActivityBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.passwordBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mPasswordData = ArrayList()

        mButton[0] = binding.passwordClear as Button
        mButton[1] = binding.passwordBtn1 as Button
        mButton[2] = binding.passwordBtn2 as Button
        mButton[3] = binding.passwordBtn3 as Button
        mButton[4] = binding.passwordBtn4 as Button
        mButton[5] = binding.passwordNum0 as Button
        mButton[6] = binding.passwordNum1 as Button
        mButton[7] = binding.passwordNum2 as Button
        mButton[8] = binding.passwordNum3 as Button
        mButton[9] = binding.passwordNum4 as Button
        mButton[10] = binding.passwordNum5 as Button
        mButton[11] = binding.passwordNum6 as Button
        mButton[12] = binding.passwordNum7 as Button
        mButton[13] = binding.passwordNum8 as Button
        mButton[14] = binding.passwordNum9 as Button

        for (i in 0..14) {
            mButton[i]!!.setOnClickListener(this)
        }

        val getIntent = requireActivity().intent!!
        val setting = getIntent.getStringExtra("SET")
        //암호 설정
        if (setting != null && setting == "SET") {
            isPasswordset = true
        }// 암호 맞추기
        else if (setting != null && setting == "GET") {
            isPasswordset = false
        }
    }

    override fun onBackKey() {
        KLog.log("@@ PassFragment onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_PassFragment_to_MainFragement)
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  PassFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.password_num0, R.id.password_num1, R.id.password_num2, R.id.password_num3, R.id.password_num4, R.id.password_num5, R.id.password_num6, R.id.password_num7, R.id.password_num8, R.id.password_num9 -> {
                val data = (v as Button).text.toString()

                if (mPasswordData!!.size < 4) {
                    mPasswordData!!.add(data)
                    printPassword()
                    setButtonText()
                }
            }
            R.id.password_clear -> {
                val size = mPasswordData!!.size
                if (size > 0 && size <= 4) {
                    mPasswordData!!.remove(mPasswordData!![size - 1])
                    printPassword()
                    setButtonText()
                }
            }
        }
    }

    private fun printPassword() {
        KLog.d("@@ start")
        for (i in mPasswordData!!.indices) {
            KLog.d( "@@ " + i + " 번쨰:" + mPasswordData!![i])
        }
        KLog.d("@@ end")
    }

    private fun setButtonText() {
        KLog.d( "@@ mPasswordData size : " + mPasswordData!!.size)
        for (i in 1..mPasswordData!!.size) mButton[i]?.text = "*"

        for (i in 1..4 - mPasswordData!!.size) {
            KLog.d("@@ delete index : " + (5 - i))
            mButton[5 - i]?.text = ""
        }

        if (mPasswordData!!.size == 4) {
            if (isPasswordset) {
                val pawd = mPasswordData!![0] + "" + mPasswordData!![1] + "" + mPasswordData!![2] + "" + mPasswordData!![3]
                SharedPreferenceUtils.write(requireContext(), PreferConst.KEY_CONF_PASSWORD, pawd)
                onBackKey()
            } else {
                val pawd = mPasswordData!![0] + "" + mPasswordData!![1] + "" + mPasswordData!![2] + "" + mPasswordData!![3]
                val password = SharedPreferenceUtils.read(requireContext(), PreferConst.KEY_CONF_PASSWORD, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                if (pawd != password) {
                    val message = getString(R.string.password_fail_string)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    mPasswordData!!.clear()
                    for (i in 1..4) {
                        mButton[i]?.text = ""
                    }
                } else {
                    val getIntent = requireActivity().intent!!
                    val startView = getIntent.getStringExtra("DATA")
                    if (startView != null && startView == DataConst.WIDGET_WRITE_BUCKET) {
                        //TODO Activity->Fragemt 변경해야함
//                        val intent = Intent(this, WriteActivity::class.java)
//                        startActivity(intent)
//                        finish()
                    } else if (startView != null && startView == DataConst.WIDGET_BUCKET_LIST) {
                        val intent = Intent(context, BucketListActivity::class.java)
                        startActivity(intent)
                        onBackKey()
                    } else if (startView != null && startView == DataConst.WIDGET_OURS_BUCKET) {
                        val intent = Intent(context, ShareListActivity::class.java)
                        startActivity(intent)
                        onBackKey()
                    } else if (startView != null && startView == DataConst.WIDGET_SHARE) {
                        val intent = Intent(context, MainFragmentActivity::class.java)
                        intent.putExtra(DataConst.WIDGET_SEND_DATA, DataConst.WIDGET_SHARE)
                        startActivity(intent)
                        onBackKey()
                    } else {
                        val intent = Intent(context, MainFragmentActivity::class.java)
                        startActivity(intent)
                        onBackKey()
                    }
                }
            }
        }
    }
}