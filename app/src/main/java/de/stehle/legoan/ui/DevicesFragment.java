package de.stehle.legoan.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import de.stehle.legoan.databinding.FragmentDevicesBinding;
import de.stehle.legoan.databinding.FragmentRemotesBinding;
import de.stehle.legoan.model.Device;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Remote;
import de.stehle.legoan.model.Switch;
import de.stehle.legoan.model.TrainHub;

public class DevicesFragment extends Fragment {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private FragmentDevicesBinding binding;
    private DeviceListAdapter devicesAdapterLeft;
    private DeviceListAdapter devicesAdapterRight;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);

        List<Device> devices = devicesManager.getDevices().getValue();

        if (binding.ListViewRight != null) {
            devicesAdapterLeft = new DeviceListAdapter(devices, getParentFragmentManager(), d -> d instanceof TrainHub);
            devicesAdapterRight = new DeviceListAdapter(devices, getParentFragmentManager(), d -> d instanceof Switch);

            binding.ListViewLeft.setAdapter(devicesAdapterLeft);
            binding.ListViewRight.setAdapter(devicesAdapterRight);
        } else {
            devicesAdapterLeft = new DeviceListAdapter(devices, getParentFragmentManager(), d -> !(d instanceof Remote));

            binding.ListViewLeft.setAdapter(devicesAdapterLeft);
        }

        devicesManager.getDevices().observe(getViewLifecycleOwner(), this::updateDevices);

        return binding.getRoot();
    }

    private void updateDevices(List<Device> devices) {
        if (devicesAdapterLeft != null) {
            devicesAdapterLeft.notifyDataSetChanged();
        }

        if (devicesAdapterRight != null) {
            devicesAdapterRight.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
