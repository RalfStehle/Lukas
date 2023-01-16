package de.stehle.legoan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import de.stehle.legoan.databinding.LayoutSwitchItemBinding;

public class SwitchFragment extends DeviceFragment {
    private LayoutSwitchItemBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutSwitchItemBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected void onDeviceChanged(Device device) {
        binding.NameContent
                .setText(device.getName());

        binding.ConnectedContent
                .setText(device.isConnected() ? "Yes" : "No");

        binding.BatteryContent
                .setText(String.format(Locale.getDefault(), "%d %%", device.getBattery()));
    }
}
