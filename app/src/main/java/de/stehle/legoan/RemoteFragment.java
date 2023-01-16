package de.stehle.legoan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

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

        binding.TrainARadio
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton == null) {
                        getRemote().setTrainA(null);
                    } else {
                        getRemote().setTrainA((TrainHub) radioButton.getTag());
                    }
                });

        binding.TrainBRadio
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton == null) {
                        getRemote().setTrainB(null);
                    } else {
                        getRemote().setTrainB((TrainHub) radioButton.getTag());
                    }
                });

        checkNone(binding.TrainARadio);
        checkNone(binding.TrainBRadio);

        DevicesManager.getInstance().getDevices().observe(getViewLifecycleOwner(), this::updateTrains);

        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getConnected().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value ? "Yes" : "No"));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(String.format(Locale.getDefault(), "%d %%", value)));

        return binding.getRoot();
    }

    private void updateTrains(List<Device> devices) {
        if (binding == null) {
            return;
        }

        List<TrainHub> trains = new ArrayList<>();

        for (Device device : devices) {
            if (device instanceof TrainHub) {
                trains.add((TrainHub) device);
            }
        }

        Remote remote = getRemote();

        TrainHub trainA = remote.getTrainA();
        TrainHub trainB = remote.getTrainB();

        if (!trains.contains(trainA)) {
            remote.setTrainA(null);
        }

        if (!trains.contains(trainB)) {
            remote.setTrainB(null);
        }

        updateRadio(binding.TrainARadio, trains, remote.getTrainA());
        updateRadio(binding.TrainBRadio, trains, remote.getTrainB());
    }

    private void updateRadio(RadioGroup radioGroup, List<TrainHub> trains, TrainHub connectedTrain) {
        int targetSize = trains.size() + 1;

        while (radioGroup.getChildCount() > targetSize) {
            radioGroup.removeViewAt(radioGroup.getChildCount() - 1);
        }

        while (radioGroup.getChildCount() < targetSize) {
            RadioButton radioButton = new RadioButton(radioGroup.getContext());
            radioButton.setId(View.generateViewId());

            radioGroup.addView(radioButton);
        }

        int index = 1;
        for (TrainHub train : trains) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(index);
            radioButton.setTag(train);
            radioButton.setText(train.getName());
            index++;
        }

        if (connectedTrain != null) {
            View view = radioGroup.getChildAt(trains.indexOf(connectedTrain) + 1);

            if (view != null) {
                radioGroup.check(view.getId());
                return;
            }
        }

        checkNone(radioGroup);
    }

    private void checkNone(RadioGroup radioGroup) {
        radioGroup.check(radioGroup.getChildAt(0).getId());
    }

    private Remote getRemote() {
        return (Remote) getDevice();
    }
}
