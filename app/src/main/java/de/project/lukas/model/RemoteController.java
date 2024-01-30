package de.project.lukas.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.Locale;

public interface RemoteController {
    void up(Remote remote);

    void down(Remote remote);

    void middle(Remote remote);

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

        public void up(Remote remote) {
            device.motorFaster();
        }

        public void down(Remote remote) {
            device.motorSlower();
        }

        public void middle(Remote remote) {
            device.motorStop();
        }
    }

    class BaseMotorController implements RemoteController {
        private final TrainBase device;
        private final LiveData<String> name;

        @Override
        public LiveData<String> getName() {
            return name;
        }

        BaseMotorController(TrainBase device) {
            this.device = device;

            name = Transformations.map(device.getName(),
                    n -> String.format(Locale.getDefault(), "Motor %s", n));
        }

        public void up(Remote remote) {
            device.motorFaster();
        }

        public void down(Remote remote) {
            device.motorSlower();
        }

        public void middle(Remote remote) {
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

        public void up(Remote remote) {
            device.lightBrighter();
        }

        public void down(Remote remote) {
            device.setLedColorHub();
            remote.setLedColorRemote(device.getCurrentColor());
        }

        public void middle(Remote remote) {
            device.lightOff();
        }
    }

    class BaseLightController implements RemoteController {
        private final TrainBase device;
        private final LiveData<String> name;

        @Override
        public LiveData<String> getName() {
            return name;
        }

        BaseLightController(TrainBase device) {
            this.device = device;

            name = Transformations.map(device.getName(),
                    n -> String.format(Locale.getDefault(), "Light %s", n));
        }

        public void up(Remote remote) {
            // device.lightBrighter();
        }

        public void down(Remote remote) {
            // device.lightDarker();
        }

        public void middle(Remote remote) {
            device.setLedColorHub();
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

        public void up(Remote remote) {
            device.toggle1();
        }

        public void middle(Remote remote) {
        }
        public void down(Remote remote) { device.toggle2();
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

        public void up(Remote remote) {
        }

        public void down(Remote remote) {
        }

        public void middle(Remote remote) {
        }
    }
}
