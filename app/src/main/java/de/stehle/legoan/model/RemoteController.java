package de.stehle.legoan.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Locale;

public interface RemoteController {
    void up();

    void down();

    void middle();

    LiveData<String> getName();

    static RemoteController noop() {
        return NoopController.getInstance();
    }

    class MotorController implements RemoteController {
        private final TrainHub device;
        private final LiveData<String> name;

        @Override
        public LiveData<String> getName() {
            return name;
        }

        MotorController(TrainHub device) {
            this.device = device;

            name = Transformations.map(device.getName(),
                    n -> String.format(Locale.getDefault(), "Motor %s", n));
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
    }

    class LightController implements RemoteController {
        private final TrainHub device;
        private final LiveData<String> name;

        @Override
        public LiveData<String> getName() {
            return name;
        }

        LightController(TrainHub device) {
            this.device = device;

            name = Transformations.map(device.getName(),
                    n -> String.format(Locale.getDefault(), "Light %s", n));
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
    }

    class SwitchController implements RemoteController {
        private final Switch device;
        private final LiveData<String> name;

        @Override
        public LiveData<String> getName() {
            return name;
        }

        SwitchController(Switch device) {
            this.device = device;

            name = Transformations.map(device.getName(),
                    n -> String.format(Locale.getDefault(), "Switch %s", n));
        }

        public void up() {
            device.toggle1();
        }

        public void down() {
        }

        public void middle() {
            device.toggle2();
        }
    }

    class NoopController implements RemoteController {
        private static final NoopController INSTANCE = new NoopController();
        private final MutableLiveData<String> name = new MutableLiveData<>();

        public static NoopController getInstance() {
            return INSTANCE;
        }

        @Override
        public LiveData<String> getName() {
            return name;
        }

        private NoopController() {
            name.setValue("None");
        }

        public void up() {
        }

        public void down() {
        }

        public void middle() {
        }
    }
}
