package de.project.lukas.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import de.project.lukas.model.Device;
import de.project.lukas.model.Remote;
import de.project.lukas.model.TrainBase;
import de.project.lukas.model.TrainHub;

public class DeviceListAdapter extends BaseAdapter {
    private final List<Device> devices;
    private final FragmentManager fragmentManager;
    private final DeviceFilter filter;

    public DeviceListAdapter(List<Device> devices, FragmentManager fragmentManager, DeviceFilter filter) {
        this.devices = devices;
        this.fragmentManager = fragmentManager;
        this.filter = filter;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Device device : devices) {
            if (filter == null || filter.shouldUse(device)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        int index = -1;
        for (Device device : devices) {
            if (filter == null || filter.shouldUse(device)) {
                index++;

                if (index == i) {
                    return device;
                }
            }
        }

        return null;
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

        view.setTag(i);

        if (view.isAttachedToWindow()) {
            handleAttachedItem(view);
        }

        return view;
    }

    private void handleAttachedItem(View view) {
        Device device = (Device) getItem((int) view.getTag());

        Fragment fragment = getFragment(view);

        if (fragment == null || !IsCorrectFragment(device, fragment)) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (device instanceof TrainHub) {
                TrainHubFragment trainHubFragment = new TrainHubFragment();
                trainHubFragment.setDevice(device);

                transaction.replace(view.getId(), trainHubFragment);
            } else if (device instanceof TrainBase) {
                TrainBaseFragment trainBaseFragment = new TrainBaseFragment();
                trainBaseFragment.setDevice(device);

                transaction.replace(view.getId(), trainBaseFragment);
            } else if (device instanceof Remote) {
                RemoteFragment remoteFragment = new RemoteFragment();
                remoteFragment.setDevice(device);

                transaction.replace(view.getId(), remoteFragment);
            } else {
                SwitchFragment switchFragment = new SwitchFragment();
                switchFragment.setDevice(device);

                transaction.replace(view.getId(), switchFragment);
            }

            transaction.commit();
        }

        fragment = getFragment(view);

        if (fragment instanceof DeviceFragment) {
            ((DeviceFragment) fragment).setDevice(device);
        }
    }

    private Fragment getFragment(View view) {
        try {
            return fragmentManager.findFragmentById(view.getId());
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private boolean IsCorrectFragment(Device device, Fragment fragment) {
        if (device instanceof TrainHub) {
            return fragment.getClass() == TrainHubFragment.class;
        } else if (device instanceof TrainBase) {
            return fragment.getClass() == TrainBaseFragment.class;
        } else if (device instanceof Remote) {
            return fragment.getClass() == RemoteFragment.class;
        } else {
            return fragment.getClass() == SwitchFragment.class;
        }
    }

}
