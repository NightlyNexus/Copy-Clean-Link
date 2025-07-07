package com.nightlynexus.cleanurlsjsonprocessor

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = true)
internal data class CleanDomain(
  val include: List<Regex>,
  val exclude: List<Regex>,
  val params: List<String>
) {
  object RegexJsonAdapter {
    @ToJson fun toJson(regex: Regex): String {
      throw UnsupportedOperationException()
    }

    @FromJson fun fromJson(pattern: String): Regex {
      return convertFromWildcardPattern(pattern).toRegex()
    }

    private fun convertFromWildcardPattern(wildcardPattern: String): String {
      return wildcardPattern
        .replace("\\", "\\\\")
        .replace(".", "\\.")
        .replace("+", "\\+")
        .replace("^", "\\^")
        .replace("$", "\\$")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("[", "\\[")
        .replace("]", "\\]")
        .replace("{", "\\{")
        .replace("}", "\\}")
        .replace("|", "\\|")
        .replace("?", "\\?")
        .replace("/", "\\/")
        .replace("*", ".*")
        .replace(""".*:\/\/.*\.""", """.*:\/\/(.*\.)?""")
    }
  }
}
