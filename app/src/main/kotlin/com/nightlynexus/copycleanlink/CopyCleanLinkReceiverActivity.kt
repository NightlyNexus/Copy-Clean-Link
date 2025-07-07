package com.nightlynexus.copycleanlink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class CopyCleanLinkReceiverActivity : Activity() {
  private lateinit var linkCleaner: LinkCleaner
  private lateinit var ampResolver: AmpResolver
  private lateinit var linkCopier: LinkCopier

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as CopyCleanLinkApplication
    linkCleaner = app.linkCleaner
    ampResolver = app.ampResolver
    linkCopier = app.linkCopier
    super.onCreate(savedInstanceState)

    val action = intent.action
    val text = when (action) {
      Intent.ACTION_PROCESS_TEXT -> {
        val textExtra = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        if (textExtra == null) {
          Toast.makeText(
            this,
            R.string.toast_process_text_missing_extra,
            LENGTH_LONG
          ).show()
          finish()
          return
        }
        textExtra
      }

      Intent.ACTION_SEND -> {
        val textExtra = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (textExtra == null) {
          Toast.makeText(
            this,
            R.string.toast_send_missing_extra,
            LENGTH_LONG
          ).show()
          finish()
          return
        }
        textExtra
      }

      else -> {
        throw AssertionError()
      }
    }

    val trimmedText = text.trim()

    val httpUrl = httpUrlOrNull(trimmedText)

    if (httpUrl == null) {
      Toast.makeText(
        this,
        getString(R.string.toast_invalid_link, trimmedText),
        LENGTH_LONG
      ).show()
      finish()
      return
    }

    var cleanedLink: String? = null
    var resolvedAmp = false

    linkCleaner.cleanLink(httpUrl, object : LinkCleaner.Callback {
      override fun cleanUrl(cleanUrl: HttpUrl) {
        if (resolvedAmp) {
          return
        }
        // TODO: Show a toast?
        val cleanedUrl = cleanUrl.toString()
        cleanedLink = cleanedUrl
        linkCopier.copyLink(cleanedUrl)
      }
    })

    ampResolver.resolveAmp(httpUrl, object : AmpResolver.Callback {
      override fun onIoFailure(e: IOException) {
        // TODO
      }

      override fun onHttpFailure(code: Int, message: String) {
        // TODO
      }

      override fun onResolved(link: String) {
        if (link == cleanedLink) {
          return
        }
        runOnUiThread {
          resolvedAmp = true
          // TODO: Show a toast?
          linkCopier.copyLink(link)
        }
      }
    })

    finish()
  }

  // Also upgrades to https.
  private fun httpUrlOrNull(input: String): HttpUrl? {
    val url = when {
      input.startsWith("ws:", ignoreCase = true) -> {
        // Upgrade to https.
        "https:${input.substring(3)}"
      }

      input.startsWith("wss:", ignoreCase = true) -> {
        "https:${input.substring(4)}"
      }

      input.startsWith("http://", ignoreCase = true) -> {
        // Upgrade to https.
        "https:${input.substring(5)}"
      }

      input.startsWith("https://", ignoreCase = true) -> {
        input
      }

      else -> "https://$input"
    }
    return url.toHttpUrlOrNull()
  }
}
