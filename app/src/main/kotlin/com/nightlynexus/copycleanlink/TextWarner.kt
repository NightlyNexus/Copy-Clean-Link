package com.nightlynexus.copycleanlink

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

internal class TextWarner(
  private val context: Context
) {
  private val resources = context.resources

  fun errorInvalidLink(text: CharSequence) {
    Toast.makeText(
      context,
      context.getString(R.string.toast_invalid_link, text),
      LENGTH_LONG
    ).show()
  }

  fun warnExtractingLinks(text: CharSequence, linkCount: Int) {
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
