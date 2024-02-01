package de.project.lukas.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.project.lukas.R;
import de.project.lukas.databinding.LayoutTrainItemBinding;
import de.project.lukas.model.DevicesManager;
import de.project.lukas.model.TrainHub;


public class TrainHubFragment extends DeviceFragment implements View.OnCreateContextMenuListener {
    private final LayoutTrainItemBinding binding;

    public static TrainHubFragment create(LayoutInflater inflater, @Nullable ViewGroup container) {
        return new TrainHubFragment(LayoutTrainItemBinding.inflate(inflater, container, false));
    }

    @SuppressLint("SetTextI18n")
    private TrainHubFragment(LayoutTrainItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;

        binding.Card.setOnCreateContextMenuListener(this);
        binding.MotorSlowerButton.setOnClickListener(view1 -> getTrain().motorSlower());
        binding.MotorStopButton.setOnClickListener(view1 -> getTrain().motorStop());
        binding.MotorFasterButton.setOnClickListener(view1 -> getTrain().motorFaster());
        binding.LedColorButton.setOnClickListener(view1 -> getTrain().setLedColorHub());
        binding.LightBrighterButton.setOnClickListener(view1 -> getTrain().lightBrighter());
        binding.LightDarkerButton.setOnClickListener(view1 -> getTrain().lightDarker());

        getName().observeForever(
                binding.NameContent::setText);

        getMessage().observeForever(
                binding.Message::setText);

        getBattery().observeForever(
                value -> binding.BatteryContent.setText(Integer.toString(value)));
    }

    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.menu_disconnect)
                .setOnMenuItemClickListener(item -> {
                    DevicesManager.getInstance().removeDevice(getDevice());
                    return true;
                });

        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.menu_switchoff)
                .setOnMenuItemClickListener(item -> {
                    DevicesManager.getInstance().switchOffDevice(getDevice());
                    return true;
                });

        menu.add(Menu.NONE, View.generateViewId(), 0, R.string.rename)
                .setOnMenuItemClickListener(item -> {
                    rename();
                    return true;
                });
    }

    private void rename() {
        new ConfirmBuilder(getActivity())
                .setTitle(R.string.rename)
                .setConfirmText(R.string.rename)
                .setValue(getName().getValue())
                .setLMaxLength(14)
                .show(value -> {
                    getTrain().rename(value);
                });
    }

    private Activity getActivity() {
        return (Activity) binding.getRoot().getContext();
    }

    private TrainHub getTrain() {
        return (TrainHub) getDevice();
    }
}
