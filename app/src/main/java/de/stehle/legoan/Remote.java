package de.stehle.legoan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

@SuppressLint("MissingPermission")
public class Remote extends Device {
    private final static UUID ServiceUUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123");
    private final static UUID CharacteristicsUUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123");
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic devicesCharacteristic;
    private LogoWriterQueue writerQueue;
    private TrainHub connectedTrain;

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    @Override
    public String getName() {
        return bluetoothDevice.getName();
    }

    public TrainHub getConnectedTrain() {
        return connectedTrain;
    }

    public void setConnectedTrain(TrainHub connectedTrain) {
        this.connectedTrain = connectedTrain;
    }

    public Remote(BluetoothDevice device, Context context) {
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

            private void parseDeviceInfo(byte[] value) {
                if (value[1] == 0x06) {
                    setBattery(value[3]);
                }
            }

            private void parseButtons(byte[] value) {
                byte buttonSide = value[1];
                byte buttonMode = value[2];

                if (buttonSide == 0) {
                    if (buttonMode == 1) {
                        leftUp();
                    } else if (buttonMode == -1) {
                        leftDown();
                    } else if (buttonMode == 127) {
                        leftMiddle();
                    }
                } else {
                    if (buttonMode == 1) {
                        rightUp();
                    } else if (buttonMode == -1) {
                        rightDown();
                    } else if (buttonMode == 127) {
                        rightMiddle();
                    }
                }
            }
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(context, true, callback);
    }

    private void leftUp() {
    }

    private void leftMiddle() {
        if (connectedTrain != null) {
            connectedTrain.nextLedColor();
        }
    }

    private void leftDown() {
    }

    private void rightUp() {
        if (connectedTrain != null) {
            connectedTrain.incrementSpeed();
        }
    }

    private void rightMiddle() {
        if (connectedTrain != null) {
            connectedTrain.stop();
        }
    }

    private void rightDown() {
        if (connectedTrain != null) {
            connectedTrain.decrementSpeed();
        }
    }

    @Override
    public void disconnect() {
        bluetoothGatt.disconnect();
    }

    public static boolean canConnect(ScanResult scanResult) {
        ScanRecord record = scanResult.getScanRecord();

        if (record.getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid: record.getServiceUuids()) {
            if (uuid.getUuid().equals(ServiceUUID)) {
                String name = record.getDeviceName().trim();

                if (name.equals("Handset")) {
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

        writerQueue.write(data);
    }

    private void initializeService() {
        if (devicesCharacteristic != null) {
            return;
        }

        if (service == null) {
            service = bluetoothGatt.getService(ServiceUUID);
        }

        if (service == null) {
            return;
        }

        if (devicesCharacteristic == null) {
            devicesCharacteristic = service.getCharacteristic(CharacteristicsUUID);
        }

        if (devicesCharacteristic == null) {
            return;
        }

        writerQueue = new LogoWriterQueue(bluetoothGatt, devicesCharacteristic);

        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        // Use a special descriptor to enable notifications.
        BluetoothGattDescriptor bluetoothDescriptor = devicesCharacteristic.getDescriptor(uuid);
        bluetoothDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        bluetoothGatt.setCharacteristicNotification(devicesCharacteristic, true);
        bluetoothGatt.writeDescriptor(bluetoothDescriptor);

        // It seems more stable to wait a little bit, because the first writes usually fail.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Activate button reports
            send(new byte[] { 0x41, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1 });
            send(new byte[] { 0x41, 0x1, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1 });

            // Activate battery reports
            send(new byte[] { 0x01, 0x06, 0x02 });
        }, 2000);
    }
}
