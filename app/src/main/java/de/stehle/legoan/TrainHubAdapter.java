package de.stehle.legoan;

import android.view.View;

import androidx.lifecycle.ViewModel;

public class TrainHubAdapter extends ViewModel implements ChangeListener {
    private final TrainHub hub;

    public TrainHubAdapter(TrainHub hub) {
        this.hub = hub;
        this.hub.subscribe(this);
    }

    @Override
    public void notifyChanged() {
    }

    public void slower(View view) {
        this.hub.decrementSpeed();
    }

    public void stop(View view) {
        this.hub.stop();
    }

    public void faster(View view) {
        this.hub.incrementSpeed();
    }
}
