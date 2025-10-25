package com.nightlynexus.copycleanlink

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

internal interface TextWarner {
  fun errorInvalidLink(text: CharSequence)

  fun warnExtractingLinks(text: CharSequence, linkCount: Int)
}

internal class RealTextWarner(
  private val context: Context
) : TextWarner {
  private val resources = context.resources

  override fun errorInvalidLink(text: CharSequence) {
    Toast.makeText(
      context,
      context.getString(R.string.toast_invalid_link, text),
      LENGTH_LONG
    ).show()
  }

  override fun warnExtractingLinks(text: CharSequence, linkCount: Int) {
    Toast.makeText(
      context,
      resources.getQuantityString(
        R.plurals.toast_extracting_links,
        linkCount,
        linkCount,
        text
      ),
      LENGTH_LONG
    ).show()
  }
}
