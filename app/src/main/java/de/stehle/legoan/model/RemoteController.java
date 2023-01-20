package de.stehle.legoan.model;

import androidx.annotation.NonNull;

import java.util.Locale;
import java.util.Objects;

public abstract class RemoteController {
    private final Device device;

    RemoteController(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    public abstract void up();

    public abstract void down();

    public abstract void middle();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoteController that = (RemoteController) o;

        return Objects.equals(device, that.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device);
    }

    public static RemoteController noop() {
        return NoopController.getInstance();
    }

    public static RemoteController forSwitch(Switch device) {
        return new SwitchController(device);
    }

    public static RemoteController forMotor(TrainHub device) {
        return new MotorController(device);
    }

    public static RemoteController forLight(TrainHub device) {
        return new LightController(device);
    }

    private static class MotorController extends RemoteController {
        MotorController(TrainHub device) {
            super(device);
        }

        @Override
        public void up() {
            ((TrainHub) getDevice()).motorFaster();
        }

        @Override
        public void down() {
            ((TrainHub) getDevice()).motorSlower();
        }

        @Override
        public void middle() {
            ((TrainHub) getDevice()).motorStop();
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "Motor %s", getDevice().getName());
        }
    }

    private static class LightController extends RemoteController {
        LightController(TrainHub device) {
            super(device);
        }

        @Override
        public void up() {
            ((TrainHub) getDevice()).lightBrighter();
        }

        @Override
        public void down() {
            ((TrainHub) getDevice()).lightDarker();
        }

        @Override
        public void middle() {
            ((TrainHub) getDevice()).ledRandom();
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "Light %s", ((TrainHub) getDevice()).getName());
        }
    }

    private static class SwitchController extends RemoteController {
        SwitchController(Switch device) {
            super(device);
        }

        @Override
        public void up() {
            ((Switch) getDevice()).toggle();
        }

        @Override
        public void down() {
            ((Switch) getDevice()).toggle();
        }

        @Override
        public void middle() {
            ((Switch) getDevice()).toggle();
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "Switch %s", ((TrainHub) getDevice()).getName());
        }
    }

    private static class NoopController extends RemoteController {
        private static final NoopController INSTANCE = new NoopController();

        public static NoopController getInstance() {
            return INSTANCE;
        }

        private NoopController() {
            super(null);
        }

        @Override
        public void up() {
        }

        @Override
        public void down() {
        }

        @Override
        public void middle() {
        }

        @NonNull
        @Override
        public String toString() {
            return "None";
        }
    }
}
