package de.stehle.legoan;

import android.app.Activity;
import android.database.DataSetObserver;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class RemoteAdapter extends Adapter {
    private final Remote device;
    private View view;
    private final DeviceListAdapter devices;
    private final DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateTrains();
        }
    };

    RemoteAdapter(Remote device, View view, Activity activity, DeviceListAdapter devices) {
        super(activity, device, view);
        this.view = view;
        this.device = device;
        this.devices = devices;
    }

    @Override
    public void connect() {
        super.connect();
        devices.registerDataSetObserver(observer);

        updateTrains();
    }

    @Override
    public void disconnect() {
        super.disconnect();
        devices.unregisterDataSetObserver(observer);
    }

    @Override
    protected void connectListeners(View view) {
        RadioGroup radioGroup = view.findViewById(R.id.RadioGroup);

        radioGroup.check(radioGroup.getChildAt(0).getId());
        radioGroup
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.getChildAt(id);

                    if (radioButton == null) {
                        device.setConnectedTrain(null);
                    } else {
                        device.setConnectedTrain((TrainHub) radioButton.getTag());
                    }
                });
    }

    @Override
    protected void connectValues(View view) {
        ((TextView) view.findViewById(R.id.NameContent))
                .setText(device.getName());

        ((TextView) view.findViewById(R.id.ConnectedContent))
                .setText(device.isConnected() ? "Yes" : "No");

        ((TextView) view.findViewById(R.id.BatteryContent))
                .setText(String.format(Locale.getDefault(), "%d %%", device.getBattery()));
    }

    private void updateTrains() {
        RadioGroup radioGroup = view.findViewById(R.id.RadioGroup);

        List<TrainHub> trains = new ArrayList<>();

        for (int i = 0; i < devices.getCount(); i++) {
            Device device = (Device) devices.getItem(i);

            if (device instanceof TrainHub) {
                trains.add((TrainHub) device);
            }
        }

        TrainHub connectedTrain = device.getConnectedTrain();

        if (!trains.contains(connectedTrain)) {
            device.setConnectedTrain(null);
        }

        int targetSize = trains.size() + 1;

        while (radioGroup.getChildCount() > targetSize) {
            radioGroup.removeViewAt(radioGroup.getChildCount() - 1);
        }

        while (radioGroup.getChildCount() < targetSize) {
            radioGroup.addView(new RadioButton(radioGroup.getContext()));
        }

        for (int i = 0; i < trains.size(); i++) {
            TrainHub train = trains.get(i);

            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i + 1);
            radioButton.setId(i + 1);
            radioButton.setTag(train);
            radioButton.setText(train.getName());
        }

        if (connectedTrain != null) {
            radioGroup.check(trains.indexOf(connectedTrain) + 1);
        } else {
            radioGroup.check(radioGroup.getChildAt(0).getId());
        }
    }
}
