package com.nightlynexus.copycleanlink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG

class CopyCleanLinkReceiverActivity : Activity() {
  private lateinit var textProgramRunner: TextProgramRunner

  override fun onCreate(savedInstanceState: Bundle?) {
    val app = application as CopyCleanLinkApplication
    textProgramRunner = app.textProgramRunner
    super.onCreate(savedInstanceState)

    val action = intent.action
    val text = when (action) {
      Intent.ACTION_PROCESS_TEXT -> {
        val textExtra = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        if (textExtra == null) {
          Toast.makeText(
            this,
            R.string.toast_process_text_missing_extra,
            LENGTH_LONG
          ).show()
          finish()
          return
        }
        textExtra
      }

      Intent.ACTION_SEND -> {
        val textExtra = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (textExtra == null) {
          Toast.makeText(
            this,
            R.string.toast_send_missing_extra,
            LENGTH_LONG
          ).show()
          finish()
          return
        }
        textExtra
      }

      else -> {
        throw AssertionError()
      }
    }

    val textWarner = RealTextWarner(this)
    textProgramRunner.run(textWarner, text)

    finish()
  }
}
