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
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.behavior.SwipeDismissBehavior;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 10000;
    private final Handler mHandler = new Handler();
    private final List<TrainHub> trains = new ArrayList<>();
    private TrainHubListAdapter trainsAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothScanner;
    private ListView trainsListView;
    private Button scanningStartButton;
    private Button scanningStopButton;
    private boolean isScanning = false;
    private TrainHub menuHub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanningStartButton = findViewById(R.id.StartScanButton);
        scanningStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        scanningStopButton = findViewById(R.id.StopScanButton);
        scanningStopButton.setVisibility(View.GONE);
        scanningStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        trainsAdapter = new TrainHubListAdapter(trains, this);
        trainsListView = findViewById(R.id.devicesListView);
        trainsListView.setAdapter(trainsAdapter);
    }

    // Device scan callback.
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

            for (TrainHub adapter : trains) {
                if (adapter.getAddress().equals(address)) {
                    return;
                }
            }

            TrainHub trainHub = new TrainHub(bluetoothAdapter.getRemoteDevice(address), MainActivity.this);

            trains.add(trainHub);
            trainsAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Options");
        menu.add(0, v.getId(), 0, "Disconnect");

        // Set the current hub from the view.
        menuHub = (TrainHub) v.getTag();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        // We only have one option yet, so we can just call disconnect directly.
        menuHub.disconnect();

        trains.remove(menuHub);
        trainsAdapter.notifyDataSetChanged();
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
        builder.setTitle("Functionality limited");
        builder.setMessage("Since location or bluetooth access has not been granted, this app will not be able to discover beacons when in the background.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.show();
    }

    public void startScanning() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN) || !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1);
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
        mHandler.postDelayed(this::stopScanning, SCAN_PERIOD);
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