package com.nightlynexus.copycleanlink

internal class LinksCombiner {
  fun combineLinks(links: List<String>): String {
    val combined = StringBuilder()
    for (i in links.indices) {
      val link = links[i]
      if (i != 0) {
        combined.append("\n\n")
      }
      combined.append(link)
    }
    return combined.toString()
  }
}
