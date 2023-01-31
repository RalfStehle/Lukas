package de.stehle.legoan.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

public abstract class Device {
    private final MutableLiveData<String> name = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> battery = new MutableLiveData<>(0);

    public LiveData<String> getName() {
        return name;
    }

    public LiveData<Boolean> getStatus() {
        return isConnected;
    }

    public LiveData<Integer> getBattery() {
        return battery;
    }

    protected void setInitialName(String value) {
        name.setValue(value);
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

    @NonNull
    @Override
    public String toString() {
        return Objects.requireNonNull(name.getValue());
    }
}
