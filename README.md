[<img src="google-play-store/google-play-badge.png"
      alt="Get it on Google Play"
      height="80">](https://play.google.com/store/apps/details?id=com.duy.wakeup)

WaveUp
======

WaveUp is an Android app that *wakes up* your phone - switches the screen on - when you *wave* over the proximity sensor.

I have developed this app because I wanted to avoid pressing the power button just to take a look at the watch - which I happen to do a lot on my phone. There are already other apps that do exactly this - and even more. I was inspired by Gravity Screen On/Off, which is a **great** app. However, I am a huge fan of open source software and try to install free software (free as in freedom, not only free as in free beer) on my phone if possible. I wasn't able to find an open source app that did this so I just did it myself.

Just wave your hand over the proximity sensor of your phone to turn the screen on. This is called *wave mode* and can be disabled in the settings screen in order to avoid accidental switching on of your screen.

It will also turn on the screen when you take your smartphone out of your pocket or purse. This is called *pocket mode* and can also be disabled in the settings screen.

Both of these modes are enabled by default.

It also locks your phone and turns off the screen if you cover the proximity sensor for one second (or a specified time). This does not have a special name but can nonetheless be changed in the settings screen too. This is not enabled by default.

For those who have never heard proximity sensor before: it is a small thingie that is somewhere near where you put your ear when you speak on the phone. You practically can't see it and it is responsible for telling your phone to switch off the screen when you're on a call.


Uninstall
---------

You cannot uninstall WaveUp 'normally' because of the administrative rights it needs.

To uninstall it, just open it and use the 'Uninstall WaveUp' button at the bottom of the menu.

Required Android Permissions:
-----------------------------

- WAKE_LOCK to turn on the screen
- USES_POLICY_FORCE_LOCK to lock the device
- RECEIVE_BOOT_COMPLETED to automatically startup on boot if selected
- READ_PHONE_STATE to suspend WaveUp while on a call

Known issues
------------

Unfortunately, some smartphones let the CPU on while listening to the proximity sensor. This is called a *wake lock* and causes considerable battery drain. This isn't my fault and I cannot do anything to change this. Other phones will "go to sleep" when the screen is turned off while still listening to the proximity sensor. In this case, the battery drain is practically zero.





