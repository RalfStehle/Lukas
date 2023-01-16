package de.stehle.legoan;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.stehle.legoan.databinding.LayoutRemoteItemBinding;

public class RemoteFragment extends DeviceFragment {
    private DeviceListAdapter devices;
    private final DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateTrains();
        }
    };

    private LayoutRemoteItemBinding binding;

    public void setDevices(DeviceListAdapter newDevices) {
        if (devices == newDevices) {
            return;
        }

        if (devices != null) {
            devices.unregisterDataSetObserver(observer);
        }

        devices = newDevices;

        if (devices != null) {
            devices.registerDataSetObserver(observer);
        }

        updateTrains();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRemoteItemBinding.inflate(inflater, container, false);

        binding.RadioGroup.check(binding.RadioGroup.getChildAt(0).getId());
        binding.RadioGroup
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.getChildAt(id);

                    if (radioButton == null) {
                        getRemote().setConnectedTrain(null);
                    } else {
                        getRemote().setConnectedTrain((TrainHub) radioButton.getTag());
                    }
                });

        return binding.getRoot();
    }

    @Override
    protected void onDeviceChanged(Device device) {
        binding.NameContent
                .setText(device.getName());

        binding.ConnectedContent
                .setText(device.isConnected() ? "Yes" : "No");

        binding.BatteryContent
                .setText(String.format(Locale.getDefault(), "%d %%", device.getBattery()));
    }

    private void updateTrains() {
        List<TrainHub> trains = new ArrayList<>();

        for (int i = 0; i < devices.getCount(); i++) {
            Device device = (Device) devices.getItem(i);

            if (device instanceof TrainHub) {
                trains.add((TrainHub) device);
            }
        }

        TrainHub connectedTrain = getRemote().getConnectedTrain();

        if (!trains.contains(connectedTrain)) {
            getRemote().setConnectedTrain(null);
        }

        int targetSize = trains.size() + 1;

        while (binding.RadioGroup.getChildCount() > targetSize) {
            binding.RadioGroup.removeViewAt(binding.RadioGroup.getChildCount() - 1);
        }

        while (binding.RadioGroup.getChildCount() < targetSize) {
            binding.RadioGroup.addView(new RadioButton(binding.RadioGroup.getContext()));
        }

        for (int i = 0; i < trains.size(); i++) {
            TrainHub train = trains.get(i);

            RadioButton radioButton = (RadioButton) binding.RadioGroup.getChildAt(i + 1);
            radioButton.setId(i + 1);
            radioButton.setTag(train);
            radioButton.setText(train.getName());
        }

        if (connectedTrain != null) {
            binding.RadioGroup.check(trains.indexOf(connectedTrain) + 1);
        } else {
            binding.RadioGroup.check(binding.RadioGroup.getChildAt(0).getId());
        }
    }

    private Remote getRemote() {
        return (Remote) getDevice();
    }
}
