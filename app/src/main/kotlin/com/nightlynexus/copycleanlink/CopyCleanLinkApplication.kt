package com.nightlynexus.copycleanlink

import android.app.Application
import android.content.ClipboardManager
import okhttp3.OkHttpClient

class CopyCleanLinkApplication : Application() {
  internal lateinit var linkCleaner: LinkCleaner
  internal lateinit var ampResolver: AmpResolver
  internal lateinit var linkCopier: LinkCopier

  override fun onCreate() {
    super.onCreate()
    val client = OkHttpClient.Builder().build()
    ampResolver = AmpResolver(client)
    linkCleaner = LinkCleaner()
    val clipboardManager = getSystemService(ClipboardManager::class.java)
    val copyLabel = getText(R.string.clipboard_copy_label)
    linkCopier = LinkCopier(
      clipboardManager,
      copyLabel
    )
  }
}
