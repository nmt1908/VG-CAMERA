package com.example.vgcamera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PurposeAdapter extends RecyclerView.Adapter<PurposeAdapter.VH> {

    public interface OnCheckedChange {
        void onChange(int position, boolean checked);
    }

    private final List<Purpose> purposes;
    private final boolean[] checked;
    private final String language;
    private final OnCheckedChange listener;

    public PurposeAdapter(List<Purpose> purposes, boolean[] checked, String language, OnCheckedChange listener) {
        this.purposes = purposes;
        this.checked = checked;
        this.language = language;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purpose_checkbox, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Purpose p = purposes.get(position);

        holder.cb.setText(p.getLabel(language));
        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(checked[position]);

        holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checked[position] = isChecked;
            if (listener != null) listener.onChange(position, isChecked);
        });

        // click cả dòng cũng toggle
        holder.itemView.setOnClickListener(v -> holder.cb.performClick());
    }

    @Override
    public int getItemCount() {
        return purposes.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb;
        VH(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cbPurpose);
        }
    }
}
