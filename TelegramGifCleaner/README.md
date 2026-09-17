# Telegram GIF Cleaner

Android/Kotlin utility that signs in to a Telegram user account through the official TDLib JSONJava interface, lists Saved GIFs (`getSavedAnimations`) and removes them from the Telegram account (`removeSavedAnimation`).

## Privacy design

- No ads, analytics, trackers, Firebase, crash reporting, or external networking SDKs.
- Android permission: `INTERNET` only.
- API ID and API Hash are entered by the user at runtime and kept only in process memory. The API Hash is not persisted or logged.
- Phone number, login code and two-step-verification password are not persisted or logged.
- Network connectivity is implemented only by official TDLib.
- Logout invokes TDLib `logOut` and then removes the app's local TDLib database directory after TDLib closes.

## Build

The repository workflow builds TDLib from the official `tdlib/td` repository at the pinned commit documented in the workflow, copies the generated JSONJava binding and native library into the Android project, generates the Gradle Wrapper, builds a Debug APK, audits permissions and packages both the APK and full source ZIP.

The CI artifact APK is named `TelegramGifCleaner.apk`.
