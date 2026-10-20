# Somnia — Android sleep journal

Native Android Java project. Dark interface, manual sleep timer, locally saved sleep history, 7-session graph, average duration, bedtime reminders and sleep goal.

## Build APK
1. Open the `Somnia` folder in Android Studio (with Android SDK 35 and JDK 17).
2. Allow Gradle to sync (requires internet for Android Gradle Plugin).
3. Choose **Build > Build APK(s)**.
4. Find `app/build/outputs/apk/debug/app-debug.apk`.

No APK is bundled: this environment does not have the Android SDK/Gradle, so compilation and device testing have not been performed.

## Notes
- This is a starter app, not a copy of Somnr's proprietary code or brand.
- Sleep is manually started/stopped; the app cannot measure sleep stages.
- Bedtime notifications use inexact alarms and may be delayed by Android battery restrictions.
- No account or cloud sync; history is kept in private app preferences and removed on uninstall.
- Sleep goal is customizable and not a medical recommendation.

## GitHub Actions build (phone only)
This repository includes `.github/workflows/build-apk.yml`. Upload the project files to the **root** of a GitHub repository, including the hidden `.github` folder. In GitHub open **Actions > Build Somnia APK > Run workflow**, then download the **Somnia-APK** artifact from the completed run. Unzip the downloaded artifact to get `app-debug.apk`.

Note: GitHub's browser file uploader does not unpack a ZIP into repository files automatically. The source files must be uploaded preserving their directory paths, or pushed using Git.
