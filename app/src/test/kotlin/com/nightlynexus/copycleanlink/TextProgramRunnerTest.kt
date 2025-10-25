package com.nightlynexus.copycleanlink

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.CountDownLatch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Test

class TextProgramRunnerTest {
  @Test fun extractsCleansAndCombinesLinks() {
    val input = """https://www.zillow.com/homedetails/22775-Sherman-Rd-Steger-IL-60475/4337424_zpid/?utm_medium=referral
Hello world!https://google.com/ https://example.com/
Great day https://share.google/playsomething OK time.gov"""

    val output = """https://www.zillow.com/homedetails/22775-Sherman-Rd-Steger-IL-60475/4337424_zpid/

https://google.com/

https://example.com/

https://store.steampowered.com/app/playsomething/

https://time.gov/"""

    val ampSiteResponseBody = """<!DOCTYPE html>
<html lang="en">
<head>
  <title>Fun Games</title>
  <meta name="description" content="There are so many games to play">
  <link href="https://store.steampowered.com/app/playsomething/" rel="canonical" />
</head>
<body>
</body>
</html>
"""

    val linksExtractor = TestLinksExtractor()
    val linksCombiner = LinksCombiner()
    val linkCleaner = LinkCleaner()
    val responseBodies = listOf(
      simpleResponseBody,
      simpleResponseBody,
      simpleResponseBody,
      ampSiteResponseBody,
      simpleResponseBody
    )
    val ampResolver = AmpResolver(TestCallFactory(responseBodies))
    val clipboardCopier = TestClipboardCopier()
    val textProgramRunner = TextProgramRunner(
      linksExtractor,
      linksCombiner,
      linkCleaner,
      ampResolver,
      clipboardCopier
    )
    val latch = CountDownLatch(1)
    val textWarner = object:TextWarner {
      override fun errorInvalidLink(text: CharSequence) {
        throw AssertionError()
      }

      override fun warnExtractingLinks(text: CharSequence, linkCount: Int) {
        assertThat(text).isEqualTo(input)
        assertThat(linkCount).isEqualTo(5)
        latch.countDown()
      }
    }
    textProgramRunner.run(textWarner, input)
    assertThat(latch.count).isEqualTo(0)
    assertThat(clipboardCopier.getCurrentText()).isEqualTo(output)
  }

  private class TestLinksExtractor : LinksExtractor {
    private val pattern = """(((?:(?i:http|https|rtsp|ftp)://(?:(?:[a-zA-Z0-9\${'$'}\-\_\.\+\!\*\'\(\)\,\;\?\&\=]|(?:\%[a-fA-F0-9]{2})){1,64}(?:\:(?:[a-zA-Z0-9\${'$'}\-\_\.\+\!\*\'\(\)\,\;\?\&\=]|(?:\%[a-fA-F0-9]{2})){1,25})?\@)?)?(?:(([a-zA-Z0-9[ -퟿豈-﷏ﷰ-￯𐀀-🿽𠀀-𯿽𰀀-𿿽񀀀-񏿽񐀀-񟿽񠀀-񯿽񰀀-񿿽򀀀-򏿽򐀀-򟿽򠀀-򯿽򰀀-򿿽󀀀-󏿽󐀀-󟿽󡀀-󯿽&&[^ [ - ]   　]]](?:[a-zA-Z0-9[ -퟿豈-﷏ﷰ-￯𐀀-🿽𠀀-𯿽𰀀-𿿽񀀀-񏿽񐀀-񟿽񠀀-񯿽񰀀-񿿽򀀀-򏿽򐀀-򟿽򠀀-򯿽򰀀-򿿽󀀀-󏿽󐀀-󟿽󡀀-󯿽&&[^ [ - ]   　]]_\-]{0,61}[a-zA-Z0-9[ -퟿豈-﷏ﷰ-￯𐀀-🿽𠀀-𯿽𰀀-𿿽񀀀-񏿽񐀀-񟿽񠀀-񯿽񰀀-񿿽򀀀-򏿽򐀀-򟿽򠀀-򯿽򰀀-򿿽󀀀-󏿽󐀀-󟿽󡀀-󯿽&&[^ [ - ]   　]]]){0,1}\.)+(xn\-\-[\w\-]{0,58}\w|[a-zA-Z[ -퟿豈-﷏ﷰ-￯𐀀-🿽𠀀-𯿽𰀀-𿿽񀀀-񏿽񐀀-񟿽񠀀-񯿽񰀀-񿿽򀀀-򏿽򐀀-򟿽򠀀-򯿽򰀀-򿿽󀀀-󏿽󐀀-󟿽󡀀-󯿽&&[^ [ - ]   　]]]{2,63})|((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))))(?:\:\d{1,5})?)([/\?](?:(?:[a-zA-Z0-9[ -퟿豈-﷏ﷰ-￯𐀀-🿽𠀀-𯿽𰀀-𿿽񀀀-񏿽񐀀-񟿽񠀀-񯿽񰀀-񿿽򀀀-򏿽򐀀-򟿽򠀀-򯿽򰀀-򿿽󀀀-󏿽󐀀-󟿽󡀀-󯿽&&[^ [ - ]   　]];/\?:@&=#~\-\.\+!\*'\(\),_\${'$'}])|(?:%[a-fA-F0-9]{2}))*)?(?:\b|${'$'}|^))""".toPattern()

    override fun extractLinks(text: CharSequence): List<String> {
      val matcher = pattern.matcher(text)
      val links = mutableListOf<String>()
      while (matcher.find()) {
        links += matcher.group()
      }
      return links
    }
  }

  private class TestClipboardCopier : ClipboardCopier {
    var copiedText: CharSequence? = null

    override fun copyText(text: CharSequence) {
      copiedText = text
    }

    override fun getCurrentText(): CharSequence? {
      return copiedText
    }
  }

  private val simpleResponseBody = """<!DOCTYPE html>
<html lang="en">
<head>
  <title>Boring</title>
  <meta name="description" content="Simple page">
</head>
<body>
</body>
</html>
"""

  private class TestCallFactory(
    private val responseBodies: List<String>
  ) : Call.Factory {
    private var index = 0

    override fun newCall(request: Request): Call {
      return TestCall(request, responseBodies[index++])
    }
  }

  private class TestCall(
    request: Request,
    responseBody: String
  ) : Call {
    private val response = Response.Builder()
      .body(responseBody.toResponseBody("text/html; charset=utf-8".toMediaType()))
      .code(200)
      .message("OK")
      .protocol(Protocol.HTTP_2)
      .request(request)
      .build()

    override fun request(): Request {
      throw UnsupportedOperationException()
    }

    override fun execute(): Response {
      return response
    }

    override fun enqueue(responseCallback: Callback) {
      responseCallback.onResponse(this, response)
    }

    override fun cancel() {
      throw UnsupportedOperationException()
    }

    override fun isExecuted(): Boolean {
      throw UnsupportedOperationException()
    }

    override fun isCanceled(): Boolean {
      throw UnsupportedOperationException()
    }

    override fun timeout(): Timeout {
      throw UnsupportedOperationException()
    }

    override fun clone(): Call {
      throw UnsupportedOperationException()
    }
  }
}
