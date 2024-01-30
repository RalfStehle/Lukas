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
public class TrainHub extends Device {
    private final static UUID ServiceUUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123");
    private final static UUID CharacteristicsUUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123");

    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private final RemoteController motorController;
    private final RemoteController lightController;
    private LegoWriterQueue writerQueue;
    private int currentSpeed;
    private int currentColor;
    private int currentBrightness;
    private long lastTime;  // Color sensor should not trigger a task more than once per second
    private PortType portA = PortType.None;
    private PortType portB = PortType.None;

    enum PortType {
        None,
        Motor,
        Light,
        ColorSensor
    }

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public RemoteController getLightController() {
        return lightController;
    }

    public RemoteController getMotorController() {
        return motorController;
    }

    public TrainHub(String name) {
        setInitialName(name);

        bluetoothDevice = null;
        bluetoothGatt = null;
        motorController = new RemoteController.MotorController(this);
        lightController = new RemoteController.LightController(this);
    }

    public TrainHub(BluetoothDevice device) {
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
                writerQueue.confirmWrite();
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] value = LegoHelper.envelopeToData(characteristic.getValue());
                Log.i("TrainHub+onCharacChange", "Bytes=\t" + HexUtils.byteToHexString(value));

                if (value[0] == 0x01) {
                    parseDeviceInfo(value);
                } else if (value[0] == 0x04) {
                    parsePortInfo(value);
                } else if (value[0] == 0x45) {
                    parseColor(value);
                }
            }

            private void parsePortInfo(byte[] value) {
                // 0: 0x01
                // 1: Port Index
                // 2: Port Mode (PlugIn: 0, PlugOut: 1)
                // 3: Device Type: (Motor: 2, Light: 8)
                PortType port = PortType.None;

                if (value[2] != 0 && value.length > 3) {
                    Log.i("TrainHub+PortType", "Bytes=\t" + HexUtils.byteToHexString(value));
                    if (value[3] == 0x02) {
                        port = PortType.Motor;         // Train Motor 88002
                    } else if (value[3] == 0x2E) {
                        port = PortType.Motor;         // Technic Großer Motor 88013
                    } else if (value[3] == 0x08) {
                        port = PortType.Light;         // LED Licht 88005
                    } else if (value[3] == 0x25) {
                        port = PortType.ColorSensor;   // Farb- & Abstandssensor 88007

                        // Activate ColorSensor, value[1] = Port: 0x00 = A, 0x01 = B, value[2] = color, value[3] = distance, value[5] = nearfield-distance
                        send(new byte[] {0x41, value[1], 0x00, 0x01, 0x00, 0x00, 0x00, 0x01});      // 2024-01-17 Ralf
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

            private void parseColor(byte[] value) {                                 // 2024-01-17 Ralf
                // um die Werte ins textView zu schreiben, benötigt man: MutableLiveData<String> message, setMessage, getMessage()

                if (System.currentTimeMillis() < (lastTime + 1000))  {    /* Sonst gibt es von einer Farbleiste gleich mehrere Ereignisse */
                    return;
                };

                if (value[2] == 0x03) {
                   // do something e.q. motorStop()
                }

                switch (value[2]) {
                    case 0x00:
                        setMessage("");  // Black
                        break;
                    case 0x03:
                        setMessage("Blue (" + Byte.toString(value[2]) + ")");
                        lastTime = System.currentTimeMillis();
                        break;
                    case 0x05:
                        setMessage("Green (" + Byte.toString(value[2]) + ")");
                        lastTime = System.currentTimeMillis();
                        break;
                    case 0x07:
                        setMessage("Yellow (" + Byte.toString(value[2]) + ")");
                        lastTime = System.currentTimeMillis();
                        break;
                    case 0x09:
                        setMessage("Red (" + Byte.toString(value[2]) + ")");
                        lastTime = System.currentTimeMillis();
                        break;
                    case 0x0A:
                        setMessage("White (" + Byte.toString(value[2]) + ")");
                        lastTime = System.currentTimeMillis();
                        break;
                    case (byte)255: setMessage("");   break;       // Sensor sendet FF=255, wenn er keine Farbe erkannt hat
                    //default:  setMessage("");   break;
                }

                Log.i("TrainHub+ColorSensor", "Bytes=\t" + HexUtils.byteToHexString(value));
                float distance = value[3];

                if (value[2] == 0x03) {
                    motorStop();
                }

            }
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(null, true, callback);
        motorController = new RemoteController.MotorController(this);
        lightController = new RemoteController.LightController(this);
    }

    @Override
    public void disconnect() {
        send(new byte[]{0x02, 0x02});
        bluetoothGatt.close();
        // if (bluetoothGatt != null) {  bluetoothGatt.disconnect();    }
    }

    @Override
    public void switchOff() {
        send(new byte[]{0x02, 0x01});
        bluetoothGatt.close();
        // if (bluetoothGatt != null) {  bluetoothGatt.disconnect();    }
    }

    public void setLedColorHub() {
        currentColor++;

        if (currentColor == 11) {
            currentColor = 0;
        }

        send(new byte[]{(byte) 0x81, 0x32, 0x11, 0x51, 0x00, (byte) currentColor});
    }

    public void lightDarker() {
        if (currentBrightness > -100) {
            currentBrightness -= 25;
        }

        updateBrightness();
    }

    public void lightBrighter() {
        if (currentBrightness < 100) {
            currentBrightness += 25;
        }

        updateBrightness();
    }

    public void lightOff() {
        currentBrightness = 0;

        updateBrightness();
    }

    private void updateBrightness() {
        byte brightness = (byte) LegoHelper.mapBrightness(currentBrightness);

        if (portA == PortType.Light) {
            send(new byte[]{(byte) 0x81, 0x00, 0x11, 0x51, 0x00, brightness});
        }

        if (portB == PortType.Light) {
            send(new byte[]{(byte) 0x81, 0x01, 0x11, 0x51, 0x00, brightness});
        }
    }

    public void motorStop() {
        currentSpeed = 0;

        updateSpeed();
    }

    public void motorSlower() {
        if (currentSpeed > -100) {
            currentSpeed -= 25;
        }

        updateSpeed();
    }

    public void motorFaster() {
        if (currentSpeed < 100) {
            currentSpeed += 25;
        }

        updateSpeed();
    }

    private void updateSpeed() {
        byte speed = LegoHelper.mapSpeed(currentSpeed);

        if (portA == PortType.Motor) {
            send(new byte[]{(byte) 0x81, 0x00, 0x11, 0x51, 0x00, speed});
        }

        if (portB == PortType.Motor) {
            send(new byte[]{(byte) 0x81, 0x01, 0x11, 0x51, 0x00, speed});
        }
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

                if (data != null && data[1] == 65) { // 010 00001 = 65
                    return true;
                }
            }
        }

        return false;
    }

    private void initializeService() {
        if (writerQueue != null) {   return;   }

        if (bluetoothGatt == null) {   return;   }

        BluetoothGattService service = bluetoothGatt.getService(ServiceUUID);

        if (service == null) {   return;   }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CharacteristicsUUID);

        if (characteristic == null) {   return;   }

        writerQueue = new LegoWriterQueue(bluetoothGatt, characteristic);
        LegoHelper.enableNotifications(bluetoothGatt, characteristic);

        // It seems more stable to wait a little bit, because the first writes usually fail.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Activate battery reports.
            send(new byte[]{0x01, 0x06, 0x02});

            // Activate port reports.
            send(new byte[]{0x03, 0x00, 0x04});

        }, 2000);
    }
}
