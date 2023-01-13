package de.stehle.legoan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class TrainHub extends BluetoothGattCallback {
    private final static byte[] speeds = {0x7E, 0x6C, 0x5A, 0x48, 0x36, 0x24, 0x12, 0x7F, (byte) 0xEC, (byte) 0xDA, (byte) 0xC8, (byte) 0xB6, (byte) 0xA4, (byte) 0x92, (byte) 0x80};
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic devicesCharacteristic;
    private final static int stopSpeed = speeds.length / 2;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private int currentSpeed = stopSpeed;
    private int currentColor;
    private int battery;
    private boolean isConnected;

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void setIsConnected(boolean value) {
        if (isConnected != value) {
            isConnected = value;
            notifyChanged();
        }
    }

    public int getBattery() {
        return battery;
    }

    private void setBattery(int value) {
        if (battery != value) {
            battery = value;
            notifyChanged();
        }
    }

    public TrainHub(BluetoothDevice bluetoothDevice, Context context) {
        this.bluetoothDevice = bluetoothDevice;
        bluetoothGatt = bluetoothDevice.connectGatt(context, true, this);
    }

    public void disconnect() {
        bluetoothGatt.disconnect();
    }

    public void subscribe(ChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyChanged() {
        for (ChangeListener listener : listeners) {
            listener.notifyChanged();
        }
    }

    public void nextLedColor() {
        currentColor++;

        if (currentColor == 11) {
            currentColor = 0;
        }

        send(new byte[]{(byte) 0x81, 0x32, 0x11, 0x51, 0x00, (byte)currentColor}); // Set color
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
        send(new byte[]{(byte) 0x81, 0x00, 0x11, 0x51, 0x00, speeds[speed]}); // Port A
    }

    public static boolean canConnect(ScanResult scanResult) {
        UUID requiredService = UUID.fromString("00001623-1212-efde-1623-785feabcd123");

        if (scanResult.getScanRecord().getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid: scanResult.getScanRecord().getServiceUuids()) {
            if (uuid.getUuid().equals(requiredService)) {
                return true;
            }
        }

        return false;
    }

    private void send(byte[] data) {
        initializeService();

        if (devicesCharacteristic == null) {
            return;
        }

        devicesCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        devicesCharacteristic.setValue(dataToEnvelope(data));

        bluetoothGatt.writeCharacteristic(devicesCharacteristic);
    }

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

    private void initializeService() {
        if (devicesCharacteristic != null) {
            return;
        }

        if (service == null) {
            service = bluetoothGatt.getService(UUID.fromString("00001623-1212-efde-1623-785feabcd123"));
        }

        if (service == null) {
            return;
        }

        if (devicesCharacteristic == null) {
            devicesCharacteristic = service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));
        }

        if (devicesCharacteristic == null) {
            return;
        }

        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        // Use a special descriptor to enable notifications.
        BluetoothGattDescriptor bluetoothDescriptor = devicesCharacteristic.getDescriptor(uuid);
        bluetoothDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        bluetoothGatt.setCharacteristicNotification(devicesCharacteristic, true);
        bluetoothGatt.writeDescriptor(bluetoothDescriptor);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Activate battery reports
            send(new byte[] { 0x01, 0x06, 0x02 });
        }, 2000);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = envelopeToData(characteristic.getValue());

        if (value[0] == 0x01) {
            parseDeviceInfo(value);
        }
    }

    private void parseDeviceInfo(byte[] value) {
        if (value[1] == 0x06) {
            setBattery(value[3]);
        }
    }

    private byte[] dataToEnvelope(byte[] data) {
        byte[] envelope = new byte[data.length + 2];

        // The first value must be the length.
        envelope[0] = (byte)(data.length + 2);
        envelope[1] = 0;

        // Copy the rest of the value.
        System.arraycopy(data, 0, envelope, 2, data.length);
        return envelope;
    }

    private  byte[] envelopeToData(byte[] envelope) {
        byte[] data = new byte[envelope.length - 2];

        System.arraycopy(envelope, 2, data, 0, envelope.length - 2);
        return data;
    }
}
