package de.stehle.legoan;

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
    private LayoutRemoteItemBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRemoteItemBinding.inflate(inflater, container, false);

        binding.RadioGroup.check(binding.RadioGroup.getChildAt(0).getId());
        binding.RadioGroup
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton == null) {
                        getRemote().setConnectedTrain(null);
                    } else {
                        getRemote().setConnectedTrain((TrainHub) radioButton.getTag());
                    }
                });

        DevicesManager.getInstance().getDevices().observe(getViewLifecycleOwner(), this::updateTrains);

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

    private void updateTrains(List<Device> devices) {
        if (binding == null) {
            return;
        }

        List<TrainHub> trains = new ArrayList<>();

        for (Device device: devices) {
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
            RadioButton radioButton = new RadioButton(binding.RadioGroup.getContext());
            radioButton.setId(View.generateViewId());

            binding.RadioGroup.addView(radioButton);
        }

        int index = 1;
        for (TrainHub train: trains) {
            RadioButton radioButton = (RadioButton) binding.RadioGroup.getChildAt(index);
            radioButton.setTag(train);
            radioButton.setText(train.getName());
            index++;
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
