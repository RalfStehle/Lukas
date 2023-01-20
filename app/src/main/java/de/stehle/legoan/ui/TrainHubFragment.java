package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutTrainItemBinding;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.TrainHub;

public class TrainHubFragment extends DeviceFragment {
    private LayoutTrainItemBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutTrainItemBinding.inflate(inflater, container, false);

        binding.MotorSlowerButton.setOnClickListener(view1 -> getTrain().motorSlower());
        binding.MotorStopButton.setOnClickListener(view1 -> getTrain().motorStop());
        binding.MotorFasterButton.setOnClickListener(view1 -> getTrain().motorFaster());
        binding.LedRandomButton.setOnClickListener(view1 -> getTrain().ledRandom());
        binding.LightBrighterButton.setOnClickListener(view1 -> getTrain().lightBrighter());
        binding.LightDarkerButton.setOnClickListener(view1 -> getTrain().lightDarker());

        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(Integer.toString(value)));

        registerForContextMenu(binding.Card);

        return binding.getRoot();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(1000, v.getId(), 0, R.string.menu_disconnect);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getGroupId() == 1000) {
            DevicesManager.getInstance().removeDevice(getDevice());
        }
        return true;
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
