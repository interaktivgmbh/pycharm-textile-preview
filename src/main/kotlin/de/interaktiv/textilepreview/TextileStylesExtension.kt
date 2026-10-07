package de.interaktiv.textilepreview

import org.intellij.plugins.markdown.extensions.MarkdownBrowserPreviewExtension
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel
import org.intellij.plugins.markdown.ui.preview.ResourceProvider

/** Adds the stylesheet that makes rendered textile fences look like text, not like code. */
internal class TextileStylesExtension : MarkdownBrowserPreviewExtension, ResourceProvider {
    override val styles: List<String> = listOf(STYLESHEET)

    override val resourceProvider: ResourceProvider = this

    override fun canProvide(resourceName: String): Boolean = resourceName == STYLESHEET

    override fun loadResource(resourceName: String): ResourceProvider.Resource? =
        ResourceProvider.loadInternalResource(TextileStylesExtension::class, STYLESHEET, "text/css")

    override fun dispose() = Unit

    class Provider : MarkdownBrowserPreviewExtension.Provider {
        override fun createBrowserExtension(panel: MarkdownHtmlPanel): MarkdownBrowserPreviewExtension = TextileStylesExtension()
    }

    private companion object {
        const val STYLESHEET = "textile-fence.css"
    }
}
