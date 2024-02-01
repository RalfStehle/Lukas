package de.project.lukas.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.project.lukas.R;
import de.project.lukas.databinding.LayoutSwitchItemBinding;
import de.project.lukas.databinding.ServoDialogBinding;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.Switch;

public class SwitchFragment extends DeviceFragment implements View.OnCreateContextMenuListener {
    @NonNull
    private final LayoutSwitchItemBinding binding;

    public static SwitchFragment create(LayoutInflater inflater, @Nullable ViewGroup container) {
        return new SwitchFragment(LayoutSwitchItemBinding.inflate(inflater, container, false));
    }

    @SuppressLint("SetTextI18n")
    private SwitchFragment(LayoutSwitchItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        binding.Card.setOnCreateContextMenuListener(this);
        binding.ToggleButton1.setOnClickListener(view1 -> getSwitch().toggle1());
        binding.ToggleButton2.setOnClickListener(view1 -> getSwitch().toggle2());

        getName().observeForever(
                binding.NameContent::setText);

        getBattery().observeForever(
                value -> binding.BatteryContent.setText(Integer.toString(value)));
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.menu_disconnect)
                .setOnMenuItemClickListener(item -> {
                    DevicesManager.getInstance().switchOffDevice(getDevice());
                    return true;
                });

        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.menu_servo)
                .setOnMenuItemClickListener(item -> {
                    setServoSetting();
                    return true;
                });
    }
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void setServoSetting() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        ServoDialogBinding binding = ServoDialogBinding.inflate(inflater);

        int width = (int)(getActivity().getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getActivity().getResources().getDisplayMetrics().heightPixels*0.50);

        final Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.background_light));

        binding.Position1.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.Position1.setText(Integer.toString(getSwitch().getServoLow()));
        binding.Position2.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.Position2.setText(Integer.toString(getSwitch().getServoHigh()));

        binding.CancelButton.setOnClickListener(view -> dialog.cancel());
        binding.OkButton.setOnClickListener(view -> {
            String servoLow = binding.Position1.getText().toString();
            String servoHigh = binding.Position2.getText().toString();

            getSwitch().adjustServo(Integer.parseInt(servoLow), Integer.parseInt(servoHigh));
            dialog.cancel();
        });

        dialog.show();
    }

    private Activity getActivity() {
        return (Activity) binding.getRoot().getContext();
    }

    private Switch getSwitch() {
        return (Switch) getDevice();
    }
}
