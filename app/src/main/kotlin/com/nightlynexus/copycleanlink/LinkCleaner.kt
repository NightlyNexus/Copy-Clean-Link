package com.nightlynexus.copycleanlink

import okhttp3.HttpUrl

internal class LinkCleaner {
  fun cleanLink(url: HttpUrl): HttpUrl {
    val cleanUrlBuilder = url.newBuilder()
    for (i in cleanDomains.indices) {
      val cleanDomain = cleanDomains[i]
      val urlString = url.toString()
      if (cleanDomain.matches(urlString)) {
        for (j in cleanDomain.params.indices) {
          val param = cleanDomain.params[j]
          cleanUrlBuilder.removeAllQueryParameters(param)
        }
      }
    }
    return cleanUrlBuilder.build()
  }

  private fun CleanDomain.matches(url: String): Boolean {
    for (i in exclude.indices) {
      val excludeRegex = exclude[i]
      if (excludeRegex.matches(url)) {
        return false
      }
    }
    for (i in include.indices) {
      val includeRegex = include[i]
      if (includeRegex.matches(url)) {
        return true
      }
    }
    return false
  }
}
