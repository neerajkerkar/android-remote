# android-remote

An android app for controlling windows pc via mobile phone through Bluetooth. The app can be used to control PPT slides, mouse as well as send text to your pc. 

You can remotely control PPT presentation on your pc via the app using the following features:
1. Start and end slide show.
2. Jump to a specific slide
3. Sync: The slide images as well as the notes are displayed in the app.
4. Go to previous and next slide
5. Mouse feature can control the pointer on your pc

Additional features:
1. Send text: The text you type in the app is sent to the active textbox on your pc.

The code that runs on the pc is written in python. Run the app.py file to start the server, or alternatetively, run Remote Server.exe in dist folder. The server uses win32api, win32com and win32con libraries for controlling windows.
