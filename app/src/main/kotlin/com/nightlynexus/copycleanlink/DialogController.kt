package com.nightlynexus.copycleanlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

internal class DialogController(
  private val textProgramRunner: TextProgramRunner,
  parentView: ViewGroup
) {
  private val context = parentView.context
  private val rootView: View
  private val input: TextView
  private val submit: View
  private val textWarner = RealTextWarner(context)

  init {
    val inflater = LayoutInflater.from(context)
    rootView = inflater.inflate(R.layout.controller_dialog, parentView, false)
    parentView.requestApplyInsets()
    input = rootView.findViewById(R.id.input)
    submit = rootView.findViewById(R.id.submit)

    submit.setOnClickListener {
      submit(input.text)
    }
  }

  fun setText(text: CharSequence?) {
    input.text = text
  }

  private fun submit(text: CharSequence) {
    textProgramRunner.run(textWarner, text)
  }

  fun getView(): View {
    return rootView
  }
}
