package de.stehle.legoan.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressLint("MissingPermission")
public final class DevicesManager extends ViewModel {
    private static final DevicesManager instance = new DevicesManager();
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private final HashMap<Device, Observer<Boolean>> observers = new HashMap<>();
    private BluetoothLeScanner bluetoothScanner;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();

            if (device.getName() == null) {
                return;
            }

            if (!hasDevice(device.getAddress())) {
                if (TrainHub.canConnect(result)) {
                    addDevice(new TrainHub(device));
                } else if (Remote.canConnect(result)) {
                    addDevice(new Remote(device));
                }
            }
        }
    };

    public static DevicesManager getInstance() {
        return instance;
    }

    public LiveData<List<Device>> getDevices() {
        return devices;
    }

    public LiveData<Boolean> isScanning() {
        return isScanning;
    }

    @SuppressLint("DefaultLocale")
    private DevicesManager() {
        boolean testing = false;

        if (testing) {
            for (int i = 0; i < 8; i++) {
                addDevice(new Switch(String.format("Switch %d", i)));
                addDevice(new Remote(String.format("Remote %d", i)));
                addDevice(new TrainHub(String.format("TrainHub %d", i)));
            }
        }
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    public void startScanning() {
        if (Boolean.TRUE.equals(isScanning.getValue())) {
            return;
        }

        isScanning.postValue(true);

        AsyncTask.execute(() -> bluetoothScanner.startScan(scanCallback));

        // Stop the scanning automatically after 20 sec.
        new Handler().postDelayed(this::stopScanning, 20000);
    }

    public void stopScanning() {
        if (Boolean.FALSE.equals(isScanning.getValue())) {
            return;
        }

        isScanning.postValue(false);

        AsyncTask.execute(() -> bluetoothScanner.stopScan(scanCallback));
    }

    private boolean hasDevice(String address) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        for (Device device : deviceList) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    private void addDevice(Device device) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        Observer<Boolean> connectedObserver = connected -> {
            if (!connected) {
                removeDevice(devices, device);
            }
        };

        deviceList.add(device);
        device.getIsConnected().observeForever(connectedObserver);

        observers.put(device, connectedObserver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deviceList.sort(Comparator.comparing((Device t) -> t.getClass().getName(), Comparator.reverseOrder()).thenComparing(Device::getName));
        }

        devices.postValue(deviceList);
    }

    public void removeDevice(Device device) {
        Observer<Boolean> connectedObserver = observers.remove(device);

        device.disconnect();
        device.getIsConnected().removeObserver(Objects.requireNonNull(connectedObserver));

        removeDevice(devices, device);
    }

    private void removeDevice(MutableLiveData<List<Device>> devices, Device device) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        if (!deviceList.remove(device)) {
            return;
        }

        devices.postValue(deviceList);
    }
}
