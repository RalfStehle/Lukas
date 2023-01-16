package de.stehle.legoan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

abstract class DeviceFragment extends Fragment implements ChangeListener {
    private static final String ARG_DEVICE = "device";
    private Device device;

    protected Device getDevice() {
        return device;
    }

    public void setDevice(Device newDevice) {
        if (newDevice == device) {
            return;
        }

        if (device != null) {
            device.unsubscribe(this);
        }

        device = newDevice;

        if (device != null) {
            device.subscribe(this);
        }

        notifyDeviceChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notifyDeviceChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setDevice(null);
    }

    @Override
    public void notifyChanged() {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(this::notifyDeviceChanged);
        }
    }

    private void notifyDeviceChanged() {
        if (getView() == null || getDevice() == null) {
            return;
        }

        requireActivity().registerForContextMenu(requireView());
        requireView().setTag(device);

        onDeviceChanged(getDevice());
    }

    protected abstract void onDeviceChanged(Device device);

    public static Device getDevice(View view) {
        return (Device) view.getTag();
    }
}
