package de.stehle.legoan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.stehle.legoan.databinding.ActivityMainBinding;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 10000;
    private final List<Device> devices = new ArrayList<>();
    private DeviceListAdapter devicesAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private boolean isScanning = false;
    private Device deviceInContextMenu;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.StartScanButton.setOnClickListener(v -> startScanning());
        binding.StopScanButton.setVisibility(View.GONE);
        binding.StopScanButton.setOnClickListener(v -> stopScanning());

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        devicesAdapter = new DeviceListAdapter(devices, getSupportFragmentManager());

        binding.DevicesListView.setAdapter(devicesAdapter);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() == null) {
                return;
            }

            if (TrainHub.canConnect(result)) {
                if (!hasDevice(result.getDevice().getAddress())) {
                    addDevice(new TrainHub(result.getDevice(), MainActivity.this));
                }
            } else if (Remote.canConnect(result)) {
                if (!hasDevice(result.getDevice().getAddress())) {
                    addDevice(new Remote(result.getDevice(), MainActivity.this));
                }
            }
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(R.string.menu_header);
        menu.add(0, v.getId(), 0, R.string.menu_disconnect);

        // Set the current hub from the view.
        deviceInContextMenu = DeviceFragment.getDevice(v);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (deviceInContextMenu == null) {
            return true;
        }

        // We only have one option yet, so we can just call disconnect directly.
        devices.remove(deviceInContextMenu);
        devicesAdapter.notifyDataSetChanged();
        deviceInContextMenu.disconnect();
        deviceInContextMenu = null;
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 1) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_warning_title);
        builder.setMessage(R.string.permission_warning_text);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    public void startScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN) || !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, 1);
                return;
            }
        } else {
            if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        }, 1);
                return;
            }
        }

        if (isScanning || bluetoothScanner == null) {
            return;
        }

        isScanning = true;
        binding.StartScanButton.setVisibility(View.GONE);
        binding.StopScanButton.setVisibility(View.VISIBLE);

        AsyncTask.execute(() -> bluetoothScanner.startScan(scanCallback));

        // Stop the scanning automatically after some time.
        new Handler().postDelayed(this::stopScanning, SCAN_PERIOD);
    }

    public void stopScanning() {
        if (!isScanning) {
            return;
        }

        isScanning = false;
        binding.StartScanButton.setVisibility(View.VISIBLE);
        binding.StopScanButton.setVisibility(View.GONE);

        AsyncTask.execute(() -> bluetoothScanner.stopScan(scanCallback));
    }

    private boolean hasDevice(String address) {
        for (Device device : devices) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    private void addDevice(Device device)  {
        devices.add(device);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            devices.sort(Comparator.comparing((Device t) -> t.getClass().getName()).thenComparing(Device::getName));
        }

        devicesAdapter.notifyDataSetChanged();
    }

    private boolean hasPermission(String id) {
        return ActivityCompat.checkSelfPermission(this, id) == PackageManager.PERMISSION_GRANTED;
    }
}