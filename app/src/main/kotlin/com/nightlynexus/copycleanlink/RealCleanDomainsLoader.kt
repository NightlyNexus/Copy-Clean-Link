package com.nightlynexus.copycleanlink

import android.content.res.AssetManager
import androidx.annotation.WorkerThread
import com.squareup.moshi.JsonAdapter
import okio.buffer
import okio.source

@WorkerThread
internal class RealCleanDomainsLoader(
  private val assets: AssetManager,
  private val fileName: String,
  private val cleanDomainListAdapter: JsonAdapter<List<CleanDomain>>
) : LinkCleaner.CleanDomainsLoader {
  override fun load(): List<CleanDomain> {
    return assets.open(fileName).source().buffer().use { source ->
      cleanDomainListAdapter.fromJson(source)!!
    }
  }
}
