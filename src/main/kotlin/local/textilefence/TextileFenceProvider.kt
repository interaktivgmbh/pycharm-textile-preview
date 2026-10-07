package local.textilefence

import com.intellij.openapi.diagnostic.logger
import org.intellij.markdown.ast.ASTNode
import org.intellij.plugins.markdown.extensions.MarkdownCodeFenceCacheableProvider

/**
 * Renders ```textile fences in the Markdown preview.
 *
 * Extends the cacheable provider only because that is the way to learn which
 * Markdown file is rendered ([collector] is set by the Markdown plugin before
 * each run). Nothing is written to the cache.
 */
internal class TextileFenceProvider : MarkdownCodeFenceCacheableProvider(null) {
    override fun isApplicable(language: String): Boolean =
        language.trim().substringBefore(' ').equals(LANGUAGE, ignoreCase = true)

    override fun generateHtml(language: String, raw: String, node: ASTNode): String {
        val baseDir = collector?.file?.parent?.let { it.fileSystem.getNioPath(it) }
        val body = try {
            TextileRenderer.render(raw, baseDir)
        } catch (e: Exception) {
            // Keep the preview usable while typing half-finished markup.
            LOG.warn("Textile rendering failed", e)
            "<p class=\"textile-fence-error\">Textile konnte nicht gerendert werden: ${e.message.orEmpty().escaped()}</p>"
        }
        return "<div class=\"textile-fence\">$body</div>"
    }

    private fun String.escaped(): String = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private companion object {
        const val LANGUAGE = "textile"
        val LOG = logger<TextileFenceProvider>()
    }
}
