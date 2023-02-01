package de.stehle.legoan.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import de.stehle.legoan.R;
import de.stehle.legoan.databinding.LayoutRemoteItemBinding;
import de.stehle.legoan.model.Device;
import de.stehle.legoan.model.DevicesManager;
import de.stehle.legoan.model.Remote;
import de.stehle.legoan.model.RemoteController;
import de.stehle.legoan.model.Switch;
import de.stehle.legoan.model.TrainHub;

public class RemoteFragment extends DeviceFragment {
    private final int disconnectMenuItemId = View.generateViewId();
    private final List<RemoteController> controllers = new ArrayList<>();
    private LayoutRemoteItemBinding binding;
    private CustomArrayAdapter spinnerAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutRemoteItemBinding.inflate(inflater, container, false);

        spinnerAdapter = new CustomArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controllers);

        DevicesManager.getInstance().getDevices().observe(getViewLifecycleOwner(), value -> {
            controllers.clear();
            controllers.add(RemoteController.noop());

            for (Device device : value) {
                if (device instanceof TrainHub) {
                    controllers.add(((TrainHub) device).getMotorController());
                    controllers.add(((TrainHub) device).getLightController());
                } else if (device instanceof Switch) {
                    controllers.add(((Switch) device).getController());
                }
            }

            spinnerAdapter.notifyDataSetChanged();
        });

        getLiveDevice().observe(getViewLifecycleOwner(), device -> {
            Remote remote = (Remote) device;

            binding.TrainASpinner.setSelection(controllers.indexOf(remote.getControllerA()));
            binding.TrainBSpinner.setSelection(controllers.indexOf(remote.getControllerB()));
        });

        binding.TrainASpinner.setAdapter(spinnerAdapter);
        binding.TrainASpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                RemoteController controller = (RemoteController) parent.getItemAtPosition(pos);

                getRemote().setControllerA(controller);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.TrainBSpinner.setAdapter(spinnerAdapter);
        binding.TrainBSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                RemoteController controller = (RemoteController) parent.getItemAtPosition(pos);

                getRemote().setControllerB(controller);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getName().observe(getViewLifecycleOwner(),
                value -> binding.NameContent.setText(value));

        getBattery().observe(getViewLifecycleOwner(),
                value -> binding.BatteryContent.setText(Integer.toString(value)));

        registerForContextMenu(binding.Card);

        return binding.getRoot();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, disconnectMenuItemId, 0, R.string.menu_disconnect);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == disconnectMenuItemId) {
            DevicesManager.getInstance().removeDevice(getDevice());
            return true;
        }

        return false;
    }

    private Remote getRemote() {
        return (Remote) getDevice();
    }

    static class CustomArrayAdapter extends ArrayAdapter<RemoteController> {
        public CustomArrayAdapter(@NonNull Context context, int resource, List<RemoteController> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);

            NameOwner.getNameOwner(view).setController(getItem(position));

            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);

            NameOwner.getNameOwner(view).setController(getItem(position));

            return view;
        }
    }

    static class NameOwner implements View.OnAttachStateChangeListener {
        private final MutableLiveData<RemoteController> controller = new MutableLiveData<>();
        private final LiveData<String> nameObservable;
        private final Observer<String> nameObserver;
        private TextView view;

        public static NameOwner getNameOwner(TextView view) {
            Object current = view.getTag();

            if (current instanceof NameOwner) {
                return (NameOwner) current;
            }

            return new NameOwner(view);
        }

        private NameOwner(TextView view) {
            this.view = view;
            this.view.setTag(this);
            this.view.addOnAttachStateChangeListener(this);

            nameObservable = Transformations.switchMap(controller, c -> c.getName());
            nameObserver = view::setText;
            nameObservable.observeForever(nameObserver);
        }

        public void setController(RemoteController controller) {
            this.controller.setValue(controller);
        }

        @Override
        public void onViewAttachedToWindow(View view) {

        }

        @Override
        public void onViewDetachedFromWindow(View view) {
            nameObservable.removeObserver(nameObserver);
            view.setTag(null);
            view.removeOnAttachStateChangeListener(this);
        }
    }
}
