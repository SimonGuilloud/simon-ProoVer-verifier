# ProoVer 2026 logo

Programmatically generated vector logo: a "Matrix"-style rain of logical
symbols (∀ ∃ ⊢ ∈ ∧ ∨ ¬ → …) behind the title **ProoVer 2026** and the
subtitle **— Lisbon —**.

## Generate

```sh
python3 generate_logo.py                 # -> proover_2026.svg (1200x675)
python3 generate_logo.py --seed 7        # different rain pattern
python3 generate_logo.py --width 1000 --height 1000 -o square.svg
python3 generate_logo.py --title "ProoVer 2026" --subtitle "— Lisbon —"
```

No dependencies (Python 3 stdlib only). Output is a self-contained SVG, so it
scales to any resolution. The layout is deterministic for a given `--seed`.

## Render to PNG

The SVG uses standard fonts; open it in any browser, or rasterize with e.g.
`rsvg-convert`, `inkscape`, `cairosvg`, or a headless browser:

```sh
rsvg-convert -w 2400 proover_2026.svg -o proover_2026.png
```

`proover_2026_preview.png` is a sample render.

## Notes

- Glyphs are drawn as `<text>`, so a viewer needs a font covering the logical
  Unicode block (DejaVu Sans Mono / Noto Sans Symbols2 are in the font stack).
  To make the file fully font-independent, convert text to paths with Inkscape
  (`--export-text-to-path`).
