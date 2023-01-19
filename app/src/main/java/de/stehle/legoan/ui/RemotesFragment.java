package de.stehle.legoan.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import de.stehle.legoan.databinding.FragmentRemotesBinding;
import de.stehle.legoan.model.Device;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Remote;
import de.stehle.legoan.model.TrainHub;

public class RemotesFragment extends Fragment {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private FragmentRemotesBinding binding;
    private DeviceListAdapter devicesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRemotesBinding.inflate(inflater, container, false);

        devicesAdapter = new DeviceListAdapter(devicesManager.getDevices().getValue(), getParentFragmentManager(), d -> d instanceof Remote);
        devicesManager.getDevices().observe(getViewLifecycleOwner(), this::updateDevices);

        binding.ListViewLeft.setAdapter(devicesAdapter);

        return binding.getRoot();
    }

    private void updateDevices(List<Device> devices) {
        if (devicesAdapter != null) {
            devicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
