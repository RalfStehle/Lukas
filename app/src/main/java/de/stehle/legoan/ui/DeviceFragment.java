package de.stehle.legoan.ui;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import de.stehle.legoan.model.Device;

public abstract class DeviceFragment extends Fragment {
    private final MutableLiveData<Device> device = new MutableLiveData<>();
    private final LiveData<String> name =
            Transformations.map(device, device -> device != null ? device.getName() : null);
    private final LiveData<Integer> battery =
            Transformations.switchMap(device, device -> device != null ? device.getBattery() : null);

    protected Device getDevice() {
        return device.getValue();
    }

    protected LiveData<String> getName() {
        return name;
    }

    protected LiveData<Integer> getBattery() {
        return battery;
    }

    public void setDevice(Device newDevice) {
        device.setValue(newDevice);
    }

    public static Device getDevice(View view) {
        return (Device) view.getTag();
    }
}
