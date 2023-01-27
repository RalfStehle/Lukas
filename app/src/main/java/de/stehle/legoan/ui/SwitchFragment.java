package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutSwitchItemBinding;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Switch;

public class SwitchFragment extends DeviceFragment {
    private final int contextMenuId = View.generateViewId();
    private final int setServoMenuId = View.generateViewId();
    private LayoutSwitchItemBinding binding;
    private String servoSetting = "0x00 (byte)0x78";

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
            inputBox();
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

    // Test von Ralf
    private void inputBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Servo einstellen");
        final EditText value = new EditText(getActivity());   // Set up the input
        value.setText(servoSetting);  //
        value.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(value);

        builder.setPositiveButton("Senden", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                servoSetting = value.getText().toString();
                //Switch.send(new byte[]);
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
