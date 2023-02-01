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

import java.util.UUID;

import de.stehle.legoan.utils.LegoHelper;
import de.stehle.legoan.utils.LegoWriterQueue;

@SuppressLint("MissingPermission")
public class Remote extends Device {
    private final static UUID ServiceUUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123");
    private final static UUID CharacteristicsUUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123");
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private LegoWriterQueue writerQueue;
    private RemoteController controllerA = RemoteController.noop();
    private RemoteController controllerB = RemoteController.noop();

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public RemoteController getControllerA() {
        return controllerA;
    }

    public RemoteController getControllerB() {
        return controllerA;
    }

    public void setControllerA(RemoteController controllerA) {
        this.controllerA = controllerA != null ? controllerA : RemoteController.noop();
    }

    public void setControllerB(RemoteController controllerB) {
        this.controllerB = controllerB != null ? controllerB : RemoteController.noop();
    }

    public Remote(String name) {
        setInitialName(name);

        bluetoothDevice = null;
        bluetoothGatt = null;
    }

    public Remote(BluetoothDevice device) {
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
                        new Handler(Looper.getMainLooper()).postDelayed(bluetoothGatt::discoverServices, 500);
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
                } else if (value[0] == 0x45) {
                    parseButtons(value);
                }
            }

            private void parseButtons(byte[] value) {
                // 0: 0x45
                // 1: Button Side (0: A, 1: B)
                // 2: Button Mode (-1: Down, 1: Up, 127: Red
                byte buttonSide = value[1];
                byte buttonMode = value[2];

                if (buttonSide == 0) {
                    if (buttonMode == 1) {
                        controllerA.up();
                    } else if (buttonMode == -1) {
                        controllerA.down();
                    } else if (buttonMode == 127) {
                        controllerA.middle();
                    }
                } else {
                    if (buttonMode == 1) {
                        controllerB.up();
                    } else if (buttonMode == -1) {
                        controllerB.down();
                    } else if (buttonMode == 127) {
                        controllerB.middle();
                    }
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
    }

    @Override
    public void disconnect() {
        // Switch Off Hub  (Hub Actions = 0x02)
        send(new byte[]{0x02, 0x01});
        bluetoothGatt.close();
        //bluetoothGatt.disconnect();
    }

    public static boolean canConnect(ScanResult scanResult) {
        ScanRecord record = scanResult.getScanRecord();

        if (record.getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid : record.getServiceUuids()) {
            if (uuid.getUuid().equals(ServiceUUID)) {
                byte[] data = record.getManufacturerSpecificData(0x397);

                if (data != null && data[1] == 66) { // 010 00010
                    return true;
                }
            }
        }

        return false;
    }

    private void send(byte[] data) {
        initializeService();

        if (writerQueue == null) {
            return;
        }

        writerQueue.write(LegoHelper.dataToEnvelope(data));
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
            // Activate button reports
            send(new byte[]{0x41, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1});
            send(new byte[]{0x41, 0x1, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1});

            // Activate battery reports
            send(new byte[]{0x01, 0x06, 0x02});
        }, 2000);
    }
}
