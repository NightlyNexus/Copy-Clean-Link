package com.nightlynexus.copycleanlink

import android.content.ClipData
import android.content.ClipboardManager

internal interface ClipboardCopier {
  fun copyText(text: CharSequence)

  fun getCurrentText(): CharSequence?
}

internal class RealClipboardCopier(
  private val clipboardManager: ClipboardManager,
  private val label: CharSequence
) : ClipboardCopier {
  override fun copyText(text: CharSequence) {
    val clip = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clip)
  }

  override fun getCurrentText(): CharSequence? {
    val clip = clipboardManager.primaryClip
    if (clip != null) {
      if (clip.itemCount != 0) {
        return clip.getItemAt(0).text
      }
    }
    return null
  }
}
