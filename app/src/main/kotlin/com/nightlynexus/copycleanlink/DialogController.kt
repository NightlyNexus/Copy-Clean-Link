package com.nightlynexus.copycleanlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import java.io.IOException
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal class DialogController(
  private val linkCleaner: LinkCleaner,
  private val ampResolver: AmpResolver,
  private val linkCopier: LinkCopier,
  parentView: ViewGroup
) {
  private val context = parentView.context
  private val rootView: View
  private val input: TextView
  private val submit: View

  init {
    val inflater = LayoutInflater.from(context)
    rootView = inflater.inflate(R.layout.controller_dialog, parentView, false)
    parentView.requestApplyInsets()
    input = rootView.findViewById(R.id.input)
    submit = rootView.findViewById(R.id.submit)

    submit.setOnClickListener {
      submit(input.text)
    }
  }

  fun setText(text: CharSequence?) {
    if (text == null) {
      input.text = null
      return
    }

    val trimmedText = text.trim()

    val httpUrl = httpUrlOrNull(trimmedText)

    if (httpUrl == null) {
      // Do nothing.
      return
    }
    input.text = httpUrl.toString()
  }

  private fun submit(text: CharSequence) {
    val trimmedText = text.trim()

    val httpUrl = httpUrlOrNull(trimmedText)

    if (httpUrl == null) {
      Toast.makeText(
        context,
        context.getString(R.string.toast_invalid_link, trimmedText),
        LENGTH_LONG
      ).show()
      return
    }

    val cleanUrl = linkCleaner.cleanLink(httpUrl).toString()
    // TODO: Show a toast?
    linkCopier.copyLink(cleanUrl)

    ampResolver.resolveAmp(httpUrl, object : AmpResolver.Callback {
      override fun onIoFailure(e: IOException) {
        // TODO
      }

      override fun onHttpFailure(code: Int, message: String) {
        // TODO
      }

      override fun onResolved(link: String) {
        if (link == cleanUrl) {
          return
        }
        rootView.post {
          // TODO: Show a toast?
          linkCopier.copyLink(link)
        }
      }
    })
  }

  // Also upgrades to https.
  private fun httpUrlOrNull(input: CharSequence): HttpUrl? {
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
        input.toString()
      }

      else -> "https://$input"
    }
    return url.toHttpUrlOrNull()
  }

  fun getView(): View {
    return rootView
  }
}
