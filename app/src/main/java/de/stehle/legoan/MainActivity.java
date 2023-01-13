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
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 10000;
    private final List<Device> devices = new ArrayList<>();
    private DeviceListAdapter devicesAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private Button scanningStartButton;
    private Button scanningStopButton;
    private boolean isScanning = false;
    private TrainHub deviceInContextMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanningStartButton = findViewById(R.id.StartScanButton);
        scanningStartButton.setOnClickListener(v -> startScanning());

        scanningStopButton = findViewById(R.id.StopScanButton);
        scanningStopButton.setVisibility(View.GONE);
        scanningStopButton.setOnClickListener(v -> stopScanning());

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        ListView devicesListView = findViewById(R.id.DevicesListView);
        devicesAdapter = new DeviceListAdapter(devices, this);
        devicesListView.setAdapter(devicesAdapter);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() == null) {
                return;
            }

            if (!TrainHub.canConnect(result)) {
                return;
            }

            String address = result.getDevice().getAddress();

            for (Device device : devices) {
                if (device.getAddress().equals(address)) {
                    return;
                }
            }

            TrainHub trainHub = new TrainHub(bluetoothAdapter.getRemoteDevice(address), MainActivity.this);

            devices.add(trainHub);
            devicesAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(R.string.menu_header);
        menu.add(0, v.getId(), 0, R.string.menu_disconnect);

        // Set the current hub from the view.
        deviceInContextMenu = (TrainHub) v.getTag();
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
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN) || !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }, 1);
            }
            return;
        }

        if (isScanning) {
            return;
        }

        isScanning = true;
        scanningStartButton.setVisibility(View.GONE);
        scanningStopButton.setVisibility(View.VISIBLE);

        AsyncTask.execute(() -> bluetoothScanner.startScan(scanCallback));

        // Stop the scanning automatically after some time.
        new Handler().postDelayed(this::stopScanning, SCAN_PERIOD);
    }

    public void stopScanning() {
        if (!isScanning) {
            return;
        }

        isScanning = false;
        scanningStartButton.setVisibility(View.VISIBLE);
        scanningStopButton.setVisibility(View.GONE);

        AsyncTask.execute(() -> bluetoothScanner.stopScan(scanCallback));
    }

    private boolean hasPermission(String id) {
        return ActivityCompat.checkSelfPermission(this, id) == PackageManager.PERMISSION_GRANTED;
    }
}