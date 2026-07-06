# DTF print files — ProoVer 2026 (navy hand fans)

Print-ready artwork for DTF (Direct-to-Film) on **COD.55 navy hand fans**,
max print area **10 × 10 cm**.

## Files to send to the printer

- **`proover_dtf_arial.png`** — recommended. 1181 × 1181 px, transparent
  background (RGBA), = 10.0 cm at 300 DPI.
- `proover_dtf_serif.png` — same, serif title (thinner strokes; slightly more
  fragile at this size).
- `proover_dtf_{arial,serif}.svg` — vector source, in case the printer prefers
  to rasterize themselves or rescale.

`preview_navy_*.png` show the intended result on a navy background — these are
**previews only**, not the print files. The real print files are transparent
(white text is invisible on a white viewer background — that's expected).

## Specs

| Property        | Value                                              |
|-----------------|----------------------------------------------------|
| Print size      | 10 × 10 cm (max)                                   |
| Resolution      | 300 DPI → 1181 × 1181 px                           |
| Background      | Transparent (only artwork prints; fan = backdrop)  |
| Garment         | Navy — required; design needs a dark backdrop      |
| Colours         | White + pale blue (CMYK-safe; printer RIP → CMYK)  |
| Format          | PNG (transparent) preferred; SVG source included   |

## Regenerate / re-export

```sh
cd 2026/logo
# square, transparent, no glow, title fitted to width:
python3 generate_logo.py --theme blue --font arial \
    --transparent --no-glow --fit-title \
    --width 1000 --height 1000 -o print/proover_dtf_arial.svg
```

Then rasterize to 1181 px (10 cm @ 300 DPI) with a headless browser,
`rsvg-convert -w 1181`, `inkscape`, or `cairosvg`. For a different size,
render at `cm / 2.54 * 300` px (e.g. 12 cm → 1417 px).

## Notes / caveats

- **Dark garment only.** The Matrix effect relies on a dark backdrop; on a
  light garment the white artwork disappears. Navy is ideal.
- **Faint symbols.** The `--transparent` mode raises the minimum symbol opacity
  so the falling-symbol fade still reads once printed; without it the faintest
  glyphs would drop out.
- **Glow off.** Soft halos dither into noise at small DTF sizes, so the print
  variant disables the glow.
- If the printer asks for a coloured/white-filled background instead of
  transparency, re-run without `--transparent` (theme `blue` gives a navy
  panel) — but that prints a visible rectangle.
