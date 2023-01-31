package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import java.nio.charset.StandardCharsets;

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutSwitchItemBinding;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Switch;

public class SwitchFragment extends DeviceFragment {
    private final int contextMenuId = View.generateViewId();
    private final int setServoMenuId = View.generateViewId();
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

        menu.add(Menu.NONE, contextMenuId, 0, R.string.menu_disconnect);
        menu.add(Menu.NONE, setServoMenuId, 0, R.string.menu_servo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == contextMenuId) {
            DevicesManager.getInstance().removeDevice(getDevice());
            return true;
        }
        if (item.getItemId() == setServoMenuId) {
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

    private Switch getSwitch() {
        return (Switch) getDevice();
    }

    // Ergänzung von Ralf
    private void setServoSetting() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.servo_dialog);

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.50);

        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.background_light));

        EditText _servopos1 = (EditText) dialog.findViewById(R.id.servopos1);
        EditText _servopos2 = (EditText) dialog.findViewById(R.id.servopos2);
        Button btn_yes = (Button) dialog.findViewById(R.id.button1);
        Button btn_cancel = (Button) dialog.findViewById(R.id.button2);

        _servopos1.setInputType(InputType.TYPE_CLASS_NUMBER);
        _servopos2.setInputType(InputType.TYPE_CLASS_NUMBER);

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                servopos1 = _servopos1.getText().toString();
                servopos2 = _servopos2.getText().toString();
                String servoSetting = servopos1 + "#" + servopos2;
                getSwitch().send(servoSetting.getBytes(StandardCharsets.UTF_8));
                dialog.cancel();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
}

    private void testServo() {
        new ConfirmBuilder(requireActivity())
                .setTitle(R.string.setup_servo)
                .setConfirmText(R.string.send)
                .setValue(servopos1)
                .show(value -> {
                    servopos1 = value;
                    getSwitch().send(servopos1.getBytes(StandardCharsets.UTF_8));
                });
    }
}
