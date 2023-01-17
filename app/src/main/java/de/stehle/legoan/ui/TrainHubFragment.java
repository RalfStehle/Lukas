package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.stehle.legoan.databinding.LayoutTrainItemBinding;
import de.stehle.legoan.model.TrainHub;

public class TrainHubFragment extends DeviceFragment {
    private LayoutTrainItemBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutTrainItemBinding.inflate(inflater, container, false);

        binding.MotorStopButton.setOnClickListener(view1 -> getTrain().motorStop());
        binding.MotorSlowerButton.setOnClickListener(view1 -> getTrain().motorSlower());
        binding.MotorFasterButton.setOnClickListener(view1 -> getTrain().motorFaster());
        binding.LedRandomButton.setOnClickListener(view1 -> getTrain().ledRandom());
        binding.LightBrighterButton.setOnClickListener(view1 -> getTrain().lightBrighter());
        binding.LightBrighterButton.setOnClickListener(view1 -> getTrain().lightDarker());

        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(Integer.toString(value)));


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private TrainHub getTrain() {
        return (TrainHub) getDevice();
    }
}
