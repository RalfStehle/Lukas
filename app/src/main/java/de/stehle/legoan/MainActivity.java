package de.stehle.legoan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import de.stehle.legoan.databinding.ActivityMainBinding;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Remote;
import de.stehle.legoan.model.Switch;
import de.stehle.legoan.model.TrainHub;
import de.stehle.legoan.ui.SectionsPagerAdapter;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private final int menuAddTrain = View.generateViewId();
    private final int menuAddRemote = View.generateViewId();
    private final int menuAddSwitch = View.generateViewId();
    private ActivityMainBinding binding;
    private int wasScanning;
    private int addedDevices;
    private ActivityResultLauncher<Intent> blueoothIntentLauncher;

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

        if (devicesManager.isTesting()) {
            registerForContextMenu(binding.Toolbar);

            addedDevices = devicesManager.getDevices().getValue().size();
        }

        blueoothIntentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        devicesManager.startScanning();
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

        if (devicesManager.isTesting()) {
            menu.add(Menu.NONE, menuAddTrain, 0, R.string.add_train);
            menu.add(Menu.NONE, menuAddRemote, 0, R.string.add_remote);
            menu.add(Menu.NONE, menuAddTrain, 0, R.string.add_switch);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        addedDevices++;

        int itemId = item.getItemId();

        if (itemId == menuAddTrain) {
            devicesManager.addDevice(new TrainHub(String.format("Train %d", addedDevices)));
        } else if (itemId == menuAddRemote) {
            devicesManager.addDevice(new Remote(String.format("Train %d", addedDevices)));
        } else if (itemId == menuAddSwitch) {
            devicesManager.addDevice(new Switch(String.format("Train %d", addedDevices)));
        }

        return false;
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

        if (!devicesManager.isBluetoothEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            blueoothIntentLauncher.launch(enableBluetoothIntent);
            return;
        }

        devicesManager.startScanning();
    }

    private boolean hasPermission(String id) {
        return ActivityCompat.checkSelfPermission(this, id) == PackageManager.PERMISSION_GRANTED;
    }
}