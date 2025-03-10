package momo.kikiplus.com.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.PasswordActivityBinding
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.finally.DataConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.com.kbucket.ui.view.activity.MainFragmentActivity
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
        if(arguments != null){
            val setting  = requireArguments().getString("SET")
            //암호 설정
            if (setting != null && setting == "SET") {
                isPasswordset = true
            }// 암호 맞추기
            else if (setting != null && setting == "GET") {
                isPasswordset = false
            }
        }else{
            KLog.d("@@ argument is null")
        }
        KLog.d("@@ PassFragment argument is isPasswordset : " + isPasswordset)
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
    }

    override fun onBackKey() {
        KLog.log("@@ PassFragment onBackKey back : " + requireArguments().getString("BACK") )
        if(isPasswordset) {
            (activity as MainFragmentActivity).setBackReceive(null)
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
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
                    val startView = requireArguments().getString("DATA")
                    KLog.d( "@@ startView : " +  requireArguments().getString("DATA"))
                    val bundle = Bundle()

                    if (startView != null && startView == DataConst.WIDGET_WRITE_BUCKET) {
                        val fragment = WriteFragment()
                        fragment.arguments =bundle
                        bundle.putString("BACK", DataConst.VIEW_MAIN)

                        (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.fragment_main, fragment)
                            .commit()

                    } else if (startView != null && startView == DataConst.WIDGET_BUCKET_LIST) {
                        val fragment = DoneFragment()
                        fragment.arguments =bundle
                        bundle.putString("BACK", DataConst.VIEW_MAIN)

                        (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.fragment_main, fragment)
                            .commit()

                    } else if (startView != null && startView == DataConst.WIDGET_OURS_BUCKET) {
                        val fragment = ShareFragment()
                        fragment.arguments =bundle
                        bundle.putString("BACK", DataConst.VIEW_MAIN)

                        (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.fragment_main, fragment)
                            .commit()
                        
                    } else if (startView != null && startView == DataConst.WIDGET_SHARE) {
                        val fragment = MainFragment()
                        fragment.arguments =bundle
                        bundle.putString("BACK", DataConst.VIEW_MAIN)

                        (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.fragment_main, fragment)
                            .commit()

                        (activity as MainFragmentActivity).shareSocial()
                    } else {
                        (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                            .replace(R.id.fragment_main, MainFragment.newInstance())
                            .commit()

                    }
                }
            }
        }
    }
}