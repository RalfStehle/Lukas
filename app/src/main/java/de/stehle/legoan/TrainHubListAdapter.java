package de.stehle.legoan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class TrainHubListAdapter extends BaseAdapter {
    private final List<TrainHub> trains;
    private LayoutInflater layoutInflater;

    public TrainHubListAdapter(List<TrainHub> trains) {
        this.trains = trains;
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

            new Connector(trains.get(i), view).connect();
        }

        return view;
    }

    class Connector implements ChangeListener {
        private final TrainHub hub;
        private final View view;

        Connector(TrainHub hub, View view) {
            this.hub = hub;
            this.view = view;
        }

        public void connect() {
            hub.subscribe(this);

            view.findViewById(R.id.stopButton).setOnClickListener(view1 -> {
                hub.stop();
            });

            view.findViewById(R.id.slowerButton).setOnClickListener(view1 -> {
                hub.decrementSpeed();
            });

            view.findViewById(R.id.fasterButton).setOnClickListener(view1 -> {
                hub.incrementSpeed();
            });

            updateValues();
        }

        private void updateValues() {
            ((TextView)view.findViewById(R.id.nameContent))
                    .setText(hub.getName());

            ((TextView)view.findViewById(R.id.connectedContent))
                    .setText(hub.isConnected() ? "Yes" : "No");

            ((TextView)view.findViewById(R.id.batteryContent))
                    .setText(Integer.toString(hub.getBattery()).concat(" %"));
        }

        @Override
        public void notifyChanged() {
            this.updateValues();
        }
    }
}
