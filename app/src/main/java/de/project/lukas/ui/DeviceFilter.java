package de.project.lukas.ui;

import de.project.lukas.model.Device;

public interface DeviceFilter {
    boolean shouldUse(Device device);
}
