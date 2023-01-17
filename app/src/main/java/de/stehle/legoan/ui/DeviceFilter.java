package de.stehle.legoan.ui;

import de.stehle.legoan.model.Device;

public interface DeviceFilter {
    boolean shouldUse(Device device);
}
