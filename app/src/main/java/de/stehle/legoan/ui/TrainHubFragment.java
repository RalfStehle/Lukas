package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutTrainItemBinding;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.TrainHub;

public class TrainHubFragment extends DeviceFragment {
    private final int disconnectMenuItemId = View.generateViewId();
    private final int renameMenuItemId = View.generateViewId();
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

        menu.add(Menu.NONE, disconnectMenuItemId, 0, R.string.menu_disconnect);
        menu.add(Menu.NONE, renameMenuItemId, 0, R.string.rename);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == disconnectMenuItemId) {
            DevicesManager.getInstance().removeDevice(getDevice());
            return true;
        } else if (itemId == renameMenuItemId) {
            rename();
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void rename() {
        new ConfirmBuilder(requireActivity())
                .setTitle(R.string.rename)
                .setConfirmText(R.string.rename)
                .setValue(getName().getValue())
                .setLMaxLength(14)
                .show(value -> {
                    getTrain().rename(value);
                });
    }

    private TrainHub getTrain() {
        return (TrainHub) getDevice();
    }
}
