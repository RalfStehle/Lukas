package de.stehle.legoan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.util.UUID;

@SuppressLint("MissingPermission")
public class Remote extends Device {
    private final static UUID ServiceUUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123");
    private final static UUID CharacteristicsUUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123");
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private LegoWriterQueue writerQueue;
    private TrainHub trainA;
    private TrainHub trainB;

    @Override
    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public TrainHub getTrainA() {
        return trainA;
    }

    public TrainHub getTrainB() {
        return trainA;
    }

    public void setTrainA(TrainHub trainA) {
        this.trainA = trainA;
    }

    public void setTrainB(TrainHub trainB) {
        this.trainB = trainB;
    }

    public Remote(BluetoothDevice device) {
        setName(device.getName());

        BluetoothGattCallback callback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                switch (newState) {
                    case BluetoothGatt.STATE_DISCONNECTED:
                        setConnected(false);
                        break;
                    case BluetoothGatt.STATE_CONNECTED:
                        setConnected(true);

                        // It seems to be more stable to wait a little bit for the discovery.
                        // Discover services and characteristics for this device
                        new Handler(Looper.getMainLooper()).postDelayed(bluetoothGatt::discoverServices, 500);
                        initializeService();
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                initializeService();
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                writerQueue.confirmWrite();
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] value = LegoHelper.envelopeToData(characteristic.getValue());

                if (value[0] == 0x01) {
                    parseDeviceInfo(value);
                } else if (value[0] == 0x45) {
                    parseButtons(value);
                }
            }

            private void parseButtons(byte[] value) {
                // 0: 0x45
                // 1: Button Side (0: Left, 1: Right)
                // 2: Button Mode (-1: Down, 1: Up, 127: Red
                byte buttonSide = value[1];
                byte buttonMode = value[2];

                if (buttonSide == 0) {
                    if (buttonMode == 1) {
                        leftUp();
                    } else if (buttonMode == -1) {
                        leftDown();
                    } else if (buttonMode == 127) {
                        leftMiddle();
                    }
                } else {
                    if (buttonMode == 1) {
                        rightUp();
                    } else if (buttonMode == -1) {
                        rightDown();
                    } else if (buttonMode == 127) {
                        rightMiddle();
                    }
                }
            }

            private void parseDeviceInfo(byte[] value) {
                // 0: 0x01
                // 1: Property Type
                // 2: -
                // 3: Value
                if (value[1] == 0x06) {
                    setBattery(value[3]);
                }
            }
        };

        bluetoothDevice = device;
        bluetoothGatt = bluetoothDevice.connectGatt(null, true, callback);
    }

    private void leftUp() {
        if (trainA != null && trainB != null) {
            trainA.motorFaster();
        } else if (trainA != null) {
            trainA.lightBrighter();
        } else if (trainB != null) {
            trainB.lightBrighter();
        }
    }

    private void leftMiddle() {
        if (trainA != null && trainB != null) {
            trainA.motorStop();
        } else if (trainA != null) {
            trainA.ledRandom();
        } else if (trainB != null) {
            trainB.ledRandom();
        }
    }

    private void leftDown() {
        if (trainA != null && trainB != null) {
            trainA.motorSlower();
        } else if (trainA != null) {
            trainA.lightDarker();
        } else if (trainB != null) {
            trainB.lightDarker();
        }
    }

    private void rightUp() {
        if (trainA != null) {
            trainA.motorFaster();
        } else if (trainB != null) {
            trainB.motorFaster();
        }
    }

    private void rightMiddle() {
        if (trainA != null) {
            trainA.motorStop();
        } else if (trainB != null) {
            trainB.motorStop();
        }
    }

    private void rightDown() {
        if (trainA != null) {
            trainA.motorSlower();
        } else if (trainB != null) {
            trainB.motorSlower();
        }
    }

    @Override
    public void disconnect() {
        bluetoothGatt.disconnect();
    }

    public static boolean canConnect(ScanResult scanResult) {
        ScanRecord record = scanResult.getScanRecord();

        if (record.getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid : record.getServiceUuids()) {
            if (uuid.getUuid().equals(ServiceUUID)) {
                String name = record.getDeviceName().trim();

                if (name.equals("Handset")) {
                    return true;
                }
            }
        }

        return false;
    }

    private void send(byte[] data) {
        initializeService();

        if (writerQueue == null) {
            return;
        }

        writerQueue.write(data);
    }

    private void initializeService() {
        if (writerQueue != null) {
            return;
        }

        BluetoothGattService service = bluetoothGatt.getService(ServiceUUID);

        if (service == null) {
            return;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CharacteristicsUUID);

        if (characteristic == null) {
            return;
        }

        writerQueue = new LegoWriterQueue(bluetoothGatt, characteristic);
        writerQueue.enableNotifications();

        // It seems more stable to wait a little bit, because the first writes usually fail.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Activate button reports
            send(new byte[]{0x41, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1});
            send(new byte[]{0x41, 0x1, 0x0, 0x1, 0x0, 0x0, 0x0, 0x1});

            // Activate battery reports
            send(new byte[]{0x01, 0x06, 0x02});
        }, 2000);
    }
}
