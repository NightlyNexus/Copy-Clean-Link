package com.nightlynexus.copycleanlink

internal data class CleanDomain(
  val include: List<Regex>,
  val exclude: List<Regex>,
  val params: List<String>
)
