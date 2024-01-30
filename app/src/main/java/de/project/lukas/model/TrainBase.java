package de.project.lukas.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Objects;
import java.util.UUID;

import de.project.lukas.utils.HexUtils;
import de.project.lukas.utils.LegoHelper;
import de.project.lukas.utils.LegoWriterQueue;

@SuppressLint("MissingPermission")
public class TrainBase extends Device {
    private final static UUID ServiceUUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123");
    private final static UUID CharacteristicsUUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123");
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private final RemoteController motorController;
    private final RemoteController lightController;
    private LegoWriterQueue writerQueue;
    private int currentSpeed;
    private int currentColor;
    private long lastTime;    // Color sensor should switch at most once per second
    private TrainHub.PortType portA = TrainHub.PortType.None;
    private TrainHub.PortType portB = TrainHub.PortType.None;

    enum PortType {
        None,
        Motor,
        Light
    }

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public RemoteController getLightController() {
        return lightController;
    }

    public RemoteController getMotorController() {
        return motorController;
    }

    public TrainBase(String name) {
        setInitialName(name);

        bluetoothDevice = null;
        bluetoothGatt = null;
        motorController = new RemoteController.BaseMotorController(this);
        lightController = new RemoteController.BaseLightController(this);
    }

    public TrainBase(BluetoothDevice device) {
        setName(device.getName());

        BluetoothGattCallback callback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                switch (newState) {
                    case BluetoothGatt.STATE_DISCONNECTED:
                        setIsConnected(false);
                        break;
                    case BluetoothGatt.STATE_CONNECTED:
                        setIsConnected(true);

                        // It seems to be more stable to wait a little bit for the discovery.
                        // Discover services and characteristics for this device
                        new Handler(Looper.getMainLooper()).postDelayed(Objects.requireNonNull(bluetoothGatt)::discoverServices, 500);
                        initializeService();
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                initializeService();
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    writerQueue.confirmWrite();
                }, 100);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] value = LegoHelper.envelopeToData(characteristic.getValue());

                //Log.i("onCharacteristicChanged", "Bytes=\t" + HexUtils.byteToHexString(value));
                if (value[0] == 0x01) {
                    parseDeviceInfo(value);
                } else if (value[0] == 0x04) {
                    parsePortInfo(value);
                } else if ((value[0] == (byte)0x45) && (value[1] == (byte)0x12)) {
                    parseColorInfo(value);
                } else if ((value[0] == (byte)0x45) && (value[1] == (byte)0x13)) {
                    Log.i("Duplo", "parseSpeedInfo(value)"+ "\t" + HexUtils.byteToHexString(value) );
                    parseSpeedInfo(value);
                }
            }

            private void parsePortInfo(byte[] value) {
                // 0: 0x01
                // 1: Port Index
                // 2: Port Mode (PlugIn: 0, PlugOut: 1)
                // 3: Device Type: (Motor: 2, Light: 8)
                TrainHub.PortType port = TrainHub.PortType.None;

                if (value[2] != 0 && value.length > 3) {
                    if (value[3] == 0x02) {
                        port = TrainHub.PortType.Motor;   // Train Motor 88002
                    } else if (value[3] == 0x2E) {
                        port = TrainHub.PortType.Motor;   // Technic Großer Motor 88013
                    } else if (value[3] == 0x08) {
                        port = TrainHub.PortType.Light;   // LED Licht 88005
                    }
                }

                if (value[1] == 0) {
                    portA = port;
                } else if (value[1] == 0x01) {
                    portB = port;
                }
            }

            private void parseDeviceInfo(byte[] value) {
                // 0: 0x01
                // 1: Property Type
                // 2: -
                // 3: Value
                if (value[1] == 0x06) {
                    setBattery(value[3]);
                }
            }

            private void parseColorInfo(byte[] value) {
                Log.i("Duplo", HexUtils.byteToHexString(value) + "\t" + (System.currentTimeMillis() - lastTime));
                //  0 "black", 1 "pink", 2 "purple", 3 "blue", 4 "lightblue", 5 "cyan", 6 "green", 7 "yellow", 8 "orange", 9 "red", 10 "white", "none"

                if (System.currentTimeMillis() < (lastTime + 1000))  {    /* Sonst gibt es von einer Farbleiste gleich mehrere Ereignisse */
                    return;
                };

                if (value[2] == 0x03) {                               /* blau 0x03 = Water Refill */
                    lastTime = System.currentTimeMillis();
                    setMessage("Blue (0x03)");
                    playSound((byte)0x07);  // Water_Refill
                    // int lastSpeed = currentSpeed;  currentSpeed = 0;  updateSpeed();
                    // currentSpeed = lastSpeed; updateSpeed();
                }

                if (value[2] == 0x06) {                          /* Green 0x06 = Richung umkehren */
                    lastTime = System.currentTimeMillis();
                    setMessage("Green (0x06)");
                    playTone((byte)0x09);
                    int lastSpeed = currentSpeed;               // Geschwindigkeit zwischenspeichern
                    currentSpeed = 0; updateSpeed();            // erst anhalten
                    currentSpeed = -lastSpeed; updateSpeed();   // dann Minus lastSpeed = Umkehren
                }

                if (value[2] == 0x09) {                           /* Red 0x09 = Motor Stop */
                    lastTime = System.currentTimeMillis();
                    setMessage("Red (0x09)");
                    playTone((byte)0x03);
                    /* Funktioniert nicht, da ständig falsche Stops auftreten */
                    // time = System.currentTimeMillis(); currentSpeed = 0; updateSpeed();
                }

                if (value[2] == 0x0A) {                           /* White 10 = LED-Farbe ändern */
                    lastTime = System.currentTimeMillis();
                    setMessage("White (0x0A)");
                    playTone((byte)0x05);
                    setLedColorHub();
                }

            }

            private void parseSpeedInfo(byte[] value) {
                //Log.i("Duplo", HexUtils.byteToHexString(value) + "\t" + (System.currentTimeMillis() - time));
                if (System.currentTimeMillis() < (lastTime + 1000))  {    /* motorStop() kann sonst nicht anhalten */
                    return;
                };
                lastTime = System.currentTimeMillis();

                if ((currentSpeed == 0) && (value[2] > 10)) {
                    if  (value[3] == 0) {
                        currentSpeed = 50;
                    } else {
                        currentSpeed = -50;     // value[3] == FF
                    }
                    updateSpeed();
                }
                // Train wurde per Hand angehalten, Motor muss dann ausgeschaltet werden
                if (value[2] == 0) {
                    currentSpeed = 0;
                    updateSpeed();
                }
            }

        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(null, true, callback);
        motorController = new RemoteController.BaseMotorController(this);
        lightController = new RemoteController.BaseLightController(this);
    }

    @Override
    public void disconnect() {
        send(new byte[]{0x02, 0x01});
        bluetoothGatt.close();
        // if (bluetoothGatt != null) {  bluetoothGatt.disconnect();    }
    }

    @Override
    public void switchOff() {
        send(new byte[]{0x01, 0x01});
        bluetoothGatt.close();
        // if (bluetoothGatt != null) {  bluetoothGatt.disconnect();    }
    }

    public void setLedColorHub() {
        currentColor++;

        if (currentColor == 11) {
            currentColor = 0;
        }
        send(new byte[]{(byte) 0x81, 0x11, 0x11, 0x51, 0x00, (byte) currentColor});
        /* BLACK= 0, PINK= 1, PURPLE= 2, BLUE= 3, LIGHTBLUE= 4, CYAN= 5, GREEN= 6, YELLOW= 7, ORANGE= 8, RED= 9, WHITE= 10, NUM_COLORS, NONE= 255 */

    }

    public void sound1() {
        playSound((byte)0x09);    // Horn
    }

    public void sound2() {
        playSound((byte)0x0A);    // Steam
    }

    public void sound3() {
        playSound((byte)0x07);    // Water_Refill
    }

    public void sound4() {
        playSound((byte)0x05);    // Station_Departure
    }

    public void motorStop() {
        lastTime = System.currentTimeMillis();  // Speedometer würde sonst beim verzögern auf 0 sofort wieder durchstarten
        playSound((byte) 0x03); // PlaySound Brake
        currentSpeed = 0;
        updateSpeed();
    }

    public void motorSlower() {
        if (currentSpeed > -80) {
            currentSpeed -= 20;
        }
        Log.i("Speed", "currentSpeed=" + currentSpeed);
        updateSpeed();
    }

    public void motorFaster() {
        if (currentSpeed < 80) {
            currentSpeed += 20;
        }
        Log.i("Speed", "currentSpeed=" + currentSpeed);
        updateSpeed();
    }

    private void updateSpeed() {
        byte speed = LegoHelper.mapSpeed(currentSpeed);
        send(new byte[]{(byte) 0x81, 0x00, 0x11, 0x51, 0x00, speed});
    }

    public void rename(String name) {
        setName(name);
        send(LegoHelper.createRenameRequest(name));
    }

    private void send(byte[] data) {
        initializeService();

        if (writerQueue == null) {
            return;
        }

        writerQueue.write(LegoHelper.dataToEnvelope(data));
    }

    public static boolean canConnect(ScanResult scanResult) {
        ScanRecord record = scanResult.getScanRecord();

        if (record.getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid : record.getServiceUuids()) {
            if (uuid.getUuid().equals(ServiceUUID)) {
                byte[] data = record.getManufacturerSpecificData(0x397);

                if (data != null && data[1] == 32) { // 001 00000 = 32
                    return true;
                }
            }
        }

        return false;
    }

    private void initializeService() {
        if (writerQueue != null) {
            return;
        }

        if (bluetoothGatt == null) {
            return;
        }

        BluetoothGattService service = bluetoothGatt.getService(ServiceUUID);

        if (service == null) {
            return;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CharacteristicsUUID);

        if (characteristic == null) {
            return;
        }

        writerQueue = new LegoWriterQueue(bluetoothGatt, characteristic);
        LegoHelper.enableNotifications(bluetoothGatt, characteristic);

        // It seems more stable to wait a little bit, because the first writes usually fail.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Activate battery reports.
            send(new byte[]{0x01, 0x06, 0x02});

            // Activate port reports.
            // try { Thread.sleep(1000);   } catch (InterruptedException e) {   throw new RuntimeException(e);    }
            send(new byte[]{0x03, 0x00, 0x04});

            // Activate ColorSensor (0x12)
            // try { Thread.sleep(1000);   } catch (InterruptedException e) {   throw new RuntimeException(e);    }
            send(new byte[] {0x41, 0x12, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01});

            // Activate Speedometer (0x13)
            // try { Thread.sleep(1000);   } catch (InterruptedException e) {   throw new RuntimeException(e);    }
            send(new byte[] {0x41, 0x13, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01});

        }, 2000);

    }

    public void playTone(byte tone) {
        send(new byte[]{(byte) 0x41, 0x01, 0x02, 0x01, 0x00, 0x00, 0x00, 0x01});  // setToneMode
        // try {  Thread.sleep(100);   } catch (InterruptedException e) {  throw new RuntimeException(e);     }
        send(new byte[]{(byte) 0x81, 0x01, 0x11, 0x51, 0x02, tone });              // playTone
    }

    public void playSound(byte sound) {
        // BRAKE= 3, STATION_DEPARTURE=  5, WATER_REFILL= 7, HORN= 9, STEAM= 10
        send(new byte[]{(byte) 0x41, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01});  // setSoundMode
        // try {  Thread.sleep(100);   } catch (InterruptedException e) {  throw new RuntimeException(e);     }
        send(new byte[]{(byte) 0x81, 0x01, 0x11, 0x51, 0x01, sound });              // playSound
    }

}
