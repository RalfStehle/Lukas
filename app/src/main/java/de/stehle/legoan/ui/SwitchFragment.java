package de.stehle.legoan.ui;

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

        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getConnected().observe(getViewLifecycleOwner(),
                value -> binding.ConnectedContent.setText(value ? "Yes" : "No"));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(String.format(Locale.getDefault(), "%d %%", value)));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
