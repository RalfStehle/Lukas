package de.stehle.legoan;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

abstract class DeviceFragment extends Fragment {
    private final MutableLiveData<Device> device = new MutableLiveData<>();
    private final LiveData<String> name =
            Transformations.map(device, device -> device != null ? device.getName() : null);
    private final LiveData<Boolean> connected =
            Transformations.switchMap(device, device -> device != null ? device.getConnected() : null);
    private final LiveData<Integer> battery =
            Transformations.switchMap(device, device -> device != null ? device.getBattery() : null);

    protected Device getDevice() {
        return device.getValue();
    }

    protected LiveData<String> getName() {
        return name;
    }

    protected LiveData<Boolean> getConnected() {
        return connected;
    }

    protected LiveData<Integer> getBattery() {
        return battery;
    }

    public void setDevice(Device newDevice) {
        device.setValue(newDevice);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        device.observe(getViewLifecycleOwner(), device -> {
            requireActivity().registerForContextMenu(view);
            requireView().setTag(device);
        });
    }

    public static Device getDevice(View view) {
        return (Device) view.getTag();
    }
}
