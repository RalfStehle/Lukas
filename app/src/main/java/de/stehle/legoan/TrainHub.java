package de.stehle.legoan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.util.UUID;

@SuppressLint("MissingPermission")
public class TrainHub extends Device {
    private final static UUID ServiceUUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123");
    private final static UUID CharacteristicsUUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123");
    private final static byte[] speeds = {0x7E, 0x6C, 0x5A, 0x48, 0x36, 0x24, 0x12, 0x7F, (byte) 0xEC, (byte) 0xDA, (byte) 0xC8, (byte) 0xB6, (byte) 0xA4, (byte) 0x92, (byte) 0x80};
    private final static int stopSpeed = speeds.length / 2;
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private LogoWriterQueue writerQueue;
    private int currentSpeed = stopSpeed;
    private int currentColor;

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    @Override
    public String getName() {
        return bluetoothDevice.getName();
    }

    public TrainHub(BluetoothDevice device, Context context) {
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
                }
            }

            private void parseDeviceInfo(byte[] value) {
                if (value[1] == 0x06) {
                    setBattery(value[3]);
                }
            }
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(context, true, callback);
    }

    @Override
    public void disconnect() {
        bluetoothGatt.disconnect();
    }

    public void nextLedColor() {
        currentColor++;

        if (currentColor == 11) {
            currentColor = 0;
        }

        send(new byte[]{(byte) 0x81, 0x32, 0x11, 0x51, 0x00, (byte)currentColor});
    }

    public void stop() {
        currentSpeed = stopSpeed;

        setSpeed(currentSpeed);
    }

    public void decrementSpeed() {
        if (currentSpeed > 0) {
            currentSpeed--;
        }

        setSpeed(currentSpeed);
    }

    public void incrementSpeed() {
        if (currentSpeed < speeds.length - 1) {
            currentSpeed++;
        }

        setSpeed(currentSpeed);
    }

    private void setSpeed(int speed) {
        send(new byte[]{(byte) 0x81, 0x00, 0x11, 0x51, 0x00, speeds[speed]});
    }

    public static boolean canConnect(ScanResult scanResult) {
        ScanRecord record = scanResult.getScanRecord();

        if (record.getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid: record.getServiceUuids()) {
            if (uuid.getUuid().equals(ServiceUUID)) {
                String name = record.getDeviceName().trim();

                if (!name.equals("Handset")) {
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
        if (writerQueue != null) {
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

        writerQueue = new LogoWriterQueue(bluetoothGatt, characteristic);
        writerQueue.enableNotifications();

        // It seems more stable to wait a little bit, because the first writes usually fail.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Activate battery reports
            send(new byte[] { 0x01, 0x06, 0x02 });
        }, 2000);
    }
}
