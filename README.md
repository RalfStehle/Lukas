<H1>Lukas</H1>
<br>
<H2> Lego Powered Up mit Android steuern</H2>
<br>
Die Lego-Fernsteuerung 88010, der Train-Hub 88009 und Arduino-ESP32-Microcontroller (als Weichensteuerung) können zusammen mit einer App gesteuert werden.<br>
<br>
Mehrere Hubs, Fernsteuerungen und Arduinos können kombiniert werden.<br>
<br>
Es ist sogar möglich mit einer Fernsteuerung zwei Train-Hubs oder einen Train-Hub und einen Arduino zu steuern.<br>
Am Train-Hub wird automatisch erkannt, ob ein LED-Licht oder ein Train-Motor angeschlossen wurde.<br>
<br>
Am Duplo Train "train base" kann bisher nur der Motor gesteuert werden.<br>
Sounds und Tone sind unberücksichtigt.
<br>
<br>
<H2> Links</H2>
[LEGO Wireless Protocol 3.0.00 documentation (https://lego.github.io/lego-ble-wireless-protocol-docs/index.html)]<br>
[Legoino von Cornelius Munz] (https://github.com/corneliusmunz/legoino/blob/master/src/Lpf2HubEmulation.cpp)<br>
[The Ultimate Guide to Android Bluetooth Low Energy] (https://punchthrough.com/android-ble-guide)<br>
[Making Android BLE work] (https://medium.com/@martijn.van.welie)<br>
[The Brick Automation Project] (https://github.com/Cosmik42/BAP)<br>
[Seeed Studio XIAO ESP32C3 - Getting Started and Ble-Example] (https://wiki.seeedstudio.com/XIAO_ESP32C3_Getting_Started)<br>
<A HREF="https://rebrickable.com/mocs/MOC-81350/DomBug/railway-electric-point-machine-with-a-china-servo" target="_blank">Weichenantrieb (MOC) by DomBug</A>
<A HREF="https://rebrickable.com/users/Rebrickable/mocs/purchases/download/0/82990/c341ec473cedcb3e6f8cb6a10d253aa2472d6cd28e1239c5a636bb22a6edaaaa/?expire=1675114402" target="_blank">Weichenantrieb.pdf von DomBug (www.lokschuppen-dominik.de)</A>
<br>
<br>


<H2>Mögliche zukünftige Erweiterungen</H2>
<H3>Integration des Farb- & Abstandssensor 88007</H3>
Mit dem Lichtsensor kann man den Zug automatisch anhalten, schneller und langsamer werden lassen oder eine Pause einlegen, wenn er an einer farbigen Legoplatte vorbeifährt.
Leider lässt sich der relativ große Sensor aber nur schwierig in die Lego-Modelle integrieren. Daher habe ich den Sensor nur mal kurz ausprobiert und nie weiter genutzt..<br>
<br>
<br>
<H2>Beispiele für hexadecimale Steuercodes</H2>
(siehe: <A HREF="https://lego.github.io/lego-ble-wireless-protocol-docs/index.html" target="_blank">LEGO Wireless Protocol 3.0.00</a>)<br>
<H3>1. Integration der Fernsteuerung 88010.<br></H3>
Nach Bluetooth-Connect muss man die Notifications am Device aktivieren. Sonst bekommt man keine Nachricht bei Betätigung einer Taste.<br>
In der Legoino Bibliothek von Cornelius Munz wird das über den Befehl „activatePortDevice" gemacht.<br> 
Diese Funktion sendet einen Wert auf die Characteristic die dem Hub sagt, dass an einem definierten virtuellen Port Notifications versendet werden, wenn sich Werte ändern.
<br>
Das 4. Byte ist der virtuelle Port mit folgenden Möglichkeiten:<br>
<br>
enum struct PoweredUpRemoteHubPort<br>
{<br>
  LEFT = 0x00,<br>
  RIGHT = 0x01,<br>
  LED = 0x34,<br>
  VOLTAGE = 0x3B,<br>
  RSSI = 0x3C<br>
};<br>
<br>
<br>
<b>Train Remote - Aktivieren der Notifications:</b><br>
<br>
Steuertasten links aktivieren:<br>
0xA, 0x00, 0x41, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01<br>
<br>
Steuertasten rechts aktivieren:<br>
0xA, 0x00, 0x41, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01<br>
<br>
Die grüne Taste ist automatisch aktiviert, eigentlich ja zum An- und Ausschalten.<br>
<br>
<br>
Java-Code-Beispiel für ENABLE NOTIFICATION:<br>
BluetoothGattCharacteristic charac1 = null;<br>
charac1 = Service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));<br>
BluetoothGattDescriptor descriptor = charac1.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));<br>
descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);<br>
bluetoothGatt.writeDescriptor(descriptor);<br>
bluetoothGatt.setCharacteristicNotification(charac1, true);<br>
bluetoothGatt.readCharacteristic(charac1);<br>
<br>
<br>
<H3>Train Remote - Auswerten der Notifications</H3>
<br>
enum struct ButtonState<br>
{<br>
  PRESSED = 0x01,<br>
  RELEASED = 0x00,<br>
  UP = 0x01,<br>
  DOWN = 0xff,<br>
  STOP = 0x7f<br>
};<br>
<br>
<br>
05 00 08 02 01   Remote Green Press<br>
05 00 08 02 00  Remote Green Release<br>
<br>
05 00 45 00 01	LEFT Taste +<br>
05 00 45 00 7F	LEFT Taste NULL (red)<br>
05 00 45 00 FF	LEFT Taste -<br>
05 00 45 00 00	Release<br>
<br>
<br>
05 00 45 01 01	RIGHT Taste +<br>
05 00 45 01 7F	RIGHT Taste NULL<br>
05 00 45 01 FF	RIGHT Taste -<br>
05 00 45 01 00	Release<br>
<br>
<br>

<H3>Train Hub - Aktivieren und Auswerten der Hub Property Reference</H3>
<br>
Senden:<br>
{0x3, 0x00, 0x04}  aktiviert Message-Type für Hub Attached I/O.<br>
<br>
Danach meldet der Hub in den Notifications was angeschlossen ist und auch Änderungen:<br>
<br>
Header (2Byte), MessageType I/O (3.Byte), Port (4.Byte), Attached (5. Byte), Type (6. Byte)<br>
Type: 02 = Train Motor, 08 = LED<br>
<br>
Beispiele:<br>
0F 00 04 00 01 02 00 00 00 00 00 00 00 00 00  eingesteckt, A, Motor<br>
0F 00 04 01 01 02 00 00 00 00 00 00 00 00 00  eingesteckt, B, Motor<br>
<br>
0F 00 04 00 01 08 00 00 00 00 00 00 00 00 00  eingesteckt, A, LED<br>
0F 00 04 01 01 08 00 00 00 00 00 00 00 00 00  eingesteckt, B, LED<br>
<br>
05 00 04 00 00                                ausgesteckt, A<br>
05 00 04 01 00                                ausgesteckt, B<br>
<br>
<br>
<H3>Notizen:<br></H3>
VOLTAGE:<br>
so wird wahrscheinlich die Spannung berechnet:<br>
(<A HREF="https://github.com/corneliusmunz/legoino/blob/master/src/Lpf2Hub.cpp" target="_blank">siehe Legoino</a>)<br>
  <br>
#define LPF2_VOLTAGE_MAX 9.6<br>
#define LPF2_VOLTAGE_MAX_RAW 3893<br>
#define LPF2_CURRENT_MAX 2444<br>
<br>
double voltage = (double)voltageRaw * LPF2_VOLTAGE_MAX / LPF2_VOLTAGE_MAX_RAW;<br>
   log_d("voltage value: %.2f [V]", voltage);<br>
   return voltage;<br>
