package de.project.lukas.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.preference.Preference;
import android.provider.Settings;
import android.util.Log;

import java.util.Objects;
import java.util.UUID;

import de.project.lukas.utils.LegoHelper;
import de.project.lukas.utils.LegoWriterQueue;

@SuppressLint("MissingPermission")
public class Switch extends Device {
    private final static UUID ServiceUUID = UUID.fromString("196988b3-b878-4b5b-a4cc-2e3eb64c1e00");
    private final static UUID CharacteristicsUUID = UUID.fromString("196988b4-b878-4b5b-a4cc-2e3eb64c1e00");
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private final RemoteController controller;
    private LegoWriterQueue writerQueue;
    private int servoLow;
    private int servoHigh;

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public int getServoLow() {
        return servoLow;
    }

    public int getServoHigh() {
        return servoHigh;
    }

    public RemoteController getController() {
        return controller;
    }

    public Switch(String name) {
        setInitialName(name);

        bluetoothDevice = null;
        bluetoothGatt = null;
        controller = new RemoteController.SwitchController(this);
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

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] bytes = characteristic.getValue();
                int battery = (bytes[0] & 0xFF)
                        | ((bytes[1] & 0xFF) << 8)
                        | ((bytes[1] & 0xFF) << 16)
                        | ((bytes[1] & 0xFF) << 24);

                setBattery(battery);
            }
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(null, true, callback);
        controller = new RemoteController.SwitchController(this);

        String servoLowKey = String.format("%s_ServoLow", getAddress());
        String servoHighKey = String.format("%s_ServoHigh", getAddress());

        servoLow = GlobalPreferences.preference.getInt(servoLowKey, 0);
        servoHigh = GlobalPreferences.preference.getInt(servoHighKey, 120);
    }

    @Override
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public void toggle1() {
        send(new byte[]{(byte)servoLow});
    }

    public void toggle2() {

        send(new byte[]{(byte)servoHigh});
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

    public void adjustServo(int low, int high) {
        servoLow = low;
        servoHigh = high;

        String servoLowKey = String.format("%s_ServoLow", getAddress());
        String servoHighKey = String.format("%s_ServoHigh", getAddress());

        GlobalPreferences.preference.edit()
                .putInt(servoLowKey, low)
                .putInt(servoHighKey, high)
                .commit();
    }

    public void send(byte[] data) {
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
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
    }
}
