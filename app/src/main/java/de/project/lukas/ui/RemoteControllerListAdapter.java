package de.project.lukas.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import java.util.List;

import de.project.lukas.model.RemoteController;

class RemoteControllerListAdapter extends ArrayAdapter<RemoteController> {
    public RemoteControllerListAdapter(@NonNull Context context, int resource, List<RemoteController> objects) {
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
