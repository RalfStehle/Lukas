<H1>Lukas</H1>
<br>
<H2> Lego Powered Up mit Android steuern</H2>
<br>
Mit dieser Android-App können Lego-Fernsteuerungen 88010, Train-Hub´s 88009 und Arduino-ESP32-Microcontroller (als Weichensteuerung) gesteuert werden.<br>
<br>
Mehrere Hubs, Fernsteuerungen und Arduinos können kombiniert werden.<br>
<br>
Es ist die Fernsteuerungen beliebig mit Train-Hubs oder Arduino-Weichensteuerungen zu verbinden.<br>
Zwei Züge mit einer Fernsteuerung sind daher kein Problem oder zwei Fernsteuerungen, die denselben Zug kontrollieren.<br>
Am Train-Hub wird automatisch erkannt, ob ein LED-Licht oder ein Train-Motor angeschlossen wurde.<br>
Auch wenn eine Fernsteuerung aktiv ist, hat die App Zugriff auf Züge und dient als Supervisor.<br>
Ein Notschalter (rote Hand rechts unten) stoppt alle Züge <br>
<br>
<br>
<div>
    <img src="https://github.com/RalfStehle/Lukas/blob/main/Screenshot_Devices.jpg" title="Screenshot" height= "500" alt="" style="margin:5px" align="left">
    <img src="https://github.com/RalfStehle/Lukas/blob/main/Screenshot_Remotes.jpg" title="Screenshot" height= "500" alt="" style="margin:5px" align="left">
</div>
<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>

<br>

<H2> Links</H2>
Besonderer Dank gilt:<br>
<A HREF="https://squidex.io" target="_blank">Sebastian Stehle - https://squidex.io</A><br>
<A HREF="https://lego.github.io/lego-ble-wireless-protocol-docs/index.html" target="_blank">LEGO Wireless Protocol 3.0.00 documentation</A><br>
<A HREF="https://github.com/corneliusmunz/legoino/blob/master/src/Lpf2HubEmulation.cpp" target="_blank">Cornelius Munz - Legoino</A><br>
<A HREF="https://punchthrough.com/android-ble-guide" target="_blank">The Ultimate Guide to Android Bluetooth Low Energy</A><br>
<A HREF="https://medium.com/@martijn.van.welie" target="_blank">Martijn van Welie - Making Android BLE work</A><br>
<A HREF="https://github.com/Cosmik42/BAP" target="_blank"The Brick Automation Project></A><br>

<br>

<H2>Lego-Weichen mit Arduino ESP32 über Bluetooth steuern</H2>
<A HREF="https://github.com/RalfStehle/Lego-Weichensteuerung" target="_blank">github.com/RalfStehle/Lego-Weichensteuerung</A><br>

<br>

<H2>Mögliche zukünftige Erweiterungen</H2>
<H3>Farb- & Abstandssensor 88007</H3>
Mit dem Lichtsensor könnte man den Zug automatisch anhalten, schneller und langsamer werden lassen oder eine Pause einlegen, je nach Farbe.
<br>
<H3>Duplo Base Train</H3>
Der Duplo Train reagiert fast auf dieselben Motorsteuerung-Codes<br>
Zusätzlich kann er Sounds wiedergeben und hat einen Farbsensor bereits eingebaut<br>
