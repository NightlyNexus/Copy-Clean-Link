package com.nightlynexus.cleanurlsjsonprocessor

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
internal data class CleanDomain(
  val include: List<WildcardRegex>,
  val exclude: List<WildcardRegex>,
  val params: List<String>
) {
  private typealias WildcardRegex =
    @Serializable(with = RegexSerializer::class) Regex

  private object RegexSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Regex) {
      throw SerializationException("RegexJsonAdapter does not support Regex serialization.")
    }

    override fun deserialize(decoder: Decoder): Regex {
      val pattern = decoder.decodeString()
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
