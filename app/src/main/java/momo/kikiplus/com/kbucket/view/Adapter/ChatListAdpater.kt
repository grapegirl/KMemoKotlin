package momo.kikiplus.com.kbucket.view.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.ContextUtils
import momo.kikiplus.com.kbucket.Utils.DataUtils
import momo.kikiplus.com.kbucket.Utils.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.view.Bean.Chat
import java.util.*

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : ChatListAdpater
 * @Description : 채팅 리스트 목록 어뎁터
 * @since 2017. 02. 26.
 */
class ChatListAdpater
/**
 * 생성자
 */
(context: Context, res: Int, list: ArrayList<Chat>) : BaseAdapter() {

    /**
     * 컨텍스트
     */
    private var mContext: Context? = null

    /**
     * 리소스 아이디
     */
    private var mRes = -1

    /**
     * 리스트 아이템
     */
    private var mListItem: ArrayList<Chat>? = null

    private val noImageBitmap: Bitmap? = null

    private val mBitmapList: ArrayList<Bitmap>? = null

    init {
        mContext = context
        mRes = res
        mListItem = list
    }

    override fun getCount(): Int {
        return if (mListItem != null) {
            mListItem!!.size
        } else -1
    }

    override fun getItem(position: Int): Any? {
        return if (mListItem != null) {
            mListItem!![position]
        } else null
    }

    override fun getItemId(position: Int): Long {
        return if (mListItem != null) {
            position.toLong()
        } else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(mRes, null)
            viewHolder = ViewHolder()
            viewHolder.myView = convertView!!.findViewById<View>(R.id.chat_line_me) as LinearLayout
            viewHolder.myNickname = convertView.findViewById<View>(R.id.comment_list_nickname_me) as TextView
            viewHolder.myContent = convertView.findViewById<View>(R.id.comment_list_text_me) as TextView
            viewHolder.otherView = convertView.findViewById<View>(R.id.chat_line_other) as LinearLayout
            viewHolder.otherNickname = convertView.findViewById<View>(R.id.comment_list_nickname_other) as TextView
            viewHolder.otherContent = convertView.findViewById<View>(R.id.comment_list_text_other) as TextView

            val typeFace = DataUtils.getHannaFont(mContext!!)
            viewHolder.myNickname!!.typeface = typeFace
            viewHolder.otherNickname!!.typeface = typeFace
            viewHolder.myContent!!.typeface = typeFace
            viewHolder.otherContent!!.typeface = typeFace

            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val nickname = mListItem!![position].nickName
        val content = mListItem!![position].content

        val myNickname = SharedPreferenceUtils.read(mContext!!, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        if (nickname != null && myNickname == nickname) {
            viewHolder.otherView!!.visibility = View.INVISIBLE
            viewHolder.myView!!.visibility = View.VISIBLE
            viewHolder.myNickname!!.text = nickname
            viewHolder.myContent!!.text = content
        } else {
            viewHolder.myView!!.visibility = View.INVISIBLE
            viewHolder.otherView!!.visibility = View.VISIBLE
            viewHolder.otherNickname!!.text = nickname
            viewHolder.otherContent!!.text = content
        }
        convertView.id = position
        return convertView
    }

    private class ViewHolder {
        var myView: LinearLayout? = null
        var otherView: LinearLayout? = null
        var myNickname: TextView? = null
        var myContent: TextView? = null
        var otherNickname: TextView? = null
        var otherContent: TextView? = null
    }
}
