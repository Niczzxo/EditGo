# EditGo — Design (v1)

Hallmark + ECC. Hallmark for the look, ECC for tokens / plan / test / review.

## Direction

- **Purpose:** edit short-form video on the phone, fast: import → trim → text / filter / music → export.
- **Audience:** creators repeating the same flow daily; the timeline and the
  Export button must be findable without thinking.
- **Tone:** utilitarian, dark, focused. No hero, no marketing copy inside the app.
- **Memorable detail:** solid purple (`#8B5CF6`) timeline fill and Export
  action, plus the original logo — a purple rounded square with a white
  play triangle cut by a diagonal edit line.
- **Constraints:** Android minSdk 28, Jetpack Compose, fully offline in v1,
  no server, no purple-to-blue gradient anywhere (Hallmark Floor rule),
  solid colours only, 48dp minimum touch targets.

## Tokens

Single source of truth: `design-tokens.json`. The Compose theme mirrors it
in `ui/theme/Color.kt` / `Theme.kt` — no literal hex past the token block.

| Token | Value | Use |
| --- | --- | --- |
| background | `#12101A` | App background |
| surface | `#1A1626` | Cards, panels, bottom tools |
| surfaceVariant | `#241E33` | Pressed / selected chip |
| primary | `#8B5CF6` | Accent, timeline fill, Export |
| primaryVariant | `#A855F7` | Highlights, logo depth |
| onBackground | `#F2F0F7` | Primary text |
| onSurfaceVariant | `#9A93AC` | Secondary text |
| outline | `#3A3347` | Dividers, slider track |
| error | `#F87171` | Export failure |
| success | `#34D399` | Export done |

## Screens

1. **Home** — EditGo header + logo, "New Project" primary action,
   recent-projects list, empty state ("No projects yet — start a new one").
   Permission state: if media permission is missing, an explanatory banner
   with a "Grant access" action, not a silent failure.
2. **Editor** — video preview (16:9, ExoPlayer), timeline bar with
   trim range, bottom tool row: Trim, Text, Filter, Music, Speed, Export.
   Only one tool panel open at a time; the active tool is purple.
3. **Trim** — start/end sliders (seconds), duration display `mm:ss`,
   clamped so start < end and both stay inside the clip.
4. **Text** — text input + size, overlay preview on the video.
5. **Filter** — Normal, Bright, Warm, Cool, B&W. Preview tint only in v1;
   baking filters into export is v2 (see README).
6. **Music** — pick an audio file, show its name, remove action.
7. **Export** — 720p / 1080p choice, Export button. Real Media3
   Transformer trim in v1; progress / success / error states are shown,
   never faked.

## States (evidence contract)

| State | Behaviour |
| --- | --- |
| Loading | Preview shows a progress indicator while the player prepares |
| Empty | Home: empty-projects message + New Project CTA |
| Error | Export failure shows the real error message + Retry |
| Partial | No video picked yet: editor tools are disabled, not hidden |
| Success | Export done: file name + "Saved" confirmation |
| Permission | Banner explains why media access is needed + grant action |

## What is NOT in v1

AI captions, templates, cloud projects, licensed music catalogue,
Instagram login — all out of scope, listed as v2 TODO in README.md.
