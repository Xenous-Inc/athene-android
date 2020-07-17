package com.xenous.athenekotlin.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DatabaseError
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.storedInStorageCategoriesArrayList
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.threads.ReadWordsThread
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.slideInFromBottom
import com.xenous.athenekotlin.utils.slideOutToBottom
import com.xenous.athenekotlin.views.adapters.CategoryWordsRecyclerViewRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_category.*
import java.util.*

class CategoryActivity : AppCompatActivity() {
    private var categoryTitle: String? = null
    private var wordsArrayList: ArrayList<Word> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        categoryTitle = intent.getStringExtra(getString(R.string.category_extra))
        if(categoryTitle == null) {
            onBackPressed()
            return
        }

        val wordsInCurrentCategoryList = storedInStorageWordsArrayList.filter {
            it.category.trim().toLowerCase(Locale.ROOT) == categoryTitle!!.trim().toLowerCase(Locale.ROOT)
        }
        wordsArrayList =
            if(wordsInCurrentCategoryList.isEmpty()) arrayListOf()
            else wordsInCurrentCategoryList.toMutableList()  as ArrayList<Word>

        categoryTitleTextView.text = categoryTitle
        categoryNoWordsTitleTextView.apply {
            visibility = if(wordsArrayList.isNotEmpty()) View.GONE else View.VISIBLE
            slideInFromBottom(duration = ANIMATION_DURATION_HALF)
        }
        categoryWordsRecyclerView.apply {
            adapter = CategoryWordsRecyclerViewRecyclerViewAdapter(this@CategoryActivity, wordsArrayList)
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            slideInFromBottom()
        }

        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPurple200,
            R.color.colorPurple400,
            R.color.colorPurple600,
            R.color.colorPurple800
        )
        swipeRefreshLayout.setOnRefreshListener {
            val readWordsThread = ReadWordsThread()
            readWordsThread.setDownloadWordResultListener(object: ReadWordsThread.ReadWordsResultListener {
                override fun onSuccess(wordsList: List<Word>, categoriesList: List<Category>) {
                    storedInStorageWordsArrayList =
                        if(wordsList.isEmpty()) arrayListOf()
                        else wordsList.toMutableList() as ArrayList

                    storedInStorageCategoriesArrayList =
                        if(categoriesList.isEmpty()) arrayListOf()
                        else categoriesList.toMutableList() as ArrayList


                    val wordsInCurrentCategoryList = storedInStorageWordsArrayList.filter {
                        it.category.trim().toLowerCase(Locale.ROOT) == categoryTitle!!.trim().toLowerCase(Locale.ROOT)
                    }
                    wordsArrayList =
                        if(wordsInCurrentCategoryList.isEmpty()) arrayListOf()
                        else wordsInCurrentCategoryList.toMutableList()  as ArrayList<Word>

                    categoryNoWordsTitleTextView.visibility =
                        if(wordsArrayList.isNotEmpty()) View.GONE else View.VISIBLE
                    if(categoryWordsRecyclerView != null) {
                        (categoryWordsRecyclerView.adapter as CategoryWordsRecyclerViewRecyclerViewAdapter).wordsArrayList = wordsArrayList
                        categoryWordsRecyclerView.adapter!!.notifyDataSetChanged()
                    }

                    swipeRefreshLayout.isRefreshing = false
                }

                override fun onFailure(exception: Exception) {
                    if(exception is FirebaseNetworkException) {
                        val intent = Intent(this@CategoryActivity, LoadingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        return
                    }

                    val toast = Toast(this@CategoryActivity)
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.database_error_unknown_error_toast_message)
                    toast.show()
                }

                override fun onError(error: DatabaseError) {
                    if(
                        error.code == DatabaseError.DISCONNECTED ||
                        error.code == DatabaseError.NETWORK_ERROR
                    ) {
                        val intent = Intent(this@CategoryActivity, LoadingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        return
                    }

                    val toast = Toast(this@CategoryActivity)
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.database_error_unknown_error_toast_message)
                    toast.show()
                }
            })
            readWordsThread.run()
        }
    }

    override fun onResume() {
        super.onResume()

        val wordsList = storedInStorageWordsArrayList.filter {
            it.category.trim().toLowerCase(Locale.ROOT) == categoryTitle?.trim()?.toLowerCase(Locale.ROOT)
        }
        wordsArrayList =
            if(wordsList.isEmpty()) arrayListOf()
            else wordsList.toMutableList()  as ArrayList<Word>



        categoryNoWordsTitleTextView.visibility =
            if(wordsArrayList.isNotEmpty()) View.GONE else View.VISIBLE
        if(categoryWordsRecyclerView != null) {
            (categoryWordsRecyclerView.adapter as CategoryWordsRecyclerViewRecyclerViewAdapter).wordsArrayList = wordsArrayList
            categoryWordsRecyclerView.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        categoryWordsRecyclerView.slideOutToBottom()
    }
}