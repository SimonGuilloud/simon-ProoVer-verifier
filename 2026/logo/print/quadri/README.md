# Quadrichromie (CMYK full-colour) artwork — ProoVer 2026

Full-colour version for **quadrichromie** printing. Layout: title in the
**top half** (horizontally centred), subtitle below, and the falling logical
symbols **everywhere around** — a **46-wide × 23-high** grid. Uses the normal
(non-bold) characters — this is *not* the E1 file.

## Files

- `proover_quadri_halo.svg` / `.png` — **with** a dark ("black") halo behind
  the text, so it separates cleanly from the rain.
- `proover_quadri_nohalo.svg` / `.png` — **without** the halo; rain runs
  directly around and behind the text.

Vector SVG is the master (send it). PNGs are 1840 × 1081 px previews.

## Specs

| Property   | Value                                             |
|------------|---------------------------------------------------|
| Colour     | Full colour / CMYK (quadrichromie)                |
| Symbol grid| 46 wide × 23 high                                 |
| Title      | Upper half, centred; gradient white→blue          |
| Background | Flat navy (`#12325e`)                             |
| Canvas     | 1840 × 1081 px (≈ 46:23 grid ratio); vector scales |

## Note on size

"46 wide × 23 high" is read here as the **symbol grid** (which matches the
generator's default 46 columns), giving a ~1.70:1 canvas. If you meant a
physical **46 × 23 cm** print (2:1), say so and I'll re-export at that exact
size/aspect from the vector source — no quality loss.

## Regenerate

```sh
cd 2026/logo
python3 generate_logo.py --theme blue --font serif --flat-bg --no-glow \
    --fit-title --title-y 0.3 --halo \
    --width 1840 --height 1081 -o print/quadri/proover_quadri_halo.svg
# drop --halo → auto off, or use --no-halo explicitly for the no-halo version
```

`--title-y` sets the title's vertical position (0.3 = upper half); `--halo` /
`--no-halo` toggle the dark halo.
