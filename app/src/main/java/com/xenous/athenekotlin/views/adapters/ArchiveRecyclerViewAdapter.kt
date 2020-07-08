package com.xenous.athenekotlin.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word

class ArchiveRecyclerViewAdapter(
    private val context: Context,
    private val archivedWordList: List<Word>
) : RecyclerView.Adapter<ArchiveRecyclerViewAdapter.ArchiveRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveRecyclerViewHolder {
        return ArchiveRecyclerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_cell_archived_word, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArchiveRecyclerViewHolder, position: Int) {
        val stringBuilder = StringBuilder()
        stringBuilder
            .append(archivedWordList[position].native)
            .append(" - ")
            .append(archivedWordList[position].foreign)

        holder.archiveWordTextView.text = stringBuilder.toString()
    }

    override fun getItemCount(): Int {
        return archivedWordList.size
    }

    inner class ArchiveRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val archiveWordTextView: TextView = itemView.findViewById(R.id.archiveWordTextView)
    }
}