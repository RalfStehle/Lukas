package de.project.lukas.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import de.project.lukas.databinding.FragmentBlankBinding;

public class PlaceholderFragment extends RecyclerView.ViewHolder {
    public PlaceholderFragment(View view) {
        super(view);
    }

    public static PlaceholderFragment create(LayoutInflater inflater, @Nullable ViewGroup container) {
        return new PlaceholderFragment(FragmentBlankBinding.inflate(inflater, container, false).getRoot());
    }
}
