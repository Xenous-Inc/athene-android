package com.xenous.athenekotlin.views.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseNetworkException
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.EditWordActivity
import com.xenous.athenekotlin.activities.LoadingActivity
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.threads.DeleteWordThread
import com.xenous.athenekotlin.threads.UpdateWordThread
import com.xenous.athenekotlin.utils.animateAlphaTo
import com.xenous.athenekotlin.views.AtheneDialog
import com.xenous.athenekotlin.views.MultipleAtheneDialog
import kotlinx.android.synthetic.main.layout_cell_word.view.*

class CategoryWordsRecyclerViewRecyclerViewAdapter(
    private val context: Context,
    var wordsArrayList: ArrayList<Word>
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
            @SuppressLint("HandlerLeak")
            val handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)

                    if(!isRunning) {
                        isRunning = true
                        holder.itemView.wordNativeTextView.animateAlphaTo(0F)
                        holder.itemView.wordForeignTextView.animateAlphaTo(1F, onAnimationEnd = {
                            isWordInForeign = !isWordInForeign
                            isRunning = false
                        })
                    }
                }
            }
            if(!isRunning) {
                isRunning = true
                holder.itemView.wordForeignTextView.animateAlphaTo(0F)
                holder.itemView.wordNativeTextView.animateAlphaTo(1F, onAnimationEnd = {
                    isWordInForeign = !isWordInForeign
                    isRunning = false

                    Thread {
                        Thread.sleep((1000 + wordsArrayList[position].native.length * (3000 / Word.MAX_LENGTH)).toLong())
                        handler.sendEmptyMessage(0)
                    }.start()
                })
            }
        }
        holder.itemView.wordCardView.setOnLongClickListener {
            val multipleAtheneDialog = MultipleAtheneDialog(context)

            multipleAtheneDialog.apply {
                addAction(
                    context.getString(R.string.start_learning),
                    object : MultipleAtheneDialog.OnItemClickListener {
                        override fun onItemClick() {
                            multipleAtheneDialog.dismiss()

                            val word = wordsArrayList[position]

                            if(
                                word.level != Word.LEVEL_ADDED.toLong() &&
                                word.level != Word.LEVEL_ARCHIVED.toLong()
                            ) {
                                val atheneDialog = AtheneDialog(context)
                                atheneDialog.apply {
                                    message = context.getString(R.string.category_word_you_are_already_learning_it_dialog_message)
                                    positiveText = context.getString(R.string.ok)
                                }
                                atheneDialog.build()
                                atheneDialog.show()

                                return
                            }

                            word.level = Word.LEVEL_DAY.toLong()
                            word.setNextDate()

                            val updateWordThread = UpdateWordThread(word)
                            updateWordThread.setUpdateWordThreadListener(object : UpdateWordThread.UpdateWordThreadListener {
                                override fun onSuccess() {
                                    val toast = Toast(context)
                                    toast.view = LayoutInflater.from(context).inflate(R.layout.layout_toast_custom, null, false)
                                    toast.duration = Toast.LENGTH_LONG
                                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                        context.getString(R.string.category_word_successfully_added_to_learning_toast_message)
                                    toast.show()
                                }

                                override fun onFailure(exception: Exception) {
                                    if(exception is FirebaseNetworkException) {
                                        val intent = Intent(context, LoadingActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(intent)

                                        return
                                    }

                                    val toast = Toast(context)
                                    toast.view = LayoutInflater.from(context).inflate(R.layout.layout_toast_custom, null, false)
                                    toast.duration = Toast.LENGTH_LONG
                                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                        context.getString(R.string.category_word_failed_to_add_word_to_learning_toast_message)
                                    toast.show()
                                }
                            })
                            updateWordThread.run()
                        }
                    }
                )
                addAction(
                    context.getString(R.string.edit),
                    object : MultipleAtheneDialog.OnItemClickListener {
                        override fun onItemClick() {
                            multipleAtheneDialog.dismiss()

                            val intent = Intent(context, EditWordActivity::class.java)
                            intent.putExtra(context.getString(R.string.word_extra), wordsArrayList[position])
                            context.startActivity(intent)
                        }
                    }
                )
                addAction(
                    context.getString(R.string.delete),
                    object : MultipleAtheneDialog.OnItemClickListener {
                        override fun onItemClick() {
                            multipleAtheneDialog.dismiss()

                            val atheneDialog = AtheneDialog(context)
                            atheneDialog.message = context.getString(R.string.category_do_you_want_to_delete_word_dialog_message)
                            atheneDialog.positiveText = context.getString(R.string.yes)
                            atheneDialog.negativeText = context.getString(R.string.cancel)
                            atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                                override fun onPositiveClick(view: View) {
                                    val deleteWordThread = DeleteWordThread(wordsArrayList[position])
                                    deleteWordThread
                                        .setDeleteWordResultListener(object : DeleteWordThread.DeleteWordResultListener {
                                            override fun onSuccess() {
                                                storedInStorageWordsArrayList.removeAt(position)
                                                wordsArrayList.removeAt(position)

                                                notifyDataSetChanged()
                                            }

                                            override fun onFailure(exception: Exception) {
                                                if(exception is FirebaseNetworkException) {
                                                    val intent = Intent(context, LoadingActivity::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                    context.startActivity(intent)

                                                    return
                                                }

                                                val toast = Toast(context)
                                                toast.view = LayoutInflater.from(context).inflate(R.layout.layout_toast_custom, null, false)
                                                toast.duration = Toast.LENGTH_LONG
                                                toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                                    context.getString(R.string.category_failed_to_delete_word_toast_message)
                                                toast.show()
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
            }
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

    inner class CategoryWordsRecyclerViewRecyclerViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView)

}