package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.animateStrikeThroughText
import com.xenous.athenekotlin.utils.animateTextColorTo
import com.xenous.athenekotlin.utils.slideInFromRight
import com.xenous.athenekotlin.utils.slideOutToLeft
import java.util.*

const val WORD_CHECKED = 9094

class WordsCheckFragmentTest(val word: Word, val handler: Handler): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_words_check, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        Prepare fragment for new word
        view.prepareFragmentForNewWord()
    }

    private fun View.prepareFragmentForNewWord() {
//        Variables Initial Block
        val clickBlocker = findViewById<FrameLayout>(R.id.clickBlocker)

        val translationsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckTranslationsLinearLayout)
        val foreignTextView = findViewById<TextView>(R.id.wordsCheckForeignTextView)
        val nativeUserAnswerEditText = findViewById<EditText>(R.id.wordsCheckNativeUserAnswerEditText)
        val nativeCorrectAnswerTextView = findViewById<TextView>(R.id.wordsCheckNativeCorrectAnswerTextView)

        val wordActionEditLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionEditLinearLayout)
        val wordActionDeleteLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionDeleteLinearLayout)

        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)
        val continueActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueActionsLinearLayout)
        val continueLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueLinearLayout)
        val forgotTextView = findViewById<TextView>(R.id.wordsCheckForgotTextView)

//        Slide in
        translationsLinearLayout.slideInFromRight(onAnimationStart = {
            foreignTextView.text = word.foreign
            nativeCorrectAnswerTextView.text = word.native
        })
        continueActionsLinearLayout.slideInFromRight()

//        Set onClick and onTouch listeners
        wordActionEditLinearLayout.setOnClickListener {
            Toast.makeText(context, "EDIT", Toast.LENGTH_LONG).show()
        }
        wordActionDeleteLinearLayout.setOnClickListener {
            Toast.makeText(context, "DELETE", Toast.LENGTH_LONG).show()
        }
        nextLinearLayout.setOnClickListener {
            clearFragmentAfterWord(onClearEnd = {
                handler.sendEmptyMessage(WORD_CHECKED)
            })
        }
        continueLinearLayout.setOnClickListener {
            if(
                nativeUserAnswerEditText.text.toString().trim().toLowerCase(Locale.ROOT) ==
                word.native?.trim()?.toLowerCase(Locale.ROOT)
            ) {
                this@prepareFragmentForNewWord.animateCorrectAnswer(
                    onAnimationStart = {
                        clickBlocker.visibility = View.VISIBLE
                    },
                    onAnimationEnd = {
                        clickBlocker.visibility = View.GONE
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

        val nativeUserAnswerFrameLayout = findViewById<FrameLayout>(R.id.wordsCheckNativeUserAnswerFrameLayout)
        val nativeUserAnswerEditText = findViewById<EditText>(R.id.wordsCheckNativeUserAnswerEditText)
        val nativeUserAnswerTextView = findViewById<TextView>(R.id.wordsCheckNativeUserAnswerTextView)
        val nativeCorrectAnswerTextView = findViewById<TextView>(R.id.wordsCheckNativeCorrectAnswerTextView)

        val wordActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionsLinearLayout)
        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)
        val continueActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueActionsLinearLayout)

        onAnimationStart?.let { it() }
        nativeUserAnswerFrameLayout.slideOutToLeft(
            onAnimationStart = {
                nativeUserAnswerEditText.clearFocus()
            },
            onAnimationEnd = {
                nativeUserAnswerFrameLayout.visibility = View.INVISIBLE
                nativeUserAnswerEditText.visibility = View.INVISIBLE
            }
        )
        nativeUserAnswerTextView.slideInFromRight(
            onAnimationStart = {
                nativeUserAnswerTextView.visibility = View.VISIBLE
                if(isForgotten) {
                    nativeUserAnswerTextView.text = "Вспомнить Перевод:"
                }
                else {
                    nativeUserAnswerTextView.text = nativeUserAnswerEditText.text.toString()
                    nativeUserAnswerTextView.animateTextColorTo(colorIncorrect)
                }
            },
            onAnimationEnd = {
                if(!isForgotten) nativeUserAnswerTextView.animateStrikeThroughText()

                nativeCorrectAnswerTextView.slideInFromRight(
                    onAnimationStart = {
                        nativeCorrectAnswerTextView.visibility = View.VISIBLE
                        nativeCorrectAnswerTextView.animateTextColorTo(colorCorrect)
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
        val colorIncorrect = ContextCompat.getColor(context, R.color.colorIncorrect)

        val nativeUserAnswerFrameLayout = findViewById<FrameLayout>(R.id.wordsCheckNativeUserAnswerFrameLayout)
        val nativeUserAnswerEditText = findViewById<EditText>(R.id.wordsCheckNativeUserAnswerEditText)
        val nativeUserAnswerTextView = findViewById<TextView>(R.id.wordsCheckNativeUserAnswerTextView)
        val nativeCorrectAnswerTextView = findViewById<TextView>(R.id.wordsCheckNativeCorrectAnswerTextView)

        val wordActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionsLinearLayout)
        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)
        val continueActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckContinueActionsLinearLayout)

        onAnimationStart?.let { it() }
        nativeUserAnswerFrameLayout.slideOutToLeft(
            onAnimationStart = {
                nativeUserAnswerEditText.clearFocus()
            },
            onAnimationEnd = {
                nativeUserAnswerFrameLayout.visibility = View.INVISIBLE
                nativeUserAnswerEditText.visibility = View.INVISIBLE
            }
        )
        nativeUserAnswerTextView.slideInFromRight(
            onAnimationStart = {
                nativeUserAnswerTextView.visibility = View.VISIBLE
                nativeUserAnswerTextView.text = "Отлично!"
            },
            onAnimationEnd = {
                nativeCorrectAnswerTextView.slideInFromRight(
                    onAnimationStart = {
                        nativeCorrectAnswerTextView.visibility = View.VISIBLE
                        nativeCorrectAnswerTextView.animateTextColorTo(colorCorrect)
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
        val translationsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckTranslationsLinearLayout)
        val wordActionsLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckWordActionsLinearLayout)
        val nextLinearLayout = findViewById<LinearLayout>(R.id.wordsCheckNextLinearLayout)

        translationsLinearLayout.slideOutToLeft()
        wordActionsLinearLayout.slideOutToLeft()
        nextLinearLayout.slideOutToLeft(onAnimationEnd = {
            onClearEnd?.let { it() }
        })
    }
}