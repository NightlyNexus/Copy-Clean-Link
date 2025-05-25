package com.nightlynexus.copycleanlink

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import java.util.concurrent.Executors
import okhttp3.HttpUrl

@MainThread
internal class LinkCleaner(
  private val cleanDomainsLoader: CleanDomainsLoader
) {
  @WorkerThread
  interface CleanDomainsLoader {
    fun load(): List<CleanDomain>
  }

  // LinkCleaner may call the Callback synchronously or asynchronously.
  @MainThread
  interface Callback {
    fun cleanUrl(cleanUrl: HttpUrl)
  }

  private val executor = Executors.newSingleThreadExecutor()
  private val mainHandler = Handler(Looper.getMainLooper())
  private var loading = false
  private val pendingCleanings = mutableListOf<Any>()
  private var realLinkCleaner: RealLinkCleaner? = null

  fun load() {
    check(!loading)
    check(realLinkCleaner == null)
    loading = true
    executor.execute {
      val cleanDomains = cleanDomainsLoader.load()
      val realLinkCleaner = RealLinkCleaner(cleanDomains)
      mainHandler.post {
        this@LinkCleaner.realLinkCleaner = realLinkCleaner
        loading = false
        for (i in pendingCleanings.indices step 2) {
          val url = pendingCleanings[i] as HttpUrl
          val callback = pendingCleanings[i + 1] as Callback
          val cleanUrl = realLinkCleaner.cleanLink(url)
          callback.cleanUrl(cleanUrl)
        }
        pendingCleanings.clear()
      }
    }
  }

  fun cleanLink(url: HttpUrl, callback: Callback) {
    val realLinkCleaner = realLinkCleaner
    if (realLinkCleaner == null) {
      check(loading)
      pendingCleanings += url
      pendingCleanings += callback
    } else {
      val cleanUrl = realLinkCleaner.cleanLink(url)
      callback.cleanUrl(cleanUrl)
    }
  }

  private class RealLinkCleaner(
    private val cleanDomains: List<CleanDomain>
  ) {
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
}
