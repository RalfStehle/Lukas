package de.project.lukas.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import de.project.lukas.R;
import de.project.lukas.databinding.LayoutRemoteItemBinding;
import de.project.lukas.model.Device;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.Remote;
import de.project.lukas.model.RemoteController;
import de.project.lukas.model.Switch;
import de.project.lukas.model.TrainBase;
import de.project.lukas.model.TrainHub;

public class RemoteFragment extends DeviceFragment implements View.OnCreateContextMenuListener {
    private final List<RemoteController> controllers = new ArrayList<>();
    private final LayoutRemoteItemBinding binding;
    private final RemoteControllerListAdapter spinnerAdapter;

    public static RemoteFragment create(LayoutInflater inflater, @Nullable ViewGroup container) {
        return new RemoteFragment(LayoutRemoteItemBinding.inflate(inflater, container, false));
    }

    @SuppressLint("SetTextI18n")
    private RemoteFragment(LayoutRemoteItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        spinnerAdapter = new RemoteControllerListAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, controllers);

        DevicesManager.getInstance().getDevices().observeForever(value -> {
            controllers.clear();
            controllers.add(RemoteController.noop());

            for (Device device : value) {
                if (device instanceof TrainHub) {
                    controllers.add(((TrainHub) device).getMotorController());
                    controllers.add(((TrainHub) device).getLightController());
                } else if ((device instanceof TrainBase)) {
                    controllers.add(((TrainBase) device).getMotorController());
                    controllers.add(((TrainBase) device).getLightController());
                } else if (device instanceof Switch) {
                    controllers.add(((Switch) device).getController());
                }
            }

            spinnerAdapter.notifyDataSetChanged();
        });

        getLiveDevice().observeForever(device -> {
            Remote remote = (Remote) device;

            binding.TrainASpinner.setSelection(controllers.indexOf(remote.getControllerA()));
            binding.TrainBSpinner.setSelection(controllers.indexOf(remote.getControllerB()));
        });

        binding.Card.setOnCreateContextMenuListener(this);
        binding.TrainASpinner.setAdapter(spinnerAdapter);
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

        getName().observeForever(
                binding.NameContent::setText);

        getBattery().observeForever(
                value -> binding.BatteryContent.setText(Integer.toString(value)));

    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.menu_disconnect)
                .setOnMenuItemClickListener(item -> {
                    DevicesManager.getInstance().removeDevice(getDevice());
                    return true;
                });

        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.menu_switchoff)
                .setOnMenuItemClickListener(item -> {
                    DevicesManager.getInstance().switchOffDevice(getDevice());
                    return true;
                });

        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.rename)
                .setOnMenuItemClickListener(item -> {
                    rename();
                    return true;
                });
    }

    private void rename() {
        new ConfirmBuilder(getActivity())
                .setTitle(R.string.rename)
                .setConfirmText(R.string.rename)
                .setValue(getName().getValue())
                .setLMaxLength(14)
                .show(value -> {
                    getRemote().rename(value);
                });
    }

    private Activity getActivity() {
        return (Activity) binding.getRoot().getContext();
    }

    private Remote getRemote() {
        return (Remote) getDevice();
    }
}
