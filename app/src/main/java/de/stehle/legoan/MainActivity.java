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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {
    private static final long SCAN_PERIOD = 60000;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanningStartButton = (Button) findViewById(R.id.StartScanButton);
        scanningStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        scanningStopButton = (Button) findViewById(R.id.StopScanButton);
        scanningStopButton.setVisibility(View.GONE);
        scanningStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();

        trainsAdapter = new TrainHubListAdapter(trains);
        trainsListView = (ListView) findViewById(R.id.devicesListView);
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
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        builder.show();
    }

    public void startScanning() {
        if (isScanning) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1);
            }
            return;
        }

        isScanning = true;
        scanningStartButton.setVisibility(View.GONE);
        scanningStopButton.setVisibility(View.VISIBLE);

        AsyncTask.execute(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                bluetoothScanner.startScan(scanCallback);
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
            }
        }, SCAN_PERIOD);
    }

    public void stopScanning() {
        if (!isScanning) {
            return;
        }

        isScanning = false;
        scanningStartButton.setVisibility(View.VISIBLE);
        scanningStopButton.setVisibility(View.GONE);

        AsyncTask.execute(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                bluetoothScanner.stopScan(scanCallback);
            }
        });
    }
}