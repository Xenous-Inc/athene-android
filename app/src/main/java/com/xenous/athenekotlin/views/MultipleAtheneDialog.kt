package com.xenous.athenekotlin.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.xenous.athenekotlin.R
import kotlinx.android.synthetic.main.layout_multiple_athene_dialog.*
import kotlinx.android.synthetic.main.layout_multiple_athene_dialog_action_textview.view.*

class MultipleAtheneDialog(
    private val context: Context
) {
    /*  Initial Block   */
    private val dialog = Dialog(context)

    private val actionsLinearLayout: LinearLayout

    init {
        dialog.setContentView(R.layout.layout_multiple_athene_dialog)

//        messageTextView = dialog.multipleAtheneDialogMessageTextView
//        negativeAnswerTextView = dialog.multipleAtheneDialogNegativeAnswerTextView
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
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        for (action in actions) {
            val actionLinearLayout =
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_multiple_athene_dialog_action_textview, null, false) as LinearLayout

            if(actions.first() == action) { actionLinearLayout.actionDivider.visibility = View.GONE }
            actionLinearLayout.actionTitleTextView.text = action.first
            actionLinearLayout.setOnClickListener { action.second.onItemClick() }
            actionsLinearLayout.addView(actionLinearLayout)
        }

        return this
    }

    fun show() {
        dialog.show()

        dialog.multipleAtheneDialogActionsLinearLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                dialog.window?.setLayout(
                    dialog.multipleAtheneDialogActionsLinearLayout.width,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                dialog.multipleAtheneDialogActionsLinearLayout
                    .viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun dismiss() {
        this.dialog.dismiss()
    }

    /*  Interfaces Block    */
    interface OnItemClickListener {
        fun onItemClick()
    }
}