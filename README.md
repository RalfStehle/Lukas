# Hubi
<br>
<br>
# Lego Powered Up mit Android steuern
<br>
<br>
## Links
[The Ultimate Guide to Android Bluetooth Low Energy] (https://punchthrough.com/android-ble-guide)<br>
[Legoino von Cornelius Munz] (https://github.com/corneliusmunz/legoino/blob/master/src/Lpf2HubEmulation.cpp)<br>
[The Brick Automation Project] (https://github.com/Cosmik42/BAP)<br>
[LEGO Wireless Protocol 3.0.00 documentation] (https://lego.github.io/lego-ble-wireless-protocol-docs/index.html)<br>
<br>
<br>
## In Planung:
<br>
1. Integration der Fernsteuerung 88010.<br>
Die App könnte zwischen Train-Hub und Remote sozusagen dazwischengeschaltet werden,<br>
praktisch als Supervisor.<br>
<br>
2.Lichtsensor-Daten auswerten<br>
Mit dem Lichtsensor kann mann den Zug automatisch anhalten, schneller und langsamer werden lassen oder eine Pause einlegen, wenn er an einer farbigen Legoplatte vorbeifährt.
<br>
<br>
## 1. Integration der Fernsteuerung 88010.<br>
Nach Connect muss man dem Gerät sagen, dass es bei Änderungen von Werten Notifications versenden soll.<br>
In der Legoino Bibliothek von Cornelius Munz wird das über den Befehl „activatePortDevice" gemacht.<br> 
Diese Funktion sendet einen Wert auf die Characteristic die dem Hub sagt, dass an einem definierten virtuellen Port Notifications versendet werden, wenn sich Werte ändern.
<br>
Das 4. Byte ist der virtuelle Port mit folgenden Möglichkeiten:<br>
br
enum struct PoweredUpRemoteHubPort<br>
{<br>
  LEFT = 0x00,
  RIGHT = 0x01,
  LED = 0x34,
  VOLTAGE = 0x3B,
  RSSI = 0x3C
};<br>
<br>
### Beispiele:<br>
Steuertasten links aktivieren:<br>
0xA, 0x00, 0x41, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01<br>
<br>
Steuertasten rechts aktivieren:<br>
0xA, 0x00, 0x41, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01<br>
<br>
Die grüne Taste ist automatisch aktiviert, eigentlich ja zum An- und Ausschalten.<br>
<br>
<br>
Die Notifications müssen aktiviert sein, evtl. daher noch folgende Befehle ausführen:<br>
BluetoothGattCharacteristic charac1 = null;<br>
charac1 = Service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));<br>
BluetoothGattDescriptor descriptor = charac1.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));<br>
descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);<br>
bluetoothGatt.writeDescriptor(descriptor);<br>
bluetoothGatt.setCharacteristicNotification(charac1, true);<br>
bluetoothGatt.readCharacteristic(charac1);<br>
<br>
<br>
### Train Remote - Auswerten der Notifications
<br>
enum struct ButtonState<br>
{<br>
  PRESSED = 0x01,
  RELEASED = 0x00,
  UP = 0x01,
  DOWN = 0xff,
  STOP = 0x7f
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
### Appendix:<br>
VOLTAGE:<br>
so wird wahrscheinlich die Spannung berechnet (siehe Legoino):<br>
https://github.com/corneliusmunz/legoino/blob/master/src/Lpf2Hub.cpp<br>
#define LPF2_VOLTAGE_MAX 9.6<br>
#define LPF2_VOLTAGE_MAX_RAW 3893<br>
#define LPF2_CURRENT_MAX 2444<br>
<br>
double voltage = (double)voltageRaw * LPF2_VOLTAGE_MAX / LPF2_VOLTAGE_MAX_RAW;<br>
   log_d("voltage value: %.2f [V]", voltage);<br>
   return voltage;<br>
