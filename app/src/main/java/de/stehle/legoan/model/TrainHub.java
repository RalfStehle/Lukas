package de.stehle.legoan.model;

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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import de.stehle.legoan.utils.LegoHelper;
import de.stehle.legoan.utils.LegoWriterQueue;

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
    private PortType portA = PortType.None;
    private PortType portB = PortType.None;

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

                if (value[0] == 0x01) {
                    parseDeviceInfo(value);
                } else if (value[0] == 0x04) {
                    parsePortInfo(value);
                }
            }

            private void parsePortInfo(byte[] value) {
                // 0: 0x01
                // 1: Port Index
                // 2: Port Mode (PlugIn: 0, PlugOut: 1)
                // 3: Device Type: (Motor: 2, Light: 8)
                PortType port = PortType.None;

                if (value[2] != 0 && value.length > 3) {
                    if (value[3] == 0x02) {
                        port = PortType.Motor;
                    } else if (value[3] == 0x08) {
                        port = PortType.Light;
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
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(null, true, callback);
        motorController = new RemoteController.MotorController(this);
        lightController = new RemoteController.LightController(this);
    }

    @Override
    public void disconnect() {
        send(new byte[]{0x02, 0x01});
        bluetoothGatt.close();
        // if (bluetoothGatt != null) {  bluetoothGatt.disconnect();    }
    }

    public void ledRandom() {
        currentColor++;

        if (currentColor == 11) {
            currentColor = 0;
        }

        send(new byte[]{(byte) 0x81, 0x32, 0x11, 0x51, 0x00, (byte) currentColor});
    }

    public void lightDarker() {
        if (currentBrightness > 0) {
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
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] namePayload = new byte[nameBytes.length + 3];

        System.arraycopy(nameBytes, 0, namePayload, 3, nameBytes.length);
        namePayload[0] = 0x01;
        namePayload[1] = 0x01; // Operation: Set
        namePayload[2] = 0x01; // Property: Name.

        setName(name);

        send(namePayload);
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

                if (data != null && data[1] == 65) { // 010 00001
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
            send(new byte[]{0x03, 0x00, 0x04});
        }, 2000);
    }
}
