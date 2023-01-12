// https://github.com/joelwass/Android-BLE-Connect-Example
// https://riptutorial.com/android/topic/10020/bluetooth-low-energy

package de.project.hubi;


import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //region Deklarationen
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    ListView mDevicesListView;  // 01.01.23
    ArrayAdapter<String> btArrayAdapter;  // 01.01.23

    Button disconnectDevice;
    Button startScanningButton;
    Button stopScanningButton;
    Button writeBytes1;
    Button writeBytes2;
    Button writeBytes3;
    Button writeBytes4;
    Button writeBytes5;
    Button writeBytes6;

    TextView textviewMessage;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Boolean btScanning = false;
    ArrayList<BluetoothDevice> bluetoothDeviceList = new ArrayList<BluetoothDevice>();
    BluetoothGatt bluetoothGatt;

    final Set<String> bluetoothKnownAddresses = new HashSet<>();
    public int currentColor = 0;
    private static final String TAG = "Hubi";

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public Map<String, String> uuids = new HashMap<String, String>();

    // Stops scanning after 5 seconds.
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 10000;
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // region Initialisierungen
        textviewMessage = (TextView) findViewById(R.id.textviewMessage );
        textviewMessage.setMovementMethod(new ScrollingMovementMethod());
        getSupportActionBar().setTitle("Legoan Basic - " + BuildConfig.VERSION_NAME + BuildConfig.VERSION_CODE);

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
            /*    startScanningButton.setVisibility(View.VISIBLE);

                BluetoothDevice device = btAdapter.getRemoteDevice("90:84:2B:12:BC:0A");   // Lego Hub
                bluetoothGatt = device.connectGatt(MainActivity.this, true, btleGattCallback);
                BluetoothDevice device = btAdapter.getRemoteDevice("70:B9:50:9F:8C:9D");  // Lego Remote Handset
                bluetoothGatt = device.connectGatt(MainActivity.this, true, btleGattCallback);
             */
                startScanning();
            }
        });
        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        writeBytes1 = (Button) findViewById(R.id.writeBytes1);
        writeBytes2 = (Button) findViewById(R.id.writeBytes2);
        writeBytes3 = (Button) findViewById(R.id.writeBytes3);
        writeBytes4 = (Button) findViewById(R.id.writeBytes4);
        writeBytes5 = (Button) findViewById(R.id.writeBytes5);
        writeBytes6 = (Button) findViewById(R.id.writeBytes6);
        // endregion

        writeBytes1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Buttons der Fernsteuerung aktivieren, damit Notifications gesendet werden
                sendByte(new byte[] {0xA, 0x00, 0x41, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01}, 1);   // LEFT  Button aktivieren
                sendByte(new byte[] {0xA, 0x00, 0x41, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01}, 1);   // RIGHT Button aktivieren
                //sendByte(new byte[] {0xA, 0x00, 0x41, 0x3B, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01}, 1);   // VOLTAGE
                sendByte(new byte[] {0xA, 0x00, 0x41, 0x34, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01}, 1);   // LED aktivieren
                sendByte(new byte[] {}, 2); // activate Notifications (BluetoothGattCallback/onCharacteristicChanged)
            }
        });
        writeBytes2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentColor++;
                if (currentColor == 11) {  currentColor = 0;   }
                sendByte(new byte[] {0x0a, 0x00, (byte)0x41, 0x32, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00}, 1);     // Set Color Mode Hub
                sendByte(new byte[] {0x08, 0x00, (byte)0x81, 0x32, 0x11, 0x51, 0x00, (byte)currentColor }, 1);  // Set Color Hub
                sendByte(new byte[] {0x08, 0x00, (byte)0x81, 0x34, 0x11, 0x51, 0x00, (byte)currentColor }, 1);  // Set Color Remote
            }
        });
        writeBytes3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendByte(new byte[] {0x08, 0x00, (byte)0x81, 0x00, 0x11, 0x60, 0x00, (byte)0x36}, 1); // Motor A 0x36=50%  7E=100%
                sendByte(new byte[] {0x08, 0x00, (byte)0x81, 0x01, 0x11, 0x51, 0x00, (byte)0x36}, 1); // Motor B 50%
            }
        });
        writeBytes4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String devicename = "88009";
                byte[] second = devicename.getBytes(StandardCharsets.ISO_8859_1);
                int len = 5 + second.length;                                // 5 = Länge des Headers, second.lenght = Länge des devicename
                byte[] first = {(byte)len, 0x00, 0x01, 0x01, 0x01};         // Header 0xXX, 0x00, 0x01, 0x01, 0x01
                byte[] result = new byte[first.length + second.length];     // bytes verbinden = result
                System.arraycopy(first, 0, result, 0, first.length);
                System.arraycopy(second, 0, result, first.length, second.length);

                textviewMessage.setText(bytesToHex(result));

                // Device umbenennen  Beispiel "Hub 01" = {0x0B, 0x00, 0x01, 0x01, 0x01, 0x48, 0x75, 0x62, 0x20, 0x30, 0x31}
                sendByte(result, 1);
                sendByte(new byte[] {}, 2); // activateNotifications
            }
        });
        writeBytes5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //sendByte(new byte[] {0x4, 0x00, 0x06, 0x02}, 1);        // 0x06 Batterie-Status 0x02 Enable Updates
                sendByte(new byte[] {0x4, 0x00, 0x06, 0x05}, 1);     // 0x06 Batterie-Status 0x05 Request Updates
                sendByte(new byte[] {0x4, 0x00, 0x06, 0x02}, 1);     // 0x06 Batterie-Status 0x02 Enable Updates
                sendByte(new byte[] {}, 2); // activate Notifications (BluetoothGattCallback/onCharacteristicChanged UND BroadcastUpdate)
            }
        });
        writeBytes6.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendByte(new byte[] {0x06, 0x00, (byte)0x45, 0x3b, 0x00, 0x00}, 1);     // 0x01 Advertising Name 0x05 Request Updates
                sendByte(new byte[] {}, 2); // activate Notifications (BluetoothGattCallback/onCharacteristicChanged UND BroadcastUpdate)
                sendByte(new byte[] {0x08, 0x00, (byte)0x45, 0x3c, 0x50, 0x0D}, 1);     // Test Voltage
            }
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
    }       // onCreate ENDE

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapter, View v, int position, long row) {
            Toast.makeText(getApplicationContext(),"Connect to " + position  + "  " + (String) (adapter.getItemAtPosition(position)), Toast.LENGTH_SHORT).show();
            String address = adapter.getItemAtPosition(position).toString().substring(0, 17);
            //bluetoothGatt = bluetoothDeviceList.get(adapter.getItemAtPosition(position)).connectGatt(this, false, btleGattCallback);
            BluetoothDevice device = btAdapter.getRemoteDevice(address);  //90:84:2B:12:BC:0A
            bluetoothGatt = device.connectGatt(MainActivity.this, true, btleGattCallback);
        }
    };

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // wenn eine Adresse schon gelistet ist, dann Methode beenden
            String address = result.getDevice().getAddress();
            if (!bluetoothKnownAddresses.add(address)) {
                return;
            }
            // btArrayAdapter speichert die Ergebnisse als String für mDevicesListView
            btArrayAdapter.add(result.getDevice().getAddress() + "  " + result.getDevice().getName() +  "  (rssi:" + result.getRssi() + ")");
            bluetoothDeviceList.add(result.getDevice());
            Log.i(TAG, result.getScanRecord().toString());
                    // auto scroll for text view
            final int scrollAmount = mDevicesListView.getCount() - mDevicesListView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0) {
                mDevicesListView.scrollTo(0, scrollAmount);
            }
            /*   Ergebnisse in Logcat ausgeben
            //if (result.getDevice().getAddress() != "70:B9:50:9F:8C:9D") {  return;  }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.i(TAG,     "Out1:: = " + result.getDevice().getAddress() + " | Name:"    + result.getDevice().getName() + " | rssi:"    + result.getRssi()  +
                        " | SID:"     + result.getAdvertisingSid() + " | Record: " + result.getScanRecord().toString()
                );
            }   // oder so..
            String sr = result.getScanRecord().getBytes().toString();
            Log.i(TAG, "Scan::" + sr);
             */

        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult sr : results) {
                Log.i(TAG, sr.toString());
            }
        }
        // diese Methode hat hier wahrscheinlich nichts verloren. Noch unklar welche Funktion. Vielleicht veraltet statt "ScanCallback leScanCallback =...."
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };

    // Device connect callback
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation

            // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);   // damit werden die Werte zusätzlich in broadcastUpdate empfangen
            // broadcastUpdate(EXTRA_DATA, characteristic);              // damit werden die Werte zusätzlich in broadcastUpdate empfangen

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //textviewMessage.setText("device read or wrote to");
                    byte[] messageBytes = characteristic.getValue();
                    Log.i(TAG, "Get:: " +  bytesToHex(messageBytes));
                }
            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            // values for newState when a device connects or disconnects
            /* gatt.STATE_CONNECTED = 2
               gatt.STATE_DISCONNECTED = 0
               gatt.STATE_CONNECTING = 1
               gatt.STATE_DISCONNECTING = 3   */
            Log.i(TAG, "BluetoothGattCallback newState = " + newState);
            switch (newState) {
                case 0:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            textviewMessage.setText("device disconnected (0)");
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
                    bluetoothGatt.discoverServices();
                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            textviewMessage.setText("unknown state (u)");
                        }
                    });
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }

            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.i(TAG, "onServicesDiscovered");
                    textviewMessage.setText("device services discovered");
                }
            });
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.i(TAG, "Log1::" + characteristic.toString());
                    byte[] value = characteristic.getValue();
                    String v = new String(value);
                    Log.i(TAG, "Log2::" + v);
                }
            });
        }
    };

    private void broadcastUpdate(final String action) {
        Log.d(TAG, "broadcastUpdate");
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();

        final Intent intent = new Intent(action);
        // Special handling for Heart Rate Measurement profile available on http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, bytesToHex(data));
            Log.i(TAG, "BroadCast-Intent: " + bytesToHex(data));
        }
        sendBroadcast(intent);
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
        Log.i(TAG,"start scanning");
        btScanning = true;
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

    public void connectToDeviceSelected() {
        textviewMessage.setText("Trying to connect to device");
        Log.i(TAG, "Verbindung zu item 0. Wird Methode benutzt?");
        bluetoothGatt = bluetoothDeviceList.get(0).connectGatt(this, false, btleGattCallback);
    }

    public void disconnectDeviceSelected() {
        textviewMessage.setText("Disconnecting from device");
        bluetoothGatt.disconnect();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            Log.i(TAG, "Service discovered: " + uuid);
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    btArrayAdapter.add("Service discovered @" + uuid);
                }
            });
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                Log.i(TAG, "Characteristic discovered for service: " + charUuid);
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        btArrayAdapter.add("Characteristic for service: " + charUuid);
                    }
                });

            }
        }
    }

    public boolean sendByte(byte value[], int type){
        //check mBluetoothGatt is available
        Log.i(TAG, "START sendBt");

        if (bluetoothGatt == null) {
            Log.i(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = bluetoothGatt.getService(UUID.fromString("00001623-1212-efde-1623-785feabcd123"));
        if (Service == null) {
            Log.i(TAG, "no service found");
            return false;
        }
        BluetoothGattCharacteristic charac1 = null;
        boolean status1 = false;

        if(type==1) {
            charac1 = Service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));
            charac1.setValue(value);
            status1 = bluetoothGatt.writeCharacteristic(charac1);
            Log.i(TAG, "Status = " + status1);
            //onReliableWriteCompleted(status1);
        }

        if (type == 2) {
            Log.e(TAG, "Type 2");

            charac1 = Service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));
            BluetoothGattDescriptor descriptor = charac1.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
            bluetoothGatt.setCharacteristicNotification(charac1, true);
            bluetoothGatt.readCharacteristic(charac1);

            /*   alle Descriptoren finden und aktivieren
            for (BluetoothGattDescriptor descriptor : charac1.getDescriptors()) {
                descriptor.getUuid().toString();
                Log.i(TAG, "Descriptor: " + descriptor.getUuid().toString());
                descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
                bluetoothGatt.setCharacteristicNotification(charac1, true);
                bluetoothGatt.readCharacteristic(charac1);
            }
            */
        }

        if (type == 3) {
            Log.e(TAG, "Type 3");

            charac1 = Service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));
            bluetoothGatt.setCharacteristicNotification(charac1, true);
            bluetoothGatt.readCharacteristic(charac1);
        }

        if (charac1 == null) {
            Log.i(TAG, "ERROR: charac1 == null");
            return false;
        }

        Log.i(TAG,"END of sendBt");
        return status1;
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length);
        for (byte byteChar : bytes) {
            stringBuilder.append(String.format("%02X ", byteChar));
        }
        return(stringBuilder.toString());
    }

    static String stringToHex(String string) {
        StringBuilder buf = new StringBuilder(200);
        for (char ch: string.toCharArray()) {
            if (buf.length() > 0)
                buf.append(' ');
            buf.append(String.format("%02x", (int) ch));
        }
        return buf.toString();
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