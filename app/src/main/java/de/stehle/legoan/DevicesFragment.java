package de.stehle.legoan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import de.stehle.legoan.databinding.FragmentRemotesBinding;

public class DevicesFragment extends Fragment {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private FragmentRemotesBinding binding;
    private DeviceListAdapter devicesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRemotesBinding.inflate(inflater, container, false);

        devicesManager.getDevices().observe(getViewLifecycleOwner(), this::updateDevices);

        return binding.getRoot();
    }

    private void updateDevices(List<Device> devices) {
        if (devicesAdapter == null) {
            devicesAdapter = new DeviceListAdapter(devices, getParentFragmentManager());

            binding.RemotesListView.setAdapter(devicesAdapter);
        } else {
            devicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
