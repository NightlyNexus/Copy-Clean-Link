package com.nightlynexus.copycleanlink

import android.app.Application
import android.content.ClipboardManager
import okhttp3.OkHttpClient

class CopyCleanLinkApplication : Application() {
  internal lateinit var clipboardCopier: ClipboardCopier
  internal lateinit var textProgramRunner: TextProgramRunner

  override fun onCreate() {
    super.onCreate()
    val linksExtractor = RealLinksExtractor()
    val linksCombiner = LinksCombiner()
    val client = OkHttpClient.Builder().build()
    val ampResolver = AmpResolver(client)
    val linkCleaner = LinkCleaner()
    val clipboardManager = getSystemService(ClipboardManager::class.java)
    val copyLabel = getText(R.string.clipboard_copy_label)
    clipboardCopier = RealClipboardCopier(
      clipboardManager,
      copyLabel
    )
    textProgramRunner = TextProgramRunner(
      linksExtractor,
      linksCombiner,
      linkCleaner,
      ampResolver,
      clipboardCopier
    )
  }
}
