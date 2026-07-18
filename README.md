<div align="center">
  <img src="https://github.com/IndusAryan/Veena/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png?raw=true" width="160" height="160">
  <h1>Veena (वीणा)</h1>
  <h3>🪷🪩🎵🦢</h3>
  <h3>An addon-based music discovery, downloader and streaming aggregator.</h3>

  <img alt="Last commit" src="https://img.shields.io/github/last-commit/IndusAryan/Veena?color=c3e7ff&style=for-the-badge">
  <img alt="Repo size" src="https://img.shields.io/github/repo-size/IndusAryan/Veena?color=c3e7ff&style=for-the-badge">
  <a href="https://github.com/IndusAryan/Veena/releases">
    <img src="https://img.shields.io/github/downloads/IndusAryan/Veena/total?color=ff9500&style=for-the-badge&label=Downloads"/>
  </a>
  <a href="https://github.com/IndusAryan/Veena/stargazers">
    <img src="https://img.shields.io/github/stars/IndusAryan/Veena?color=ffff00&style=for-the-badge&label=Stars"/>
  </a>
  <br><br>
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-a503fc?logo=kotlin&logoColor=white&style=for-the-badge"/>
  <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label="/>

  <br>

  <a href="https://github.com/IndusAryan/Veena/releases/latest">
    <img src="https://raw.githubusercontent.com/NeoApplications/Neo-Backup/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" width="200">
  </a>
</div>

---

## ✨ Features

- 🧩 **Addon Architecture** — Modular system for discovery, streaming and downloading.
- 🔍 **Multi-API Support** — Search across multiple sources simultaneously using custom addons.
- 📥 **Music Downloader** — High-quality music downloads with metadata tagging.
- 📻 **Powerful Streamer** — Seamless streaming experience with ExoPlayer/Media3 integration.
- 🎨 **Material 3 & Haze** — Modern, beautiful UI with glassmorphic blur effects and dynamic colors.
- ⚡ **No Built-in Providers** — Privacy-focused approach where users control their sources.
- 🗂 **Local Library** — Manage your downloaded music with a built-in library manager.
- ❤️ **Favourites** — Collect or favourite songs in library.

---

## 📱 Screenshots

<p align="center">
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png?raw=true" width="200" />
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png?raw=true" width="200" />
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png?raw=true" width="200" />
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png?raw=true" width="200" />
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png?raw=true" width="200" />
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png?raw=true" width="200" />
  <img src="https://github.com/IndusAryan/Veena/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/7.png?raw=true" width="200" />
</p>

---

## 🛠 Built With

| Component | Library |
|---|---|
| **UI Framework** | Jetpack Compose |
| **Design System** | Material 3 Expressive You + Retro + Glass |
| **Language** | Kotlin |
| **Networking** | OkHTTP 3 |
| **JSON Parser** | Kotlin X Serialization |
| **Navigation** | Compose Navigation |
| **Media** | Media3 (ExoPlayer & Transformer) |
| **Dependency Injection** | Hilt / Dagger2 |
| **Database** | Room 3 |
| **Preferences Store** | DataStore |
| **Image Loading** | Coil 3.5 |
| **Addon Runtime** | DexClassLoader (`.veena`) |
| **Scripting Engine** | QuickJS-KT (from Dokar3) |
| **Background & Downloads** | WorkManager |
| **Metadata & ID3 Tagging** | Kyant's TagLib |

---

## 📂 Project Structure

```
Veena/
├── app/                                    # Main Android application
│   └── src/main/java/com/indus/veena/
│       ├── database/                       # Persistence (Room, DataStore)
│       ├── di/                             # Dependency Injection (Hilt)
│       ├── extension/                      # Addon loading & management
│       ├── helpers/                        # Component helpers
│       ├── lifecycle/                      # Lifecycle-aware observers
│       ├── models/                         # Core data models & DTOs
│       ├── navigation/                     # Compose navigation graphs
│       ├── repository/                     # Data layer abstraction
│       ├── service/                        # Media3 playback services
│       ├── ui/                             # Screens & Theme
│       └── util/                           # General utility classes
│
├── extension-contract/                     # SDK for addon development
│   └── src/main/java/com/indus/veena/contract/   # Core interfaces & contracts
│
└── veena-gradle-plugin/                    # Addon build tooling
    └── src/main/java/com/indus/veena/gradle/     # Packaging & build tasks
```

---

## 🛡️ Privacy

Veena does not collect any user data, telemetry, or tracking information. Since the app is an aggregator that relies on user-provided addons, any data exchange happens directly between your device and the service provider defined in the addon. No intermediate servers are used.

---

## 🔮 Upcoming Features

- 🌐 **Translations** — Bringing Veena to more languages.
- 🔥 **Trending Discovery** — A dedicated screen for discovering trending music via addons.
- 📦 **Official Addons** — Official repository for curated music discovery addons.
- 🎨 **Enhanced Theming** — More customization options for the UI.

---

## 🤝 Community & Support

Join our community for support, updates, and addon sharing!

<a href="https://t.me/VeenaApp"><img src="https://img.shields.io/badge/Telegram-26A5E4?style=for-the-badge&logo=telegram&logoColor=white" /></a>

---

## 💖 Support the Project

- ⭐ **Star this repo** to show your appreciation and help others discover Veena.
- 🐛 **Report bugs** in [Issues](https://github.com/IndusAryan/Veena/issues) with steps to reproduce.
- 🛠 **Contribute** by building addons or submitting pull requests.

---

## 🧩 Addon Development

Interested in building your own addon for Veena? Documentation is currently under development.

[**Addon Documentation (Coming Soon)**](https://github.com/IndusAryan/Veena/wiki)

---

## ⚖️ Legal Disclaimer

Veena is a content aggregator and a specialized browser for music services.

- **No Hosting:** Veena does not host, store, or distribute any copyrighted media or audio files.
- **User Responsibility:** All content is accessed via third-party addons provided by the community. Users are responsible for ensuring their use of such addons complies with local laws and the terms of service of the content providers.
- **Educational Purpose:** This project is for personal use and educational purposes.

---

## 📄 License

Licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

Any redistribution or modification must remain open-source under the same license. Commercial redistribution without attribution or license compliance is strictly prohibited.

---

<div align="center">
  Built with ❤️‍🔥 by Indus Aryan
</div>