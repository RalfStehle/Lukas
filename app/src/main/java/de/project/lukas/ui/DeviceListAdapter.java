package de.project.lukas.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.project.lukas.databinding.FragmentBlankBinding;
import de.project.lukas.model.Device;
import de.project.lukas.model.Remote;
import de.project.lukas.model.Switch;
import de.project.lukas.model.TrainBase;
import de.project.lukas.model.TrainHub;

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_REMOTE = 1;
    private static final int TYPE_SWITCH = 2;
    private static final int TYPE_TRAIN_BASE = 3;
    private static final int TYPE_TRAIN_HUB = 4;
    private final List<Device> devices;
    private final DeviceFilter filter;
    private final List<Object> items = new ArrayList<>();

    static final class Dummy {

    }

    public DeviceListAdapter(List<Device> devices, DeviceFilter filter) {
        this.devices = devices;
        this.filter = filter;

        refresh();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        items.clear();

        Device prevDevice = null;
        for (Device device : devices) {
            if (filter == null || filter.shouldUse(device)) {
                if (items.size() % 2 == 1 &&
                    prevDevice != null &&
                    prevDevice.getClass() != device.getClass()) {
                    items.add(new Dummy());
                }

                items.add(device);
                prevDevice = device;
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object device = items.get(position);

        if (device == null) {
            return 0;
        } else if (device instanceof Remote) {
            return TYPE_REMOTE;
        } else if (device instanceof Switch) {
            return TYPE_SWITCH;
        } else if (device instanceof TrainBase) {
            return TYPE_TRAIN_BASE;
        } else if (device instanceof TrainHub) {
            return TYPE_TRAIN_HUB;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_REMOTE) {
            return RemoteFragment.create(inflater, parent);
        } else if (viewType == TYPE_SWITCH) {
            return SwitchFragment.create(inflater, parent);
        } else if (viewType == TYPE_TRAIN_BASE) {
            return TrainBaseFragment.create(inflater, parent);
        } else if (viewType == TYPE_TRAIN_HUB) {
            return TrainHubFragment.create(inflater, parent);
        } else {
            return PlaceholderFragment.create(inflater, parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (item instanceof Device) {
            ((DeviceFragment)holder).setDevice((Device)item);
        }
    }
}
