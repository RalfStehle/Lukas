package de.stehle.legoan.model;

public abstract class RemoteController {
    private TrainHub train;

    RemoteController(TrainHub train) {
        this.train = train;
    }

    public TrainHub getTrain() {
        return train;
    }

    public abstract void up();

    public abstract void down();

    public abstract void middle();

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
    }
}
