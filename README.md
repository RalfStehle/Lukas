# Lukas (Working Title)

Android Remote Control for Lego Powered Up.

## Control your Lego Powered Up Trains with Android

With this Android App you can...

* Control one or multiple Train Hub 88009.
* Control Arduino-ESP32 if you have custom switches (using the Powered Up protocol).
* Connect Lego remotes to train hubs or switches.

## Connect Remotes

This app also works as a mediator between remotes and train hubs. Instead of connecting remotes directly to the train you can decide which train is controlled by witch remote with this app. This is helpful if multiple people play with trains and you want to control which person controls which train (e.g. if your kids are fighting to control the cool train).

For emergencies there is also a button to stop all trains immediately.

## Screenshot

<img src="https://github.com/RalfStehle/Lukas/blob/main/Screenshot_Devices.jpg" width="300">
<img src="https://github.com/RalfStehle/Lukas/blob/main/Screenshot_Remotes.jpg" width="300">

## Links

These resources have been super helpful to implement this project:

* https://lego.github.io/lego-ble-wireless-protocol-docs/index.html
* https://github.com/corneliusmunz/legoino/blob/master/src/Lpf2HubEmulation.cpp
* https://punchthrough.com/android-ble-guide (The Ultimate Guide to Android Bluetooth Low Energy)
* https://medium.com/@martijn.van.welie (Martijn van Welie - Making Android BLE work)
* https://github.com/Cosmik42/BAP (The Brick Automation Project)

For the switches a custom MOC has been built and Arduino ESP32 is used as a controller: https://github.com/RalfStehle/Lego-Weichensteuerung#readme

## Extensions and Contributions

Feel welcome to contribute to the application. Just create an issued and provide a pull request.

Possible exensions are...

* Support for color and distance sensor: 88007, e.g. to stop a train automatically when a color is detected.
* Duplo Base Train (almost same protocol, also supports color sensor and can play sounds)
