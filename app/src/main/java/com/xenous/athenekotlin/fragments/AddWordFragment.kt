package com.xenous.athenekotlin.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.categoriesArrayList
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.threads.AddCategoryThread
import com.xenous.athenekotlin.threads.AddWordThread
import com.xenous.athenekotlin.utils.ERROR_CODE
import com.xenous.athenekotlin.utils.SUCCESS_CODE
import com.xenous.athenekotlin.utils.forbiddenSymbols
import com.xenous.athenekotlin.utils.getCurrentDateTimeInMills
import com.xenous.athenekotlin.views.AtheneDialog
import com.xenous.athenekotlin.views.OpeningView
import com.xenous.athenekotlin.views.adapters.ChooseCategoryRecyclerViewAdapter
import com.xenous.athenekotlin.views.adapters.OnItemClickListener
import java.util.*

class AddWordFragment: Fragment() {

    private lateinit var addWordForeignEditText: EditText
    private lateinit var addWordNativeEditText : EditText
    private lateinit var addWordContinueImageView : ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_word, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewsById(view)

        val outerFrameLayout = view.findViewById<FrameLayout>(R.id.editWordCategoryOuterFrameLayout)
        val categoryFrameLayout = view.findViewById<FrameLayout>(R.id.editWordCategoryFrameLayout)

        val openingView = OpeningView.Builder(outerFrameLayout).build(layoutInflater)

        openingView.onStateChangeListener = object : OpeningView.OnStateChangeListener {
            override fun onExpand() {
                openingView.categoriesListRecyclerView.adapter = getCategoriesRecyclerViewAdapter(openingView)
                openingView.categoriesListRecyclerView.layoutManager = GridLayoutManager(context,2)
            }

            override fun onCollapse() {
                openingView.categoriesListRecyclerView.adapter = null
            }
        }
        openingView.addWordAddCategoryTextView.setOnClickListener {
            openingView.categoryChosenTextView.text = "Без категории"

            val atheneDialog = AtheneDialog(context!!)
            atheneDialog.message = getString(R.string.add_new_category_message)
            atheneDialog.hint = getString(R.string.category_hint)
            atheneDialog.negativeText = "Отмена"
            atheneDialog.positiveText = "OK"
            atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                override fun onPositiveClick(view: View) {
                    atheneDialog.dismiss()

                    val categoryText = atheneDialog.categoryEditText.text.toString()

                    if(categoryText.isEmpty()) {
                        openingView.categoryChosenTextView.text = "Без категории"

                        return
                    }
                    for(forbiddenSymbol in forbiddenSymbols) {
                        if(categoryText.contains(forbiddenSymbol)) {
                            openingView.categoryChosenTextView.text = "Без категории"

                            createAtheneDialog(getString(R.string.category_contains_forbidden_symbols))

                            return
                        }
                    }

                    val category = Category(
                        categoryText
                    )
                    categoriesArrayList.add(category)
                    openingView.categoryChosenTextView.text = category.title

                    if(categoriesArrayList.contains(category)) {
                        val addCategoryThread = AddCategoryThread(category)
                        addCategoryThread.setAddCategoryResultListener(object : AddCategoryThread.AddCategoryResultListener {

                            override fun onSuccess(category: Category) {
                            }

                            override fun onFailure(exception: Exception) {
                                val atheneDialog = AtheneDialog(context!!)
                                atheneDialog.message = getString(R.string.add_new_category_error_message)
                                atheneDialog.positiveText = getString(R.string.yes)
                                atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                                    override fun onPositiveClick(view: View) {
                                        atheneDialog.dismiss()
                                    }

                                    override fun onNegativeClickListener(view: View) {}
                                })
                                atheneDialog.build().show()
                            }
                        })
                    }
                }

                override fun onNegativeClickListener(view: View) {
                    atheneDialog.dismiss()
                }
            })
            atheneDialog.build()
            atheneDialog.show()
            openingView.collapse()
        }

        addWordContinueImageView.setOnClickListener {
            val foreignWordText = addWordForeignEditText.text.toString().toLowerCase(Locale.ROOT).trim()
            val nativeWordText = addWordNativeEditText.text.toString().toLowerCase(Locale.ROOT).trim()

            if(foreignWordText.isEmpty() or nativeWordText.isEmpty()) {
                createAtheneDialog(getString(R.string.fill_all_fields_message))

                return@setOnClickListener
            }

            val categoryText = openingView.categoryChosenTextView.text.toString()

            val word = Word(
                foreignWordText,
                nativeWordText,
                categoryText,
                getCurrentDateTimeInMills()
            )

            when(word.filter()) {
                Word.WORD_IS_TO_LONG -> {
                    createAtheneDialog(getString(R.string.word_is_too_long_message))
                    return@setOnClickListener
                }
                Word.WORD_CONTAINS_FORBIDDEN_SYMBOLS -> {
                    createAtheneDialog(getString(R.string.word_contains_forbidden_symbols))
                    return@setOnClickListener
                }
                Word.WORD_IS_NULL ->  return@setOnClickListener
            }

            val addWordThread = AddWordThread(word)
            addWordThread.setAddWordResultListener(object : AddWordThread.AddWordResultListener {
                override fun onSuccess(word: Word) {
                }

                override fun onFailure(exception: Exception) {
                    val atheneDialog = AtheneDialog(context!!)
                    atheneDialog.message = getString(R.string.add_new_word_error_message)
                    //ToDo: Fix athene dialog click listeners
                    atheneDialog.positiveText = getString(R.string.yes)
                    atheneDialog.build().show()
                }
            })
            addWordContinueImageView.isClickable = false
        }

        openingView.apply {
            setOnClickListener(View.OnClickListener {
//                Clear Input Focus and Hide Keyboard
                view.clearFocus()
                val imm =
                    activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            })
            setExpandedSize(categoryFrameLayout)
            applyToView(categoryFrameLayout)
        }
    }

    private fun findViewsById(view: View) {
        this.addWordForeignEditText = view.findViewById(R.id.editWordForeignEditText)
        this.addWordNativeEditText = view.findViewById(R.id.editWordNativeEditText)
        this.addWordContinueImageView = view.findViewById(R.id.editWordContinueImageView)
    }

    private fun getCategoriesRecyclerViewAdapter(openingView: OpeningView) = ChooseCategoryRecyclerViewAdapter(
        context!!,
        getCategoriesArrayListWithDefault(),
        object : OnItemClickListener {
            override fun onClick(view: View, category: Category) {

                 openingView.collapse()
                 openingView.categoryChosenTextView.text = category.title
            }
        }
    )

    private fun getAddCategoryHandler(position: Int) = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if(msg.what == SUCCESS_CODE) {
                if(msg.obj != null) {
                    if(msg.obj is Category) {
                        val category = msg.obj as Category

                        categoriesArrayList[position] = category
                    }
                }
            }
            else if(msg.what == ERROR_CODE) {
                val atheneDialog = AtheneDialog(context!!)
                atheneDialog.message = getString(R.string.unknown_error_message)
                atheneDialog.positiveText = "OK"

                atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                    override fun onPositiveClick(view: View) {
                        atheneDialog.dismiss()
                    }

                    override fun onNegativeClickListener(view: View) {}
                })
                atheneDialog.build()
                atheneDialog.show()
            }
        }
    }

    private fun getAddWordHandler(openingView: OpeningView) = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            addWordContinueImageView.isClickable = true

            if(msg.what == SUCCESS_CODE) {
                addWordNativeEditText.setText("")
                addWordForeignEditText.setText("")
                openingView.categoryChosenTextView.text = "Без категории"
            }
            else if(msg.what == ERROR_CODE) {
                createAtheneDialog(getString(R.string.word_sending_error_message))
            }
        }
    }

    private fun createAtheneDialog(message: String) {
        val atheneDialog = AtheneDialog(context!!)
        atheneDialog.message = message
        atheneDialog.positiveText = "OK"
        atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
            override fun onPositiveClick(view: View) {
                atheneDialog.dismiss()
            }

            override fun onNegativeClickListener(view: View) {
            }
        })
        atheneDialog.build()
        atheneDialog.show()
    }
}