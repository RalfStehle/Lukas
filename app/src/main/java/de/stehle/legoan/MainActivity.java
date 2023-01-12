// https://github.com/joelwass/Android-BLE-Connect-Example
// https://riptutorial.com/android/topic/10020/bluetooth-low-energy
// https://proandroiddev.com/android-bluetooth-low-energy-building-chat-app-with-ble-d2700956715b

package de.stehle.legoan;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    ListView mDevicesListView;
    ArrayAdapter<String> btArrayAdapter;
    final Set<String> bluetoothKnownAddresses = new HashSet<>();

    Button disconnectDevice;
    Button startScanningButton;
    Button stopScanningButton;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;

    TextView textviewMessage;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    final List<TrainAdapter> trains = new ArrayList<TrainAdapter>();
    private static final String TAG = "Hubi";

    Boolean btScanning = false;
    ArrayList<BluetoothDevice> bluetoothDeviceList = new ArrayList<BluetoothDevice>();
    //BluetoothGatt bluetoothGatt;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public Map<String, String> uuids = new HashMap<String, String>();

    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textviewMessage = (TextView) findViewById(R.id.textviewMessage);
        textviewMessage.setMovementMethod(new ScrollingMovementMethod());

        disconnectDevice = (Button) findViewById(R.id.DisconnectButton);
        disconnectDevice.setVisibility(View.VISIBLE);
        disconnectDevice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                disconnectDeviceSelected();
            }
        });
        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BluetoothDevice device = btAdapter.getRemoteDevice("90:84:2B:12:BC:0A");  //
                // bluetoothGatt = device.connectGatt(MainActivity.this, true, btleGattCallback);
                startScanning();

                // connect("90:84:2B:12:BC:0A");
            }
        });
        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        button1 = (Button) findViewById(R.id.writeBytes1);
        button2 = (Button) findViewById(R.id.writeBytes2);
        button3 = (Button) findViewById(R.id.writeBytes3);
        button4 = (Button) findViewById(R.id.writeBytes4);
        button5 = (Button) findViewById(R.id.writeBytes5);
        button6 = (Button) findViewById(R.id.writeBytes6);

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                driveTrainBackwards(0);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopTrain(0);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                driveTrainForward(0);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                driveTrainBackwards(1);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopTrain(1);
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { nextLedColor(0); }
        });


        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mDevicesListView = (ListView) findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(btArrayAdapter);
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapter, View v, int position, long row) {
            Toast.makeText(getApplicationContext(), "Connect to " + position + "  " + (String) (adapter.getItemAtPosition(position)), Toast.LENGTH_SHORT).show();
            String address = adapter.getItemAtPosition(position).toString().substring(0, 17);
            connect(address);
            // bluetoothGatt = bluetoothDeviceList.get(adapter.getItemAtPosition(position)).connectGatt(this, false, btleGattCallback);
            // BluetoothDevice device = btAdapter.getRemoteDevice(address);  //90:84:2B:12:BC:0A
            // bluetoothGatt = device.connectGatt(MainActivity.this, true, btleGattCallback);
        }
    };

    private void connect(String address) {
        for (TrainAdapter adapter : this.trains) {
            if (adapter.getAddress() == address) {
                return;
            }
        }
        BluetoothDevice device = btAdapter.getRemoteDevice(address);  //90:84:2B:12:BC:0A
        BluetoothGatt bluetoothGatt = device.connectGatt(MainActivity.this, true, btleGattCallback);

        this.trains.add(new TrainAdapter(address, bluetoothGatt));
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getName() == null) {
                return;
            }

            String address = result.getDevice().getAddress();

            if (!bluetoothKnownAddresses.add(address)) {
                return;
            }

            btArrayAdapter.add(result.getDevice().getAddress() + "  " + result.getDevice().getName() + "  (rssi:" + result.getRssi() + ")");
            bluetoothDeviceList.add(result.getDevice());
            Log.i(TAG, "ScanResult::" + result.getScanRecord().getBytes().toString());
            // auto scroll for text view
            final int scrollAmount = mDevicesListView.getCount() - mDevicesListView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                mDevicesListView.scrollTo(0, scrollAmount);
            }
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    textviewMessage.setText("device read or wrote to");
                    Log.i("onCharacteristicChanged", "device read or wrote");
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            Log.i("onConnectionStateChange", "newState = " + newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            textviewMessage.setText("device disconnected (1)");
                            disconnectDevice.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            textviewMessage.setText("device connected (2)");
                            disconnectDevice.setVisibility(View.VISIBLE);
                        }
                    });

                    // discover services and characteristics for this device
                    gatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            textviewMessage.setText("we encounterned an unknown state (u)");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.i("onServicesDiscovered", "7");
                    textviewMessage.setText("device services discovered (4)");
                }
            });
            //  displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.i("BluetoothGattCallback", characteristic.getUuid().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("onRequestPermissionsResult", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        Log.i("startScanning", "start scanning");
        btScanning = true;
        bluetoothKnownAddresses.clear();
        bluetoothDeviceList.clear();
        btArrayAdapter.clear();
        bluetoothKnownAddresses.clear();
        textviewMessage.setText("");
        textviewMessage.setText("Started Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
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
        Log.i("stopScanning", "stopping scanning");
        textviewMessage.setText("Stopped Scanning");
        btScanning = false;
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    public void disconnectDeviceSelected() {
        textviewMessage.setText("Disconnecting from device");
        // bluetoothGatt.disconnect();
    }

    private void driveTrainBackwards(int index) {
        if (index < 0 || index >= trains.size()) {
            return;
        }

        trains.get(index).decrementSpeed();
    }

    private void driveTrainForward(int index) {
        if (index < 0 || index >= trains.size()) {
            return;
        }

        trains.get(index).incrementSpeed();
    }

    private void stopTrain(int index) {
        if (index < 0 || index >= trains.size()) {
            return;
        }

        trains.get(index).stop();
    }

    private void nextLedColor(int index) {
        if (index < 0 || index >= trains.size()) {
            return;
        }

        trains.get(index).nextLedColor();
    }
    /*
    public boolean writeCharacteristic(byte value[],int type){
        //check mBluetoothGatt is available
        Log.i("writeCharacteristic", "START");

        if (bluetoothGatt == null) {
            Log.e("writeCharacteristic", "lost connection");
            return false;
        }
        BluetoothGattService Service = bluetoothGatt.getService(UUID.fromString("00001623-1212-efde-1623-785feabcd123"));
        if (Service == null) {
            Log.i("writeCharacteristic", "service not found!");
            //////////NO service found...........
            return false;
        }
        BluetoothGattCharacteristic charac1 = null;
        boolean status1 = false;

        if(type==1) {
            charac1 = Service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));
            charac1.setValue(value);
            status1 = bluetoothGatt.writeCharacteristic(charac1);
            Log.i("writeCharacteristic", "setValue-Status: " + status1);
            //onReliableWriteCompleted(status1);
        }

        if (charac1 == null) {
            Log.i("writeCharacteristic", "ERROR: charac1 == null");
            return false;
        }

        Log.i("writeCharacteristic","ENDE /"+type);
        return status1;
    }
    */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}


/*
    // https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
    // Custom Adapter ListView, Layout device_list_item.xml
    public class DeviceList {
        public String address;
        public String name;
        public String pssi;

        public DeviceList(String address, String name, String pssi) {
            this.address = address;
            this.name = name;
            this.pssi = pssi;
        }
    }
    public class CustomAdapter extends ArrayAdapter<DeviceList> {

        public CustomAdapter(Context context, ArrayList<DeviceList> devices) {
            super(context, 0, devices);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            DeviceList list = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item, parent, false);
            }
            // Lookup view for data population
            TextView tvAddress = (TextView) convertView.findViewById(R.id.deviceAdress);
            TextView tvName = (TextView) convertView.findViewById(R.id.deviceName);
            TextView tvPssi = (TextView) convertView.findViewById(R.id.devicePssi);
            // Populate the data into the template view using the data object
            tvAddress.setText(list.address);
            tvName.setText(list.name);
            tvPssi.setText(list.pssi);
            // Return the completed view to render on screen
            return convertView;
        }
    }
 */