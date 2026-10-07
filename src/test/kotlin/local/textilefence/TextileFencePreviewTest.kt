package local.textilefence

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.plugins.markdown.ui.preview.html.MarkdownUtil
import java.io.File

/** Runs the real Markdown preview HTML generation with the plugin loaded. */
class TextileFencePreviewTest : BasePlatformTestCase() {
    fun `test textile fence is rendered by the plugin`() {
        val text = "# Titel\n\n```textile\n*fett* und @code@\n\n{{collapse(Mehr)\ninnen\n}}\n```\n\nDanach *Markdown*.\n"
        val file = myFixture.configureByText("entwurf.md", text).virtualFile

        val html = MarkdownUtil.generateMarkdownHtml(file, text, project)

        assertTrue(html, Regex("<code class=\"language-textile\"[^>]*><div class=\"textile-fence\">").containsMatchIn(html))
        assertTrue(html, "<strong>fett</strong>" in html)
        assertTrue(html, "<details><summary>Mehr</summary>" in html)
        assertTrue(html, Regex("<em[^>]*>Markdown</em>").containsMatchIn(html))
    }

    fun `test image of a real file on disk is found in the bilder folder`() {
        val dir = FileUtil.createTempDirectory("textile-fence", null)
        VfsRootAccess.allowRootAccess(testRootDisposable, dir.path)
        File(dir, "bilder").mkdir()
        File(dir, "bilder/a1-name.png").writeBytes(byteArrayOf(0))
        val text = "```textile\n!a1-name.png!\n```\n"
        val md = File(dir, "entwurf.md").apply { writeText(text) }
        val file = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(md)!!

        val html = MarkdownUtil.generateMarkdownHtml(file, text, project)

        assertTrue(html, "src=\"bilder/a1-name.png\"" in html)
    }

    fun `test other fences are left to the built-in highlighter`() {
        val text = "```python\nprint('x')\n```\n"
        val file = myFixture.configureByText("entwurf.md", text).virtualFile

        val html = MarkdownUtil.generateMarkdownHtml(file, text, project)

        assertFalse(html, "textile-fence" in html)
    }
}
