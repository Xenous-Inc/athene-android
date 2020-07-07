package com.xenous.athenekotlin.activities

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.categoriesArrayList
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.threads.AddCategoryThread
import com.xenous.athenekotlin.threads.UpdateWordThread
import com.xenous.athenekotlin.views.AtheneDialog
import com.xenous.athenekotlin.views.OpeningView
import com.xenous.athenekotlin.views.adapters.ChooseCategoryRecyclerViewAdapter
import com.xenous.athenekotlin.views.adapters.OnItemClickListener
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

class EditWordActivity : AppCompatActivity() {
    //------------------Views-----------------------------------------------------------------------
    private lateinit var editWordForeignEditText: EditText
    private lateinit var editWordNativeEditText: EditText
    private lateinit var editWordContinueImageView: ImageView

    //---------------------------Data---------------------------------------------------------------
    private var word: Word? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word)
        findViewsById()

        try {
            word = intent.getParcelableExtra(getString(R.string.word_extra))
        }
        catch(e: NullPointerException) {
            e.printStackTrace()
            finish()
        }

        val categoryFrameLayout = findViewById<FrameLayout>(R.id.editWordCategoryFrameLayout)
        val outerFrameLayout = findViewById<FrameLayout>(R.id.editWordCategoryOuterFrameLayout)

        val openingView = OpeningView.Builder(outerFrameLayout).build(layoutInflater)
        openingView.onStateChangeListener = object : OpeningView.OnStateChangeListener {
            override fun onExpand() {
                openingView.categoriesListRecyclerView.adapter = getCategoriesRecyclerViewAdapter(openingView)
                openingView.categoriesListRecyclerView.layoutManager = GridLayoutManager(this@EditWordActivity,2)
            }

            override fun onCollapse() {
                openingView.categoriesListRecyclerView.adapter = null
            }
        }
        openingView.addWordAddCategoryTextView.setOnClickListener {
            openingView.collapse()

            val atheneDialog = AtheneDialog(this)
            atheneDialog.apply {
                message = getString(R.string.add_category_message)
                hint = getString(R.string.category_hint)
                positiveText = getString(R.string.add)
                negativeText = getString(R.string.cancel)
            }
            atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                override fun onPositiveClick(view: View) {
                    val categoryTitle = atheneDialog.categoryEditText.text.toString().trim()
                    openingView.categoryChosenTextView.text = categoryTitle

                    var isNew = true
                    for(category in categoriesArrayList) {
                        if(category.title == categoryTitle) {
                            isNew = false
                            break
                        }
                    }
                    if(isNew) {
                        val addCategoryThread = AddCategoryThread(Category(categoryTitle))
                        addCategoryThread.setAddCategoryResultListener(object : AddCategoryThread.AddCategoryResultListener {
                            override fun onSuccess(category: Category) {
                                //ToDo: Disable loading screen
                                categoriesArrayList.add(category)
                            }

                            override fun onFailure(exception: Exception) {
                                val failAtheneDialog = AtheneDialog(this@EditWordActivity)
                                failAtheneDialog.apply {
                                    message = getString(R.string.add_new_category_error_message)
                                    positiveText = getString(R.string.yes)
                                    build()
                                }
                                failAtheneDialog.show()
                                openingView.categoryChosenTextView.text = "Без категории"
                            }
                        })
                        addCategoryThread.run()
                    }
                }
            })
            atheneDialog.build()
            atheneDialog.show()
        }
        openingView.apply {
            setOnClickListener(View.OnClickListener {
//                Clear Input Focus and Hide Keyboard
                view.clearFocus()
                val imm =
                   getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            })
            setExpandedSize(categoryFrameLayout)
            applyToView(categoryFrameLayout)
        }

        editWordContinueImageView.setOnClickListener {
            val editingWord = Word(
                editWordNativeEditText.text.toString().trim().toLowerCase(Locale.ROOT),
                editWordForeignEditText.text.toString().trim().toLowerCase(Locale.ROOT),
                openingView.categoryChosenTextView.text.toString().trim(),
                word!!.lastDateCheck,
                word!!.level,
                word!!.uid

            )

            if(word != editingWord) {
                val updateWordThread = UpdateWordThread(editingWord)
                updateWordThread.setUpdateWordThreadListener(object : UpdateWordThread.UpdateWordThreadListener {
                    override fun onSuccess() {
//                      ToDo: Add loading screen
                        onBackPressed()
                    }

                    override fun onFailure(exception: Exception) {
                        val atheneDialog = AtheneDialog(this@EditWordActivity)
                        atheneDialog.apply {
                            message = getString(R.string.update_word_error_message)
                            positiveText = getString(R.string.yes)
                            build()
                        }
                        atheneDialog.show()
                    }
                })
                updateWordThread.run()
            }
        }

        editWordNativeEditText.setText(word!!.native)
        editWordForeignEditText.setText(word!!.foreign)
        openingView.categoryChosenTextView.text = word!!.category
    }

    private fun findViewsById() {
        this.editWordForeignEditText = findViewById(R.id.editWordForeignEditText)
        this.editWordNativeEditText = findViewById(R.id.editWordNativeEditText)
        this.editWordContinueImageView = findViewById(R.id.editWordContinueImageView)
    }

    private fun getCategoriesRecyclerViewAdapter(openingView: OpeningView) = ChooseCategoryRecyclerViewAdapter(
        this,
        getCategoriesArrayListWithDefault(),
        object : OnItemClickListener {
            override fun onClick(view: View, category: Category) {
                openingView.collapse()
                openingView.categoryChosenTextView.text = category.title
            }
        }
    )
}