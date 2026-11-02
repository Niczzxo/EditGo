# EditGo

Fast video editor for short-form video (Reels-style), built from scratch in
Kotlin + Jetpack Compose. Dark + purple theme. Original code, original logo,
original name — inspired by the *workflow* of Instagram Edits, not a copy
of its code, icons, or assets.

- App name: **EditGo**
- Package: `io.github.niczzxo.editgo`
- Version: **0.1.0** (versionCode 1), minSdk 28, targetSdk 34

## Edits-style flow — what v1 includes

| Flow | Status in v1 |
| --- | --- |
| Home → New Project → Editor | ✅ Works |
| 9:16 vertical preview on top | ✅ Works (ExoPlayer) |
| Timeline at the bottom (trim range in purple) | ✅ Works |
| Tool row in order: Trim, Text, Filter, Music, Speed, Sticker, Transition, Export | ✅ Works (UI) |
| Trim (start/end sliders, duration display) | ✅ Works, and export really trims |
| Text overlay (input + size + on-video preview) | ✅ Works (preview) |
| Filter (Normal, Bright, Warm, Cool, B&W) | ⚠️ Preview tint only — not baked into export yet |
| Music (pick local audio, shows name) | ⚠️ Preview only — export is video-only for now |
| Speed (0.5x / 1x / 1.5x / 2x) | ✅ Works in preview playback |
| Sticker (built-in text stickers) | ✅ Works (preview overlay) |
| Transition (None / Fade / Slide / Zoom) | ⚠️ Saved, preview-only — needs multi-clip (v2) |
| Export 720p / 1080p | ⚠️ Real Media3 Transformer trim; resolution choice is saved for v2 transcoding, v1 keeps source resolution |

## What v1 does NOT do (not possible without Meta's servers)

- Instagram login, Meta AI features, auto-captions, cloud templates,
  licensed music catalogue, cloud projects/sync. These need Meta's
  servers and licences; EditGo is local-files-only by design.
- Project persistence: projects live in memory only and do not survive
  an app restart (Room/DataStore is v2).
- Filter/music baking into the exported file (v2, Media3 Transformer effects).

Export never fakes success: it reports Running → Done (file name + size)
or Failed (real error + Retry).

## Build the APK (GitHub Actions)

No signing secrets, no release yet. Push to `main` (or run the workflow
manually) and the `Android CI` workflow runs unit tests, builds the debug
APK, and uploads it as the `editgo-debug-apk` artifact.

```bash
# local (needs JDK 17 + Android SDK 34):
./gradlew testDebugUnitTest
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Design

Tokens: `design-tokens.json`, rationale: `DESIGN.md`.
Theme: Dark + purple — background `#12101A`, surface `#1A1626`,
primary `#8B5CF6` / `#A855F7`, no gradients.

## Project layout

```
app/src/main/java/io/github/niczzxo/editgo/
  MainActivity.kt            Home <-> Editor navigation, permissions, pickers
  domain/                    TrimRange, Format, VideoFilter, ExportQuality, Project
  data/                      ProjectRepository (in-memory in v1)
  export/                    VideoExporter (Media3 Transformer trim)
  ui/theme/                  Color.kt, Theme.kt, Type.kt (tokens)
  ui/home/                   HomeScreen
  ui/editor/                 EditorScreen, EditorPanels, EditorViewModel
app/src/test/.../domain/      Unit tests (TrimRange, Format, Filter, Project)
.github/workflows/android.yml  CI: unit tests + assembleDebug + APK artifact
```

## v2 TODO

- Persist projects (Room/DataStore)
- Bake filters + text/sticker overlays + music into export (Transformer effects)
- Real 720p/1080p transcoding on export
- Multi-clip timeline + transitions between clips
- Auto-captions on-device (no server) — investigate on-device speech model
- Templates (local, user-made)
