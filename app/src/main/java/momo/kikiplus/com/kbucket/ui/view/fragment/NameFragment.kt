package momo.kikiplus.com.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.SetNicknameActivityBinding
import momo.kikiplus.data.sqlite.SQLQuery
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.finally.DataConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.com.kbucket.ui.view.activity.MainFragmentActivity

class NameFragment : Fragment() , IBackReceive, View.OnClickListener  {

    companion object {
        fun newInstance() = NameFragment()
    }

    private lateinit var binding : SetNicknameActivityBinding
    private var mButton: Button? = null
    private var mSqlQuery: SQLQuery? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.set_nickname_activity, container, false)
        binding = SetNicknameActivityBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.nicknameBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mSqlQuery = SQLQuery()
        mButton = binding.nicknameOkBtn as Button
        mButton!!.setOnClickListener(this)

        val nickname = SharedPreferenceUtils.read(requireContext(), PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        if (nickname != null) {
            ( binding.nicknameEditText as EditText).setText(nickname)
            ( binding.nicknameEditText as EditText).requestFocus(nickname.length)
        }
    }

    override fun onBackKey() {
        KLog.log("@@ NameFragment onBackKey back : " + requireArguments().getString("BACK") )
        (activity as MainFragmentActivity).setBackReceive(null)
        if(requireArguments().getString("BACK") == DataConst.VIEW_MAIN){
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  NameFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    override fun onClick(v: View) {
        var nickname: String? = (binding.nicknameEditText as EditText).text.toString()
        KLog.d("@@ nickname : " + nickname!!)
        nickname = nickname.replace(" ".toRegex(), "")
        if (nickname.isEmpty()) {
            val message = getString(R.string.nickname_fail_string)
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            return
        }
        SharedPreferenceUtils.write(requireContext(), PreferConst.KEY_USER_NICKNAME, nickname)
        if (mSqlQuery!!.containsUserTable(requireContext())) {
            mSqlQuery!!.updateUserNickName(requireContext(), nickname)
        } else {
            mSqlQuery!!.insertUserNickName(requireContext(), nickname)
        }
        onBackKey()
    }
}