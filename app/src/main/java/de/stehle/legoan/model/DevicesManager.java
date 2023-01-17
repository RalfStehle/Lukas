package de.stehle.legoan.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressLint("MissingPermission")
public final class DevicesManager extends ViewModel {
    private static final DevicesManager instance = new DevicesManager();
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Device>> remotes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private BluetoothLeScanner bluetoothScanner;

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() == null) {
                return;
            }

            if (TrainHub.canConnect(result)) {
                if (!hasDevice(devices, result.getDevice().getAddress())) {
                    addDevice(devices, new TrainHub(result.getDevice()));
                }
            } else if (Remote.canConnect(result)) {
                if (!hasDevice(remotes, result.getDevice().getAddress())) {
                    addDevice(remotes, new Remote(result.getDevice()));
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

    public LiveData<List<Device>> getRemotes() {
        return remotes;
    }

    public LiveData<Boolean> isScanning() {
        return isScanning;
    }

    private DevicesManager() {
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @SuppressLint("MissingPermission")
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

    private boolean hasDevice(MutableLiveData<List<Device>> devices, String address) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        for (Device device : deviceList) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    private void addDevice(MutableLiveData<List<Device>> devices, Device device) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        deviceList.add(device);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deviceList.sort(Comparator.comparing((Device t) -> t.getClass().getName()).thenComparing(Device::getName));
        }

        devices.postValue(deviceList);
    }

    public void removeDevice(Device device) {
        device.disconnect();

        removeDevice(devices, device);
        removeDevice(remotes, device);
    }

    private void removeDevice(MutableLiveData<List<Device>> devices, Device device) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        if (!deviceList.remove(device)) {
            return;
        }

        devices.postValue(deviceList);
    }
}
