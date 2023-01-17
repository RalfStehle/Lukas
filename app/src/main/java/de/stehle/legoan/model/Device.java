package de.stehle.legoan.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class Device {
    private final MutableLiveData<String> name = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> battery = new MutableLiveData<>(0);

    public String getName() {
        return name.getValue();
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
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

    protected void setIsConnected(boolean value) {
        isConnected.postValue(value);
    }

    public abstract String getAddress();

    public abstract void disconnect();
}
