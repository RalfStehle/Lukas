package de.stehle.legoan.ui;

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
import de.stehle.legoan.model.Device;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Remote;
import de.stehle.legoan.model.RemoteController;
import de.stehle.legoan.model.TrainHub;

public class RemoteFragment extends DeviceFragment {
    private LayoutRemoteItemBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRemoteItemBinding.inflate(inflater, container, false);

        binding.TrainARadio
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton != null) {
                        getRemote().setControllerA((RemoteController) radioButton.getTag());
                    }
                });

        binding.TrainBRadio
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton != null) {
                        getRemote().setControllerB((RemoteController) radioButton.getTag());
                    }
                });

        checkNone(binding.TrainARadio);
        checkNone(binding.TrainBRadio);

        DevicesManager.getInstance().getDevices().observe(getViewLifecycleOwner(), this::updateTrains);

        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getConnected().observe(getViewLifecycleOwner(),
                value -> binding.ConnectedContent.setText(value ? "Yes" : "No"));

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

        TrainHub trainA = remote.getControllerA().getTrain();
        TrainHub trainB = remote.getControllerB().getTrain();

        if (!trains.contains(trainA)) {
            remote.setControllerA(null);
        }

        if (!trains.contains(trainB)) {
            remote.setControllerB(null);
        }

        updateRadio(binding.TrainARadio, trains, remote.getControllerA().getTrain());
        updateRadio(binding.TrainBRadio, trains, remote.getControllerB().getTrain());
    }

    private void updateRadio(RadioGroup radioGroup, List<TrainHub> trains, TrainHub connectedTrain) {
        int targetSize = trains.size() * 2 + 1;

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
            String name = train.getName();

            RadioButton motorRadio = (RadioButton) radioGroup.getChildAt(index);
            motorRadio.setTag(RemoteController.motor(train));
            motorRadio.setText(String.format(Locale.getDefault(), "%s Motor", name));
            index++;

            RadioButton lightRadio = (RadioButton) radioGroup.getChildAt(index);
            lightRadio.setTag(RemoteController.light(train));
            lightRadio.setText(String.format(Locale.getDefault(), "%s Light", name));
            index++;
        }
    }

    private void checkNone(RadioGroup radioGroup) {
        radioGroup.check(radioGroup.getChildAt(0).getId());
    }

    private Remote getRemote() {
        return (Remote) getDevice();
    }
}
