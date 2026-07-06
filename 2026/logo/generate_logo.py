#!/usr/bin/env python3
"""Programmatic generator for the ProoVer 2026 logo.

Emits a self-contained, resolution-independent SVG: a "Matrix"-style rain of
logical/unicode symbols (forall, exists, turnstile, epsilon, and, or, not, ...)
falling behind the title "ProoVer 2026" with the subtitle "— Lisbon —".

Everything is deterministic given --seed, so the same command always produces
the same image. No third-party dependencies.

Themes:
    matrix  Vampire-black background with the classic Matrix greens
            (#003B00 / #008F11 / #00FF41) for the rain and the title.
    blue    Deep-blue background with full-white text and rain.

Fonts (applied to the title/subtitle only; the rain is always monospace):
    serif   Georgia / DejaVu Serif (the original look)
    arial   Arial / Helvetica

Usage:
    python3 generate_logo.py                              # matrix + serif
    python3 generate_logo.py --theme blue --font arial -o out.svg
    python3 generate_logo.py --width 1000 --height 1000   # square variant
"""

from __future__ import annotations

import argparse
import html
import random

# The logical alphabet raining down the background.
LOGIC_SYMBOLS = [
    "∀", "∃", "∄", "⊢", "⊨", "∈", "∉", "∧", "∨", "¬",
    "→", "↔", "⊃", "⊥", "⊤", "≡", "⊆", "⊂", "∪", "∩",
    "∅", "λ", "∴", "∵", "□", "◇", "⊕", "≠", "≤", "≥",
    "Γ", "Δ", "Φ", "Ψ", "φ", "ψ", "⊬", "⊭", "∎",
]

# Colour themes. Each defines the background, the rain (head + tail base), the
# title gradient stops, and the subtitle colour.
THEMES = {
    # Classic "Matrix" palette:
    #   Vampire Black #0D0208, Dark Green #003B00, Islam Green #008F11,
    #   Erin #00FF41.
    "matrix": {
        "bg_inner": "#0D0208",
        "bg_outer": "#000000",
        "rain_head": "#c9ffd6",       # near-white leading glyph
        "rain_base": (0, 143, 17),    # Islam Green #008F11
        "rain_tail": (0, 59, 0),      # Dark Green  #003B00 (fade target)
        "title_top": "#e9ffe9",
        "title_bottom": "#00FF41",    # Erin
        "subtitle": "#00d637",
    },
    # White text over a deep blue background.
    "blue": {
        "bg_inner": "#12325e",
        "bg_outer": "#040a18",
        "rain_head": "#ffffff",
        "rain_base": (150, 190, 255),
        "rain_tail": (40, 70, 130),
        "title_top": "#ffffff",
        "title_bottom": "#ffffff",
        "subtitle": "#dbe6ff",
    },
}

FONTS = {
    "serif": "'Georgia','DejaVu Serif',serif",
    "arial": "Arial,'Helvetica Neue',Helvetica,sans-serif",
}


def esc(s: str) -> str:
    return html.escape(s, quote=True)


def _lerp(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> str:
    c = tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))
    return f"#{c[0]:02x}{c[1]:02x}{c[2]:02x}"


def build_svg(
    width: int,
    height: int,
    seed: int,
    title: str,
    subtitle: str,
    theme: str,
    font: str,
    transparent: bool = False,
    glow: bool = True,
    fit_title: bool = False,
) -> str:
    """Build the logo SVG.

    transparent  Omit the background fill and the legibility vignette, so the
                 file prints only the artwork (ideal for DTF on a garment whose
                 colour supplies the backdrop). Also lifts the faint-symbol
                 opacity floor so the Matrix fade survives a physical print.
    glow         Soft blur halo behind the rain/title. Turn off for small DTF
                 prints, where halos dither into noise.
    fit_title    Stretch the title to a fixed fraction of the width via
                 textLength, so long titles fill a square without overflowing.
    """
    rng = random.Random(seed)
    th = THEMES[theme]
    title_font = FONTS[font]

    # For a print with no dark backdrop, faint glyphs must not fade to nothing.
    op_floor = 0.42 if transparent else 0.14
    op_span = 0.55 if transparent else 0.78

    # --- Matrix rain layout -------------------------------------------------
    col_w = max(18, round(width / 46))          # column spacing / glyph size
    font_size = round(col_w * 0.92)
    row_h = round(col_w * 1.18)
    n_cols = width // col_w + 1
    n_rows = height // row_h + 2

    glyphs: list[str] = []
    for c in range(n_cols):
        x = c * col_w + col_w // 2
        # Each column is a falling stream: a bright head with a fading tail
        # above it. Randomise where the head sits and how long the tail is.
        head_row = rng.randint(-n_rows, n_rows)
        tail = rng.randint(6, n_rows + 4)
        for t in range(tail):
            r = head_row - t
            if r < -1 or r > n_rows:
                continue
            y = r * row_h + row_h
            ch = esc(rng.choice(LOGIC_SYMBOLS))

            if t == 0:
                fill = th["rain_head"]
                opacity = 1.0
            else:
                # Fade the tail: brightest (base colour) just behind the head,
                # blending toward the darker tail colour further up.
                frac = 1.0 - t / tail
                opacity = round(op_floor + op_span * frac, 3)
                fill = _lerp(th["rain_tail"], th["rain_base"], frac)

            glyphs.append(
                f'<text x="{x}" y="{y}" font-size="{font_size}" '
                f'fill="{fill}" opacity="{opacity}">{ch}</text>'
            )

    rain = "\n    ".join(glyphs)

    # --- Central text sizing ------------------------------------------------
    cx = width / 2
    title_size = round(height * 0.16)
    sub_size = round(height * 0.052)
    title_y = height * 0.5
    sub_y = title_y + title_size * 0.72
    vignette_r = width * 0.42

    glow_ref = ' filter="url(#glow)"' if glow else ""
    # Fit a long title to the frame width without overflowing a square.
    title_extra = (
        f' textLength="{round(width * 0.86)}" lengthAdjust="spacingAndGlyphs"'
        if fit_title else ""
    )

    bg_layer = "" if transparent else (
        f'<rect width="{width}" height="{height}" fill="url(#bg)"/>'
    )
    vignette_layer = "" if transparent else (
        f'<ellipse cx="{cx}" cy="{title_y - title_size*0.15}" '
        f'rx="{vignette_r}" ry="{vignette_r*0.62}" fill="url(#vignette)"/>'
    )
    glow_def = "" if not glow else f"""<filter id="glow" x="-30%" y="-30%" width="160%" height="160%">
      <feGaussianBlur stdDeviation="{round(height*0.006,2)}" result="b"/>
      <feMerge>
        <feMergeNode in="b"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>"""

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}"
     viewBox="0 0 {width} {height}" font-family="'DejaVu Sans Mono','Noto Sans Symbols2',monospace">
  <defs>
    <radialGradient id="bg" cx="50%" cy="45%" r="75%">
      <stop offset="0%" stop-color="{th['bg_inner']}"/>
      <stop offset="100%" stop-color="{th['bg_outer']}"/>
    </radialGradient>
    <radialGradient id="vignette" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="{th['bg_outer']}" stop-opacity="0.92"/>
      <stop offset="55%" stop-color="{th['bg_outer']}" stop-opacity="0.72"/>
      <stop offset="100%" stop-color="{th['bg_outer']}" stop-opacity="0"/>
    </radialGradient>
    <linearGradient id="title" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="{th['title_top']}"/>
      <stop offset="100%" stop-color="{th['title_bottom']}"/>
    </linearGradient>
    {glow_def}
  </defs>

  <!-- background -->
  {bg_layer}

  <!-- matrix rain of logical symbols -->
  <g text-anchor="middle"{glow_ref}>
    {rain}
  </g>

  <!-- darken the rain behind the title -->
  {vignette_layer}

  <!-- title + subtitle -->
  <g text-anchor="middle" font-family="{title_font}">
    <text x="{cx}" y="{title_y}" font-size="{title_size}" font-weight="bold"
          fill="url(#title)"{glow_ref}
          dominant-baseline="middle" letter-spacing="{round(title_size*0.01,2)}"{title_extra}>{esc(title)}</text>
    <text x="{cx}" y="{sub_y}" font-size="{sub_size}" fill="{th['subtitle']}"
          letter-spacing="{round(sub_size*0.28,2)}"
          dominant-baseline="middle" font-style="italic">{esc(subtitle)}</text>
  </g>
</svg>
"""


def main() -> None:
    p = argparse.ArgumentParser(description="Generate the ProoVer 2026 logo (SVG).")
    p.add_argument("-o", "--output", default="proover_2026.svg")
    p.add_argument("--width", type=int, default=1200)
    p.add_argument("--height", type=int, default=675)
    p.add_argument("--seed", type=int, default=2026)
    p.add_argument("--title", default="ProoVer 2026")
    p.add_argument("--subtitle", default="— Lisbon —")
    p.add_argument("--theme", choices=sorted(THEMES), default="matrix")
    p.add_argument("--font", choices=sorted(FONTS), default="serif")
    p.add_argument("--transparent", action="store_true",
                   help="No background/vignette; lifts faint-symbol opacity "
                        "(for DTF/print where the garment supplies the backdrop).")
    p.add_argument("--no-glow", dest="glow", action="store_false",
                   help="Disable the soft glow (recommended for small prints).")
    p.add_argument("--fit-title", action="store_true",
                   help="Stretch the title to fill the width (good for squares).")
    args = p.parse_args()

    svg = build_svg(
        args.width, args.height, args.seed,
        args.title, args.subtitle, args.theme, args.font,
        transparent=args.transparent, glow=args.glow, fit_title=args.fit_title,
    )
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(svg)
    print(f"wrote {args.output} "
          f"({args.width}x{args.height}, seed={args.seed}, "
          f"theme={args.theme}, font={args.font}, "
          f"transparent={args.transparent}, glow={args.glow})")


if __name__ == "__main__":
    main()
