# Nano Health Task — Feed app (Compose + ExoPlayer + Room + Retrofit)

This repository contains a sample feed application (Android) that demonstrates:
- Mixed media feed (images & videos)
- Video auto-play when a post is in view, pause when not
- Shared ExoPlayer provided by Hilt, with controller hidden by default and shown on tap
- Pagination using a remote mock API (https://mock.apidog.com/m1/1181613-1175965-default/getHomeFeeds)
- Caching network results in Room and video bytes cached via ExoPlayer `SimpleCache`

This README includes architecture, folder structure, key libraries, assumptions, how to run, and demo guidance (short video and screenshots).

---

## Architecture

- MVVM (ViewModel -> Repository -> Data sources)
- Layers:
  - UI: Jetpack Compose screens under `app/src/main/java/.../feed/ui`
  - Domain / Models: simple model classes under `feed/model`
  - Data: `feed/data` contains `RemoteFeedsRepository` (Retrofit) and Room local caching (`feed/data/local`)
  - DI: Hilt modules under `di/` provide network, Room, ExoPlayer, and repository bindings

Key design decisions:
- Use a shared Hilt-provided `ExoPlayer` instance to minimize memory and allow quick switching between videos.
- Repository handles pagination and caching: it fetches pages from the mock remote endpoint, appends them to Room, and falls back to Room when the network fails.
- UI uses a single-player pattern: `PlayerView` is attached to the shared player when a post is active; otherwise a thumbnail placeholder is shown.

---

## Folder structure (important files)

- `app/` — Android app module
  - `src/main/java/com/ibrahim/nano_health_task/`
    - `di/MediaModule.kt` — Hilt providers (ExoPlayer, cache, data sources)
    - `feed/ui/` — Compose screens and `VideoPlayer.kt`
    - `feed/data/` — Repository and Room entities/DAO
    - `feed/model/` — UI/domain models (Post, Media types)
  - `build.gradle.kts` — app dependencies (Room, Retrofit, Hilt, ExoPlayer, Coil)

---

## Libraries & Tools

- Kotlin 2.x
- Jetpack Compose
- ExoPlayer (2.18.x) — video playback and caching
- Hilt — dependency injection
- Retrofit + OkHttp + Gson — network client
- Room — local database caching
- Coil — image loading in Compose
- Coroutines / Flow — asynchronous operations
- MockK (test) + kotlinx-coroutines-test — unit testing

---

## Assumptions

- The remote mock API `https://mock.apidog.com/m1/1181613-1175965-default/getHomeFeeds` accepts a `page` query parameter for simple pagination. 
- The app intentionally uses a single shared ExoPlayer for simplicity and resource efficiency. If you need multiple simultaneous players or per-item state (positions), the implementation must be adapted.
- The feed items use `thumbnailUrl` fields for video placeholders; where missing, the app falls back to the media URL.
- The demo project focuses on architecture and behavior; UI styling is minimal.

---

## How to run

1. Open the project in Android Studio (Arctic Fox or newer). Make sure you have an Android SDK (compileSdk 36) installed.
2. Build & run on a device or emulator (recommended with network access).
3. The app fetches feeds from the mock Apidog endpoint. Pull-to-refresh and infinite scroll are supported.

Quick Gradle commands (from project root):

```bash
./gradlew clean assembleDebug
./gradlew :app:testDebugUnitTest
```

---

## Features implemented

- Feed with mixed media posts (image, video, image+video combos)
- Grid layout for posts with multiple media (2x2 preview) and a "+N" overlay for more than 4 items
- Full-screen vertical media viewer when tapping a media cell
- ExoPlayer-based video playback with a PlayerView controller that is hidden by default and toggles when the user taps the video
- Caching: Room for feed items and ExoPlayer `SimpleCache` for video bytes
- Networking: Retrofit + OkHttp to the Apidog mock endpoint with page query
- ViewModel + Repository mapping of network DTOs -> UI models
- Unit tests for repository and ViewModel (MockK + coroutines-test)

---

## Screenshots

Add these screenshots to the README pages (hosted in the repo):

- ![Screenshot1](app/screenShots/Screenshot%202026-01-21%20at%202.47.52%E2%80%AFPM.png)
- ![Screenshot2](app/screenShots/Screenshot%202026-01-21%20at%202.48.38%E2%80%AFPM.png)
- ![Screenshot3](app/screenShots/Screenshot%202026-01-21%20at%202.48.46%E2%80%AFPM.png)

(These images are included in the repository under `app/screenShots/` — the README references them relative to the repo root so they render on GitHub.)

---

## Demo video

A short demo screen recording is available in the repository. You can view or download it here:

- GitHub (blob page): https://github.com/ibrahimali30/nano_health_task/blob/main/app/screenShots/Screen%20Recording%202026-01-21%20at%202.44.49%E2%80%AFPM.mov
- Raw/download link: https://raw.githubusercontent.com/ibrahimali30/nano_health_task/main/app/screenShots/Screen%20Recording%202026-01-21%20at%202.44.49%E2%80%AFPM.mov

---

## Tests

- Unit tests live under `app/src/test/kotlin/` (ViewModel and Repository). Run them with:

---

## Next improvements (optional)

- Persist per-post playback position and restore it when replaying.
- Use a paged data source (Paging 3) for more robust pagination and placeholders.
- Improve offline-first behavior (stale-while-revalidate) and clearer UI status for offline mode.

---
