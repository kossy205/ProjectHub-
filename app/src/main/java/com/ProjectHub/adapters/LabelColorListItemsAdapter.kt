package com.ProjectHub.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ProjectHub.R
import com.ProjectHub.databinding.ItemCardSelectedMemberBinding
import com.ProjectHub.databinding.ItemLabelColorBinding
import java.util.*

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemLabelColorBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {

            holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                holder.binding.ivSelectedColor.visibility = View.VISIBLE
            } else {
                holder.binding.ivSelectedColor.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {

                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(val binding: ItemLabelColorBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {

        fun onClick(position: Int, color: String)
    }
}