package de.project.lukas.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import de.project.lukas.MainActivity;
import de.project.lukas.databinding.FragmentDevicesBinding;
import de.project.lukas.model.Device;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.Remote;
import de.project.lukas.model.Switch;
import de.project.lukas.model.TrainHub;

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateDevices(List<Device> devices) {
        if (devicesAdapterLeft != null) {
            devicesAdapterLeft.notifyDataSetChanged();
        }

        if (devicesAdapterRight != null) {
            devicesAdapterRight.notifyDataSetChanged();
        }
    }

}
