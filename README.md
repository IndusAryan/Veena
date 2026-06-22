<div align="center">
<div align="center">
  <img src="https://github.com/IndusAryan/Veena/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png?raw=true" width="160" height="160">
  <h1>Veena (वीणा)</h1>
<h3>🪷🪩🎵🦢</h3>
<h3>An addon-based music discovery, downloader and streaming aggregator.</h3>

<!-- ---------- Badges ---------- -->
  <div align="center">
    <img alt="Last commit" src="https://img.shields.io/github/last-commit/IndusAryan/Veena?color=c3e7ff&style=for-the-badge">
    <img alt="Repo size" src="https://img.shields.io/github/repo-size/IndusAryan/Veena?color=c3e7ff&style=for-the-badge">
    <a href="https://github.com/IndusAryan/Veena/releases">
      <img src="https://img.shields.io/github/downloads/IndusAryan/Veena/total?color=ff9500&style=for-the-badge&label=Downloads"/>
    </a>
    <a href="https://github.com/IndusAryan/Veena/stargazers">
      <img src="https://img.shields.io/github/stars/IndusAryan/Veena?color=ffff00&style=for-the-badge&label=Stars"/>
    </a>
    <br>
    <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-a503fc?logo=kotlin&logoColor=white&style=for-the-badge"/>
    <img alt="Jetpack Compose" src="https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label="/>
  </div>

<br>

<!-- GitHub Trending 
<a href="https://trendshift.io/repositories/810855532" target="_blank"><img src="https://trendshift.io/api/badge/repositories/13482" alt="IndusAryan%2FVeena | Trendshift" style="width: 250px; height: 55px;" width="250" height="55"/></a> 
-->

<br>

#### Download
<div align="center">
  <!--<a href="#"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" width="200"></a>--> 
  <a href="https://github.com/IndusAryan/Veena/releases/latest"><img src="https://raw.githubusercontent.com/NeoApplications/Neo-Backup/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" width="200"></a> 
  <!--<a href="#"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" width="200"></a>-->
</div>
</div>

---

## ✨ Features

- 🧩 **Addon Architecture** — Completely modular system. Add your own providers discovery, streaming and downloading.
- 🔍 **Multi-API Support** — Search across multiple sources simultaneously using custom addons.
- 📥 **Music Downloader** — High-quality music downloads with metadata tagging.
- 📻 **Powerful Streamer** — Seamless streaming experience with ExoPlayer/Media3 integration.
- 🎨 **Material 3 & Haze** — Modern, beautiful UI with glassmorphic blur effects and dynamic colors.
- ⚡ **No Built-in Providers** — Privacy-focused approach where users control their sources.
- 🗂 **Local Library** — Manage your downloaded music with a built-in library manager.
- ❤️**Favourites** — Collect or favourite songs in library.


---

## 📱 Screenshots

<p align="center">          
 <img src="https://github.com/IndusAryan/Veena/blob/main/asset/1.png?raw=true" width="200" />          
 <!--<img src="https://github.com/IndusAryan/Veena/blob/main/asset/2.png?raw=true" width="200" />-->          
 <img src="https://github.com/IndusAryan/Veena/blob/main/asset/3.png?raw=true" width="200" />          
 <img src="https://github.com/IndusAryan/Veena/blob/main/asset/4.png?raw=true" width="200" /> 
 <img src="https://github.com/IndusAryan/Veena/blob/main/asset/5.png?raw=true" width="200" /> 
 <img src="https://github.com/IndusAryan/Veena/blob/main/asset/6.png?raw=true" width="200" /> 
 <img src="https://github.com/IndusAryan/Veena/blob/main/asset/7.png?raw=true" width="200" /> 
</p>

---

## 🛠 Built With

| Component                                  | Library                                   |
|--------------------------------------------|-------------------------------------------|
| **UI Framework**                           | Jetpack Compose                           |
| **Design System**                          | Material 3 Expressive You + Retro + Glass |
| **Language**                               | Kotlin                                    |
| **Networking**                             | OkHTTP 3                                  |
| **JSON Parser**                            | Kotlin X Serialization                    |
| **Navigation**                             | Compose Navigation                        |
| **Media**                                  | Media3 (ExoPlayer & Transformer)          |
| **Dependency Injection**                   | Hilt/Dagger2                              |
| **Database**                               | Room 3                                    |
| **Preferences Store**                      | DataStore                                 |
| **Image Loading**                          | Coil 3.5                                  |
| **Addon Runtime**                          | DexClass Loader (.veena)                  |
| **Scripting Engine (for JS based addons)** | QuickJS-KT (from Dokar3)                  |
| **Background and Downloads**               | WorkManager                               |
| **Metadata and ID3 Tagging**               | Kyant's TagLib                            |

---

## 📂 Package Hierarchy

```text
com.indus.veena
├── database     # Room entities, DAOs and migrations
├── di           # Hilt modules and dependency injection
├── extension    # Addon/Extension contract implementation
├── helpers      # Utility helper classes
├── lifecycle    # Component lifecycle management
├── models       # Data models and DTOs
├── navigation   # Compose destination and graph definitions
├── repository   # Data layer and source abstraction
├── service      # Media session and background services
├── ui           # Compose UI layers
│   └── screens  # App screens (Home, Player, Addons, etc.)
└── util         # Common utilities and extensions
```

---

## 🛡️ Privacy Notice
Veena does not collect any user data, telemetry, or tracking information. Since the app is an aggregator that relies on user-provided addons, any data exchange happens directly between your device and the service provider defined in the addon. No intermediate servers are used.

---

## 🔮 Upcoming Features
- 🌐 **Translations** — Bringing Veena to more languages.
- 🔥 **Trending Discovery** — A dedicated screen for discovering trending music via addons.
- 📦 **Official Addons** — Official repository for curated music discovery addons.
- 🎨 **Enhanced Theming** — More customization options for the UI.

---

## 🤝 App Support
Join our community for support, updates, and addon sharing!

<a href="https://t.me/VeenaApp"><img src="https://img.shields.io/badge/Telegram-26A5E4?style=for-the-badge&logo=telegram&logoColor=white" /></a>

---

## 💖 Support & Donation
If you find Veena useful, please consider supporting the development:
- ⭐ **Star this repo** to show your appreciation and share the app.
- 🐛 **Open bugs** in [Issues](https://github.com/IndusAryan/Veena/issues) and provide steps to replicate.
- 🛠 **Contribute** by creating addons or by contributing to the app.

---

## 🧩 Addons Documentation
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
This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.
> To protect the integrity of the project, any redistribution or modification must remain open-source under the same license. Commercial redistribution without attribution or license violation is strictly prohibited.

---
<div align="center">
Built with❤️‍🔥by the Indus Aryan.
</div> 
</div>