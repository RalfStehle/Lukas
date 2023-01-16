package de.stehle.legoan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class Device {
    private final MutableLiveData<String> name = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> battery = new MutableLiveData<>(0);

    public String getName() {
        return name.getValue();
    }

    public LiveData<Boolean> getConnected() {
        return connected;
    }

    public LiveData<Integer> getBattery() {
        return battery;
    }

    protected void setName(String value) {
        name.postValue(value);
    }

    protected void setBattery(int value) {
        battery.postValue(value);
    }

    protected void setConnected(boolean value) {
        connected.postValue(value);
    }

    public abstract String getAddress();

    public abstract void disconnect();
}
