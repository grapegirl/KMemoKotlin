package momo.kikiplus.com.kbucket.view.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.ByteUtils
import momo.kikiplus.com.kbucket.Utils.DataUtils
import momo.kikiplus.com.kbucket.Utils.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Bean.PostData
import java.util.*

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : CardViewListAdpater
 * @Description : 카드뷰 리스트 목록 어뎁터
 * @since 2015. 1. 6.
 */
class CardViewListAdpater
/**
 * 생성자
 */
(context: Context, res: Int, list: ArrayList<PostData>,
 /**
  * 클릭 리스너
  */
 private val mClickListener: View.OnClickListener,
 /**
  * 롱 클릭 리스너
  */
 private val mLongClickListener: View.OnLongClickListener) : BaseAdapter() {

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
    private var mListItem: ArrayList<PostData>? = null

    private val noImageBitmap: Bitmap

    private var mBitmapList: ArrayList<Bitmap>? = null

    init {
        mContext = context
        mRes = res
        mListItem = list
        noImageBitmap = ByteUtils.getResBitmap(mContext!!, R.drawable.nophoto)
        setImageBitmap()
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

    private fun setImageBitmap() {
        mBitmapList = ArrayList()
        val sqlQuery = SQLQuery()
        for (i in mListItem!!.indices) {
            val contents = mListItem!![i].contents
            val date = mListItem!![i].date
            var bitmap: Bitmap? = null
            val bytes = sqlQuery.selectImage(mContext!!, contents!!, date!!)
            if (bytes == null) {
                bitmap = noImageBitmap
            } else {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            mBitmapList!!.add(bitmap!!)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(mRes, null)
            viewHolder = ViewHolder()
            viewHolder.leftPanel = convertView!!.findViewById<View>(R.id.left_photo_right_textview_panel) as RelativeLayout
            viewHolder.rightPanel = convertView.findViewById<View>(R.id.left_textview_right_photo_panel) as RelativeLayout
            viewHolder.dateView = convertView.findViewById<View>(R.id.cardview_title_textview) as TextView
            viewHolder.contentView = convertView.findViewById<View>(R.id.cardview_contents_textview) as TextView
            viewHolder.contentView2 = convertView.findViewById<View>(R.id.cardview_contents_textview2) as TextView
            viewHolder.imageView = convertView.findViewById<View>(R.id.cardview_contents_imageview) as ImageView
            viewHolder.imageView2 = convertView.findViewById<View>(R.id.cardview_contents_imageview2) as ImageView

            val typeFace = DataUtils.getHannaFont(mContext!!)
            viewHolder.dateView!!.typeface = typeFace
            viewHolder.contentView!!.typeface = typeFace
            viewHolder.contentView2!!.typeface = typeFace

            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        val date = mListItem!![position].date!! + "   "
        val content = mListItem!![position].contents
        val bitmap = mBitmapList!![position]

        viewHolder.dateView!!.text = date
        if (position % 2 == 0) {
            //짝수인경우
            viewHolder.leftPanel!!.visibility = View.GONE
            viewHolder.rightPanel!!.visibility = View.VISIBLE
            viewHolder.dateView!!.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            viewHolder.contentView2!!.text = content
            viewHolder.imageView2!!.setImageBitmap(bitmap)
        } else {
            //홀수인경우
            viewHolder.leftPanel!!.visibility = View.VISIBLE
            viewHolder.rightPanel!!.visibility = View.GONE
            viewHolder.dateView!!.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            viewHolder.contentView!!.text = content
            viewHolder.imageView!!.setImageBitmap(bitmap)
        }
        convertView.setOnClickListener(mClickListener)
        convertView.setOnLongClickListener(mLongClickListener)
        convertView.id = position
        return convertView
    }

    private class ViewHolder {
        var leftPanel: RelativeLayout? = null
        var rightPanel: RelativeLayout? = null
        var dateView: TextView? = null
        var contentView: TextView? = null
        var contentView2: TextView? = null
        var imageView: ImageView? = null
        var imageView2: ImageView? = null
    }
}
