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
        view.findViewById(R.id.MotorStopButton).setOnClickListener(view1 -> device.motorStop());
        view.findViewById(R.id.MotorSlowerButton).setOnClickListener(view1 -> device.motorSlower());
        view.findViewById(R.id.MotorFasterButton).setOnClickListener(view1 -> device.motorFaster());
        view.findViewById(R.id.LedRandomButton).setOnClickListener(view1 -> device.ledRandom());
        view.findViewById(R.id.LightBrighterButton).setOnClickListener(view1 -> device.lightBrighter());
        view.findViewById(R.id.LightDarkerButton).setOnClickListener(view1 -> device.lightDarker());
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
