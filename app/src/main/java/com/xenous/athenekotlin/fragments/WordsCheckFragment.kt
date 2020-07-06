package com.xenous.athenekotlin.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.EditWordActivity
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.animateStrikeThroughText
import com.xenous.athenekotlin.utils.animateTextColorTo
import com.xenous.athenekotlin.utils.slideInFromRight
import com.xenous.athenekotlin.utils.slideOutToLeft
import java.util.*

class WordsCheckFragment(private val word: Word?, private val isLast: Boolean): Fragment() {

    interface OnWordCheckStateChangeListener {
        fun onWordChecked()

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

        if(word != null) {
            view.prepareFragmentForNewWord(word)
        }
        else {
            view.showNoWordsTitle()
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
            Log.d("BOB", word.uid)
            startActivity(
                Intent(
                    activity!!,
                    EditWordActivity::class.java
                ).putExtra(
                    getString(R.string.word_extra),
                    word
                )
            )
        }
        wordActionDeleteLinearLayout.setOnClickListener {
            Toast.makeText(context, "DELETE", Toast.LENGTH_LONG).show()
        }
        nextLinearLayout.setOnClickListener {
            clearFragmentAfterWord(onClearEnd = {
                onWordCheckStateChangeListener?.onWordChecked()
            })
        }
        continueLinearLayout.setOnClickListener {
            if(
                foreignUserAnswerEditText.text.toString().trim().toLowerCase(Locale.ROOT) ==
                word.foreign?.trim()?.toLowerCase(Locale.ROOT)
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
}