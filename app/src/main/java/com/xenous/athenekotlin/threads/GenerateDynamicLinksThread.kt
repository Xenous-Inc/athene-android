package com.xenous.athenekotlin.threads

import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.xenous.athenekotlin.utils.ERROR_CODE
import com.xenous.athenekotlin.utils.SUCCESS_CODE
import com.xenous.athenekotlin.utils.USER_REFERENCE
import com.xenous.athenekotlin.utils.WORD_CATEGORY_DATABASE_KEY
import java.net.URI
import java.net.URISyntaxException

class GenerateDynamicLinksThread(
    private val handler: Handler?,
    private val category: String
) {

    private companion object {
        const val TAG = "GenerateDynamicLink"
    }

    fun generate() {
        val uri = generateLink()
        if(uri == null) {
            Log.d(TAG, "Error while generating uri")
            sendErrorMessage()

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
            .addOnSuccessListener {
                Log.d(TAG, "Short link has been completely created")

                sendSuccessMessage(it.shortLink.toString())
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while creating short link, The error is - $it")

                sendErrorMessage()
            }
    }

    private fun sendErrorMessage() {
        if(handler == null) {
            Log.d(TAG, "Handler is null")
            return
        }

        val message = Message.obtain()
        message.apply {
            what = ERROR_CODE
            obj = null
        }

        handler.sendMessage(message)
    }

    private fun sendSuccessMessage(link: String?) {
        if(handler == null) {
            Log.d(TAG, "Handler is null")
            return
        }

        val message = Message.obtain()
        message.apply {
            what = SUCCESS_CODE
            obj = link
        }

        handler.sendMessage(message)
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
        val uri = "https://athene.page.link"

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser == null) {
            Log.d(TAG, "User is null")
            sendErrorMessage()

            return null
        }

        var currentUri = appendQueryParameters(uri, USER_REFERENCE, currentUser.toString())
        currentUri = appendQueryParameters(currentUri.toString(), WORD_CATEGORY_DATABASE_KEY, category)

        return currentUri
    }
}