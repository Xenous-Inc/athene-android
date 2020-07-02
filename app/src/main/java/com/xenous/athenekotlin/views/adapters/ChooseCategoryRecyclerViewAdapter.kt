package com.xenous.athenekotlin.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category

class ChooseCategoryRecyclerViewAdapter(
    private val context: Context,
    private val categoriesList: MutableList<Category>,
    private val onItemClickListener: OnItemClickListener
): RecyclerView.Adapter<ChooseCategoryRecyclerViewAdapter.ChooseCategoryRecyclerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChooseCategoryRecyclerViewHolder {
        return ChooseCategoryRecyclerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.cell_category_choose, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChooseCategoryRecyclerViewHolder, position: Int) {
        holder.categoryNameTextView.text = categoriesList[position].category
        holder.categoryCardView.setOnClickListener { view ->
            onItemClickListener.onClick(view)
        }
    }


    override fun getItemCount(): Int {
        return categoriesList.size
    }

    inner class ChooseCategoryRecyclerViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        var categoryNameTextView: TextView = itemView.findViewById(R.id.categoryChooseTextView)
        var categoryCardView: CardView = itemView.findViewById(R.id.categoryChooseCardView)
    }
}