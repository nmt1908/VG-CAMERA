package com.example.vgcamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class LanguageAdapter extends ArrayAdapter<LanguageItem> {

    private Context context;
    private List<LanguageItem> items;

    public LanguageAdapter(@NonNull Context context, List<LanguageItem> items) {
        super(context, R.layout.spinner_item, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = convertView == null ? inflater.inflate(R.layout.spinner_item, parent, false) : convertView;
        TextView text = view.findViewById(R.id.spinner_item_text);
        LanguageItem item = items.get(position);
        text.setText(item.getLabel());
        return view;
    }
}
