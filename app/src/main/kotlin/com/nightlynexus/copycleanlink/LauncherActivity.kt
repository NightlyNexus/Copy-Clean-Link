package com.nightlynexus.copycleanlink

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
  private lateinit var clipboardCopier: ClipboardCopier
  private lateinit var textProgramRunner: TextProgramRunner

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as CopyCleanLinkApplication
    clipboardCopier = app.clipboardCopier
    textProgramRunner = app.textProgramRunner
    super.onCreate(savedInstanceState)

    val contentView = findViewById<ViewGroup>(android.R.id.content)
    window.setLayout(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.WRAP_CONTENT
    )

    val dialogController = DialogController(
      textProgramRunner,
      contentView
    )

    if (savedInstanceState == null) {
      // The window must be in focus for Android to allow us to look at the clipboard.
      contentView.viewTreeObserver.addOnWindowFocusChangeListener(
        object : ViewTreeObserver.OnWindowFocusChangeListener {
          override fun onWindowFocusChanged(hasFocus: Boolean) {
            contentView.viewTreeObserver.removeOnWindowFocusChangeListener(this)
            dialogController.setText(clipboardCopier.getCurrentText())
          }
        }
      )
    }

    contentView.addView(dialogController.getView())
  }
}
