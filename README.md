# Textile Fence Preview

Prototyp eines PyCharm-Plugins: Codeblöcke mit der Sprache `textile` werden in der
eingebauten Markdown-Vorschau als fertiger Text dargestellt statt als Code.

````markdown
```textile
*Ausgangslage*

{{collapse(Technische Hinweise)
* Punkt
}}
```
````

Zusätzlich zu normalem Textile (Bibliothek Mylyn WikiText):

- `{{collapse(Titel)}} ... }}` wird zum Aufklappbereich, wie in Redmine.
- `!bild.png!` ohne Pfad wird im Ordner der Markdown-Datei und bis zu drei Ebenen
  darunter gesucht (in Redmine wären das Ticket-Anhänge).
- Der Kopieren-Knopf der Vorschau kopiert den Textile-Rohtext, also genau das, was nach Redmine gehört.

Zum Ausprobieren: `beispiel/beispiel.md`.

## Installieren

Settings → Plugins → Zahnrad → *Install Plugin from Disk…* →
`build/distributions/textile-fence-preview-0.1.0.zip`.

## Bauen und testen

```bash
./gradlew test buildPlugin
```

Gebaut wird gegen das lokal installierte PyCharm, ohne Download der IDE. Der Pfad steht in
`gradle.properties` (`platformLocalPath`), dort auch das JBR von PyCharm als JDK, weil
PyCharm 2026.2 für Java 25 gebaut ist.

Tests: `TextileRendererTest` prüft den Renderer allein, `TextileFencePreviewTest` lässt die echte
HTML-Erzeugung des Markdown-Plugins mit geladenem Plugin laufen (ohne Fenster).

## Grenzen

- Die genutzte Schnittstelle (`org.intellij.markdown.fenceGeneratingProvider`) ist bei JetBrains
  als `Internal` und `Obsolete` markiert. Sie funktioniert, kann sich aber ohne Ankündigung ändern.
  Das Plugin ist deshalb auf Build 262 (PyCharm 2026.2) begrenzt und muss bei einem großen
  Update neu gebaut und geprüft werden.
- Die experimentelle Compose-Vorschau stellt den Block nicht dar, nur die Standard-Vorschau.
- Mylyn ist nicht Redmines Textile-Umsetzung (RedCloth). Kleine Unterschiede sind möglich,
  `#1234` wird nicht verlinkt, verschachtelte `collapse` werden nicht unterstützt.
  Verbindlich bleibt der Vorschau-Tab in Redmine.
