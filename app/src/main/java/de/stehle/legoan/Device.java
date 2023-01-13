package de.stehle.legoan;

import java.util.ArrayList;
import java.util.List;

public abstract class Device {
    private final List<ChangeListener> listeners = new ArrayList<>();

    public void subscribe(ChangeListener listener) {
        listeners.add(listener);
    }

    protected void notifyChanged() {
        for (ChangeListener listener : listeners) {
            listener.notifyChanged();
        }
    }

    public abstract void disconnect();
}
