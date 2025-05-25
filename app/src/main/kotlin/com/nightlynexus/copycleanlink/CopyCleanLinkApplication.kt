package com.nightlynexus.copycleanlink

import android.app.Application
import android.content.ClipboardManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.OkHttpClient

class CopyCleanLinkApplication : Application() {
  internal lateinit var linkCleaner: LinkCleaner
  internal lateinit var ampResolver: AmpResolver
  internal lateinit var linkCopier: LinkCopier

  override fun onCreate() {
    super.onCreate()
    val client = OkHttpClient.Builder().build()
    ampResolver = AmpResolver(client)
    val moshi = Moshi.Builder()
      .add(CleanDomain.RegexJsonAdapter)
      .build()
    val cleanDomainListAdapter = moshi.adapter<List<CleanDomain>>(
      Types.newParameterizedType(List::class.java, CleanDomain::class.java)
    )
    // https://github.com/brave/adblock-lists/blob/653cbb7f1d96092307f630d3fc9e2f64985c5570/brave-lists/clean-urls.json
    // TODO: This is way too slow, unnecessarily.
    val cleanDomainsLoader = RealCleanDomainsLoader(
      assets,
      "clean_urls.json",
      cleanDomainListAdapter
    )
    linkCleaner = LinkCleaner(cleanDomainsLoader)
    linkCleaner.load()
    val clipboardManager = getSystemService(ClipboardManager::class.java)
    val copyLabel = getText(R.string.clipboard_copy_label)
    linkCopier = LinkCopier(
      clipboardManager,
      copyLabel
    )
  }
}
