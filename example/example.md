# Ticket draft: Search ignores the language filter

Drafted next to the code, reviewed in a merge request, pasted into Redmine.

| Tracker | Priority | Target version |
|---------|----------|----------------|
| Bug     | High     | 2.4            |

## Description

```textile
h3. What happens

Searching with the language filter set to *German* still lists English pages.
Users have to scroll past results they cannot read.

# Open the search page.
# Choose _German_ in the language filter.
# Search for @invoice@.

|_. Before (bug) |_. After (expected) |
|!{width:340px}search-before.png!|!{width:340px}search-after.png!|

h3. Acceptance criteria

* Only pages in the chosen language are listed.
* The result count matches the list.
* %{color:#c0392b}No change% for users without a language filter.

bq. Reported three times to the support team this week.

{{collapse(Technical notes for the developer)
The filter value is read, but never passed to the catalog query:

<pre><code class="python">
def search(request, text, language=None):
    query = {"SearchableText": text, "sort_on": "modified"}
    # language is read above, but not used here
    return catalog(**query)[:50]
</code></pre>

Add @Language@ to the query when it is set, plus a test with two pages in different languages.
}}
```

Everything outside the `textile` block is plain *Markdown* again.
