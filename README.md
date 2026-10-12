# Friday Night Funkin' PlayTest Mobile (Android)

A native Android rhythm game application rewritten in Kotlin and Jetpack Compose, featuring Friday Night Funkin' gameplay, mobile touch hitboxes, and an advanced Debug Suite.

## Features

- **Rhythm Engine & Gameplay**:
  - 4-direction strumline notes (Left, Down, Up, Right) with custom neon glowing arrows and receptors.
  - Multi-touch 4-column touch hitbox overlay calibrated for Android devices.
  - Authentic FNF timing and judgements: `SICK!!` (350), `GOOD` (200), `BAD` (100), `SHIT` (50), and `MISS` (-100).
  - Dynamic dual-color health bar with animated Boyfriend and Opponent heads reacting to health states.
  - Expressive character stage view with BF, GF, and Opponents dancing and singing to the beat.
  - Low-latency procedural synth engine providing real beat playback, bass drums, snares, hi-hats, and BoyFriend/Opponent voice tones.

- **Debug Menu Suite** *(New)*:
  - **Chart Editor & Chart Tester**: Interactive 16-step grid per measure with real-time note placement and instant playtesting.
  - **Song Warp**: Direct jump to any track (Tutorial through Week 7) and difficulty level.
  - **Audio & Latency Calibration**: Metronome tap box to measure audio-visual latency and save custom offsets.
  - **Gameplay Modifiers**: Botplay toggle, God Mode (practice), Ghost Tapping toggle, Downscroll toggle, and Hitbox Opacity slider.
  - **Animation & Sprite Inspector**: Preview character singing poses and reactions.

- **Game Modes**:
  - **Story Mode**: Play through weekly battles across Tutorial, Week 1, Week 2, Week 3, Week 4, Week 6, and Week 7.
  - **Freeplay**: Select any track, preview beats, switch difficulties (Easy, Normal, Hard, Erect, Nightmare), and track personal high scores.
  - **Options & Customization**: Hitbox opacity, downscroll, ghost tapping, haptic feedback, and note splashes.

## Architecture

- **Language**: Kotlin 2.1
- **UI Framework**: Jetpack Compose with Material Design 3
- **Audio Engine**: Low-latency procedural `AudioTrack` PCM synthesizer
- **Build System**: Gradle 9.3.1 (Kotlin DSL) with Android Gradle Plugin 8.9.0

## License

Apache License 2.0. Friday Night Funkin' assets and characters are copyright The Funkin' Crew.
