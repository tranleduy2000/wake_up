cp build\outputs\apk\release\app-release.apk app-release.apk
adb uninstall com.duy.wakeup
adb install -r  app-release.apk
adb shell am start -n "com.duy.wakeup/com.duy.wakeup.activities.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
cd ..
exit