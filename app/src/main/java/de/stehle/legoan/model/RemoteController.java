package de.stehle.legoan.model;

import java.util.Locale;
import java.util.Objects;

public abstract class RemoteController {
    private final TrainHub train;

    RemoteController(TrainHub train) {
        this.train = train;
    }

    public TrainHub getTrain() {
        return train;
    }

    public abstract void up();

    public abstract void down();

    public abstract void middle();

    public abstract String getName();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RemoteController that = (RemoteController) o;

        return Objects.equals(train, that.train);
    }

    @Override
    public int hashCode() {
        return Objects.hash(train);
    }

    public static RemoteController noop() {
        return new NullController();
    }

    public static RemoteController motor(TrainHub train) {
        return new MotorController(train);
    }

    public static RemoteController light(TrainHub train) {
        return new LightController(train);
    }

    private static class MotorController extends RemoteController {
        MotorController(TrainHub train) {
            super(train);
        }

        @Override
        public void up() {
            getTrain().motorFaster();
        }

        @Override
        public void down() {
            getTrain().motorSlower();
        }

        @Override
        public void middle() {
            getTrain().motorStop();
        }

        @Override
        public String getName() {
            return String.format(Locale.getDefault(), "%s Motor", getTrain().getName());
        }
    }

    private static class LightController extends RemoteController {
        LightController(TrainHub train) {
            super(train);
        }

        @Override
        public void up() {
            getTrain().lightBrighter();
        }

        @Override
        public void down() {
            getTrain().lightDarker();
        }

        @Override
        public void middle() {
            getTrain().ledRandom();
        }

        @Override
        public String getName() {
            return String.format(Locale.getDefault(), "%s Motor", getTrain().getName());
        }
    }

    private static class NullController extends RemoteController {
        NullController() {
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

        @Override
        public String getName() {
            return "None";
        }
    }
}
