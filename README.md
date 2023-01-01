# MP3 Player
Overlay based Song Player for Android 6 and above

## Usage / How to set up 
1. Install the app. Once installed, open it
2. You may see the UI.....i am lazy to complete it
3. Tap the button, which should take you to a special settings menu
4. It will ask you to enable permission for overlay, you are required to grant it (only then it will appear)
5. Next, allow permissions for files and audio (files are for indexing songs, audio is for the small visualizer)
6. Once all permissions are granted, tap the button and voila! The overlay should appear!

## Sample images
> Main overlay
>>  <img src="/assets/Untitled523_20230101181318.png" alt="Main overlay" width="200" height="225"/>


> Mini overlay
>> <img src="/assets/Untitled523_20230101181320.png" alt="Main overlay" width="200" height="225"/>


> Tiny overlay
>> <img src="/assets/Untitled523_20230101181321.png" alt="Main overlay" width="200" height="225"/>


## Features (Still more to come)
- Overlay based song player
- Song downloader on youtube url
- Convenience for the user(?)
- No need to pause your game and change songs, just use the overlay at the same time!

## Troubleshooting
### The overlay does not appear for me after granting all permissions!
- This is likely an issue in the code somewhere i am not sure of.
- Devices which are not supported for now are **Redmi**.

### Why does it ask permission to record voice?
- It is to grant access for the mini visualizer (top left of overlay)

### Why does it not create the cache folder for me?
- Either you do not have any songs, or the songs do not have an image metadata in them.
- This can be fixed by having at least one song with image metadata, which then the application can register

## Bugs
### 1. Visualizer wont work on Android 7
- If you come across any more bug, please report to me at my discord `Pannikuパン肉#5360`!
