package de.project.lukas.ui;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.recyclerview.widget.RecyclerView;

import de.project.lukas.model.Device;

public abstract class DeviceFragment extends RecyclerView.ViewHolder {
    private final MutableLiveData<Device> device = new MutableLiveData<>();

    private final LiveData<String> name =
            Transformations.switchMap(device, device -> device != null ? device.getName() : null);

    private final LiveData<Integer> battery =
            Transformations.switchMap(device, device -> device != null ? device.getBattery() : null);

    private final LiveData<String> message =
            Transformations.switchMap(device, device -> device != null ? device.getMessage() : null);    // 2024-01-17 Ralf

    protected Device getDevice() {
        return device.getValue();
    }

    protected LiveData<Device> getLiveDevice() {
        return device;
    }

    protected LiveData<String> getName() {
        return name;
    }

    protected LiveData<String> getMessage() {
        return message;
    }

    protected LiveData<Integer> getBattery() {
        return battery;
    }

    protected DeviceFragment(View view) {
        super(view);
    }

    public void setDevice(Device newDevice) {
        if (newDevice != device.getValue()) {
            device.setValue(newDevice);
        }
    }
}
