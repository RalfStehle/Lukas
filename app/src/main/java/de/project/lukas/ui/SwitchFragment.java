package de.project.lukas.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

import de.project.lukas.R;
import de.project.lukas.databinding.LayoutSwitchItemBinding;
import de.project.lukas.databinding.ServoDialogBinding;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.Switch;

public class SwitchFragment extends DeviceFragment {
    private final int disconnectMenuItemId = View.generateViewId();
    private final int setServoMenuItemId = View.generateViewId();
    private LayoutSwitchItemBinding binding;
    private String servopos1 = "0";
    private String servopos2 = "120";


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
            setServoSetting();
            return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Ergänzung von Ralf
    @SuppressLint("ResourceAsColor")
    private void setServoSetting() {
        ServoDialogBinding binding = ServoDialogBinding.inflate(getLayoutInflater());

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(binding.getRoot());

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.50);

        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.background_light));

        binding.Position1.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.Position2.setInputType(InputType.TYPE_CLASS_NUMBER);

        binding.OkButton.setOnClickListener(view -> {
            servopos1 = binding.Position1.getText().toString();
            servopos2 = binding.Position2.getText().toString();

            String servoSetting = servopos1 + "#" + servopos2;

            getSwitch().send(servoSetting.getBytes(StandardCharsets.UTF_8));
            dialog.cancel();
        });
        binding.CancelButton.setOnClickListener(view -> dialog.cancel());
        dialog.show();
    }

    private Switch getSwitch() {
        return (Switch) getDevice();
    }
}
