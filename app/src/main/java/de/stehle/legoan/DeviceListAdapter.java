package de.stehle.legoan;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {
    private final List<Device> devices;
    private final FragmentManager fragmentManager;

    public DeviceListAdapter(List<Device> devices, FragmentManager fragmentManager) {
        this.devices = devices;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return devices.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = new FragmentContainerView(viewGroup.getContext());
            view.setId(View.generateViewId());
            view.setTag(i);

            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    handleAttachedItem(view);
                }

                @Override
                public void onViewDetachedFromWindow(View view) {

                }
            });
        }

        if (view.isAttachedToWindow()) {
            handleAttachedItem(view);
        }

        return view;
    }

    private void handleAttachedItem(View view) {
        Device device = devices.get((int) view.getTag());

        Fragment fragment = getFragment(view);

        if (fragment == null || !IsCorrectFragment(device, fragment)) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (device instanceof TrainHub) {
                TrainHubFragment trainHubFragment = new TrainHubFragment();
                trainHubFragment.setDevice(device);

                transaction.replace(view.getId(), trainHubFragment);
            } else if (device instanceof Remote) {
                RemoteFragment remoteFragment = new RemoteFragment();
                remoteFragment.setDevice(device);
                remoteFragment.setDevices(this);

                transaction.replace(view.getId(), remoteFragment);
            } else {
                SwitchFragment switchFragment = new SwitchFragment();
                switchFragment.setDevice(device);

                transaction.replace(view.getId(), switchFragment);
            }

            transaction.commit();
        }

        DeviceFragment adapter = (DeviceFragment) getFragment(view);

        if (adapter != null) {
            adapter.setDevice(device);
        }
    }

    private Fragment getFragment(View view) {
        try {
            return FragmentManager.findFragment(view);
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private boolean IsCorrectFragment(Device device, Fragment fragment) {
        if (device instanceof TrainHub) {
            return fragment.getClass() == TrainHubFragment.class;
        } else if (device instanceof Remote) {
            return fragment.getClass() == RemoteFragment.class;
        } else {
            return fragment.getClass() == SwitchFragment.class;
        }
    }
}
