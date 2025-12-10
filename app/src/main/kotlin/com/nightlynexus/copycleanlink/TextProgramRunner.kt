package com.nightlynexus.copycleanlink

import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import okhttp3.HttpUrl.Companion.toHttpUrl

internal class TextProgramRunner(
  private val linksExtractor: LinksExtractor,
  private val linksCombiner: LinksCombiner,
  private val linkCleaner: LinkCleaner,
  private val ampResolver: AmpResolver,
  private val clipboardCopier: ClipboardCopier
) {
  private var ampResolverCalls = mutableListOf<AmpResolver.Call>()

  fun run(textWarner: TextWarner, text: CharSequence) {
    for (i in ampResolverCalls.indices) {
      val ampResolverCall = ampResolverCalls[i]
      ampResolverCall.cancel()
    }

    val links = linksExtractor.extractLinks(text)

    if (links.isEmpty()) {
      textWarner.errorInvalidLink(text)
      return
    }

    // Warn the user if we are not using the original text.
    if (links.size != 1 || links.first() != text.toString()) {
      textWarner.warnExtractingLinks(text, links.size)
    }

    val cleanLinks = CopyOnWriteArrayList<String>()
    for (i in links.indices) {
      val link = links[i]
      val httpUrl = link.normalizeScheme().toHttpUrl()

      val cleanHttpUrl = linkCleaner.cleanLink(httpUrl)
      val cleanUrl = cleanHttpUrl.toString()
      cleanLinks += cleanUrl

      ampResolverCalls += ampResolver.resolveAmp(cleanHttpUrl)
    }

    // TODO: Show a toast?
    clipboardCopier.copyText(linksCombiner.combineLinks(cleanLinks))

    for (i in ampResolverCalls.indices) {
      val ampResolverCall = ampResolverCalls[i]

      val callback = object : AmpResolver.Callback {
        override fun onIoFailure(e: IOException) {
          // TODO
        }

        override fun onHttpFailure(code: Int, message: String) {
          // TODO
        }

        override fun onResolved(link: String) {
          if (link == cleanLinks[i]) {
            return
          }
          cleanLinks[i] = link
          // TODO: Show a toast?
          clipboardCopier.copyText(linksCombiner.combineLinks(cleanLinks))
        }
      }
      ampResolverCall.enqueue(callback)
    }
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
