package de.stehle.legoan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import de.stehle.legoan.databinding.LayoutTrainItemBinding;

public class TrainHubFragment extends DeviceFragment {
    private LayoutTrainItemBinding binding;

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

        getConnected().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value ? "Yes" : "No"));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(String.format(Locale.getDefault(), "%d %%", value)));

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
