package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

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
    private final List<RemoteController> controllers = new ArrayList<>();
    private LayoutRemoteItemBinding binding;
    private ArrayAdapter<RemoteController> spinnerAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRemoteItemBinding.inflate(inflater, container, false);

        spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllers);

        DevicesManager.getInstance().getDevices().observe(getViewLifecycleOwner(), value -> {
            controllers.clear();
            controllers.add(RemoteController.noop());

            for (Device device : value) {
                if (device instanceof TrainHub) {
                    controllers.add(RemoteController.motor((TrainHub) device));
                    controllers.add(RemoteController.light((TrainHub) device));
                }
            }

            spinnerAdapter.notifyDataSetChanged();
        });

        getLiveDevice().observe(getViewLifecycleOwner(), device -> {
            Remote remote = (Remote) device;

            binding.TrainASpinner.setSelection(controllers.indexOf(remote.getControllerA()));
            binding.TrainBSpinner.setSelection(controllers.indexOf(remote.getControllerB()));
        });

        binding.TrainASpinner.setAdapter(spinnerAdapter);
        binding.TrainASpinner.setSelection(controllers.indexOf(getRemote().getControllerA()));
        binding.TrainASpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                RemoteController controller = (RemoteController) parent.getItemAtPosition(pos);

                getRemote().setControllerA(controller);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.TrainBSpinner.setAdapter(spinnerAdapter);
        binding.TrainBSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                RemoteController controller = (RemoteController) parent.getItemAtPosition(pos);

                getRemote().setControllerB(controller);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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

    private Remote getRemote() {
        return (Remote) getDevice();
    }
}
