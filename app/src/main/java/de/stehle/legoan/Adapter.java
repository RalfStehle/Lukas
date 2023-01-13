package de.stehle.legoan;

import android.app.Activity;
import android.view.View;

abstract class Adapter implements ChangeListener {
    private final Activity activity;
    private final Device device;
    private final View view;

    protected Adapter(Activity activity, Device device, View view) {
        this.activity = activity;
        this.device = device;
        this.view = view;
        this.view.setTag(this);
    }

    protected abstract void connectValues(View view);

    protected abstract void connectListeners(View view);

    public void connect() {
        device.subscribe(this);

        connectListeners(view);
        connectValues(view);
    }

    public void disconnect() {
        device.unsubscribe(this);
    }

    @Override
    public void notifyChanged() {
        activity.runOnUiThread(() -> connectValues(view));
    }

    public static Device getDevice(View view) {
        Object adapter = view.getTag();

        if (adapter == null) {
            return null;
        }

        return ((Adapter) adapter).device;
    }

    public static boolean disconnect(View view, Device targetDevice) {
        if (view == null) {
            return true;
        }

        Object adapter = view.getTag();

        if (adapter == null) {
            return true;
        }

        if (targetDevice == ((Adapter) adapter).device) {
            return false;
        }

        ((Adapter) adapter).disconnect();
        return true;
    }
}
