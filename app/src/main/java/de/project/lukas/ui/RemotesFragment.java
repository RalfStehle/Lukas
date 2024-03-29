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

import de.project.lukas.databinding.FragmentRemotesBinding;
import de.project.lukas.model.Device;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.Remote;

@SuppressLint("NotifyDataSetChanged")
public class RemotesFragment extends Fragment {
    private final DevicesManager devicesManager = DevicesManager.getInstance();
    private FragmentRemotesBinding binding;
    private DeviceListAdapter devicesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRemotesBinding.inflate(inflater, container, false);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.VERTICAL, false);

        devicesAdapter = new DeviceListAdapter(devicesManager.getDevices().getValue(), d -> d instanceof Remote);
        devicesManager.getDevices().observe(getViewLifecycleOwner(), this::updateDevices);

        binding.ListView.setAdapter(devicesAdapter);
        binding.ListView.setLayoutManager(layoutManager);

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
