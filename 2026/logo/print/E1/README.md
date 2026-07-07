# E1 (1-colour screen print) artwork — ProoVer 2026

Optimized for **screen print / pad print in a single colour** (marking code
"E1") on **navy hand fans**, print size **10 × 7.5 cm**.

## Files

- **`proover_E1_1color_white.svg`** — vector master (send this).
- **`proover_E1_1color_white.pdf`** — vector PDF at exactly 100 × 75 mm.
- `preview_navy.png` — how it looks on the navy fan (preview only; the real
  files are 1 colour / transparent).

## What makes it E1-safe

Screen print lays down flat solid ink and can't reproduce gradients,
semi-transparency, or hairline strokes. Versus the DTF artwork this version:

- **One ink only** — white. No fades, no gradient (screen print can't do them).
- **Symbols 2× larger and bold**, with a slight same-colour stroke, so every
  stroke clears the ~0.2–0.3 mm screen-print minimum (at 1× they were ~0.15 mm
  and would break up).
- **Bold subtitle** — the thin italic serif "— Lisbon —" is now a solid weight.
- **Keep-out zone** behind the title/subtitle so no symbol merges into the
  letters (there's no vignette to separate them in 1 colour).
- **Transparent background** — only the white ink prints; the navy fan is the
  backdrop.

## Confirm with the printer

- **Minimum line thickness for E1** — norms are ~0.2–0.3 mm; this file is built
  to clear that, but their spec is the authority.
- **Max marking area / position** for E1 (or MAT ESP E1) — confirm 10 × 7.5 cm
  is allowed; I can re-export to any size from the vector source.
- They may ask for **text converted to outlines** — say the word and I'll
  provide an outlined version (no font dependency).

## Regenerate

```sh
cd 2026/logo
python3 generate_logo.py --theme blue --font serif \
    --transparent --no-glow --fit-title --rain-scale 2 \
    --ink "#ffffff" --width 1181 --height 886 \
    -o print/E1/proover_E1_1color_white.svg
```

Change `--ink` for a different single colour; `--rain-scale` for symbol size.
