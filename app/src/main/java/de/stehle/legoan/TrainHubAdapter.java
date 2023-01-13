package de.stehle.legoan;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

class TrainHubAdapter extends Adapter {
    private final TrainHub device;

    TrainHubAdapter(TrainHub device, View view, Activity activity) {
        super(activity, device, view);
        this.device = device;
    }

    @Override
    protected void connectListeners(View view) {
        view.findViewById(R.id.StopButton).setOnClickListener(view1 -> device.stop());
        view.findViewById(R.id.SlowerButton).setOnClickListener(view1 -> device.decrementSpeed());
        view.findViewById(R.id.FasterButton).setOnClickListener(view1 -> device.incrementSpeed());
        view.findViewById(R.id.LightButton).setOnClickListener(view1 -> device.nextLedColor());
    }

    @Override
    protected void connectValues(View view) {
        ((TextView) view.findViewById(R.id.NameContent))
                .setText(device.getName());

        ((TextView) view.findViewById(R.id.ConnectedContent))
                .setText(device.isConnected() ? "Yes" : "No");

        ((TextView) view.findViewById(R.id.BatteryContent))
                .setText(String.format(Locale.getDefault(), "%d %%", device.getBattery()));
    }
}
