package com.xenous.athenekotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseNetworkException
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.EditWordActivity
import com.xenous.athenekotlin.activities.LoadingActivity
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.threads.DeleteWordThread
import com.xenous.athenekotlin.utils.animateStrikeThroughText
import com.xenous.athenekotlin.utils.animateTextColorTo
import com.xenous.athenekotlin.utils.slideInFromRight
import com.xenous.athenekotlin.utils.slideOutToLeft
import com.xenous.athenekotlin.views.AtheneDialog
import kotlinx.android.synthetic.main.fragment_words_check.*
import java.util.*

class WordsCheckFragment(
    private var word: Word?,
    private val isLast: Boolean
): Fragment() {

    private lateinit var fragmentView: View
    val index = word?.let { storedInStorageWordsArrayList.indexOf(word!!) }

    interface OnWordCheckStateChangeListener {
        fun onWordChecked()

        fun onWordDelete() {  }

        fun onRightAnswer(word: Word)

        fun onWordMistake(word: Word)

        fun onWordsEnd()
    }

    var onWordCheckStateChangeListener: OnWordCheckStateChangeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if(word != null) {
            inflater.inflate(R.layout.fragment_words_check, container, false)
        } else {
            inflater.inflate(R.layout.fragment_words_check_no_words, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view
        if(word != null) {
            view.prepareFragmentForNewWord(word!!)
        }
        else {
            view.showNoWordsTitle()
        }
    }

    fun notifyDataSetChanged() {
        if(index != null) {
            word = storedInStorageWordsArrayList[index]

            word?.let {
                wordsCheckNativeTextView.text = it.native
                wordsCheckForeignCorrectAnswerTextView.text = it.foreign

                wordsCheckWordActionEditLinearLayout.setOnClickListener {
                    val intent = Intent(activity!!, EditWordActivity::class.java)
                    intent.putExtra(getString(R.string.word_extra), word)
                    startActivity(intent)
                }
                wordsCheckWordActionDeleteLinearLayout.setOnClickListener { view ->
                    val atheneDialog = AtheneDialog(context!!)
                    atheneDialog.message = context!!.getString(R.string.words_check_do_you_want_to_delete_word_dialog_message)
                    atheneDialog.positiveText = context!!.getString(R.string.yes)
                    atheneDialog.negativeText = context!!.getString(R.string.cancel)
                    atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                        override fun onPositiveClick(view: View) {
                            val deleteWordThread = DeleteWordThread(word)
                            deleteWordThread.setDeleteWordResultListener(object : DeleteWordThread.DeleteWordResultListener {
                                override fun onSuccess() {
                                    onWordCheckStateChangeListener?.onWordDelete()
                                }

                                override fun onFailure(exception: Exception) {
                                    if(exception is FirebaseNetworkException) {
                                        val intent = Intent(context, LoadingActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        context!!.startActivity(intent)

                                        return
                                    }

                                    val toast = Toast(context)
                                    toast.view = LayoutInflater.from(context).inflate(R.layout.layout_toast_custom, null, false)
                                    toast.duration = Toast.LENGTH_LONG
                                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                        context!!.getString(R.string.category_word_failed_to_add_word_to_learning_toast_message)
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
        }
    }

    private fun View.showNoWordsTitle() {
        val noWordsTitleTextView = findViewById<TextView>(R.id.wordsCheckNoWordsTitleTextView)

        noWordsTitleTextView.slideInFromRight(onAnimationEnd = {
            onWordCheckStateChangeListener?.onWordsEnd()
        })
    }

    private fun View.prepareFragmentForNewWord(word: Word) {
//      Variables Initial Block
        val clickBlocker = findViewById<FrameLayout>(R.id.clickBlocker)

        val translationsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckTranslationsLinearLayout)
        val nativeTextView = findViewById<TextView>(R.id.wordsCheckNativeTextView)
        val foreignUserAnswerEditText = findViewById<EditText>(R.id.wordsCheckForeignUserAnswerEditText)
        val foreignCorrectAnswerTextView = findViewById<TextView>(R.id.wordsCheckForeignCorrectAnswerTextView)

        val wordActionEditLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionEditLinearLayout)
        val wordActionDeleteLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionDeleteLinearLayout)

        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)
        val continueActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueActionsLinearLayout)
        val continueLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueLinearLayout)
        val forgotTextView = findViewById<TextView>(R.id.wordsCheckForgotTextView)

//        Slide in
        translationsLinearLayout.slideInFromRight(onAnimationStart = {
            nativeTextView.text = word.native
            foreignCorrectAnswerTextView.text = word.foreign
        })
        continueActionsLinearLayout.slideInFromRight()

//        Set onClick and onTouch listeners
        wordActionEditLinearLayout.setOnClickListener {
            val intent = Intent(activity!!, EditWordActivity::class.java)
            intent.putExtra(getString(R.string.word_extra), word)
            startActivity(intent)
        }
        wordActionDeleteLinearLayout.setOnClickListener {
            val atheneDialog = AtheneDialog(context)
            atheneDialog.message = context.getString(R.string.words_check_do_you_want_to_delete_word_dialog_message)
            atheneDialog.positiveText = context.getString(R.string.yes)
            atheneDialog.negativeText = context.getString(R.string.cancel)
            atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                override fun onPositiveClick(view: View) {
                    val deleteWordThread = DeleteWordThread(word)
                    deleteWordThread.setDeleteWordResultListener(object : DeleteWordThread.DeleteWordResultListener {
                        override fun onSuccess() {
                            onWordCheckStateChangeListener?.onWordDelete()
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
        nextLinearLayout.setOnClickListener {
            clearFragmentAfterWord(onClearEnd = {
                onWordCheckStateChangeListener?.onWordChecked()
            })
        }
        continueLinearLayout.setOnClickListener {
            if(foreignUserAnswerEditText.text.toString().trim().isNotBlank()) {
                if(
                    foreignUserAnswerEditText.text.toString().trim().toLowerCase(Locale.ROOT) ==
                    word.foreign.trim().toLowerCase(Locale.ROOT)
                ) {
                    this@prepareFragmentForNewWord.animateCorrectAnswer(
                        onAnimationStart = {
                            clickBlocker.visibility = View.VISIBLE
                        },
                        onAnimationEnd = {
                            clickBlocker.visibility = View.GONE

                            onWordCheckStateChangeListener?.onRightAnswer(word.apply { increaseLevel() })
                        }
                    )
                }
                else {
                    this@prepareFragmentForNewWord.animateIncorrectAnswer(
                        false,
                        onAnimationStart = {
                            clickBlocker.visibility = View.VISIBLE
                        },
                        onAnimationEnd = {
                            clickBlocker.visibility = View.GONE

                            onWordCheckStateChangeListener?.onWordMistake(word.apply { resetProgress() })
                        }
                    )
                }
            }
            else {
                val atheneDialog = AtheneDialog(context)
                atheneDialog.message = context.getString(R.string.words_check_blank_input_is_not_allowed_dialog_message)
                atheneDialog.positiveText = context.getString(R.string.ok)
                atheneDialog.build()
                atheneDialog.show()
            }
        }
        forgotTextView.setOnClickListener {
            this@prepareFragmentForNewWord.animateIncorrectAnswer(
                true,
                onAnimationStart = {
                    clickBlocker.visibility = View.VISIBLE
                },
                onAnimationEnd = {
                    clickBlocker.visibility = View.GONE

                    onWordCheckStateChangeListener?.onWordMistake(word.apply { resetProgress() })
                }
            )
        }
    }

    private fun View.animateIncorrectAnswer(
        isForgotten: Boolean,
        onAnimationStart: (() -> Unit)? = null,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        //        Variables Initial Block
        val colorCorrect = ContextCompat.getColor(context, R.color.colorCorrect)
        val colorIncorrect = ContextCompat.getColor(context, R.color.colorIncorrect)

        val foreignUserAnswerFrameLayout = findViewById<FrameLayout>(R.id.wordsCheckForeignUserAnswerFrameLayout)
        val foreignUserAnswerEditText = findViewById<EditText>(R.id.wordsCheckForeignUserAnswerEditText)
        val foreignUserAnswerTextView = findViewById<TextView>(R.id.wordsCheckForeignUserAnswerTextView)
        val foreignCorrectAnswerTextView = findViewById<TextView>(R.id.wordsCheckForeignCorrectAnswerTextView)

        val wordActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionsLinearLayout)
        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)
        val continueActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueActionsLinearLayout)

        onAnimationStart?.let { it() }
        foreignUserAnswerFrameLayout.slideOutToLeft(
            onAnimationStart = {
                foreignUserAnswerEditText.clearFocus()
            },
            onAnimationEnd = {
                foreignUserAnswerFrameLayout.visibility = View.INVISIBLE
                foreignUserAnswerEditText.visibility = View.INVISIBLE
            }
        )
        foreignUserAnswerTextView.slideInFromRight(
            onAnimationStart = {
                foreignUserAnswerTextView.visibility = View.VISIBLE
                if(isForgotten) {
                    foreignUserAnswerTextView.text = getString(R.string.words_check_recall_translation)
                }
                else {
                    foreignUserAnswerTextView.text = foreignUserAnswerEditText.text.toString()
                    foreignUserAnswerTextView.animateTextColorTo(colorIncorrect)
                }
            },
            onAnimationEnd = {
                if(!isForgotten) foreignUserAnswerTextView.animateStrikeThroughText()

                foreignCorrectAnswerTextView.slideInFromRight(
                    onAnimationStart = {
                        foreignCorrectAnswerTextView.visibility = View.VISIBLE
                        foreignCorrectAnswerTextView.animateTextColorTo(colorCorrect)
                    },
                    onAnimationEnd = {
                        wordActionsLinearLayout.slideInFromRight(onAnimationStart = {
                            wordActionsLinearLayout.visibility = View.VISIBLE
                        })
                        continueActionsLinearLayout.slideOutToLeft(onAnimationEnd = {
                            continueActionsLinearLayout.visibility = View.GONE
                        })
                        nextLinearLayout.slideInFromRight(
                            onAnimationStart = {
                                nextLinearLayout.visibility = View.VISIBLE
                            },
                            onAnimationEnd = {
                                onAnimationEnd?.let { it() }
                            }
                        )
                    }
                )
            }
        )
    }

    private fun View.animateCorrectAnswer(
        onAnimationStart: (() -> Unit)? = null,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        //        Variables Initial Block
        val colorCorrect = ContextCompat.getColor(context, R.color.colorCorrect)

        val foreignUserAnswerFrameLayout = findViewById<FrameLayout>(R.id.wordsCheckForeignUserAnswerFrameLayout)
        val foreignUserAnswerEditText = findViewById<EditText>(R.id.wordsCheckForeignUserAnswerEditText)
        val foreignUserAnswerTextView = findViewById<TextView>(R.id.wordsCheckForeignUserAnswerTextView)
        val foreignCorrectAnswerTextView = findViewById<TextView>(R.id.wordsCheckForeignCorrectAnswerTextView)

        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)
        val continueActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueActionsLinearLayout)

        onAnimationStart?.let { it() }
        foreignUserAnswerFrameLayout.slideOutToLeft(
            onAnimationStart = {
                foreignUserAnswerEditText.clearFocus()
            },
            onAnimationEnd = {
                foreignUserAnswerFrameLayout.visibility = View.INVISIBLE
                foreignUserAnswerEditText.visibility = View.INVISIBLE
            }
        )
        foreignUserAnswerTextView.slideInFromRight(
            onAnimationStart = {
                foreignUserAnswerTextView.visibility = View.VISIBLE
                foreignUserAnswerTextView.text = getString(R.string.words_check_fine)
            },
            onAnimationEnd = {
                foreignCorrectAnswerTextView.slideInFromRight(
                    onAnimationStart = {
                        foreignCorrectAnswerTextView.visibility = View.VISIBLE
                        foreignCorrectAnswerTextView.animateTextColorTo(colorCorrect)
                    },
                    onAnimationEnd = {
                        continueActionsLinearLayout.slideOutToLeft(onAnimationEnd = {
                            continueActionsLinearLayout.visibility = View.GONE
                        })
                        nextLinearLayout.slideInFromRight(
                            onAnimationStart = {
                                nextLinearLayout.visibility = View.VISIBLE
                            },
                            onAnimationEnd = {
                                onAnimationEnd?.let { it() }
                            }
                        )
                    }
                )
            }
        )
    }

    private fun View.clearFragmentAfterWord(onClearEnd: (() -> Unit)? = null) {
        val titleTextView = findViewById<TextView>(R.id.wordsCheckTitleTextView)
        val translationsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckTranslationsLinearLayout)
        val wordActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionsLinearLayout)
        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)

        if(isLast) { titleTextView.slideOutToLeft() }
        translationsLinearLayout.slideOutToLeft()
        wordActionsLinearLayout.slideOutToLeft()
        nextLinearLayout.slideOutToLeft(onAnimationEnd = {
            onClearEnd?.let { it() }
        })
    }

    fun clearFragmentAfterWord(onClearEnd: (() -> Unit)? = null) {
        fragmentView.clearFragmentAfterWord(onClearEnd)
    }
}