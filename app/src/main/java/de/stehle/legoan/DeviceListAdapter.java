package de.stehle.legoan;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

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

        if (view == null || view.getTag() != device) {
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            if (getItemViewType(i) == 0) {
                view = layoutInflater.inflate(R.layout.layout_train_item, viewGroup, false);

                new TrainHubAdapter((TrainHub)device, view, activity).connect();
            } else {
                view = layoutInflater.inflate(R.layout.layout_switch_item, viewGroup, false);

                new SwitchHubAdapter((Switch)device, view, activity).connect();
            }
        }

        return view;
    }

    static class TrainHubAdapter implements ChangeListener {
        private final TrainHub device;
        private final View view;
        private final Activity activity;

        TrainHubAdapter(TrainHub device, View view, Activity activity) {
            // Attach the hub to the view, so that we can later access it when we create the context menu.
            this.device = device;
            this.view = view;
            this.view.setTag(device);
            this.activity = activity;
        }

        public void connect() {
            device.subscribe(this);

            activity.registerForContextMenu(view);

            view.findViewById(R.id.StopButton).setOnClickListener(view1 -> device.stop());
            view.findViewById(R.id.SlowerButton).setOnClickListener(view1 -> device.decrementSpeed());
            view.findViewById(R.id.FasterButton).setOnClickListener(view1 -> device.incrementSpeed());
            view.findViewById(R.id.LightButton).setOnClickListener(view1 -> device.nextLedColor());

            updateValues();
        }

        private void updateValues() {
            ((TextView)view.findViewById(R.id.NameContent))
                    .setText(device.getName());

            ((TextView)view.findViewById(R.id.ConnectedContent))
                    .setText(device.isConnected() ? "Yes" : "No");

            ((TextView)view.findViewById(R.id.BatteryContent))
                    .setText(String.format(Locale.getDefault(), "%d %%", device.getBattery()));
        }

        @Override
        public void notifyChanged() {
            activity.runOnUiThread(this::updateValues);
        }
    }

    static class SwitchHubAdapter implements ChangeListener {
        private final Switch device;
        private final View view;
        private final Activity activity;

        SwitchHubAdapter(Switch device, View view, Activity activity) {
            // Attach the hub to the view, so that we can later access it when we create the context menu.
            this.device = device;
            this.view = view;
            this.view.setTag(device);
            this.activity = activity;
        }

        public void connect() {
            device.subscribe(this);

            activity.registerForContextMenu(view);

            view.findViewById(R.id.ToggleButton).setOnClickListener(view1 -> device.toggle());

            updateValues();
        }

        private void updateValues() {
            ((TextView)view.findViewById(R.id.NameContent))
                    .setText(device.getName());

            ((TextView)view.findViewById(R.id.ConnectedContent))
                    .setText(device.isConnected() ? "Yes" : "No");

            ((TextView)view.findViewById(R.id.BatteryContent))
                    .setText(String.format(Locale.getDefault(), "%d %%", device.getBattery()));
        }

        @Override
        public void notifyChanged() {
            activity.runOnUiThread(this::updateValues);
        }
    }
}
