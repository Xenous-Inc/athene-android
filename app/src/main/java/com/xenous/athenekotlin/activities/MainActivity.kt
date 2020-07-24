package com.xenous.athenekotlin.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Student
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.exceptions.UserIsAlreadyInClassroomException
import com.xenous.athenekotlin.fragments.FragmentsViewPagerAdapter
import com.xenous.athenekotlin.storage.checkingWordsArrayList
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.storage.storedInStorageCategoriesArrayList
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.threads.*
import com.xenous.athenekotlin.utils.*
import com.xenous.athenekotlin.views.AtheneDialog
import kotlinx.android.synthetic.main.activity_main.*
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size


class MainActivity : AppCompatActivity() {
    private var viewPager: ViewPager? = null
    private lateinit var spinKitView: SpinKitView
    private lateinit var dotsIndicator: DotsIndicator

    private companion object {
        const val TAG = "MainActivity"
        const val DYNAMIC_LINK_TAG = "DynamicLink"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start notification
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(this)
        }
        startAlarm(this)

        val signOutImageView = findViewById<ImageView>(R.id.signOutImageView)
        signOutImageView.setOnClickListener {
            val atheneDialog = AtheneDialog(this)
            atheneDialog.apply {
                message = "Вы уверены, что хотите выйти?"
                positiveText = getString(R.string.yes)
                negativeText = getString(R.string.cancel)
                setOnAnswersItemClickListener(object  : AtheneDialog.OnAnswersItemClickListener {
                    override fun onPositiveClick(view: View) {
                        Firebase.auth.signOut()
                        val intent = Intent(this@MainActivity, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                })
                build()
                show()
            }
        }

        spinKitView = findViewById(R.id.spinKitView)
        viewPager = findViewById(R.id.viewPager)

        swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPurple200,
            R.color.colorPurple400,
            R.color.colorPurple600,
            R.color.colorPurple800
        )
        swipeRefreshLayout.setOnRefreshListener {
            val readWordsThread = ReadWordsThread()
            readWordsThread.setDownloadWordResultListener(object : ReadWordsThread.ReadWordsResultListener {
                override fun onSuccess(wordsList: List<Word>, categoriesList: List<Category>) {
                    storedInStorageCategoriesArrayList =
                        if(categoriesList.isNotEmpty()) categoriesList.toMutableList() as ArrayList
                        else arrayListOf()

                    storedInStorageWordsArrayList =
                        if(wordsList.isNotEmpty()) wordsList.toMutableList() as ArrayList
                        else arrayListOf()

                    swipeRefreshLayout.isRefreshing = false
                    viewPager?.let { pager ->
                        pager.adapter?.let { adapter ->
                            (adapter as FragmentsViewPagerAdapter).notifyDataSetChanged()
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    if(exception is FirebaseNetworkException) {
                        val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        return
                    }

                    val toast = Toast(this@MainActivity)
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
                        val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)

                        return
                    }

                    val toast = Toast(this@MainActivity)
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.database_error_unknown_error_toast_message)
                    toast.show()
                }
            })
            readWordsThread.run()
        }
        setStatusBarHeight()

//      Read Word Thread
        val readWordsThread = ReadWordsThread()
        readWordsThread.setDownloadWordResultListener(object : ReadWordsThread.ReadWordsResultListener {
            override fun onSuccess(
                wordsList: List<Word>,
                categoriesList: List<Category>
            ) {
                Log.d(TAG, "Words List Size is ${wordsList.size}")
                Log.d(TAG, "Categories List Size is ${categoriesList.size}")

                storedInStorageWordsArrayList = wordsList.toMutableList()
                storedInStorageCategoriesArrayList = categoriesList.toMutableList()
                checkingWordsArrayList = initWordsToCheck(wordsList)

//              Optimize View Pager
                viewPager?.adapter = FragmentsViewPagerAdapter(supportFragmentManager, 0)
                viewPager?.offscreenPageLimit = 4
                viewPager?.currentItem = 1
                viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageSelected(position: Int) {
                        notifyDataSetChange()
                    }

                    override fun onPageScrollStateChanged(state: Int) {  }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {  }
                })

//              Connect
                dotsIndicator = findViewById(R.id.dotsIndicator)
                viewPager?.let { dotsIndicator.setViewPager(it) }
                dotsIndicator.visibility = View.VISIBLE

                spinKitView.visibility = View.INVISIBLE

                parseDynamicLink(intent)
            }

            override fun onFailure(exception: Exception) {
                if(exception is FirebaseNetworkException) {
                    val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                    return
                }

                val toast = Toast(this@MainActivity)
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
                    val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                    return
                }

                val toast = Toast(this@MainActivity)
                toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.duration = Toast.LENGTH_LONG
                toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.database_error_unknown_error_toast_message)
                toast.show()
            }
        })
        readWordsThread.run()
    }

    override fun onResume() {
        super.onResume()

        notifyDataSetChange()
    }

    private fun notifyDataSetChange() {
        viewPager?.let { pager ->
            pager.adapter?.let { adapter ->
                (adapter as FragmentsViewPagerAdapter).notifyDataSetChanged()
            }
        }
    }

    private fun setStatusBarHeight() {
        statusBarHorizontalGuideline.setOnApplyWindowInsetsListener { _, insets ->
            val statusBarSize = insets.systemWindowInsetTop
            statusBarHorizontalGuideline.setGuidelineBegin(statusBarSize)

            return@setOnApplyWindowInsetsListener insets
        }
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
            .addShapes(Shape.Circle, Shape.Square)
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

            val currentTime = getCurrentDateTimeAtZeroHoursInMills()
            if(currentTime > word.dateOfNextCheck) {
                if(word.dateOfNextCheck == Word.LEVEL_DAY.toLong() || word.dateOfNextCheck == Word.LEVEL_WEEK.toLong()) {
                    if(currentTime - word.dateOfNextCheck > Word.CHECK_INTERVAL[Word.LEVEL_DAY]!! * 3) {
                        word.level = Word.LEVEL_DAY.toLong()
                    }
                }
                checkingWordsList.add(word)
            }
        }

        return checkingWordsList.shuffled().toMutableList()
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

                                override fun onFailure(exception: Exception) {
                                    if(exception is FirebaseNetworkException) {
                                        val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)

                                        return
                                    }

                                    val toast = Toast(this@MainActivity)
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
                                        val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)

                                        return
                                    }

                                    val toast = Toast(this@MainActivity)
                                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                                    toast.duration = Toast.LENGTH_LONG
                                    toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.database_error_unknown_error_toast_message)
                                    toast.show()
                                }
                            }
                        )
                        readSharingCategoryThread.run()
                    }
                }
                else if(path == "/invite") {
                    Log.d(DYNAMIC_LINK_TAG, "Receive Invite Link")
                    val teacherUid = link.getQueryParameter("teacherId") // ToDo: Replace to constants
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
                                    when(exception) {
                                        is UserIsAlreadyInClassroomException ->
                                            createUserIsAlreadyInClassAtheneDialog()
                                        is FirebaseNetworkException -> {
                                            val loadingActivityIntent = Intent(this@MainActivity, LoadingActivity::class.java)
                                            loadingActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(loadingActivityIntent)
                                        }
                                        else -> {
                                            val toast = Toast(this@MainActivity)
                                            toast.view =
                                                layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                                            toast.duration = Toast.LENGTH_LONG
                                            toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                                getString(R.string.database_error_unknown_error_toast_message)
                                            toast.show()
                                        }
                                    }
                                }

                                override fun onError(databaseError: DatabaseError) {
                                    if(
                                        databaseError.code == DatabaseError.DISCONNECTED ||
                                        databaseError.code == DatabaseError.NETWORK_ERROR
                                    ) {
                                        val loadingActivityIntent = Intent(this@MainActivity, LoadingActivity::class.java)
                                        loadingActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(loadingActivityIntent)

                                        return
                                    }

                                    val toast = Toast(this@MainActivity)
                                    toast.view =
                                        layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                                    toast.duration = Toast.LENGTH_LONG
                                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                        getString(R.string.database_error_unknown_error_toast_message)
                                    toast.show()
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

    private fun createUserIsAlreadyInClassAtheneDialog() {
        val atheneDialog = AtheneDialog(this)
        atheneDialog.message = getString(R.string.main_user_is_already_in_class_dialog_message)
        atheneDialog.positiveText = getString(R.string.understand)
        atheneDialog.build()
        atheneDialog.show()
    }

    private fun createInviteToClassroomAtheneDialog(classroom: Classroom) {
        //ToDo: Replace with string resource
        val message = "Учитель " + classroom.teacherName + " Приглашает вас в класс " + classroom.classroomName

        Log.d("BOB", classroom.categoriesTitlesList.size.toString())
        Log.d("BOB", classroom.wordsListsList.size.toString())
        val atheneDialog = AtheneDialog(this)
        atheneDialog.message = message
        atheneDialog.hint = getString(R.string.main_invite_student_name_input_hint)
        atheneDialog.positiveText = getString(R.string.accept)
        atheneDialog.negativeText = getString(R.string.deny)
        atheneDialog.setOnAnswersItemClickListener(
                object : AtheneDialog.OnAnswersItemClickListener {
            override fun onPositiveClick(view: View) {
                val studentName = atheneDialog.categoryEditText.text.toString().trim()

                val synchronizeStudentWithClassroomThread
                        = SynchronizeStudentWithClassroomThread(
                            classroom,
                            Student(
                                studentName,
                                FirebaseAuth.getInstance().currentUser!!.uid
                            )
                        )
                synchronizeStudentWithClassroomThread.run()
            }
        })
        atheneDialog.build()
        atheneDialog.show()
    }

    private fun createAddCategoryAtheneDialog(categoryTitle: String, wordsList: List<Word>) {
        val message = getString(R.string.main_are_you_sure_to_add_shared_category_dialog_message, categoryTitle, wordsList.size)

        val atheneDialog = AtheneDialog(this)
        atheneDialog.message = message
        atheneDialog.positiveText = getString(R.string.add)
        atheneDialog.negativeText = getString(R.string.cancel)
        atheneDialog.setOnAnswersItemClickListener(object : AtheneDialog.OnAnswersItemClickListener {
            override fun onPositiveClick(view: View) {

//              Check is current category is already existed
                var isCategoryNew = true
                for(category in getCategoriesArrayListWithDefault()) {
                    if(category.title == categoryTitle) {
                        isCategoryNew = false

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

                            storedInStorageCategoriesArrayList.add(category)
                            notifyDataSetChange()
                        }

                        override fun onFailure(exception: Exception) {
                            if(exception is FirebaseNetworkException) {
                                val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)

                                return
                            }

                            val toast = Toast(this@MainActivity)
                            toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                            toast.duration = Toast.LENGTH_LONG
                            toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                getString(R.string.database_error_unknown_error_toast_message)
                            toast.show()

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

                            storedInStorageWordsArrayList.add(word)
                            notifyDataSetChange()
                        }

                        override fun onFailure(exception: Exception) {
                            if(exception is FirebaseNetworkException) {
                                val intent = Intent(this@MainActivity, LoadingActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)

                                return
                            }

                            val toast = Toast(this@MainActivity)
                            toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                            toast.duration = Toast.LENGTH_LONG
                            toast.view.findViewById<TextView>(R.id.toastTextView).text =
                                getString(R.string.database_error_unknown_error_toast_message)
                            toast.show()

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