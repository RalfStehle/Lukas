package de.project.lukas.ui;

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

import de.project.lukas.R;
import de.project.lukas.databinding.LayoutTrainBaseItemBinding;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.TrainBase;


public class TrainBaseFragment extends DeviceFragment {
    private final int disconnectMenuItemId = View.generateViewId();
    private final int switchOffMenuItemId = View.generateViewId();
    private final int renameMenuItemId = View.generateViewId();
    private LayoutTrainBaseItemBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutTrainBaseItemBinding.inflate(inflater, container, false);

        binding.MotorSlowerButton.setOnClickListener(view1 -> getTrain().motorSlower());
        binding.MotorStopButton.setOnClickListener(view1 -> getTrain().motorStop());
        binding.MotorFasterButton.setOnClickListener(view1 -> getTrain().motorFaster());
        binding.LedColorButton.setOnClickListener(view1 -> getTrain().setLedColorHub());
        binding.sound1.setOnClickListener(view1 -> getTrain().sound1());
        binding.sound2.setOnClickListener(view1 -> getTrain().sound2());
        binding.sound3.setOnClickListener(view1 -> getTrain().sound3());
        binding.sound4.setOnClickListener(view1 -> getTrain().sound4());
        binding.sound1.setOnLongClickListener(view1 -> {
            getTrain().sound1();
            return true;
        });


        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(Integer.toString(value)));

        getMessage().observe(getViewLifecycleOwner(),
                value -> binding.Message.setText(value));   // 2024-01-17 Ralf

        registerForContextMenu(binding.Card);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, disconnectMenuItemId, 0, R.string.menu_disconnect);
        menu.add(Menu.NONE, switchOffMenuItemId, 0, R.string.menu_switchoff);
        menu.add(Menu.NONE, renameMenuItemId, 0, R.string.rename);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();


        if (itemId == disconnectMenuItemId) {
            DevicesManager.getInstance().removeDevice(getDevice());
            return true;
        } else if (itemId == switchOffMenuItemId) {
            DevicesManager.getInstance().switchOffDevice(getDevice());
            return true;
        } else if (itemId == renameMenuItemId) {
            rename();
            return true;
        }

        return false;
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

    private TrainBase getTrain() {
        return (TrainBase) getDevice();
    }
}
