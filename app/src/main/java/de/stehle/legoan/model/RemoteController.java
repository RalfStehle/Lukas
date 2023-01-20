package de.stehle.legoan.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public interface RemoteController {
    void up();

    void down();

    void middle();

    static RemoteController noop() {
        return NoopController.getInstance();
    }

    class MotorController implements RemoteController {
        private final TrainHub device;

        MotorController(TrainHub device) {
            this.device = device;
        }

        public void up() {
            device.motorFaster();
        }

        public void down() {
            device.motorSlower();
        }

        public void middle() {
            device.motorStop();
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "Motor %s", device.getName());
        }
    }

    class LightController implements RemoteController {
        private final TrainHub device;

        LightController(TrainHub device) {
            this.device = device;
        }

        public void up() {
            device.lightBrighter();
        }

        public void down() {
            device.lightDarker();
        }

        public void middle() {
            device.ledRandom();
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "Light %s", device.getName());
        }
    }

    class SwitchController implements RemoteController {
        private Switch device;

        SwitchController(Switch device) {
            this.device = device;
        }

        public void up() {
            device.toggle();
        }

        public void down() {
            device.toggle();
        }

        public void middle() {
            device.toggle();
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.getDefault(), "Switch %s", device.getName());
        }
    }

    class NoopController implements RemoteController {
        private static final NoopController INSTANCE = new NoopController();

        public static NoopController getInstance() {
            return INSTANCE;
        }

        private NoopController() {
        }

        public void up() {
        }

        public void down() {
        }

        public void middle() {
        }

        @NonNull
        @Override
        public String toString() {
            return "None";
        }
    }
}
