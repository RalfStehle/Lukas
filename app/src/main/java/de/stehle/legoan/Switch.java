package de.stehle.legoan;

public class Switch extends Device {
    @Override
    public String getAddress() {
        return "test";
    }

    @Override
    public void disconnect() {
    }

    public void toggle() {
    }

    public Switch(String name) {
        setName(name);
    }
}

