package com.xenous.athenekotlin.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.database.DatabaseError
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.exceptions.UserExistInClassroomException
import com.xenous.athenekotlin.fragments.FragmentsViewPagerAdapter
import com.xenous.athenekotlin.storage.categoriesArrayList
import com.xenous.athenekotlin.storage.checkingWordsArrayList
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.storage.wordsArrayList
import com.xenous.athenekotlin.threads.*
import com.xenous.athenekotlin.utils.USER_REFERENCE
import com.xenous.athenekotlin.utils.WORD_CATEGORY_DATABASE_KEY
import com.xenous.athenekotlin.utils.getCurrentDateTimeInMills
import com.xenous.athenekotlin.views.AtheneDialog
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape.Companion.CIRCLE
import nl.dionsegijn.konfetti.models.Shape.Companion.RECT
import nl.dionsegijn.konfetti.models.Size

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var spinKitView: SpinKitView
    private lateinit var dotsIndicator: DotsIndicator

    private companion object {
        const val TAG = "MainActivity"
        const val DYNAMIC_LINK_TAG = "DynamicLink"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        spinKitView = findViewById(R.id.spinKitView)
        viewPager = findViewById(R.id.viewPager)



//      Read Word Thread
        val readWordsThread = ReadWordsThread()
        readWordsThread.setDownloadWordResultListener(object : ReadWordsThread.ReadWordsResultListener {
            override fun onSuccess(
                wordsList: List<Word>,
                categoriesList: List<Category>
            ) {
                Log.d(TAG, "Words List Size is ${wordsList.size}")
                Log.d(TAG, "Categories List Size is ${categoriesList.size}")

                wordsArrayList = wordsList.toMutableList()
                categoriesArrayList = categoriesList.toMutableList()
                checkingWordsArrayList = initWordsToCheck(wordsList)

//              Optimize View Pager
                viewPager.adapter = FragmentsViewPagerAdapter(supportFragmentManager, 0)
                viewPager.offscreenPageLimit = 4
                viewPager.currentItem = 1

//              Connect
                dotsIndicator = findViewById(R.id.dotsIndicator)
                dotsIndicator.setViewPager(viewPager)

                spinKitView.visibility = View.INVISIBLE

                parseDynamicLink(intent)
            }

            override fun onError(error: DatabaseError) {
                Log.d(TAG, "Error in database. The error is ${error.message}")
            }
        })
        readWordsThread.run()
    }

    fun callConfetti() {
        val konfettiView = findViewById<KonfettiView>(R.id.konfettiView)
        konfettiView.build()
            .addColors(
                Color.MAGENTA,
                Color.YELLOW,
                resources.getColor(R.color.colorCorrect),
                resources.getColor(R.color.colorIncorrect)
            )
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(CIRCLE, RECT)
            .addSizes(Size(10, 5f))
            .setPosition(-50f, konfettiView.width + 50f, -50f, -50f)
            .streamFor(300, 1500L)
    }

    private fun initWordsToCheck(words : List<Word>): MutableList<Word> {
        val checkingWordsList = mutableListOf<Word>()

        for(word in words) {
            if(word.level == Word.LEVEL_ARCHIVED.toLong() || word.level == Word.LEVEL_ADDED.toLong()) {
                continue
            }

            val currentTime = getCurrentDateTimeInMills()
            if(currentTime > word.lastDateCheck) {
                if(word.lastDateCheck == Word.LEVEL_DAY.toLong() || word.lastDateCheck == Word.LEVEL_WEEK.toLong()) {
                    if(currentTime - word.lastDateCheck > Word.CHECK_INTERVAL[Word.LEVEL_DAY]!! * 3) {
                        word.level = Word.LEVEL_DAY.toLong()
                    }
                }
                checkingWordsList.add(word)
            }
        }

        return checkingWordsList
    }

    private fun parseDynamicLink(intent: Intent?) {
        if(intent == null) {
            return
        }

//      Parse Dynamic Links
        FirebaseDynamicLinks
            .getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener {
                if(it == null) {
                    Log.d(DYNAMIC_LINK_TAG, "PendingDynamicLinkData is null")

                    return@addOnSuccessListener
                }
                val link = it.link
                if(link == null) {
                    Log.d(DYNAMIC_LINK_TAG, "Link is null")

                    return@addOnSuccessListener
                }

                val path = link.path
                if(path == null) {
                    Log.d(DYNAMIC_LINK_TAG, "Path is null")

                    return@addOnSuccessListener
                }

                if(path == "/category") {
                    Log.d(DYNAMIC_LINK_TAG, "Receive Add Category Link")
                    val sharingUserUid = link.getQueryParameter(USER_REFERENCE)
                    val categoryTitle = link.getQueryParameter(WORD_CATEGORY_DATABASE_KEY)
                    if(sharingUserUid != null && categoryTitle != null) {
                        val readSharingCategoryThread = ReadSharingCategoryThread(
                            sharingUserUid,
                            categoryTitle
                        )
                        readSharingCategoryThread.setReadSharingCategoryListener(
                            object : ReadSharingCategoryThread.ReadSharingCategoryListener {

                                override fun onSuccess(sharingWordsList: List<Word>) {
                                    Log.d(
                                        DYNAMIC_LINK_TAG,
                                        "Sharing words size is ${sharingWordsList.size}"
                                    )

                                    createAddCategoryAtheneDialog(categoryTitle, sharingWordsList)
                                }

                                override fun onError(error: DatabaseError) {
                                    Log.d(
                                        DYNAMIC_LINK_TAG,
                                        "Error while reading data from database.The error is ${error.message}"
                                    )

//                              ToDo: Create Athene Dialog
                                }
                            }
                        )
                        readSharingCategoryThread.run()
                    }
                }
                else if(path == "/invite") {
                    Log.d(DYNAMIC_LINK_TAG, "Receive Invite Link")
                    val teacherUid = link.getQueryParameter("teacherId") //ToDo: Replace to constants
                    val classUid = link.getQueryParameter("classId")
                    if(teacherUid != null && classUid != null) {
                        val readClassroomThread = ReadClassroomThread(
                            teacherUid,
                            classUid
                        )
                        readClassroomThread.setReadClassroomResultListener(
                            object : ReadClassroomThread.ReadClassroomResultListener {
                                override fun onSuccess(classroom: Classroom) {
                                    Log.d(DYNAMIC_LINK_TAG, classroom.toString())

                                    createInviteToClassroomAtheneDialog(classroom)
                                }

                                override fun onFailure(exception: Exception) {
                                    if(exception is UserExistInClassroomException) {
                                        createUserExistAtheneDialog()
                                    }
                                }

                                override fun onError(databaseError: DatabaseError) {
                                    Log.d(
                                        DYNAMIC_LINK_TAG,
                                        "Error while reading from database." +
                                                " ${databaseError.message}"
                                    )
                                }
                            }
                        )
                        readClassroomThread.run()
                    }
                    else {
                        Log.d(DYNAMIC_LINK_TAG, "One or more query parameters are null")
                    }
                }
            }
            .addOnFailureListener {
                Log.d(
                    DYNAMIC_LINK_TAG,
                    "Error while receiving dynamic link. The error is ${it.message}"
                )
            }
            .addOnCanceledListener {
                Log.d(DYNAMIC_LINK_TAG, "Transaction canceled")
            }
    }

    private fun createUserExistAtheneDialog() {
        val atheneDialog = AtheneDialog(this)
        atheneDialog.message = getString(R.string.user_already_exist)
        atheneDialog.positiveText = getString(R.string.understand)
        atheneDialog.build()
        atheneDialog.show()
    }

    private fun createInviteToClassroomAtheneDialog(classroom: Classroom) {
        val message = "Учитель " + classroom.teacherName + " Приглашает вас в класс " + classroom.classroomName

        val atheneDialog = AtheneDialog(this)
        atheneDialog.message = message
        atheneDialog.hint = getString(R.string.student_name_hint)
        atheneDialog.positiveText = getString(R.string.accept)
        atheneDialog.negativeText = getString(R.string.deny)
        atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
            override fun onPositiveClick(view: View) {
                //Add User To Classroom
            }
        })
        atheneDialog.build()
        atheneDialog.show()
    }

    private fun createAddCategoryAtheneDialog(categoryTitle: String, wordsList: List<Word>) {
        val message = "Хотите ли вы добавить новую категорию $categoryTitle? Новых слов для добавления ${wordsList.size}"

        val atheneDialog = AtheneDialog(this)
        atheneDialog.message = message
        atheneDialog.positiveText = getString(R.string.add_category_message)
        atheneDialog.negativeText = getString(R.string.cancel)
        atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
            override fun onPositiveClick(view: View) {

//              Check is current category is already existed
                var isCategoryNew = false
                for(category in getCategoriesArrayListWithDefault()) {
                    if(category.title == categoryTitle) {
                        isCategoryNew = true

                        break
                    }
                }

//              Pushing category is it is not existed
                if(isCategoryNew) {
                    val addCategoryThread = AddCategoryThread(Category(categoryTitle))
                    addCategoryThread.setAddCategoryResultListener(object :
                        AddCategoryThread.AddCategoryResultListener {
                        override fun onSuccess(category: Category) {
                            Log.d(TAG, "Category has been successfully added to database")
                            //categoriesArrayList.add(category)
                        }

                        override fun onFailure(exception: Exception) {
                            Log.d(
                                TAG,
                                "Error while adding category to database. The error is ${exception.message}"
                            )
                        }
                    })
                    addCategoryThread.run()
                }

//              Add words
                for(word in wordsList) {
                    val addWordThread = AddWordThread(word)
                    addWordThread.setAddWordResultListener(object : AddWordThread.AddWordResultListener {

                        override fun onSuccess(word: Word) {
                            Log.d(TAG, "Word has been successfully added")

                            wordsArrayList.add(word)
                        }

                        override fun onFailure(exception: Exception) {
                            Log.d(
                                TAG,
                                "Error while adding word to database. Error is $exception"
                            )
                        }
                    })
                    addWordThread.run()
                }
            }
        })
        atheneDialog.build()
        atheneDialog.show()
    }
}