#!/usr/bin/env python3
"""Programmatic generator for the ProoVer 2026 logo.

Emits a self-contained, resolution-independent SVG: a "Matrix"-style rain of
logical/unicode symbols (forall, exists, turnstile, epsilon, and, or, not, ...)
falling behind the title "ProoVer 2026" with the subtitle "— Lisbon —".

Everything is deterministic given --seed, so the same command always produces
the same image. No third-party dependencies.

Usage:
    python3 generate_logo.py                 # writes proover_2026.svg
    python3 generate_logo.py -o out.svg --seed 7
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


def esc(s: str) -> str:
    return html.escape(s, quote=True)


def build_svg(
    width: int,
    height: int,
    seed: int,
    title: str,
    subtitle: str,
) -> str:
    rng = random.Random(seed)

    # --- Matrix rain layout -------------------------------------------------
    col_w = max(18, round(width / 46))          # column spacing / glyph size
    font_size = round(col_w * 0.92)
    row_h = round(col_w * 1.18)
    n_cols = width // col_w + 1
    n_rows = height // row_h + 2

    # Head (brightest) colour and the trailing green.
    head = "#e8fff1"
    green = (80, 240, 140)

    glyphs: list[str] = []
    for c in range(n_cols):
        x = c * col_w + col_w // 2
        # Each column is a falling stream: a bright head with a fading tail
        # above it. Randomise where the head sits and how long the tail is.
        head_row = rng.randint(-n_rows, n_rows)
        tail = rng.randint(6, n_rows + 4)
        # A gentle per-column vertical phase makes columns look independent.
        for t in range(tail):
            r = head_row - t
            if r < -1 or r > n_rows:
                continue
            y = r * row_h + row_h
            ch = esc(rng.choice(LOGIC_SYMBOLS))

            if t == 0:
                fill = head
                opacity = 1.0
            else:
                # Fade the tail from bright green to transparent.
                frac = 1.0 - t / tail
                opacity = round(0.12 + 0.75 * frac, 3)
                # Slightly brighten glyphs near the head.
                lift = int(60 * frac)
                col = (
                    min(255, green[0] + lift),
                    min(255, green[1] + lift // 2),
                    min(255, green[2] + lift),
                )
                fill = f"#{col[0]:02x}{col[1]:02x}{col[2]:02x}"

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

    # A soft radial vignette darkens the rain behind the text for legibility.
    vignette_r = width * 0.42

    return f"""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}"
     viewBox="0 0 {width} {height}" font-family="'DejaVu Sans Mono','Noto Sans Symbols2',monospace">
  <defs>
    <radialGradient id="bg" cx="50%" cy="45%" r="75%">
      <stop offset="0%" stop-color="#04120a"/>
      <stop offset="100%" stop-color="#000000"/>
    </radialGradient>
    <radialGradient id="vignette" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#000000" stop-opacity="0.92"/>
      <stop offset="55%" stop-color="#000000" stop-opacity="0.72"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0"/>
    </radialGradient>
    <linearGradient id="title" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#ffffff"/>
      <stop offset="100%" stop-color="#7dffb0"/>
    </linearGradient>
    <filter id="glow" x="-30%" y="-30%" width="160%" height="160%">
      <feGaussianBlur stdDeviation="{round(height*0.006,2)}" result="b"/>
      <feMerge>
        <feMergeNode in="b"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>

  <!-- background -->
  <rect width="{width}" height="{height}" fill="url(#bg)"/>

  <!-- matrix rain of logical symbols -->
  <g text-anchor="middle" filter="url(#glow)">
    {rain}
  </g>

  <!-- darken the rain behind the title -->
  <ellipse cx="{cx}" cy="{title_y - title_size*0.15}" rx="{vignette_r}" ry="{vignette_r*0.62}"
           fill="url(#vignette)"/>

  <!-- title + subtitle -->
  <g text-anchor="middle" font-family="'Georgia','DejaVu Serif',serif">
    <text x="{cx}" y="{title_y}" font-size="{title_size}" font-weight="bold"
          fill="url(#title)" filter="url(#glow)"
          dominant-baseline="middle" letter-spacing="{round(title_size*0.01,2)}">{esc(title)}</text>
    <text x="{cx}" y="{sub_y}" font-size="{sub_size}" fill="#9dffca"
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
    args = p.parse_args()

    svg = build_svg(args.width, args.height, args.seed, args.title, args.subtitle)
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(svg)
    print(f"wrote {args.output} ({args.width}x{args.height}, seed={args.seed})")


if __name__ == "__main__":
    main()
