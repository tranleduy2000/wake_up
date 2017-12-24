# Changelog
All notable changes to this project will be documented in this file. Format inspired by http://keepachangelog.com/ and this example https://github.com/olivierlacan/keep-a-changelog/blob/master/CHANGELOG.md

## [1.5-5] - Unreleased
## Updated
 - Chinese translation.
 - Japanese translation.
## Fixed
 - New-line escaping in Romanian translation.

## [1.5-4] - 2017-12-14
## Added
 - Chinese translation.

## [1.5-3] - 2017-12-12
## Updated
 - Nearly all translations.

## [1.5-2] - 2017-11-03
### Added
 - Bulgarian translation.

## [1.5-1] - 2017-09-03
### Added
 - Danish translation.

## [1.5] - 2017-08-17
### Added
 - Danish translation.
 - Explanation for device admin rights.
### Updated
 - Basque translation.
### Fixed
 - Build for SDK 8 and 11
 - Or rather 'fixed': catch SecurityExceptions that occur for some unknown reason to me while locking the screen.

## [1.4-2] - 2017-07-18
### Updated
 - German translation.
 - Spanish translation.

## [1.4-1] - 2017-07-17
### Updated
 - Japanese translation.
 - Romanian translation.
### Fixed
 - Crash in pre 14 devices due to wrong setting text

## [1.4] - 2017-07-16
### Added
 - Multiple wave option to avoid accidental waving up.

## [1.3-8] - 2017-05-24
### Added
 - Indonesian translation.

## [1.3-7] - 2017-05-23
### Added
 - Dutch translation.

## [1.3-6] - 2017-04-06
### Added
 - Basque translation.

## [1.3-5] - 2017-03-13
### Added
 - Privacy Policy (only English).

## [1.3-4] - 2017-03-01
### Added
 - Russian translation.

### Fixed
 - Small bug that didn't restart the app after an update.
### Changed
 - 'Lock Screen' is off by default.
 - Improved switching on of screen (should be a little more reliable).

## [1.3-3] - 2017-01-04
### Updated
 - Brazilian Portuguese translation.

## [1.3-2] - 2016-12-31
### Fixed
 - Small bug that ignored 'waving up' if the screen had just turned off because of timeout but wasn't yet locked.
   Thanks NZedPred!
 - Small issue with new line characters in some languages
### Changed
 - Improved some strings

## [1.3-1] - 2016-12-25 - Merry Christmas! :)
### Added
 - Description for 'lock in landscape mode'
### Fixed
 - Only shows the WaveUp service running (or not) toast when the app is enabled or disabled.

## [1.3] - 2016-12-23
### Added
 - Option to make time to cover to configurable
 - Brazilian Portuguese translation. Thanks Chorus Pocus! :)
 - Hungarian translation. Thank you gidano! :)

## [1.2-3] - 2016-12-21
### Fixed
 - Crash in ancient Android devices (pre SDK 14) because os missing SwitchPreference

## [1.2-2] - 2016-09-23
### Updated
 - French translation

## [1.2-1] - 2016-07-16
### Solved
 - Compilation problem due to missing French translation

## [1.2] - 2016-07-08 (actually still unreleased, there is a problem with the F-Droid build I need to fix)

### Added
 - Initial dialog explaining how to uninstall the app and warning about false battery statistics

### Removed
 - Logging options (it always logs to logcat now)
 - Write to external storage permission

## [1.1] - 2016-06-25

### Fixed
 - Issue while turning to landscape if the proximity sensor was covered just before. Now the turning off of screen is canceled.
 - If device admin rights are manually removed, the lock screen setting will turn off automatically and WaveUp will not try to lock the device.

### Changed
 - Listens for proximity sensor immediately after turning screen on.

## [1.0] - 2016-06-08

### Changed
 - Reduced the time it vibrates to notify of locki

### Fixed
 - Root permission option remains unchecked if root access not granted

### Removed
 - External SD Card permission. If log option is on, it will be written to Internal Storage/Android/data/com.duy.wakeup/files

## [0.99-2] 2016-05-23

### Fixed
 - Minor root permission reported errors (only relevant for Smart Lock workaround)
 - Suspending while ongoing call

## [0.99-1] 2016-05-21

### Added
 -  French translation. Thank you ko_lo!

### Fixed
 - SECURITY: avoid fragment injection!
 - Wave mode. Thanks again Tsuyoshi!
 - Android M read phone state permission
 - Material design for pre-lollipop devices

### Known issues
 - Suspending while ongoing call not working

## [0.99] - 2016-05-18

### Added
 - Romanian translation. Thank you so much licaon-kter!
 - Option to vibrate on lock.

### Fixed
 - Minor code improvements. Thank you so much Tsuyoshi!

### Removed
 - Auto start option

## [0.98-1] - 2016-05-15
### Fixed
 - Build issues with F-Droid
 - Minor logging changes

## [0.98] - 2016-05-12
### Improved
 - (and simplified) algorithm to switch screen on and off. The sensor is always off if the options and the phone state allow it.
 This might breakt some stuff but I really hope it doesn't :)

### Fixed
 - Crash while receiving a call if log to file activated

## [0.97] - 2016-05-11
### Added
 - Suspend WaveUp while on a phone call (needs READ_PHONE_STATE permission)

## [0.96-2] - 2016-05-05
### Fixed
- Compatibility issues: improvement in near/far measurement. Some phones report strange values and this should fix it.

## [0.96-1] - 2016-05-04
### Fixed
- Fix crash at startup on some phones (upgraded appcompat)

## [0.96] - 2016-05-04
### Added
- Japanese translation. Thank you Tsuyoshi!
- Small improvement in lock settings. Thank you for this too Tsuyoshi! :)

## [0.95] - 2016-04-30
### Added
- Switch off screen simulating power button

### Changed
- Performance improvements

### Known issues
- Lock in landscape option isn't really working

## [0.94] - 2016-04-27
### Fixed
- App crash while starting for the first time after a boot (if disabled)

## [0.93] - 2016-04-22
### Added
- Lock in landscape mode option

## [0.92] - 2016-04-18
### Added
- Logging options (included log to file)

## [0.91] - 2016-04-10
### Added
- First version of WaveUp
