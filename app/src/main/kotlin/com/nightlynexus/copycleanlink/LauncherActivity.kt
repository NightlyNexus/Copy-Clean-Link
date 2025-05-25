package com.nightlynexus.copycleanlink

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
  private lateinit var linkCleaner: LinkCleaner
  private lateinit var ampResolver: AmpResolver
  private lateinit var linkCopier: LinkCopier

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as CopyCleanLinkApplication
    linkCleaner = app.linkCleaner
    ampResolver = app.ampResolver
    linkCopier = app.linkCopier
    super.onCreate(savedInstanceState)


    val contentView = findViewById<ViewGroup>(android.R.id.content)
    window.setLayout(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.WRAP_CONTENT
    )

    val dialogController = DialogController(
      linkCleaner,
      ampResolver,
      linkCopier,
      contentView
    )

    if (savedInstanceState == null) {
      // The window must be in focus for Android to allow us to look at the clipboard.
      contentView.viewTreeObserver.addOnWindowFocusChangeListener(
        object : ViewTreeObserver.OnWindowFocusChangeListener {
          override fun onWindowFocusChanged(hasFocus: Boolean) {
            contentView.viewTreeObserver.removeOnWindowFocusChangeListener(this)
            dialogController.setText(linkCopier.getCurrentText())
          }
        }
      )
    }

    contentView.addView(dialogController.getView())
  }
}
