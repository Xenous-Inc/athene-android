package com.xenous.athenekotlin.threads

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.xenous.athenekotlin.utils.USER_REFERENCE
import com.xenous.athenekotlin.utils.WORD_CATEGORY_DATABASE_KEY
import java.net.URI
import java.net.URISyntaxException

class GenerateDynamicLinksThread(
    private val categoryTitle: String
) {

    private companion object {
        const val TAG = "GenerateDynamicLink"
    }

    interface GenerateDynamicLinkResultListener {
        fun onSuccess(shortLink: String?)

        fun onFailure(exception: Exception)

        fun onCanceled() {}
    }

    private var generateDynamicLinkResultListener: GenerateDynamicLinkResultListener? = null

    fun setGenerateDynamicLinkResultListener(generateDynamicLinkResultListener: GenerateDynamicLinkResultListener) {
        this.generateDynamicLinkResultListener = generateDynamicLinkResultListener
    }

    fun run() {
        val uri = generateLink()
        if(uri == null) {
            Log.d(TAG, "Error while generating uri")
            generateDynamicLinkResultListener?.onFailure(Exception("URI must not be null"))

            return
        }

        Log.d(TAG, "Current uri : $uri")

        FirebaseDynamicLinks
            .getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(uri.toString()))
            .setDomainUriPrefix("https://athene.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.xenous.athenekotlin").build()
            )
            .buildShortDynamicLink()
            .addOnSuccessListener { shortDynamicLink ->
                Log.d(TAG, "Short link has been completely created")

                generateDynamicLinkResultListener?.onSuccess(shortDynamicLink.shortLink.toString())
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "Error while creating short link, The error is - $e")

                generateDynamicLinkResultListener?.onFailure(e)
            }
    }

    @Throws(URISyntaxException::class)
    private fun appendQueryParameters(
        uri: String,
        key: String,
        parameter: String
    ): URI? {
        val oldUri = URI(uri)
        val buildQueryBuilder = StringBuilder()

        buildQueryBuilder.append(key)
        buildQueryBuilder.append("=")
        buildQueryBuilder.append(parameter)

        var newQuery = oldUri.query
        if(newQuery == null) {
            newQuery = buildQueryBuilder.toString()
        }
        else {
            newQuery += "&$buildQueryBuilder"
        }
        return URI(
            oldUri.scheme,
            oldUri.authority,
            oldUri.path,
            newQuery,
            oldUri.fragment
        )
    }

    private fun generateLink(): URI? {
        val uri = "https://athene.page.link/category"

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser == null) {
            Log.d(TAG, "User is null")
            generateDynamicLinkResultListener?.onFailure(Exception("FirabaseUser must not be null"))

            return null
        }

        var currentUri = appendQueryParameters(uri, USER_REFERENCE, currentUser.uid)
        currentUri = appendQueryParameters(currentUri.toString(), WORD_CATEGORY_DATABASE_KEY, categoryTitle)

        return currentUri
    }
}