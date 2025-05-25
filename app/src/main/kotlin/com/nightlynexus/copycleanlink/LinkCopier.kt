package com.nightlynexus.copycleanlink

import android.content.ClipData
import android.content.ClipboardManager

internal class LinkCopier(
  private val clipboardManager: ClipboardManager,
  private val label: CharSequence
) {
  fun copyLink(url: CharSequence) {
    val clip = ClipData.newPlainText(label, url)
    clipboardManager.setPrimaryClip(clip)
  }

  fun getCurrentText(): CharSequence? {
    val clip = clipboardManager.primaryClip
    if (clip != null) {
      if (clip.itemCount != 0) {
        return clip.getItemAt(0).text
      }
    }
    return null
  }
}
