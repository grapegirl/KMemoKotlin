package momo.kikiplus.refactoring.kbucket.ui.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.kbucket.ui.view.recycler.ContentViewHolder

class RecyclerViewAdapter( clickListener: View.OnClickListener) : RecyclerView.Adapter<ContentViewHolder>() {

    var mItems : ArrayList<String> = ArrayList<String>()
    var mClickListener : View.OnClickListener = clickListener

    //뷰 홀더를 생성하고 뷰를 붙여주는 부
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view  = LayoutInflater.from(parent.context)
        .inflate(R.layout.bucket_list_line, parent, false)
        return ContentViewHolder(view)
    }

    override fun getItemCount(): Int {
        KLog.d("@@ getItemCount : " + mItems.size)
        return mItems.size
    }

    //재활용하는 뷰를 호출하여 실행하는 메서드, 뷰 홀더를 전달하고 어댑터는 position 인자의 데이터를 결합한다./
    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val item : String = mItems.get(position)
        holder.mEditBox.setText(item)
        holder.mModButton.tag = position.toString()
        holder.mDelButton.tag = position.toString()
        holder.mModButton.setOnClickListener(mClickListener)
        holder.mDelButton.setOnClickListener(mClickListener)

    }

    fun updateItems(items : ArrayList<String>){
        mItems.clear()
        mItems = items
        KLog.d("@@ updateItems mItems size : ${mItems.size}")
        notifyDataSetChanged()
    }

}