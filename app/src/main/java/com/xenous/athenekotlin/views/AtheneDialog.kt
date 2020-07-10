package com.xenous.athenekotlin.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.xenous.athenekotlin.R
import kotlinx.android.synthetic.main.layout_athene_dialog.*

class AtheneDialog(
    context: Context
) {
    private val dialog = Dialog(context)

    private val messageTextView: TextView
    private val positiveAnswerTextView: TextView
    private val negativeAnswerTextView: TextView
    val categoryEditText: EditText

    var message = ""
    var hint = ""
    var positiveText = ""
    var negativeText = ""

    interface OnAnswersItemClickListener {
        fun onPositiveClick(view: View)

        fun onNegativeClickListener(view: View) { }
    }

    private var onAnswersItemClickListener: OnAnswersItemClickListener? = null

    init {
        dialog.setContentView(R.layout.layout_athene_dialog)

        messageTextView = dialog.atheneDialogMessageTextView
        positiveAnswerTextView = dialog.atheneDialogPositiveAnswerTextView
        negativeAnswerTextView = dialog.atheneDialogNegativeAnswerTextView
        categoryEditText = dialog.atheneDialogEditText
    }

    fun build(): AtheneDialog {
        this.dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        this.messageTextView.text = this.message
        if(hint.isNotEmpty()) {
            this.categoryEditText.visibility = View.VISIBLE
            this.categoryEditText.hint = this.hint
        }
        if(negativeText.isNotEmpty()) {
            this.negativeAnswerTextView.visibility = View.VISIBLE
            this.negativeAnswerTextView.text = this.negativeText
            this.negativeAnswerTextView.setOnClickListener {
                dismiss()
                onAnswersItemClickListener?.onNegativeClickListener(it)
            }
        }
        this.positiveAnswerTextView.text = this.positiveText
        this.positiveAnswerTextView.setOnClickListener {
            dismiss()
            onAnswersItemClickListener?.onPositiveClick(it)
        }

        return this
    }

    fun show() {
        this.dialog.show()
    }

    fun dismiss() {
        this.dialog.dismiss()
    }

    fun setOnAnswersItemClickListener(answersItemClickListener: OnAnswersItemClickListener) {
        this.onAnswersItemClickListener = answersItemClickListener
    }
}