package com.xenous.athenekotlin.fragments

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.storage.storedInStorageCategoriesArrayList
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.threads.AddCategoryThread
import com.xenous.athenekotlin.threads.AddWordThread
import com.xenous.athenekotlin.utils.*
import com.xenous.athenekotlin.views.AtheneDialog
import com.xenous.athenekotlin.views.OpeningView
import com.xenous.athenekotlin.views.adapters.ChooseCategoryRecyclerViewAdapter
import com.xenous.athenekotlin.views.adapters.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_add_word.*
import kotlinx.android.synthetic.main.layout_opening_view.*
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

        val outerFrameLayout = view.findViewById<FrameLayout>(R.id.addWordCategoryOuterFrameLayout)
        val categoryFrameLayout = view.findViewById<FrameLayout>(R.id.addWordCategoryFrameLayout)

        val openingView = OpeningView.Builder(outerFrameLayout).build(layoutInflater)

        openingView.onStateChangeListener = object : OpeningView.OnStateChangeListener {
            override fun onExpand() {
                openingView.categoriesListRecyclerView.adapter = getCategoriesRecyclerViewAdapter(openingView)
                openingView.categoriesListRecyclerView.layoutManager = GridLayoutManager(context,2)
                openingView.categoriesListRecyclerView.alpha = 0F
                openingView.categoriesListRecyclerView
                    .animateAlphaTo(1F, duration = ANIMATION_DURATION_TWO_THIRDS)
            }

            override fun onCollapse() {
                openingView.categoriesListRecyclerView.adapter = null
            }
        }
        openingView.addWordAddCategoryTextView.setOnClickListener {
            openingView.categoryChosenTextView.text = getString(R.string.no_category)

            val atheneDialog = AtheneDialog(context!!)
            atheneDialog.message = getString(R.string.add_word_add_category_dialog_message)
            atheneDialog.hint = getString(R.string.add_word_add_category_input_hint)
            atheneDialog.negativeText = getString(R.string.cancel)
            atheneDialog.positiveText = getString(R.string.add)
            atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                override fun onPositiveClick(view: View) {
                    atheneDialog.dismiss()

                    val categoryText = atheneDialog.categoryEditText.text
                                                .toString()
                                                .trim()
                                                .toLowerCase(Locale.ROOT)

                    if(categoryText.isEmpty()) {
                        openingView.categoryChosenTextView.text = getString(R.string.no_category)

                        return
                    }
                    for(forbiddenSymbol in forbiddenSymbols) {
                        if(categoryText.contains(forbiddenSymbol)) {
                            openingView.categoryChosenTextView.text = getString(R.string.no_category)

                            createAtheneDialog(getString(R.string.add_word_add_category_category_contains_forbidden_symbols_dialog_message))

                            return
                        }
                    }

                    val category = Category(
                        categoryText
                    )

                    var isNew = true
                    for(c in storedInStorageCategoriesArrayList) {
                        if(c.title == category.title) {
                            isNew = false
                            break
                        }
                    }

                    if(isNew) {
                        val addCategoryThread = AddCategoryThread(category)
                        addCategoryThread.setAddCategoryResultListener(object : AddCategoryThread.AddCategoryResultListener {

                            override fun onSuccess(category: Category) {
                                openingView.categoryChosenTextView.text = category.title
                                storedInStorageCategoriesArrayList.add(category)
                            }

                            override fun onFailure(exception: Exception) {
                                val atheneDialog = AtheneDialog(context!!)
                                atheneDialog.message = getString(R.string.add_word_add_category_error_dialog_message)
                                atheneDialog.positiveText = getString(R.string.ok)
                                atheneDialog.build()
                                atheneDialog.show()
                            }
                        })
                        addCategoryThread.run()
                    }
                    else {
                        openingView.categoryChosenTextView.text = category.title
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
                createAtheneDialog(getString(R.string.add_word_fill_all_fields_dialog_message))

                return@setOnClickListener
            }

            val categoryText = openingView.categoryChosenTextView.text.toString()

            val word = Word(
                nativeWordText,
                foreignWordText,
                categoryText,
                getCurrentDateTimeAtZeroHoursInMills() + Word.LEVEL_DAY
            )

            when(word.filter()) {
                Word.WORD_IS_TOO_LONG -> {
                    createAtheneDialog(getString(R.string.add_word_word_is_too_long_dialog_message))

                    return@setOnClickListener
                }
                Word.WORD_CONTAINS_FORBIDDEN_SYMBOLS -> {
                    createAtheneDialog(getString(R.string.add_word_word_contains_forbidden_symbols_dialog_message))

                    return@setOnClickListener
                }
                Word.WORD_IS_NULL ->  return@setOnClickListener
            }

            val addWordThread = AddWordThread(word)
            addWordThread.setAddWordResultListener(object : AddWordThread.AddWordResultListener {
                override fun onSuccess(word: Word) {
                    storedInStorageWordsArrayList.add(word)

                    lottieAnimationView.apply {
                        visibility = View.VISIBLE

                        addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationEnd(animation: Animator?) {
                                lottieAnimationView.animateAlphaTo(
                                    0F,
                                    duration = ANIMATION_DURATION_HALF,
                                    onAnimationEnd = {
                                        addWordForeignEditText.clearInputLetterByLetter(ANIMATION_DURATION_TWO_THIRDS)
                                        addWordNativeEditText.clearInputLetterByLetter(ANIMATION_DURATION_TWO_THIRDS)

                                        lottieAnimationView.alpha = 1F
                                        lottieAnimationView.visibility = View.GONE
                                        addWordContinueImageView.setImageDrawable(activity!!.resources.getDrawable(R.drawable.ic_add_word_white))
                                        addWordContinueImageView.isClickable = true
                                    }
                                )
                            }

                            override fun onAnimationRepeat(animation: Animator?) {  }

                            override fun onAnimationCancel(animation: Animator?) {  }

                            override fun onAnimationStart(animation: Animator?) {  }
                        })
                        setAnimation(R.raw.animation_success)
                        playAnimation()
                    }
                }

                override fun onFailure(exception: Exception) {
                    val atheneDialog = AtheneDialog(context!!)
//                    todo: handle error
                    atheneDialog.message = getString(R.string.add_word_unknown_error_dialog_message)
                    atheneDialog.positiveText = getString(R.string.ok)
                    atheneDialog.build().show()

                    addWordContinueImageView.setImageDrawable(activity!!.resources.getDrawable(R.drawable.ic_add_word_white))
                    addWordContinueImageView.isClickable = true
                }
            })
            addWordThread.run()

            run {
                val threeBounce = ThreeBounce()
                addWordContinueImageView.setImageDrawable(threeBounce)
                threeBounce.start()
            }
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
        this.addWordForeignEditText = view.findViewById(R.id.addWordForeignEditText)
        this.addWordNativeEditText = view.findViewById(R.id.addWordNativeEditText)
        this.addWordContinueImageView = view.findViewById(R.id.addWordContinueImageView)
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

    private fun createAtheneDialog(message: String) {
        val atheneDialog = AtheneDialog(context!!)
        atheneDialog.message = message
        atheneDialog.positiveText = getString(R.string.ok)
        atheneDialog.build()
        atheneDialog.show()
    }

    fun notifyDataSetChanged() {
        val categoriesArrayList = getCategoriesArrayListWithDefault()
        val chosenCategoryText = addWordCategoryChosenTextView.text.toString()

        if(!categoriesArrayList.any { it.title == chosenCategoryText }) {
            addWordCategoryChosenTextView.text = getString(R.string.no_category)
        }
    }
}