package de.stehle.legoan;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {
    private final List<Device> devices;
    private final Activity activity;
    private LayoutInflater layoutInflater;

    public DeviceListAdapter(List<Device> devices, Activity activity) {
        this.devices = devices;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);

        if (item instanceof TrainHub) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Device device = devices.get(i);

        // Disconnect the adapter if is associated to another device.
        boolean isDisconnected = Adapter.disconnect(view, device);

        int viewType = getItemViewType(i);

        if (view == null) {
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            if (viewType == 0) {
                view = layoutInflater.inflate(R.layout.layout_train_item, viewGroup, false);
            } else {
                view = layoutInflater.inflate(R.layout.layout_switch_item, viewGroup, false);
            }

            activity.registerForContextMenu(view);
        }

        // Only create the adapter when necessary.
        if (isDisconnected) {
            if (viewType == 0) {
                new TrainHubAdapter((TrainHub)device, view, activity).connect();
            } else {
                new SwitchHubAdapter((Switch)device, view, activity).connect();
            }
        }

        return view;
    }

}
