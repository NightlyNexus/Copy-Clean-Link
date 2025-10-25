package com.nightlynexus.copycleanlink

import android.content.ClipData
import android.content.ClipboardManager

internal class ClipboardCopier(
  private val clipboardManager: ClipboardManager,
  private val label: CharSequence
) {
  fun copyText(text: CharSequence) {
    val clip = ClipData.newPlainText(label, text)
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
