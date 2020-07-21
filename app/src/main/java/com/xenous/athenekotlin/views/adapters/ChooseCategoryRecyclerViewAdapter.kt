package com.xenous.athenekotlin.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import kotlin.random.Random

class ChooseCategoryRecyclerViewAdapter(
    private val context: Context,
    private val categoriesList: MutableList<Category>,
    private val onItemClickListener: OnItemClickListener
): RecyclerView.Adapter<ChooseCategoryRecyclerViewAdapter.ChooseCategoryRecyclerViewHolder>() {
    private companion object {
        val colors = listOf(
            R.color.colorDoubleDragon,
            R.color.colorCassandora,
            R.color.colorPurple400,
            R.color.colorCassandora,
            R.color.colorPurple800,
            R.color.colorDoubleDragon,
            R.color.colorPurple800
        )
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChooseCategoryRecyclerViewHolder {
        return ChooseCategoryRecyclerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_cell_category_choose, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChooseCategoryRecyclerViewHolder, position: Int) {
        if(position == 0) {
            holder.categoryCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorGrey))
        }
        else {
            val color = colors[Random.nextInt(colors.size)]

            holder.categoryCardView.setCardBackgroundColor(ContextCompat.getColor(context, color))
        }
        holder.categoryNameTextView.text = categoriesList[position].title
        holder.categoryCardView.setOnClickListener { view ->
            onItemClickListener.onClick(view, categoriesList[position])
        }
    }


    override fun getItemCount(): Int {
        return categoriesList.size
    }

    inner class ChooseCategoryRecyclerViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryChooseTextView)
        val categoryCardView: CardView = itemView.findViewById(R.id.categoryChooseCardView)
    }
}