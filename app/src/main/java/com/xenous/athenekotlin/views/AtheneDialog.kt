package com.xenous.athenekotlin.views

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView

import com.xenous.athenekotlin.R

class AtheneDialog(
    context: Context
) {
    private val dialog = Dialog(context)

    private val messageTextView: TextView
    private val positiveAnswerTextView: TextView
    private val negativeAnswerTextView: TextView
    private val categoryEditText: EditText

    internal var message= ""
    internal var hint = ""
    internal var positiveText = ""
    internal var negativeText = ""

    interface OnAnswersItemClickListener {
        fun onPositiveClick(view: android.view.View)

        fun onNegativeClickListener(view: android.view.View)
    }

    init {
        dialog.setContentView(R.layout.dialog_athene)

        messageTextView = dialog.findViewById(R.id.messageTextView)
        positiveAnswerTextView = dialog.findViewById(R.id.positiveAnswerTextView)
        negativeAnswerTextView = dialog.findViewById(R.id.negativeAnswerTextView)
        categoryEditText = dialog.findViewById(R.id.addNewCategoryEditText)
    }

    fun build(): AtheneDialog {
        this.messageTextView.text = this.message
        if(hint.isNotEmpty()) {
            this.categoryEditText.visibility = android.view.View.VISIBLE
            this.categoryEditText.hint = this.hint
        }
        this.negativeAnswerTextView.text = this.negativeText
        this.positiveAnswerTextView.text = this.positiveText

        return this
    }

    fun show() {
        this.dialog.show()
    }

    fun setOnAnswersItemClickListener(answersItemClickListener: OnAnswersItemClickListener) {
        this.positiveAnswerTextView.setOnClickListener {
            answersItemClickListener.onPositiveClick(it)
        }

        this.negativeAnswerTextView.setOnClickListener {
            answersItemClickListener.onNegativeClickListener(it)
        }
    }


}