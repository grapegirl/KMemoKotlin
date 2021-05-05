package momo.kikiplus.refactoring.kbucket.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.StringUtils
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.ui.view.adapter.holder.DetailViewHolder
import java.util.*

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : ShareListAdpater
 * @Description : 리스트 목록 어뎁터
 * @since 2021. 05. 05
 */
class DataListAdapter(context: Context, res: Int, list: ArrayList<Bucket>, clickListener: View.OnClickListener) :
    RecyclerView.Adapter<DetailViewHolder>() {

    private var mContext: Context? = null
    private var mRes = -1
    var mItems: ArrayList<Bucket> = ArrayList<Bucket>()
    open var mClickListener: View.OnClickListener = clickListener


    init {
        mContext = context
        mRes = res
        mItems = list
        mClickListener = clickListener

        if(mRes == R.layout.share_list_line){
            KLog.log("@@ DataListAdpater  R.layout.share_list_line : " + mRes)
        }else{
            KLog.log("@@ DataListAdpater  R.layout.comment_layout : " + mRes)
        }
        KLog.log("@@ DataListAdpater mRes : " + mRes)
        KLog.log("@@ DataListAdpater mDataLsit : " + mItems.size)
    }

    //뷰 홀더를 생성하고 뷰를 붙여주는 부
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.share_list_line, parent, false)
        return DetailViewHolder(view, mRes)
    }

    override fun getItemCount(): Int {
        KLog.d("@@ getItemCount mItems size : ${mItems.size}")
        return mItems.size
    }

    override fun getItemId(position: Int): Long {
        KLog.d("@@ getItemId mItems position : ${position}")
        return if (mItems != null) {
            position.toLong()
        } else 0
    }

    open fun updateItems(items: ArrayList<Bucket>) {
        mItems.clear()
        mItems = items
        KLog.d("@@ updateItems mItems size : ${mItems.size}")
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item: Bucket = mItems.get(position)
        if (mRes == R.layout.comment_layout) {
            val date = item.date
            var convertDate: String?
            try {
                convertDate = StringUtils.convertTime(date!!)
            } catch (e: Exception) {
                convertDate = date
            }

            val data = item.content + "/" + convertDate
            holder.mEditBox.setText(data)
            holder.mButton.setText(item.nickName)
        } else {
            holder.mEditBox.setText(item.content)
            holder.mButton.setOnClickListener(mClickListener)
            holder.mButton.setTag(position)
        }

        holder.mButton.text = item.nickName!!
    }


}
