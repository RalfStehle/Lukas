package de.stehle.legoan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import de.stehle.legoan.databinding.ActivityMainBinding;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private Device deviceInContextMenu;
    private ActivityMainBinding binding;
    private int wasScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ViewPager.setAdapter(new SectionsPagerAdapter(this, getSupportFragmentManager()));
        binding.Tabs.setupWithViewPager(binding.ViewPager);

        binding.ScanStartButton.setOnClickListener(this::startScanning);
        binding.ScanStopButton.setOnClickListener(v -> devicesManager.stopScanning());

        binding.ScanStartButtonTop.setOnClickListener(this::startScanning);
        binding.ScanStopButtonTop.setOnClickListener(v -> devicesManager.stopScanning());

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        devicesManager.setBluetoothManager(bluetoothManager);
        devicesManager.isScanning().observe(this, isScanning -> {
            wasScanning++;

            if (wasScanning >= 3) {
                binding.ScanStartButton.setVisibility(View.GONE);
                binding.ScanStopButton.setVisibility(View.GONE);
            } else if (isScanning) {
                binding.ScanStartButton.setVisibility(View.GONE);
                binding.ScanStopButton.setVisibility(View.VISIBLE);
            } else {
                binding.ScanStartButton.setVisibility(View.VISIBLE);
                binding.ScanStopButton.setVisibility(View.GONE);
            }

            if (isScanning) {
                binding.ScanStartButtonTop.setVisibility(View.GONE);
                binding.ScanStopButtonTop.setVisibility(View.VISIBLE);
            } else {
                binding.ScanStartButtonTop.setVisibility(View.VISIBLE);
                binding.ScanStopButtonTop.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

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
        devicesManager.removeDevice(deviceInContextMenu);
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

    public void startScanning(View view) {
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

        devicesManager.startScanning();
    }

    private boolean hasPermission(String id) {
        return ActivityCompat.checkSelfPermission(this, id) == PackageManager.PERMISSION_GRANTED;
    }
}