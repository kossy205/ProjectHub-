package com.ProjectHub.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ProjectHub.R
import com.ProjectHub.adapters.LabelColorListItemsAdapter
import com.ProjectHub.databinding.DialogListBinding
import com.ProjectHub.databinding.ItemTaskBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        val binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        setUpRecyclerView(binding)
    }

    private fun setUpRecyclerView(binding: DialogListBinding) {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(context)
        adapter = LabelColorListItemsAdapter(context, list, mSelectedColor)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener {

            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }
        }
    }

    protected abstract fun onItemSelected(color: String)
}