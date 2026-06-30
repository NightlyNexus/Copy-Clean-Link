package com.nightlynexus.cleanurlsjsonprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

// https://github.com/brave/adblock-lists/blob/0edb6bc8f98c43bf936f14c31b2b43151d452b7e/brave-lists/clean-urls.json
internal class CleanUrlsJsonProcessor(
  private val codeGenerator: CodeGenerator
) : SymbolProcessor {
  private var generated = false

  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (!generated) {
      val cleanDomains = cleanDomains()
      val cleanDomainClassName = ClassName(
        "com.nightlynexus.copycleanlink",
        "CleanDomain"
      )
      val initializer = initializer(
        cleanDomains,
        cleanDomainClassName
      )
      val cleanDomainsProperty = PropertySpec.builder(
        "cleanDomains",
        List::class.asClassName().parameterizedBy(cleanDomainClassName)
      )
        .addModifiers(KModifier.INTERNAL)
        .initializer(initializer)
        .build()
      val fileSpec = FileSpec.builder(
        "com.nightlynexus.copycleanlink",
        "CleanDomains"
      )
        .addProperty(cleanDomainsProperty)
        .build()
      fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
      generated = true
    }
    return emptyList()
  }

  @OptIn(ExperimentalSerializationApi::class)
  private fun cleanDomains(): List<CleanDomain> {
    val json = Json
    return json.decodeFromStream(
      CleanDomain::class.java.classLoader.getResourceAsStream("clean_urls.json")!!
    )
  }

  private fun initializer(
    cleanDomains: List<CleanDomain>,
    cleanDomainClassName: ClassName
  ): CodeBlock {
    val builder = CodeBlock.builder()
    builder.add("listOf(")
    for (i in cleanDomains.indices) {
      val cleanDomain = cleanDomains[i]

      if (i != 0) {
        builder.add(", ")
      }
      builder.add("%T(", cleanDomainClassName)
      builder.add("listOf(")
      for (j in cleanDomain.include.indices) {
        val include = cleanDomain.include[j]

        if (j != 0) {
          builder.add(", ")
        }
        builder.add("%S.toRegex()", include.pattern)
      }
      builder.add("), ")
      builder.add("listOf(")
      for (j in cleanDomain.exclude.indices) {
        val exclude = cleanDomain.exclude[j]

        if (j != 0) {
          builder.add(", ")
        }
        builder.add("%S.toRegex()", exclude.pattern)
      }
      builder.add("), ")
      builder.add("listOf(")
      for (j in cleanDomain.params.indices) {
        val param = cleanDomain.params[j]

        if (j != 0) {
          builder.add(", ")
        }
        builder.add("%S", param)
      }
      builder.add(")")
      builder.add(")", cleanDomainClassName)
    }
    builder.add(")")
    return builder.build()
  }
}
