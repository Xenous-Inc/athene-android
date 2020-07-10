package com.xenous.athenekotlin.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.xenous.athenekotlin.R
import kotlinx.android.synthetic.main.layout_multiple_athene_dialog.*

class MultipleAtheneDialog(
    private val context: Context
) {
    /*  Initial Block   */
    private val dialog = Dialog(context)

    private val messageTextView: TextView
    private val negativeAnswerTextView: TextView
    private val actionsLinearLayout: LinearLayout

    init {
        dialog.setContentView(R.layout.layout_multiple_athene_dialog)

        messageTextView = dialog.multipleAtheneDialogMessageTextView
        negativeAnswerTextView = dialog.multipleAtheneDialogNegativeAnswerTextView
        actionsLinearLayout = dialog.multipleAtheneDialogActionsLinearLayout
    }

    /*  Variables Block  */
    var message = context.getString(R.string.multiple_athene_dialog_message)
    var negativeAnswer = context.getString(R.string.cancel)
    private val actions = mutableListOf<Pair<String, OnItemClickListener>>()

    /*  Methods Block   */
    fun addAction(actionText: String, onItemClickListener: OnItemClickListener) {
        actions.add(Pair(actionText, onItemClickListener))
    }

    fun build(): MultipleAtheneDialog {
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        messageTextView.text = this.message
        negativeAnswerTextView.text = this.negativeAnswer
        negativeAnswerTextView.setOnClickListener { dismiss() }
        for (action in actions) {
            val actionTextView =
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_multiple_athene_dialog_action_textview, null, false) as TextView

            actionTextView.text = action.first
            actionTextView.setOnClickListener { action.second.onItemClick() }
            actionsLinearLayout.addView(actionTextView)
        }

        return this
    }

    fun show() {
        this.dialog.show()
    }

    fun dismiss() {
        this.dialog.dismiss()
    }

    /*  Interfaces Block    */
    interface OnItemClickListener {
        fun onItemClick()
    }
}