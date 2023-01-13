package de.stehle.legoan;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

class SwitchAdapter extends Adapter {
    private final Switch device;

    SwitchAdapter(Switch device, View view, Activity activity) {
        super(activity, device, view);
        this.device = device;
    }

    @Override
    protected void connectListeners(View view) {
        view.findViewById(R.id.ToggleButton).setOnClickListener(view1 -> device.toggle());
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
