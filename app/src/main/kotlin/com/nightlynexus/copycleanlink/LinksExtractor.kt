package com.nightlynexus.copycleanlink

import android.util.Patterns

internal class LinksExtractor {
  private val pattern = Patterns.WEB_URL

  fun extractLinks(text: CharSequence): List<String> {
    val matcher = pattern.matcher(text)
    val links = mutableListOf<String>()
    while (matcher.find()) {
      links += matcher.group()
    }
    return links
  }
}
