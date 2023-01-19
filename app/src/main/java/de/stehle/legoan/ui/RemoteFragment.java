package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutRemoteItemBinding;
import de.stehle.legoan.model.Device;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Remote;
import de.stehle.legoan.model.RemoteController;
import de.stehle.legoan.model.TrainHub;

public class RemoteFragment extends DeviceFragment {
    private LayoutRemoteItemBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRemoteItemBinding.inflate(inflater, container, false);

        binding.TrainARadio
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton != null) {
                        getRemote().setControllerA((RemoteController) radioButton.getTag());
                    }
                });

        binding.TrainBRadio
                .setOnCheckedChangeListener((target, id) -> {
                    RadioButton radioButton = (RadioButton) target.findViewById(id);

                    if (radioButton != null) {
                        getRemote().setControllerB((RemoteController) radioButton.getTag());
                    }
                });

        checkNone(binding.TrainARadio);
        checkNone(binding.TrainBRadio);

        DevicesManager.getInstance().getDevices().observe(getViewLifecycleOwner(), this::updateTrains);

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

        menu.add(0, v.getId(), 0, R.string.menu_disconnect);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        DevicesManager.getInstance().removeDevice(getDevice());
        return true;
    }

    private void updateTrains(List<Device> devices) {
        if (binding == null) {
            return;
        }

        List<RemoteController> controllers = new ArrayList<>();

        for (Device device : devices) {
            if (device instanceof TrainHub) {
                controllers.add(RemoteController.motor((TrainHub) device));
                controllers.add(RemoteController.light((TrainHub) device));
            }
        }

        Remote remote = getRemote();

        RemoteController controllerA = remote.getControllerA();
        RemoteController controllerB = remote.getControllerB();

        if (!controllers.contains(controllerA)) {
            remote.setControllerA(null);
        }

        if (!controllers.contains(controllerB)) {
            remote.setControllerB(null);
        }

        updateRadio(binding.TrainARadio, controllers, remote.getControllerA());
        updateRadio(binding.TrainBRadio, controllers, remote.getControllerB());
    }

    private void updateRadio(RadioGroup radioGroup, List<RemoteController> controllers, RemoteController connectedController) {
        int targetSize = controllers.size() + 1;

        while (radioGroup.getChildCount() > targetSize) {
            radioGroup.removeViewAt(radioGroup.getChildCount() - 1);
        }

        while (radioGroup.getChildCount() < targetSize) {
            RadioButton radioButton = new RadioButton(radioGroup.getContext());
            radioButton.setId(View.generateViewId());

            radioGroup.addView(radioButton);
        }

        int index = 1;
        for (RemoteController controller : controllers) {
            RadioButton motorRadio = (RadioButton) radioGroup.getChildAt(index);
            motorRadio.setTag(controller);
            motorRadio.setText(controller.getName());
            index++;
        }

        int indexOfSelection = controllers.indexOf(connectedController);

        if (indexOfSelection >= 0) {
            View selectedButton = radioGroup.getChildAt(indexOfSelection);

            if (selectedButton != null) {
                radioGroup.check(selectedButton.getId());
                return;
            }
        }

        checkNone(radioGroup);
    }

    private void checkNone(RadioGroup radioGroup) {
        radioGroup.check(radioGroup.getChildAt(0).getId());
    }

    private Remote getRemote() {
        return (Remote) getDevice();
    }
}
