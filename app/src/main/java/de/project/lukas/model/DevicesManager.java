package de.project.lukas.model;

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
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressLint("MissingPermission")
public final class DevicesManager extends ViewModel {
    private static final DevicesManager instance = new DevicesManager();
    private final MutableLiveData<List<Device>> devices = new MutableLiveData<>(new ArrayList<>());   // verbundene Devices
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
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
                } else if (TrainBase.canConnect(result)) {
                    addDevice(new TrainBase(device));
                } else if (Remote.canConnect(result)) {
                    addDevice(new Remote(device));
                } else if (Switch.canConnect(result)) {
                    addDevice((new Switch(device)));
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

    public boolean isTesting() {   return false;    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("DefaultLocale")
    private DevicesManager() {
        if (isTesting()) {
            for (int i = 0; i < 6; i++) {
                addDevice(new Switch(String.format("Switch #%d", i)));
                addDevice(new Remote(String.format("Remote #%d", i)));
                addDevice(new TrainHub(String.format("Train Hub #%d", i)));
                addDevice(new TrainBase(String.format("Train Base #%d", i)));
            }
        }
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        this.bluetoothManager = bluetoothManager;
    }

    public void startScanning() {
        if (Boolean.TRUE.equals(isScanning.getValue())) {
            return;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return;
        }

        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothScanner == null) {
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

    public void addDevice(Device deviceToAdd) {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        deviceList.add(deviceToAdd);
        deviceToAdd.getName().observeForever(v -> sortDevices());

        deviceToAdd.getStatus().observeForever(isConnected -> {
            if (!isConnected) {
                removeDevice(deviceToAdd);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deviceList.sort(Comparator.comparing((Device t) -> t.getClass().getName(), Comparator.reverseOrder()).thenComparing(Device::toString));
        }

        devices.postValue(deviceList);
    }

    public void removeDevice(Device device) {
        device.disconnect();

        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        if (!deviceList.remove(device)) {
            return;
        }

        devices.postValue(deviceList);
    }

    public void switchOffDevice(Device device) {
        device.switchOff();

        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        if (!deviceList.remove(device)) {
            return;
        }

        devices.postValue(deviceList);
    }

    private void sortDevices() {
        List<Device> deviceList = devices.getValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && deviceList != null) {
            deviceList.sort(Comparator.comparing((Device t) -> t.getClass().getName(), Comparator.reverseOrder()).thenComparing(Device::toString));
        }

        devices.postValue(deviceList);
    }

    public void AllTrainMotorStop() {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        for (Device device : deviceList) {
            if (device instanceof TrainHub) {
                ((TrainHub) device).motorStop();
            }
            if (device instanceof TrainBase) {
                ((TrainBase) device).motorStop();
            }
        }
    }

    @SuppressWarnings("unused")
    public void disconnectAll() {
        List<Device> deviceList = Objects.requireNonNull(devices.getValue());

        for (Device device : deviceList) {
            device.disconnect();
        }
    }
}
