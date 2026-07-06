# DTF print files — ProoVer 2026 (navy hand fans)

Print-ready artwork for DTF (Direct-to-Film) on **COD.55 navy hand fans**,
print size **10 × 7.5 cm** (4:3).

All PNGs are 1181 × 886 px = 10 × 7.5 cm at 300 DPI.

Two background options — send both and let the printer advise:

**Transparent** (only artwork prints; the navy fan is the backdrop)
- **`proover_dtf_arial.png`** — recommended.
- `proover_dtf_serif.png` — serif title (thinner strokes; more fragile).

**Navy background** (prints a filled navy panel; works on any fan shade but
shows a rectangle edge)
- `proover_dtf_arial_navybg.png`
- `proover_dtf_serif_navybg.png`

`proover_dtf_*.svg` — vector source, if the printer prefers to rasterize or
rescale themselves.

Note: on a transparent file the white text looks invisible in a white image
viewer — that is expected; it shows against the navy fan (or the navybg files).

## Specs

| Property        | Value                                              |
|-----------------|----------------------------------------------------|
| Print size      | 10 × 7.5 cm (4:3)                                  |
| Resolution      | 300 DPI → 1181 × 886 px                            |
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
    --width 1000 --height 750 -o print/proover_dtf_arial.svg
```

Then rasterize to 1181 × 886 px (10 × 7.5 cm @ 300 DPI) with a headless browser,
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
