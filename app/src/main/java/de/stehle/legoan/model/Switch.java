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

import java.util.Objects;
import java.util.UUID;

import de.stehle.legoan.utils.LegoWriterQueue;

@SuppressLint("MissingPermission")
public class Switch extends Device {
    private final static UUID ServiceUUID = UUID.fromString("196988b3-b878-4b5b-a4cc-2e3eb64c1e00");
    private final static UUID CharacteristicsUUID = UUID.fromString("196988b4-b878-4b5b-a4cc-2e3eb64c1e00");
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private LegoWriterQueue writerQueue;

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public Switch(String name) {
        setInitialName(name);

        bluetoothDevice = null;
        bluetoothGatt = null;
    }

    public Switch(BluetoothDevice device) {
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
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(null, true, callback);
    }

    @Override
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public void toggle() {
        send(new byte[]{(byte) 0x42});
    }

    public static boolean canConnect(ScanResult scanResult) {
        ScanRecord record = scanResult.getScanRecord();

        if (record.getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid : record.getServiceUuids()) {
            if (uuid.getUuid().equals(ServiceUUID)) {
                return true;
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

        writerQueue = new LegoWriterQueue(bluetoothGatt, characteristic);
    }
}
