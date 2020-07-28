package com.xenous.athenekotlin.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.get
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.FirebaseNetworkException
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.CategoryActivity
import com.xenous.athenekotlin.activities.LoadingActivity
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.threads.DisbandCategoryThread
import com.xenous.athenekotlin.threads.GenerateDynamicLinksThread
import com.xenous.athenekotlin.threads.UpdateWordThread
import com.xenous.athenekotlin.utils.*
import kotlinx.android.synthetic.main.layout_category_cell_opening.view.*
import kotlinx.android.synthetic.main.layout_scrollview.view.*
import kotlinx.android.synthetic.main.layout_toast_custom.view.*
import java.util.*
import kotlin.collections.ArrayList

class CategoriesScrollView(val activity: Activity) {
    private val layoutInflater = activity.layoutInflater

    @SuppressLint("InflateParams")
    val scrollView: ScrollView = layoutInflater.inflate(
        R.layout.layout_scrollview,
        null,
        false
    ) as ScrollView

    private val categoriesMutableList =
        getCategoriesArrayListWithDefault().filter { it.title != activity.getString(R.string.no_category) } as ArrayList
    private val categoryCellOpeningMutableList = mutableListOf<CategoryCellOpening>()
    private val wordsInCategoriesMatrix = mutableListOf<ArrayList<Word>>()

    init {
        scrollView.isVerticalScrollBarEnabled = false

        wordsInCategoriesMatrix.clear()
        categoriesMutableList.forEach { category ->
            drawCell(category)

            val wordsInCurrentCategoryMutableList = ArrayList<Word>()
            storedInStorageWordsArrayList.forEach { word ->
                if(
                    word.category.trim().toLowerCase(Locale.ROOT) ==
                    category.title.trim().toLowerCase(Locale.ROOT)
                ) {
                    wordsInCurrentCategoryMutableList.add(word)
                }
            }

            wordsInCategoriesMatrix.add(wordsInCurrentCategoryMutableList)
        }
    }

    private fun drawCell(category: Category, elementIndex: Int? = null) {
        var index = elementIndex ?: categoryCellOpeningMutableList.size
//        Init categoryCell
        val categoryCellOpening = CategoryCellOpening.Builder().build(layoutInflater)
        categoryCellOpeningMutableList.add(index, categoryCellOpening)

//        Configure categoryCell
        categoryCellOpening.view.categoryTitleTextView.text = category.title
        categoryCellOpening.view.categoryCardView.setOnClickListener {_ ->
            if(!categoryCellOpening.isExpanded) {
                categoryCellOpening.expand()
                categoryCellOpeningMutableList.forEach { if (it != categoryCellOpening) it.collapse() }
            }
            else {
                categoryCellOpening.collapse()
            }
        }

        categoryCellOpening.view.categoryActionAddToLearningLinearLayout.setOnClickListener {
            index = categoriesMutableList.indexOf(category)

            fun notifyAboutSuccessfulAddingWordsToLearning(
                wordsSuccessfullyAddedToLearning: Int,
                wordsAddedForLearning: Int
            ) {
                val toast = Toast(activity)
                toast.view =
                    activity.layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.view.toastTextView.text = activity.getString(
                    R.string.categories_words_were_successfully_added_toast_message,
                    wordsSuccessfullyAddedToLearning,
                    wordsAddedForLearning
                )
                toast.show()

            }

            val wordsToLearnArrayList = arrayListOf<Word>()

            wordsInCategoriesMatrix[index].forEach {
                if(it.level.toInt() == Word.LEVEL_ADDED) {
                    wordsToLearnArrayList.add(it)
                }
            }

            val atheneDialog = AtheneDialog(activity)
            if(wordsToLearnArrayList.isEmpty()) atheneDialog.apply {
                message = activity.getString(R.string.categories_you_are_already_learning_this_dialog_message)
                positiveText = activity.getString(R.string.ok)
            }
            else atheneDialog.apply {
                message = activity.getString(R.string.categories_do_you_want_to_learn_words_dialog_message, wordsToLearnArrayList.size, category.title)
                positiveText = activity.getString(R.string.yes)
                negativeText = activity.getString(R.string.cancel)
                setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                    override fun onPositiveClick(view: View) {
                        var wordsAddedForLearningCounter = 0
                        var wordsSuccessfullyAddedForLearningCounter = 0

                        wordsToLearnArrayList.forEach { word ->
                            word.level = Word.LEVEL_DAY.toLong()
                            word.setNextDate()

                            val updateWordThread = UpdateWordThread(word)
                            updateWordThread.setUpdateWordThreadListener(object : UpdateWordThread.UpdateWordThreadListener {
                                override fun onSuccess() {
                                    wordsSuccessfullyAddedForLearningCounter++

                                    if (++wordsAddedForLearningCounter >= wordsToLearnArrayList.size) {
                                        notifyAboutSuccessfulAddingWordsToLearning(
                                            wordsSuccessfullyAddedForLearningCounter,
                                            wordsAddedForLearningCounter
                                        )
                                    }

                                }

                                override fun onFailure(exception: Exception) {
                                    if (++wordsAddedForLearningCounter >= wordsToLearnArrayList.size) {
                                        notifyAboutSuccessfulAddingWordsToLearning(
                                            wordsSuccessfullyAddedForLearningCounter,
                                            wordsAddedForLearningCounter
                                        )
                                    }
                                }
                            })
                            updateWordThread.run()
                        }
                    }
                })
            }
            atheneDialog.build()
            atheneDialog.show()
        }
        categoryCellOpening.view.categoryActionShareLinearLayout.setOnClickListener {
            index = categoriesMutableList.indexOf(category)

            run {
                val threeBounce = ThreeBounce()
                val imageView = categoryCellOpening.view.categoryActionShareImageView

                categoryCellOpening.view.categoryActionShareLinearLayout.isClickable = false
                imageView.apply {
                    setImageDrawable(threeBounce)
                    this.layoutParams = this.layoutParams.apply {
                        width = imageView.measuredWidth
                        height = imageView.measuredHeight
                    }
                }
                threeBounce.start()
            }

            val generateDynamicLinksThread =
                GenerateDynamicLinksThread(categoriesMutableList[index].title)
            generateDynamicLinksThread.setGenerateDynamicLinkResultListener(object : GenerateDynamicLinksThread.GenerateDynamicLinkResultListener {
                override fun onSuccess(shortLink: String?) {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        activity.getString(R.string.share_extra_text, categoriesMutableList[index].title) + shortLink
                    )
                    activity.startActivity(Intent.createChooser(intent, "Поделиться категорией"))

//                    Disable loading animation
                    run {
                        @SuppressLint("HandlerLeak")
                        val handler = object : Handler() {
                            override fun handleMessage(msg: Message) {
                                super.handleMessage(msg)

                                categoryCellOpening.view.categoryActionShareLinearLayout.isClickable = true
                                categoryCellOpening.view.categoryActionShareImageView.animateAlphaTo(
                                    0F,
                                    duration = ANIMATION_DURATION_TWO_THIRDS,
                                    onAnimationEnd = {
                                        categoryCellOpening.view.categoryActionShareImageView
                                            .setImageDrawable(activity.getDrawable(R.drawable.ic_share_white))
                                        categoryCellOpening.view.categoryActionShareImageView.animateAlphaTo(
                                            1F,
                                            duration = ANIMATION_DURATION_TWO_THIRDS
                                        )
                                    }
                                )
                            }
                        }

                        Thread {
                            Thread.sleep(1500)
                            handler.sendEmptyMessage(0)
                        }.start()
                    }
                }

                override fun onFailure(exception: Exception) {
                    if(
                        exception is ApiException &&
                        exception.statusCode == CommonStatusCodes.NETWORK_ERROR
                    ) {
                        val intent = Intent(activity, LoadingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        activity.startActivity(intent)

                        return
                    }

                    val toast = Toast(activity)
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                        if(exception is ApiException && exception.statusCode == CommonStatusCodes.TIMEOUT) activity.getString(R.string.api_exception_timeout_toast_message)
                        else activity.getString(R.string.database_error_unknown_error_toast_message)

                    toast.show()


                    categoryCellOpening.view.categoryActionShareLinearLayout.isClickable = true
                    categoryCellOpening.view.categoryActionShareImageView
                        .setImageDrawable(activity.getDrawable(R.drawable.ic_share_white))
                }
            })
            generateDynamicLinksThread.run()
        }
        categoryCellOpening.view.categoryActionMoreDetailsLinearLayout.setOnClickListener {
            index = categoriesMutableList.indexOf(category)

            val intent = Intent(
                activity,
                CategoryActivity::class.java
            ).putExtra(
                activity.getString(R.string.category_extra),
                categoriesMutableList[index].title
            )

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                categoryCellOpening.view.categoryTitleTextView,
                activity.getString(R.string.category_title_transition_name)
            )
            activity.startActivity(intent, options.toBundle())
        }
        categoryCellOpening.view.categoryActionDeleteLinearLayout.setOnClickListener {
            index = categoriesMutableList.indexOf(category)

            val atheneDialog = AtheneDialog(activity)
            atheneDialog.message = activity.getString(R.string.categories_do_you_want_to_disband_category_toast_message)
            atheneDialog.positiveText = activity.getString(R.string.yes)
            atheneDialog.negativeText = activity.getString(R.string.cancel)
            atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
                override fun onPositiveClick(view: View) {

                    val disbandCategoryThread =
                        DisbandCategoryThread(categoriesMutableList[index], wordsInCategoriesMatrix[index], activity)
                    disbandCategoryThread.setOnOnDisbandCategoryListener(object : DisbandCategoryThread.OnDisbandCategoryListener {
                        override fun onSuccess() {
                            notifyDataSetChange()
                        }

                        override fun onFailure(exception: Exception) {
                            if(exception is FirebaseNetworkException) {
                                val intent = Intent(activity, LoadingActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                activity.startActivity(intent)

                                return
                            }

                            val toast = Toast(activity)
                            toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                            toast.duration = Toast.LENGTH_LONG
                            toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                activity.getString(R.string.categories_failed_to_disband_category_toast_message)
                            toast.show()
                        }
                    })
                    disbandCategoryThread.run()
                }
            })
            atheneDialog.build()
            atheneDialog.show()
        }

//        Attach view to scrollView
        scrollView.scrollViewContentLinearLayout.addView(categoryCellOpening.view, index)

//        Init expanded sizes and hide actionsLinearLayout
        categoryCellOpening.view.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                categoryCellOpening.setExpandedSize(categoryCellOpening.actionsLinearLayout)
                categoryCellOpening.collapse(0)

                categoryCellOpening.view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun notifyDataSetChange() {
        fun slideInAddedElement(difference: DifferenceFixer.Difference) {
            categoriesMutableList.add(difference.index, difference.element)
            drawCell(difference.element, difference.index)
            scrollView.scrollViewContentLinearLayout[difference.index].visibility = View.INVISIBLE

            categoryCellOpeningMutableList[difference.index].setOnStateChangeListener(object : CategoryCellOpening.OnStateChangeListener {
                override fun onCollapse() {
                    super.onCollapse()

                    categoryCellOpeningMutableList[difference.index].removeOnStateChangeListener()

                    @SuppressLint("HandlerLeak")
                    val handler = object : Handler() {
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)

                            val linearLayoutElement = scrollView.scrollViewContentLinearLayout[difference.index]
                            val expandedViewHeight = linearLayoutElement.measuredHeight

                            linearLayoutElement.visibility = View.INVISIBLE
                            linearLayoutElement.animateHeightTo(0, duration = 0)
                            linearLayoutElement.animateAlphaTo(
                                expectingAlpha = 1F,
                                duration = 1,
                                onAnimationEnd = {
                                    linearLayoutElement.animateHeightTo(
                                        expectingHeight = expandedViewHeight,
                                        duration = ANIMATION_DURATION_HALF
                                    )
                                    linearLayoutElement.animateAlphaTo(
                                        expectingAlpha = 1F,
                                        duration = ANIMATION_DURATION_HALF,
                                        onAnimationEnd = {
                                            linearLayoutElement.visibility = View.VISIBLE
                                            linearLayoutElement.slideInFromRight(duration = ANIMATION_DURATION_HALF)
                                            linearLayoutElement.layoutParams = linearLayoutElement.layoutParams.apply {
                                                height = LinearLayout.LayoutParams.WRAP_CONTENT
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                    Thread {
                        Thread.sleep(0)
                        handler.sendEmptyMessage(0)

                    }.start()
                }
            })
        }

        fun rebindWordsForCells() {
            wordsInCategoriesMatrix.clear()
            categoriesMutableList.forEach { category ->
                val wordsInCurrentCategoryMutableList = ArrayList<Word>()
                storedInStorageWordsArrayList.forEach { word ->
                    if(
                        word.category.trim().toLowerCase(Locale.ROOT) ==
                        category.title.trim().toLowerCase(Locale.ROOT)
                    ) {
                        wordsInCurrentCategoryMutableList.add(word)
                    }
                }

                wordsInCategoriesMatrix.add(wordsInCurrentCategoryMutableList)

                categoryCellOpeningMutableList[categoriesMutableList.indexOf(category)]
                    .actionMoreDetailsLinearLayout.setOnClickListener {
                        val intent = Intent(
                            activity,
                            CategoryActivity::class.java
                        ).putExtra(
                            activity.getString(R.string.category_extra),
                            category.title
                        ).putParcelableArrayListExtra(
                            activity.getString(R.string.words_extra),
                            wordsInCurrentCategoryMutableList
                        )

                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            categoryCellOpeningMutableList[categoriesMutableList.indexOf(category)].view.categoryTitleTextView,
                            activity.getString(R.string.category_title_transition_name)
                        )
                        activity.startActivity(intent, options.toBundle())
                    }
            }
        }

        val newCategoriesMutableList =
            getCategoriesArrayListWithDefault().filter { it.title != activity.getString(R.string.no_category) } as ArrayList

        val differenceFixer = DifferenceFixer(
            categoriesMutableList.clone() as ArrayList<Category>,
            newCategoriesMutableList.clone() as ArrayList<Category>
        )

        val differencesOfTypeRemoved =
            arrayListOf<DifferenceFixer.Difference>()

        var nextDifference = differenceFixer.getNextDifference()
        while(nextDifference != null) {
            val currentDifference = nextDifference
            nextDifference = differenceFixer.getNextDifference()

            when(currentDifference.type) {
                DifferenceFixer.Difference.Type.ADDED_TO -> slideInAddedElement(currentDifference)
                DifferenceFixer.Difference.Type.REMOVED_AT -> {
                    differencesOfTypeRemoved.add(currentDifference)

                    if(nextDifference == null) {
                        var onHiddenElementsCounter = 0

                        differencesOfTypeRemoved.sortedBy { it.index }.reversed().forEach { differenceOfTypeRemoved ->
                            scrollView.scrollViewContentLinearLayout[differenceOfTypeRemoved.index].apply {
                                slideOutToLeft(
                                    duration = ANIMATION_DURATION_HALF,
                                    onAnimationEnd = {
                                        visibility = View.INVISIBLE
                                        animateHeightTo(
                                            expectingHeight = 0,
                                            duration = ANIMATION_DURATION_HALF
                                        )
                                        animateAlphaTo(
                                            expectingAlpha = 0F,
                                            duration = ANIMATION_DURATION_HALF,
                                            onAnimationEnd = {
//                                                If all elements are already hidden, delete all
                                                if(++onHiddenElementsCounter == differencesOfTypeRemoved.size) {
                                                    scrollView.scrollViewContentLinearLayout.removeViewAt(differenceOfTypeRemoved.index)
                                                    categoriesMutableList.removeAt(differenceOfTypeRemoved.index)
                                                    categoryCellOpeningMutableList.removeAt(differenceOfTypeRemoved.index)

//                                                    Rebind words list for cells
                                                    rebindWordsForCells()
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

//        Rebind words list for cells
        if(differencesOfTypeRemoved.isEmpty()) { rebindWordsForCells() }
    }
}