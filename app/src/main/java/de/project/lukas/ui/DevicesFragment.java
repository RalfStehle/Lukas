package de.project.lukas.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import de.project.lukas.databinding.FragmentDevicesBinding;
import de.project.lukas.model.Device;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.Remote;

@SuppressLint("NotifyDataSetChanged")
public class DevicesFragment extends Fragment {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private FragmentDevicesBinding binding;
    private DeviceListAdapter devicesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDevicesBinding.inflate(inflater, container, false);

        List<Device> devices = devicesManager.getDevices().getValue();

        devicesAdapter = new DeviceListAdapter(devices, d -> !(d instanceof Remote));
        devicesAdapter.notifyDataSetChanged();

        if (binding.GridView != null) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);

            binding.GridView.setLayoutManager(layoutManager);
            binding.GridView.setAdapter(devicesAdapter);
        } else if (binding.ListView != null) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.VERTICAL, false);

            binding.ListView.setLayoutManager(layoutManager);
            binding.ListView.setAdapter(devicesAdapter);
        }

        devicesManager.getDevices().observe(getViewLifecycleOwner(),
                this::updateDevices);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateDevices(List<Device> devices) {
        if (devicesAdapter != null) {
            devicesAdapter.refresh();
        }
    }
}
