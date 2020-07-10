package com.xenous.athenekotlin.views.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.EditWordActivity
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.threads.DeleteWordThread
import com.xenous.athenekotlin.utils.animateAlphaTo
import com.xenous.athenekotlin.views.AtheneDialog
import com.xenous.athenekotlin.views.MultipleAtheneDialog
import kotlinx.android.synthetic.main.layout_cell_word.view.*

class CategoryWordsRecyclerViewRecyclerViewAdapter(
    private val context: Context,
    private val wordsArrayList: ArrayList<Word>
) : RecyclerView.Adapter<CategoryWordsRecyclerViewRecyclerViewAdapter.CategoryWordsRecyclerViewRecyclerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryWordsRecyclerViewRecyclerViewHolder {
        return CategoryWordsRecyclerViewRecyclerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_cell_word, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: CategoryWordsRecyclerViewRecyclerViewHolder,
        position: Int
    ) {
        var isWordInForeign = true
        var isRunning = false

        holder.itemView.wordCardView.setOnClickListener {
            if(!isRunning) {
                isRunning = true
                if(isWordInForeign) {
                    holder.itemView.wordForeignTextView.animateAlphaTo(0F)
                    holder.itemView.wordNativeTextView.animateAlphaTo(1F, onAnimationEnd = {
                        isWordInForeign = !isWordInForeign
                        isRunning = false
                    })
                }
                else {
                    holder.itemView.wordNativeTextView.animateAlphaTo(0F)
                    holder.itemView.wordForeignTextView.animateAlphaTo(1F, onAnimationEnd = {
                        isWordInForeign = !isWordInForeign
                        isRunning = false
                    })
                }
            }
        }
        holder.itemView.wordCardView.setOnLongClickListener {
            val multipleAtheneDialog = MultipleAtheneDialog(context)

            multipleAtheneDialog.addAction(
                "Изучать",
                object : MultipleAtheneDialog.OnItemClickListener {
                    override fun onItemClick() {
                        multipleAtheneDialog.dismiss()
                        Toast.makeText(context, "Добавлено", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            multipleAtheneDialog.addAction(
                "Редактировать",
                object : MultipleAtheneDialog.OnItemClickListener {
                    override fun onItemClick() {
                        multipleAtheneDialog.dismiss()

                        val intent = Intent(context, EditWordActivity::class.java)
                        intent.putExtra(context.getString(R.string.word_extra), wordsArrayList[position])
                        context.startActivity(intent)
                    }
                }
            )
            multipleAtheneDialog.addAction(
                context.getString(R.string.delete),
                object : MultipleAtheneDialog.OnItemClickListener {
                    override fun onItemClick() {
                        multipleAtheneDialog.dismiss()

                        val atheneDialog = AtheneDialog(context)
                        atheneDialog.message = "Вы точно хотите удалить данное слово?"
                        atheneDialog.positiveText = context.getString(R.string.yes)
                        atheneDialog.negativeText = context.getString(R.string.cancel)
                        atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                            override fun onPositiveClick(view: View) {
                                val deleteWordThread = DeleteWordThread(wordsArrayList[position])
                                deleteWordThread
                                    .setDeleteWordResultListener(object : DeleteWordThread.DeleteWordResultListener {
                                        override fun onSuccess() {
                                            com.xenous.athenekotlin.storage.wordsArrayList.removeAt(position)
                                            wordsArrayList.removeAt(position)
                                            notifyDataSetChanged()
                                        }

                                        override fun onFailure(exception: Exception) {
//                                todo: handle error
                                        }
                                    })
                                deleteWordThread.run()
                            }

                            override fun onNegativeClickListener(view: View) {
                                super.onNegativeClickListener(view)

                                atheneDialog.dismiss()
                            }
                        })
                        atheneDialog.build()
                        atheneDialog.show()
                    }
                }
            )
            multipleAtheneDialog.build()
            multipleAtheneDialog.show()

            return@setOnLongClickListener true
        }
        holder.itemView.wordForeignTextView.text = wordsArrayList[position].foreign
        holder.itemView.wordNativeTextView.text = wordsArrayList[position].native
    }

    override fun getItemCount(): Int {
        return wordsArrayList.size
    }

    inner class CategoryWordsRecyclerViewRecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)
}