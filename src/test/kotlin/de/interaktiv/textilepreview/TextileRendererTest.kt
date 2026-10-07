package de.interaktiv.textilepreview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TextileRendererTest {
    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `renders basic textile`() {
        val html = TextileRenderer.render("*Ziel*\n\n* eins mit @code@\n* zwei", null)
        assertTrue(html, "<strong>Ziel</strong>" in html)
        assertTrue(html, "<code>code</code>" in html)
        assertEquals(html, 2, Regex("<li>").findAll(html).count())
    }

    @Test
    fun `keeps single line breaks like Redmine`() {
        val html = TextileRenderer.render("Zeile eins\nZeile zwei", null)
        assertTrue(html, Regex("Zeile eins<br\\s*/?>\\s*Zeile zwei").containsMatchIn(html))
    }

    @Test
    fun `collapse macro becomes details with the first argument as label`() {
        val html = TextileRenderer.render("Vorher\n\n{{collapse(Technische Hinweise, Verbergen)\n*Geprüft*\n\n* Punkt\n}}\n\nNachher", null)
        assertTrue(html, "<details><summary>Technische Hinweise</summary>" in html)
        assertTrue(html, Regex("<details>.*<strong>Geprüft</strong>.*<li>Punkt</li>.*</details>", RegexOption.DOT_MATCHES_ALL).containsMatchIn(html))
        assertTrue(html, html.indexOf("Nachher") > html.indexOf("</details>"))
        assertFalse(html, "{{" in html || "}}" in html)
    }

    @Test
    fun `collapse without argument gets the default label`() {
        val html = TextileRenderer.render("{{collapse\ninnen\n}}", null)
        assertTrue(html, "<summary>Show</summary>" in html)
    }

    @Test
    fun `pre code block is shown as written, like in Redmine`() {
        val html = TextileRenderer.render(
            "Vorher\n\n<pre><code class=\"python\">\ndef f(x):\n    return x < 1 and *nicht fett*\n</code></pre>\n\nNachher",
            null,
        )
        assertTrue(html, "<pre><code class=\"language-python\">def f(x):\n    return x &lt; 1 and *nicht fett*</code></pre>" in html)
        assertFalse(html, "<br" in html || "<p><pre>" in html || "<strong>" in html)
        assertTrue(html, html.indexOf("Nachher") > html.indexOf("</pre>"))
    }

    @Test
    fun `pre block without language and inside collapse`() {
        val html = TextileRenderer.render("{{collapse(Log)\n<pre>\na < b\n}}\n</pre>\n}}", null)
        assertTrue(html, Regex("<details><summary>Log</summary>.*<pre><code>a &lt; b\n}}</code></pre>.*</details>", RegexOption.DOT_MATCHES_ALL).containsMatchIn(html))
    }

    @Test
    fun `highlighter colors pre blocks with a known language`() {
        val highlighter = TextileRenderer.Highlighter { language, code ->
            if (language == "python") "<span style=\"color:#cc7832\">${code.replace("<", "&lt;")}</span>" else null
        }
        val html = TextileRenderer.render(
            "<pre><code class=\"python\">x < 1</code></pre>\n\n<pre><code class=\"unknown\">y < 2</code></pre>",
            null,
            highlighter,
        )
        assertTrue(html, "<code class=\"language-python\"><span style=\"color:#cc7832\">x &lt; 1</span></code>" in html)
        assertTrue(html, "<code class=\"language-unknown\">y &lt; 2</code>" in html)
    }

    @Test
    fun `image by file name is found in a subfolder`() {
        tmp.newFolder("bilder")
        tmp.newFile("bilder/a1-name.png")
        val html = TextileRenderer.render("|!a1-name.png!|!fehlt.png!|", tmp.root.toPath())
        assertTrue(html, "src=\"bilder/a1-name.png\"" in html)
        assertTrue(html, "src=\"fehlt.png\"" in html)
        assertTrue(html, "<table" in html)
    }

    @Test
    fun `image with style attribute is found and keeps its width`() {
        tmp.newFolder("bilder")
        tmp.newFile("bilder/a1-name.png")
        val html = TextileRenderer.render("|!{width:400px}a1-name.png!|", tmp.root.toPath())
        assertTrue(html, "src=\"bilder/a1-name.png\"" in html)
        assertTrue(html, "width:400px" in html)
    }

    @Test
    fun `the example file renders every feature it shows`() {
        val example = File("example/example.md")
        val textile = example.readText().substringAfter("```textile\n").substringBefore("\n```")
        val html = TextileRenderer.render(textile, example.parentFile.toPath())

        assertTrue(html, "<h3" in html && "<ol>" in html && "<blockquote>" in html)
        assertTrue(html, "src=\"images/search-before.png\"" in html && "src=\"images/search-after.png\"" in html)
        assertTrue(html, "color:#c0392b" in html)
        assertTrue(html, "<details><summary>Technical notes for the developer</summary>" in html)
        assertTrue(html, "<pre><code class=\"language-python\">def search(" in html)
        assertFalse(html, "{{" in html || "<br/>def" in html)
    }

    @Test
    fun `image path that exists stays unchanged`() {
        tmp.newFile("direkt.png")
        val html = TextileRenderer.render("!direkt.png!", tmp.root.toPath())
        assertTrue(html, "src=\"direkt.png\"" in html)
    }
}
