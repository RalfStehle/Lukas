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

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutSwitchItemBinding;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Switch;
import de.stehle.legoan.utils.HexUtils;

public class SwitchFragment extends DeviceFragment {
    private final int disconnectMenuItemId = View.generateViewId();
    private final int setServoMenuItemId = View.generateViewId();
    private LayoutSwitchItemBinding binding;
    private String servoSetting = "00 78";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutSwitchItemBinding.inflate(inflater, container, false);

        binding.ToggleButton1.setOnClickListener(view1 -> getSwitch().toggle1());
        binding.ToggleButton2.setOnClickListener(view1 -> getSwitch().toggle2());

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
        menu.add(Menu.NONE, setServoMenuItemId, 0, R.string.menu_servo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == disconnectMenuItemId) {
            DevicesManager.getInstance().removeDevice(getDevice());
            return true;
        } else if (itemId == setServoMenuItemId) {
            testServo();
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void testServo() {
        new ConfirmBuilder(requireActivity())
                .setTitle(R.string.setup_servo)
                .setConfirmText(R.string.send)
                .setValue(servoSetting)
                .show(value -> {
                    servoSetting = value;
                    getSwitch().send(HexUtils.hexStringToByteArray(value));
                });
    }

    private Switch getSwitch() {
        return (Switch) getDevice();
    }
}
