package momo.kikiplus.refactoring.kbucket.ui.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import java.util.*

class BaseExpandableAdapter(c: Context, groupList: ArrayList<String>,
                            childList: ArrayList<ArrayList<String>>
) : BaseExpandableListAdapter() {

    private var groupList: ArrayList<String>? = groupList
    private var childList: ArrayList<ArrayList<String>>? = childList
    private var inflater: LayoutInflater? = null
    private var viewHolder: ViewHolder? = null

    init {

        inflater = LayoutInflater.from(c)
        //groupList = groupList
        //childList = childList
    }

    // 그룹 포지션을 반환한다.
    override fun getGroup(groupPosition: Int): String {
        return groupList!![groupPosition]
    }

    // 그룹 사이즈를 반환한다.
    override fun getGroupCount(): Int {
        return groupList!!.size
    }

    // 그룹 ID를 반환한다.
    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var v: View? = convertView

        if (v == null) {
            viewHolder = ViewHolder()
            v = inflater!!.inflate(R.layout.notice_list_row, parent, false)
            viewHolder!!.tv_groupName = v!!.findViewById<View>(R.id.tv_group) as TextView
            v.tag = viewHolder
        } else {
            viewHolder = v.tag as ViewHolder
        }

        viewHolder!!.tv_groupName!!.text = getGroup(groupPosition)

        return v
    }

    // 차일드뷰를 반환한다.
    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return childList!![groupPosition][childPosition]
    }

    // 차일드뷰 사이즈를 반환한다.
    override fun getChildrenCount(groupPosition: Int): Int {
        return childList!![groupPosition].size
    }

    // 차일드뷰 ID를 반환한다.
    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    // 차일드뷰 각각의 ROW
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        var v: View? = convertView

        if (v == null) {
            viewHolder = ViewHolder()
            v = inflater!!.inflate(R.layout.notice_list_row, null)
            viewHolder!!.tv_childName = v!!.findViewById<View>(R.id.tv_child) as TextView
            v.tag = viewHolder
        } else {
            viewHolder = v.tag as ViewHolder
        }

        viewHolder!!.tv_childName!!.text = getChild(groupPosition, childPosition)

        return v
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    internal inner class ViewHolder {
        var tv_groupName: TextView? = null
        var tv_childName: TextView? = null
    }

}