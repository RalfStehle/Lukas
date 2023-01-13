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
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Device device = devices.get(i);

        if (view == null || view.getTag() != device) {
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            if (device instanceof TrainHub) {
                view = layoutInflater.inflate(R.layout.layout_train_item, viewGroup, false);

                new TrainHubAdapter((TrainHub)device, view, activity).connect();
            } else if (device instanceof SwitchHub) {
                view = layoutInflater.inflate(R.layout.layout_switch_item, viewGroup, false);

                new SwitchHubAdapter((SwitchHub)device, view, activity).connect();
            }
        }

        return view;
    }

    static class TrainHubAdapter implements ChangeListener {
        private final TrainHub hub;
        private final View view;
        private final Activity activity;

        TrainHubAdapter(TrainHub hub, View view, Activity activity) {
            // Attach the hub to the view, so that we can later access it when we create the context menu.
            this.hub = hub;
            this.view = view;
            this.view.setTag(hub);
            this.activity = activity;
        }

        public void connect() {
            hub.subscribe(this);

            activity.registerForContextMenu(view);

            view.findViewById(R.id.StopButton).setOnClickListener(view1 -> hub.stop());
            view.findViewById(R.id.SlowerButton).setOnClickListener(view1 -> hub.decrementSpeed());
            view.findViewById(R.id.FasterButton).setOnClickListener(view1 -> hub.incrementSpeed());
            view.findViewById(R.id.LightButton).setOnClickListener(view1 -> hub.nextLedColor());

            updateValues();
        }

        private void updateValues() {
            ((TextView)view.findViewById(R.id.NameContent))
                    .setText(hub.getName());

            ((TextView)view.findViewById(R.id.ConnectedContent))
                    .setText(hub.isConnected() ? "Yes" : "No");

            ((TextView)view.findViewById(R.id.BatteryContent))
                    .setText(String.format(Locale.getDefault(), "%d %%", hub.getBattery()));
        }

        @Override
        public void notifyChanged() {
            activity.runOnUiThread(this::updateValues);
        }
    }

    static class SwitchHubAdapter implements ChangeListener {
        private final SwitchHub hub;
        private final View view;
        private final Activity activity;

        SwitchHubAdapter(SwitchHub hub, View view, Activity activity) {
            // Attach the hub to the view, so that we can later access it when we create the context menu.
            this.hub = hub;
            this.view = view;
            this.view.setTag(hub);
            this.activity = activity;
        }

        public void connect() {
            hub.subscribe(this);

            activity.registerForContextMenu(view);

            view.findViewById(R.id.ToggleButton).setOnClickListener(view1 -> hub.toggle());

            updateValues();
        }

        private void updateValues() {
            ((TextView)view.findViewById(R.id.NameContent))
                    .setText(hub.getName());

            ((TextView)view.findViewById(R.id.ConnectedContent))
                    .setText(hub.isConnected() ? "Yes" : "No");

            ((TextView)view.findViewById(R.id.BatteryContent))
                    .setText(String.format(Locale.getDefault(), "%d %%", hub.getBattery()));
        }

        @Override
        public void notifyChanged() {
            activity.runOnUiThread(this::updateValues);
        }
    }
}
