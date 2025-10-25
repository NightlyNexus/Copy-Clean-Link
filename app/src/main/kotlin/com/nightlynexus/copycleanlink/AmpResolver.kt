package com.nightlynexus.copycleanlink

import androidx.annotation.WorkerThread
import java.io.IOException
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.ByteString.Companion.encodeUtf8

internal class AmpResolver(private val client: OkHttpClient) {
  private val htmlLinkPrefix = "<link ".encodeUtf8()
  private val htmlLinkSuffix = '>'.code.toByte()
  private val canonicalAttribute = "rel=\"canonical\"".encodeUtf8()
  private val hrefPrefix = "href=\"".encodeUtf8()
  private val hrefSuffix = '"'.code.toByte()

  @WorkerThread
  interface Callback {
    fun onIoFailure(e: IOException)

    fun onHttpFailure(
      code: Int,
      message: String
    )

    fun onResolved(link: String)
  }

  fun resolveAmp(
    ampUrl: HttpUrl,
    callback: Callback
  ): Call {
    val request = Request.Builder()
      .url(ampUrl)
      .build()
    val call = client.newCall(request)
    call.enqueue(object : okhttp3.Callback {
      override fun onFailure(
        call: Call,
        e: IOException
      ) {
        if (!call.isCanceled()) {
          callback.onIoFailure(e)
        }
      }

      override fun onResponse(
        call: Call,
        response: Response
      ) {
        response.use {
          if (!response.isSuccessful) {
            callback.onHttpFailure(response.code, response.message)
            return
          }
          val source = response.body.source()
          val buffer = source.buffer
          while (true) {
            val htmlLinkPrefixIndex = source.indexOf(htmlLinkPrefix)
            if (htmlLinkPrefixIndex == -1L) {
              callback.onHttpFailure(response.code, "Missing link in HTML document.")
              return
            }
            source.skip(htmlLinkPrefixIndex + htmlLinkPrefix.size)
            val htmlLinkSuffixIndex = source.indexOf(htmlLinkSuffix)
            if (htmlLinkSuffixIndex == -1L) {
              callback.onHttpFailure(response.code, "Missing link ending in HTML document.")
              return
            }
            val canonicalAttributeIndex = buffer.indexOf(canonicalAttribute)
            if (canonicalAttributeIndex != -1L && canonicalAttributeIndex < htmlLinkSuffixIndex) {
              val hrefPrefixIndex = buffer.indexOf(hrefPrefix)
              if (hrefPrefixIndex == -1L) {
                callback.onHttpFailure(response.code, "Missing link in HTML document.")
                return
              }
              buffer.skip(hrefPrefixIndex + hrefPrefix.size)
              val hrefSuffixIndex = buffer.indexOf(hrefSuffix)
              if (hrefSuffixIndex == -1L) {
                callback.onHttpFailure(response.code, "Missing link ending quotation mark.")
                return
              }
              val link = buffer.readUtf8(hrefSuffixIndex)
              callback.onResolved(link)
              return
            }
          }
        }
      }
    })
    return call
  }
}
