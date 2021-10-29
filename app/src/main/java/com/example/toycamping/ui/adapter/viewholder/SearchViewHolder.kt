package com.example.toycamping.ui.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.toycamping.R
import com.example.toycamping.databinding.ItemSearchBinding
import com.example.toycamping.room.CampingEntity

class SearchViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_search, parent, false
    )
) {

    private val binding = ItemSearchBinding.bind(itemView)

    fun bind(
        item: CampingEntity,
        itemClickListener: SearchListener
    ) {

        binding.apply {
            name.text = item.name
            address.text = item.address

            itemView.setOnClickListener {
                itemClickListener.getItemClick(item)
            }
        }
    }
}

interface SearchListener {
    fun getItemClick(item: CampingEntity)
}