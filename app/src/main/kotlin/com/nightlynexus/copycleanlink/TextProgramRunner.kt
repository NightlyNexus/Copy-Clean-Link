package com.nightlynexus.copycleanlink

import android.content.Context
import android.os.Handler
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.MainThread
import java.io.IOException
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrl

internal class TextProgramRunner(
  private val linksExtractor: LinksExtractor,
  private val linksCombiner: LinksCombiner,
  private val linkCleaner: LinkCleaner,
  private val ampResolver: AmpResolver,
  private val clipboardCopier: ClipboardCopier
) {
  private var ampResolverCalls = mutableListOf<Call>()

  @MainThread
  fun run(context: Context, mainHandler: Handler, text: CharSequence) {
    for (i in ampResolverCalls.indices) {
      val ampResolverCall = ampResolverCalls[i]
      ampResolverCall.cancel()
    }

    val links = linksExtractor.extractLinks(text)

    if (links.isEmpty()) {
      Toast.makeText(
        context,
        context.getString(R.string.toast_invalid_link, text),
        LENGTH_LONG
      ).show()
      return
    }

    // Warn the user if we are not using the original text.
    if (links.size != 1 || links.first() != text.toString()) {
      Toast.makeText(
        context,
        context.resources.getQuantityString(
          R.plurals.toast_extracting_links,
          links.size,
          links.size,
          text
        ),
        LENGTH_LONG
      ).show()
    }

    val cleanLinks = ArrayList<String>(links.size)
    for (i in links.indices) {
      val link = links[i]
      val httpUrl = link.normalizeScheme().toHttpUrl()

      val cleanUrl = linkCleaner.cleanLink(httpUrl).toString()
      cleanLinks += cleanUrl

      ampResolverCalls += ampResolver.resolveAmp(httpUrl, object : AmpResolver.Callback {
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
          mainHandler.post {
            cleanLinks[i] = link
            // TODO: Show a toast?
            clipboardCopier.copyText(linksCombiner.combineLinks(cleanLinks))
          }
        }
      })
    }
    // TODO: Show a toast?
    clipboardCopier.copyText(linksCombiner.combineLinks(cleanLinks))
  }

  // Also upgrades to https.
  private fun CharSequence.normalizeScheme(): String {
    return when {
      startsWith("ws:", ignoreCase = true) -> {
        // Upgrade to https.
        "https:${substring(3)}"
      }

      startsWith("wss:", ignoreCase = true) -> {
        "https:${substring(4)}"
      }

      startsWith("http://", ignoreCase = true) -> {
        // Upgrade to https.
        "https:${substring(5)}"
      }

      startsWith("https://", ignoreCase = true) -> {
        toString()
      }

      else -> "https://$this"
    }
  }
}
