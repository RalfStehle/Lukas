package de.stehle.legoan;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class TrainHubListAdapter extends BaseAdapter {
    private final List<TrainHub> trains;
    private final Activity activity;
    private LayoutInflater layoutInflater;

    public TrainHubListAdapter(List<TrainHub> trains, Activity activity) {
        this.trains = trains;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return trains.size();
    }

    @Override
    public Object getItem(int i) {
        return trains.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            if (layoutInflater == null) {
                layoutInflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            view = layoutInflater.inflate(R.layout.layout_train_item, viewGroup, false);

            new Connector(trains.get(i), view, activity).connect();
        }

        return view;
    }

    static class Connector implements ChangeListener {
        private final TrainHub hub;
        private final View view;
        private final Activity activity;

        Connector(TrainHub hub, View view, Activity activity) {
            this.hub = hub;
            this.view = view;
            this.activity = activity;
        }

        public void connect() {
            hub.subscribe(this);

            view.findViewById(R.id.StopButton).setOnClickListener(view1 -> {
                hub.stop();
            });

            view.findViewById(R.id.SlowerButton).setOnClickListener(view1 -> {
                hub.decrementSpeed();
            });

            view.findViewById(R.id.FasterButton).setOnClickListener(view1 -> {
                hub.incrementSpeed();
            });

            updateValues();
        }

        private void updateValues() {
            ((TextView)view.findViewById(R.id.NameContent))
                    .setText(hub.getName());

            ((TextView)view.findViewById(R.id.ConnectedContent))
                    .setText(hub.isConnected() ? "Yes" : "No");

            ((TextView)view.findViewById(R.id.BatteryContent))
                    .setText(Integer.toString(hub.getBattery()).concat(" %"));
        }

        @Override
        public void notifyChanged() {
            activity.runOnUiThread(this::updateValues);
        }
    }
}
