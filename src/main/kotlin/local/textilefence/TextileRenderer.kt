package local.textilefence

import org.eclipse.mylyn.wikitext.parser.MarkupParser
import org.eclipse.mylyn.wikitext.parser.builder.HtmlDocumentBuilder
import org.eclipse.mylyn.wikitext.textile.TextileLanguage
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * Textile to HTML, plus the Redmine bits that plain Textile does not know:
 * the `{{collapse(...)}}` macro and images referenced by file name only
 * (in Redmine they point to ticket attachments).
 */
object TextileRenderer {
    private val COLLAPSE = Regex(
        """^\{\{collapse(?:\((.*?)\))?[ \t]*\n(.*?)^}}[ \t]*$""",
        setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL),
    )
    private val IMG_SRC = Regex("""(<img\b[^>]*\bsrc=")([^"]+)(")""")
    private const val IMAGE_SEARCH_DEPTH = 3
    private const val COLLAPSE_DEFAULT_LABEL = "Anzeigen"

    /** [baseDir] is the folder of the Markdown file, used to find images. */
    fun render(source: String, baseDir: Path?): String {
        val html = renderWithCollapse(source.replace("\r\n", "\n"))
        return if (baseDir == null) html else resolveImages(html, baseDir)
    }

    private fun renderWithCollapse(source: String): String = buildString {
        var pos = 0
        for (match in COLLAPSE.findAll(source)) {
            append(textile(source.substring(pos, match.range.first)))
            // Like Redmine: the first macro argument is the label, a second one would be the "hide" label.
            val label = match.groupValues[1].substringBefore(',').trim().ifEmpty { COLLAPSE_DEFAULT_LABEL }
            append("<details><summary>").append(escape(label)).append("</summary>")
            append(renderWithCollapse(match.groupValues[2]))
            append("</details>")
            pos = match.range.last + 1
        }
        append(textile(source.substring(pos)))
    }

    private fun textile(source: String): String {
        if (source.isBlank()) return ""
        val out = StringWriter()
        val builder = HtmlDocumentBuilder(out)
        builder.setEmitAsDocument(false)
        MarkupParser(TextileLanguage(), builder).parse(source)
        return out.toString()
    }

    private fun resolveImages(html: String, baseDir: Path): String = IMG_SRC.replace(html) { match ->
        val src = match.groupValues[2]
        val found = if (isRelative(src) && !baseDir.resolve(src).exists()) findByName(baseDir, src.substringAfterLast('/')) else null
        if (found == null) match.value else match.groupValues[1] + found + match.groupValues[3]
    }

    private fun isRelative(src: String): Boolean = ':' !in src && !src.startsWith("/")

    private fun findByName(baseDir: Path, name: String): String? =
        Files.walk(baseDir, IMAGE_SEARCH_DEPTH).use { paths ->
            paths.filter { it.name == name && it.isRegularFile() }.sorted().findFirst().orElse(null)
        }?.let { baseDir.relativize(it).joinToString("/") }

    private fun escape(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
